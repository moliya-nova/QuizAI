"""记忆管理 — 短期记忆 / 摘要记忆 / 长期记忆的持久化与查询（MongoDB 版本）"""

import time
from typing import Dict, List

import pymongo
from motor.motor_asyncio import AsyncIOMotorClient
from langgraph.checkpoint.mongodb import MongoDBSaver

from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


class MemoryManager:
    def __init__(self, mongo_uri: str = "mongodb://admin:quizai123@localhost:27017", db_name: str = "quizai_agent"):
        self._mongo_uri = mongo_uri
        self._db_name = db_name
        self._motor_client: AsyncIOMotorClient | None = None
        self._pymongo_client: pymongo.MongoClient | None = None
        self.checkpointer: MongoDBSaver | None = None
        self._db = None
        self._chat_messages = None
        self._thread_users = None
        self._thread_access: Dict[str, float] = {}
        self._threads_loaded = False
        self._threads_cache: List[Dict] = []
        self._threads_cache_time: float = 0.0

    # 异步初始化：连接 MongoDB，创建 MongoDBSaver checkpointer
    async def init(self):
        # Motor 异步客户端（用于消息日志等自建集合）
        self._motor_client = AsyncIOMotorClient(self._mongo_uri)
        self._db = self._motor_client[self._db_name]
        self._chat_messages = self._db["chat_messages"]
        self._thread_users = self._db["thread_users"]

        # 创建索引
        await self._chat_messages.create_index("thread_id")
        await self._chat_messages.create_index([("thread_id", 1), ("created_at", 1)])
        await self._thread_users.create_index("thread_id", unique=True)

        # PyMongo 同步客户端（LangGraph checkpointer 要求）
        self._pymongo_client = pymongo.MongoClient(self._mongo_uri)
        self.checkpointer = MongoDBSaver(
            client=self._pymongo_client,
            db_name=self._db_name,
            checkpoint_collection_name="checkpoints",
        )

        logger.info("MemoryManager 初始化完成（MongoDB）")

    # 记录一条消息到 chat_messages 集合
    async def save_message(self, thread_id: str, role: str, content: str):
        await self._chat_messages.insert_one({
            "thread_id": thread_id,
            "role": role,
            "content": content,
            "created_at": time.time(),
        })

    # 记录 thread_id 与用户名的映射（已存在则忽略）
    async def save_thread_user(self, thread_id: str, username: str):
        await self._thread_users.update_one(
            {"thread_id": thread_id},
            {"$setOnInsert": {"thread_id": thread_id, "username": username, "created_at": time.time()}},
            upsert=True,
        )

    # 获取指定会话的消息历史
    async def get_message_history(self, thread_id: str) -> List[Dict]:
        cursor = self._chat_messages.find(
            {"thread_id": thread_id},
            {"_id": 0, "role": 1, "content": 1, "created_at": 1},
        ).sort("_id", 1)
        return [doc async for doc in cursor]

    # 更新线程访问时间
    def touch_thread(self, thread_id: str):
        self._thread_access[thread_id] = time.time()

    # 从 MongoDB 加载已有的 thread_id 列表及其最后活跃时间
    async def _load_existing_threads(self):
        try:
            pipeline = [
                {"$group": {"_id": "$thread_id", "last_time": {"$max": "$created_at"}}},
            ]
            async for doc in self._chat_messages.aggregate(pipeline):
                thread_id = doc["_id"]
                if thread_id not in self._thread_access:
                    self._thread_access[thread_id] = doc["last_time"] or 0.0
            self._threads_loaded = True
            logger.info(f"从 MongoDB 加载了 {len(self._thread_access)} 个历史会话线程")
        except Exception as e:
            self._threads_loaded = True
            logger.warning(f"加载历史会话线程失败（首次运行时正常）: {e}")

    # 确保历史线程列表已加载
    async def ensure_threads_loaded(self):
        if not self._threads_loaded:
            await self._load_existing_threads()

    # 清除指定会话的持久记忆
    async def delete_memory(self, thread_id: str):
        try:
            await self.checkpointer.adelete_thread(thread_id)
        except Exception as e:
            logger.warning(f"删除 checkpointer 线程失败（可忽略）: {e}")
        await self._chat_messages.delete_many({"thread_id": thread_id})
        await self._thread_users.delete_one({"thread_id": thread_id})
        self._thread_access.pop(thread_id, None)
        logger.info(f"已清除会话记忆: thread_id={thread_id}")

    # 返回活跃会话线程列表（带 10 秒缓存）
    async def get_active_threads(self) -> List[Dict]:
        now = time.time()
        ttl = 900  # 15 分钟

        # 10 秒缓存：管理页面 30 秒轮询，缓存避免每次查库
        if now - self._threads_cache_time < 10 and self._threads_cache:
            return self._threads_cache

        # 1. 清理内存字典中的过期条目
        expired = [tid for tid, t in self._thread_access.items() if now - t > ttl]
        for tid in expired:
            del self._thread_access[tid]

        # 2. 查询 thread_users 并 LEFT JOIN chat_messages 的最后消息时间
        db_threads: Dict[str, Dict] = {}
        try:
            pipeline = [
                {
                    "$lookup": {
                        "from": "chat_messages",
                        "localField": "thread_id",
                        "foreignField": "thread_id",
                        "as": "messages",
                    }
                },
                {
                    "$project": {
                        "thread_id": 1,
                        "username": 1,
                        "last_msg_time": {"$max": "$messages.created_at"},
                    }
                },
            ]
            async for doc in self._thread_users.aggregate(pipeline):
                db_threads[doc["thread_id"]] = {
                    "username": doc.get("username", ""),
                    "last_access": doc.get("last_msg_time") or 0.0,
                }
        except Exception as e:
            logger.warning(f"查询历史会话失败: {e}")

        # 3. 合并：内存字典优先级高于数据库
        merged: Dict[str, float] = {}
        for tid, info in db_threads.items():
            merged[tid] = info["last_access"]
        for tid, t in self._thread_access.items():
            merged[tid] = t

        # 4. 过滤过期条目，构建返回列表
        result = []
        for tid, last_access in merged.items():
            if now - last_access > ttl:
                continue
            username = db_threads.get(tid, {}).get("username", "")
            result.append({
                "thread_id": tid,
                "username": username,
                "last_access": int(last_access),
                "expires_in": max(0, int(ttl - (now - last_access))),
            })

        result.sort(key=lambda x: x["last_access"], reverse=True)

        # 更新缓存
        self._threads_cache = result
        self._threads_cache_time = now
        return result

    # 清除所有会话记忆（内存 + MongoDB 全量清理）
    async def clear_all_memory(self):
        # 1. 获取所有 thread_id
        all_thread_ids = set(self._thread_access.keys())
        try:
            async for doc in self._thread_users.find({}, {"thread_id": 1}):
                all_thread_ids.add(doc["thread_id"])
        except Exception:
            pass

        thread_ids = list(all_thread_ids)
        success_count = 0
        for tid in thread_ids:
            try:
                await self.checkpointer.adelete_thread(tid)
                success_count += 1
            except Exception as e:
                logger.warning(f"删除线程 {tid} 失败（跳过）: {e}")
            self._thread_access.pop(tid, None)

        # 清理消息日志和用户映射
        try:
            await self._chat_messages.delete_many({})
            await self._thread_users.delete_many({})
        except Exception as e:
            logger.warning(f"清理日志数据失败: {e}")
        logger.info(f"已清除所有会话记忆，成功 {success_count}/{len(thread_ids)} 个线程")
