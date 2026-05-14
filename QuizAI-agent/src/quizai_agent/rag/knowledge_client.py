"""Java 后端知识库客户端 — 调用 Spring Boot 接口获取知识内容用于 RAG 检索"""

import httpx

from quizai_agent.core.settings import settings
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


async def fetch_all_published_knowledge() -> list[dict]:
    """获取所有已发布的知识内容（题目、解析等）

    Returns:
        知识内容列表，每个元素包含 id, title, content 等字段
    """
    url = f"{settings.java_backend_url}/api/question/all"

    try:
        transport = httpx.AsyncHTTPTransport(proxy=None)
        async with httpx.AsyncClient(timeout=30.0, transport=transport) as client:
            response = await client.get(url)
            response.raise_for_status()

            data = response.json()
            if data.get("code") == 200:
                knowledge_list = data.get("data", [])
                logger.info(f"获取到 {len(knowledge_list)} 条知识内容")
                return knowledge_list
            else:
                logger.warning(f"获取知识内容失败: {data.get('msg', '未知错误')}")
                return []
    except httpx.TimeoutException:
        logger.error(f"获取知识内容超时: {url}")
        return []
    except httpx.HTTPStatusError as e:
        logger.error(f"获取知识内容HTTP错误: {e.response.status_code}")
        return []
    except Exception as e:
        logger.error(f"获取知识内容异常: {e}")
        return []


async def index_knowledge_to_rag() -> dict:
    """将所有已发布的知识内容索引到 RAG 向量库

    Returns:
        索引结果统计
    """
    from quizai_agent.rag.store_factory import get_doc_store, get_vector_store
    from quizai_agent.rag.splitter import compute_md5, split_article
    from datetime import datetime, timezone

    knowledge_list = await fetch_all_published_knowledge()
    logger.info(f"获取到 {len(knowledge_list)} 条已发布知识，开始索引...")
    if not knowledge_list:
        logger.warning("没有获取到任何知识内容，请检查 Java 后端接口")
        return {"total": 0, "indexed": 0, "skipped": 0, "errors": 0}

    doc_store = get_doc_store()
    vector_store = get_vector_store()

    # 检测不一致：docstore 有记录但 ChromaDB 为空 → 清空 docstore 强制全量重建
    chroma_count = vector_store.collection.count()
    doc_count = len(await doc_store.list_all())
    if doc_count > 0 and chroma_count == 0:
        logger.warning(f"检测到不一致: docstore={doc_count} 篇, chroma=0 条。清空 docstore 强制重建索引")
        await doc_store.clear_all()

    indexed = 0
    skipped = 0
    errors = 0

    for knowledge in knowledge_list:
        try:
            knowledge_id = knowledge.get("id")
            title = knowledge.get("title", "")
            content = knowledge.get("content", "")

            if not content:
                logger.warning(f"知识 {knowledge_id} ({title}) 内容为空，跳过")
                errors += 1
                continue

            # 使用 knowledge_id 作为 article_id
            content_hash = compute_md5(content)
            existing = await doc_store.get_by_article_id(knowledge_id)

            # MD5 去重：内容未变则跳过
            if existing and existing["content_hash"] == content_hash:
                skipped += 1
                continue

            # 已有但内容变化 → 删除旧数据
            if existing:
                await vector_store.delete_by_article_id(knowledge_id)
                await doc_store.delete_by_article_id(knowledge_id)

            # 切分文章
            parent_id, chunks = split_article(
                article_id=knowledge_id,
                title=title,
                url="",  # 校园知识没有URL
                content=content,
                content_hash=content_hash,
            )

            # 存入 ChromaDB
            await vector_store.add_documents(chunks)

            # 存入 SQLite
            now_iso = datetime.now(timezone.utc).isoformat()
            await doc_store.upsert(
                parent_id=parent_id,
                article_id=knowledge_id,
                title=title,
                url="",
                content=content,
                content_hash=content_hash,
                now_iso=now_iso,
            )

            indexed += 1
            logger.debug(f"知识内容 {knowledge_id} 索引完成: {title}")

        except Exception as e:
            logger.error(f"索引知识内容 {knowledge.get('id')} 失败: {e}")
            errors += 1

    total = len(knowledge_list)
    chroma_count = vector_store.collection.count()
    logger.info(f"知识库索引完成: total={total}, indexed={indexed}, skipped={skipped}, errors={errors}, chroma_count={chroma_count}")

    return {
        "total": total,
        "indexed": indexed,
        "skipped": skipped,
        "errors": errors,
    }
