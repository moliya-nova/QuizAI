"""MongoDB 父块文档存储

存储完整文章（父块），通过 parent_id 与 ChromaDB 中的子块关联。
"""

from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)

COLLECTION_NAME = "parent_documents"


class DocStore:
    def __init__(self, db):
        self._db = db
        self._collection = None

    async def init(self):
        self._collection = self._db[COLLECTION_NAME]
        await self._collection.create_index("content_hash")
        await self._collection.create_index("article_id", unique=True)
        logger.info("DocStore 初始化完成（MongoDB）")

    async def close(self):
        self._collection = None

    async def upsert(
        self,
        parent_id: str,
        article_id: int,
        title: str,
        url: str,
        content: str,
        content_hash: str,
        now_iso: str,
    ):
        await self._collection.update_one(
            {"parent_id": parent_id},
            {"$set": {
                "article_id": article_id,
                "title": title,
                "url": url,
                "content": content,
                "content_hash": content_hash,
                "updated_at": now_iso,
            }, "$setOnInsert": {
                "parent_id": parent_id,
                "created_at": now_iso,
            }},
            upsert=True,
        )

    async def get_by_article_id(self, article_id: int) -> dict | None:
        doc = await self._collection.find_one(
            {"article_id": article_id},
            {"_id": 0},
        )
        return doc

    async def get_by_parent_id(self, parent_id: str) -> dict | None:
        doc = await self._collection.find_one(
            {"parent_id": parent_id},
            {"_id": 0},
        )
        return doc

    async def clear_all(self):
        await self._collection.delete_many({})
        logger.info("DocStore 已清空所有记录")

    async def delete_by_article_id(self, article_id: int):
        await self._collection.delete_one({"article_id": article_id})

    async def list_all(self) -> list[dict]:
        cursor = self._collection.find({}, {"_id": 0}).sort("article_id", 1)
        return [doc async for doc in cursor]
