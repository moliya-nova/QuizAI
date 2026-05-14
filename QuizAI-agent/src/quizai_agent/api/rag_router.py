"""RAG API 端点 — 文档索引、删除、批量重建、知识库索引"""

from datetime import datetime, timezone

from fastapi import APIRouter

from quizai_agent.rag.store_factory import get_doc_store, get_vector_store
from quizai_agent.rag.splitter import compute_md5, split_article
from quizai_agent.rag.knowledge_client import index_knowledge_to_rag
from quizai_agent.models.schemas.rag import ArticleIndexRequest, BatchIndexRequest
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)
router = APIRouter(prefix="/rag", tags=["rag"])


# ── 端点 ──

# 索引单篇文档（Spring Boot 在内容发布/修改时调用）
@router.post("/index")
async def index_article(request: ArticleIndexRequest):
    try:
        doc_store = get_doc_store()
        vector_store = get_vector_store()

        content_hash = compute_md5(request.content)
        now_iso = datetime.now(timezone.utc).isoformat()

        # MD5 去重：内容未变则跳过
        existing = await doc_store.get_by_article_id(request.article_id)
        if existing and existing["content_hash"] == content_hash:
            logger.info(f"文章 {request.article_id} 内容未变，跳过索引")
            return {
                "code": 200,
                "msg": "Article unchanged, skipped",
                "data": {
                    "article_id": request.article_id,
                    "parent_id": existing["parent_id"],
                    "chunks_count": 0,
                    "action": "skipped",
                },
            }

        # 已有但内容变化 → 删除旧数据
        if existing:
            await vector_store.delete_by_article_id(request.article_id)
            await doc_store.delete_by_article_id(request.article_id)
            logger.info(f"文章 {request.article_id} 内容变化，删除旧索引")

        # 切分
        parent_id, chunks = split_article(
            article_id=request.article_id,
            title=request.title,
            url=request.url,
            content=request.content,
            content_hash=content_hash,
        )

        # 存入 ChromaDB
        count = await vector_store.add_documents(chunks)

        # 存入 SQLite
        await doc_store.upsert(
            parent_id=parent_id,
            article_id=request.article_id,
            title=request.title,
            url=request.url,
            content=request.content,
            content_hash=content_hash,
            now_iso=now_iso,
        )

        action = "updated" if existing else "created"
        logger.info(f"文章 {request.article_id} 索引完成: {action}, {count} 个子块")
        return {
            "code": 200,
            "msg": f"Article {action} successfully",
            "data": {
                "article_id": request.article_id,
                "parent_id": parent_id,
                "chunks_count": count,
                "action": action,
            },
        }
    except Exception as e:
        logger.error(f"索引文章失败: {e}")
        return {"code": 500, "msg": f"Indexing failed: {e}", "data": None}


# 删除文章索引
@router.delete("/index/{article_id}")
async def delete_article(article_id: int):
    try:
        doc_store = get_doc_store()
        vector_store = get_vector_store()

        await vector_store.delete_by_article_id(article_id)
        await doc_store.delete_by_article_id(article_id)

        logger.info(f"文章 {article_id} 索引已删除")
        return {
            "code": 200,
            "msg": "Article deleted from index",
            "data": {"article_id": article_id},
        }
    except Exception as e:
        logger.error(f"删除文章索引失败: {e}")
        return {"code": 500, "msg": f"Delete failed: {e}", "data": None}


# 批量重建索引（由 Spring Boot 推送文档列表）
@router.post("/index/all")
async def index_all_articles(request: BatchIndexRequest):
    try:
        doc_store = get_doc_store()
        vector_store = get_vector_store()
    except Exception as e:
        logger.error(f"初始化存储失败: {e}")
        return {"code": 500, "msg": f"Storage init failed: {e}", "data": None}

    indexed = 0
    skipped = 0
    errors = 0

    for article in request.articles:
        try:
            if not article.content:
                errors += 1
                continue

            content_hash = compute_md5(article.content)
            existing = await doc_store.get_by_article_id(article.article_id)
            if existing and existing["content_hash"] == content_hash:
                skipped += 1
                continue

            if existing:
                await vector_store.delete_by_article_id(article.article_id)
                await doc_store.delete_by_article_id(article.article_id)

            parent_id, chunks = split_article(
                article.article_id, article.title, article.url,
                article.content, content_hash=content_hash,
            )
            await vector_store.add_documents(chunks)

            now_iso = datetime.now(timezone.utc).isoformat()
            await doc_store.upsert(
                parent_id=parent_id,
                article_id=article.article_id,
                title=article.title,
                url=article.url,
                content=article.content,
                content_hash=content_hash,
                now_iso=now_iso,
            )
            indexed += 1
        except Exception as e:
            logger.error(f"索引文章 {article.article_id} 失败: {e}")
            errors += 1

    total = len(request.articles)
    logger.info(f"批量索引完成: total={total}, indexed={indexed}, skipped={skipped}, errors={errors}")
    return {
        "code": 200,
        "msg": "Re-index complete",
        "data": {
            "total": total,
            "indexed": indexed,
            "skipped": skipped,
            "errors": errors,
        },
    }


# 从 Java 后端索引知识库
@router.post("/index/knowledge")
async def index_knowledge():
    try:
        result = await index_knowledge_to_rag()
        return {
            "code": 200,
            "msg": "Knowledge indexing complete",
            "data": result,
        }
    except Exception as e:
        logger.error(f"索引知识库失败: {e}")
        return {"code": 500, "msg": f"Knowledge indexing failed: {e}", "data": None}


# 诊断：检查 RAG 存储状态
@router.get("/status")
async def rag_status():
    try:
        doc_store = get_doc_store()
        vector_store = get_vector_store()

        parent_docs = await doc_store.list_all()
        chroma_count = vector_store.collection.count()

        return {
            "code": 200,
            "msg": "success",
            "data": {
                "chroma_chunk_count": chroma_count,
                "sqlite_article_count": len(parent_docs),
                "articles": [
                    {"id": d["article_id"], "title": d["title"], "content_len": len(d["content"])}
                    for d in parent_docs
                ],
            },
        }
    except Exception as e:
        logger.error(f"RAG 状态查询失败: {e}")
        return {"code": 500, "msg": str(e), "data": None}


# 诊断：测试 Java 后端知识库接口连通性
@router.get("/test-java")
async def test_java_connection():
    import httpx
    from quizai_agent.core.settings import settings

    url = f"{settings.java_backend_url}/api/question/all"
    try:
        transport = httpx.AsyncHTTPTransport(proxy=None)
        async with httpx.AsyncClient(timeout=10.0, transport=transport) as client:
            response = await client.get(url)
            return {
                "code": 200,
                "msg": "success",
                "data": {
                    "url": url,
                    "status_code": response.status_code,
                    "response_preview": response.text[:500],
                },
            }
    except Exception as e:
        return {
            "code": 500,
            "msg": str(e),
            "data": {"url": url},
        }
