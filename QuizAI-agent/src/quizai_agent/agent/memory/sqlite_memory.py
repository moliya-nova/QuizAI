"""记忆管理 — 短期记忆 / 摘要记忆 / 长期记忆的持久化与查询"""

import time
from typing import Dict, List

from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver

from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


import os

class MemoryManager:
    def __init__(self):
        os.makedirs("data", exist_ok=True)
        self._saver_ctx = AsyncSqliteSaver.from_conn_string("data/memory.db")
        self.checkpointer = None
        self._thread_access: Dict[str, float] = {}
        self._threads_loaded = False
        self._threads_cache: List[Dict] = []
        self._threads_cache_time: float = 0.0

    # 异步初始化：进入 AsyncSqliteSaver 上下文并建表
    async def init(self):
        self.checkpointer = await self._saver_ctx.__aenter__()
        await self._init_message_log()
        logger.info("MemoryManager 初始化完成")

    # 初始化消息日志表和线程用户映射表
    async def _init_message_log(self):
        await self.checkpointer.conn.execute("""
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                thread_id TEXT NOT NULL,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at REAL NOT NULL
            )
        """)
        await self.checkpointer.conn.execute(
            "CREATE INDEX IF NOT EXISTS idx_chat_messages_thread ON chat_messages(thread_id)"
        )
        await self.checkpointer.conn.execute(
            "CREATE INDEX IF NOT EXISTS idx_chat_messages_thread_time ON chat_messages(thread_id, created_at)"
        )
        await self.checkpointer.conn.execute("""
            CREATE TABLE IF NOT EXISTS thread_users (
                thread_id TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                created_at REAL NOT NULL
            )
        """)
        await self.checkpointer.conn.commit()

    # 记录一条消息到日志表
    async def save_message(self, thread_id: str, role: str, content: str):
        await self.checkpointer.conn.execute(
            "INSERT INTO chat_messages (thread_id, role, content, created_at) VALUES (?, ?, ?, ?)",
            (thread_id, role, content, time.time()),
        )
        await self.checkpointer.conn.commit()

    # 记录 thread_id 与用户名的映射（已存在则忽略）
    async def save_thread_user(self, thread_id: str, username: str):
        await self.checkpointer.conn.execute(
            "INSERT OR IGNORE INTO thread_users (thread_id, username, created_at) VALUES (?, ?, ?)",
            (thread_id, username, time.time()),
        )
        await self.checkpointer.conn.commit()

    # 获取指定会话的消息历史
    async def get_message_history(self, thread_id: str) -> List[Dict]:
        cursor = await self.checkpointer.conn.execute(
            "SELECT role, content, created_at FROM chat_messages WHERE thread_id = ? ORDER BY id",
            (thread_id,),
        )
        rows = await cursor.fetchall()
        return [
            {"role": row[0], "content": row[1], "created_at": row[2]}
            for row in rows
        ]

    # 更新线程访问时间
    def touch_thread(self, thread_id: str):
        self._thread_access[thread_id] = time.time()

    # 从 SQLite 加载已有的 thread_id 列表及其最后活跃时间
    async def _load_existing_threads(self):
        try:
            cursor = await self.checkpointer.conn.execute(
                """SELECT cm.thread_id, MAX(cm.created_at) AS last_time
                   FROM chat_messages cm
                   GROUP BY cm.thread_id"""
            )
            rows = await cursor.fetchall()
            for row in rows:
                thread_id = row[0]
                if thread_id not in self._thread_access:
                    self._thread_access[thread_id] = row[1] or 0.0
            self._threads_loaded = True
            logger.info(f"从 SQLite 加载了 {len(rows)} 个历史会话线程")
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
        await self.checkpointer.conn.execute(
            "DELETE FROM chat_messages WHERE thread_id = ?", (thread_id,)
        )
        await self.checkpointer.conn.execute(
            "DELETE FROM thread_users WHERE thread_id = ?", (thread_id,)
        )
        await self.checkpointer.conn.commit()
        self._thread_access.pop(thread_id, None)
        logger.info(f"已清除会话记忆: thread_id={thread_id}")

    # 返回活跃会话线程列表（带 10 秒缓存，避免频繁查询数据库）
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

        # 2. 单次查询：thread_users LEFT JOIN chat_messages
        db_threads: Dict[str, Dict] = {}
        try:
            cursor = await self.checkpointer.conn.execute(
                """SELECT tu.thread_id, tu.username, MAX(cm.created_at) AS last_msg_time
                   FROM thread_users tu
                   LEFT JOIN chat_messages cm ON cm.thread_id = tu.thread_id
                   GROUP BY tu.thread_id, tu.username"""
            )
            rows = await cursor.fetchall()
            for row in rows:
                db_threads[row[0]] = {
                    "username": row[1],
                    "last_access": row[2] or 0.0,
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

    # 清除所有会话记忆（内存 + 数据库全量清理）
    async def clear_all_memory(self):
        # 1. 从数据库获取所有 thread_id（避免遗漏内存中没有的记录）
        all_thread_ids = set(self._thread_access.keys())
        try:
            cursor = await self.checkpointer.conn.execute(
                "SELECT DISTINCT thread_id FROM thread_users"
            )
            rows = await cursor.fetchall()
            for row in rows:
                all_thread_ids.add(row[0])
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
            await self.checkpointer.conn.execute("DELETE FROM chat_messages")
            await self.checkpointer.conn.execute("DELETE FROM thread_users")
            await self.checkpointer.conn.commit()
        except Exception as e:
            logger.warning(f"清理日志数据失败: {e}")
        logger.info(f"已清除所有会话记忆，成功 {success_count}/{len(thread_ids)} 个线程")
