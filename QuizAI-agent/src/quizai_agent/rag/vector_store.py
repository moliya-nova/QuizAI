"""ChromaDB 向量存储 — 手动管理 embedding，绕过 ChromaDB EmbeddingFunction 兼容问题"""

import asyncio

import chromadb

from quizai_agent.core.settings import settings
from quizai_agent.core.logger import get_logger
from quizai_agent.rag.embedding import SentenceTransformerEmbeddingFunction

logger = get_logger(__name__)

COLLECTION_NAME = "zhihuitong_articles"


class VectorStore:
    def __init__(self, persist_dir: str | None = None):
        self.persist_dir = persist_dir or settings.vector_store_path
        self.client = chromadb.PersistentClient(path=self.persist_dir)
        self.embedding_fn = SentenceTransformerEmbeddingFunction(
            model_name=settings.embedding_model,
            device=settings.embedding_device,
        )
        self._init_collection()

    def _init_collection(self):
        try:
            self.collection = self.client.get_collection(name=COLLECTION_NAME)
            # 检查维度兼容性
            if self.collection.count() > 0:
                try:
                    test_embedding = self.embedding_fn.embed_query("test")
                    self.collection.query(query_embeddings=[test_embedding], n_results=1)
                except Exception as dim_err:
                    logger.warning(f"Embedding 模型维度不匹配，将重建集合: {dim_err}")
                    self._recreate_collection()
        except Exception as e:
            if "does not exist" in str(e).lower() or "not found" in str(e).lower():
                self.collection = self.client.get_or_create_collection(
                    name=COLLECTION_NAME,
                    metadata={"hnsw:space": "cosine"},
                )
                logger.info(f"VectorStore 新建集合: {COLLECTION_NAME}")
            else:
                logger.warning(f"集合初始化异常，将重建: {e}")
                self._recreate_collection()

        logger.info(
            f"VectorStore 就绪: dir={self.persist_dir}, "
            f"model={settings.embedding_model}, "
            f"collection={COLLECTION_NAME}, count={self.collection.count()}"
        )

    def _recreate_collection(self):
        try:
            self.client.delete_collection(COLLECTION_NAME)
        except Exception:
            pass
        self.collection = self.client.get_or_create_collection(
            name=COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"},
        )
        logger.warning(f"集合已重建（旧数据已清除，需重新执行 POST /rag/index/all）")

    def _add_documents_sync(self, documents: list[dict]) -> int:
        if not documents:
            return 0
        ids = [d["chunk_id"] for d in documents]
        contents = [d["page_content"] for d in documents]
        metadatas = [d["metadata"] for d in documents]
        # 手动计算 embedding，直接传给 ChromaDB
        embeddings = self.embedding_fn.embed_documents(contents)
        self.collection.upsert(ids=ids, embeddings=embeddings, metadatas=metadatas)
        return len(documents)

    async def add_documents(self, documents: list[dict]) -> int:
        return await asyncio.to_thread(self._add_documents_sync, documents)

    def _delete_by_article_id_sync(self, article_id: int):
        self.collection.delete(where={"article_id": article_id})

    async def delete_by_article_id(self, article_id: int):
        await asyncio.to_thread(self._delete_by_article_id_sync, article_id)

    def _similarity_search_sync(self, query: str, k: int) -> list[dict]:
        count = self.collection.count()
        if count == 0:
            return []
        actual_k = min(k, count)
        # 手动计算 query embedding
        query_embedding = self.embedding_fn.embed_query(query)
        results = self.collection.query(query_embeddings=[query_embedding], n_results=actual_k)

        docs = []
        for i in range(len(results["ids"][0])):
            docs.append({
                "chunk_id": results["ids"][0][i],
                "page_content": results["documents"][0][i] if results.get("documents") else "",
                "metadata": results["metadatas"][0][i] if results.get("metadatas") else {},
                "distance": results["distances"][0][i] if results.get("distances") else None,
            })
        return docs

    async def similarity_search(self, query: str, k: int = 5) -> list[dict]:
        return await asyncio.to_thread(self._similarity_search_sync, query, k)
