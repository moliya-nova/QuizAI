"""SSE (Server-Sent Events) 格式化工具 — 跨模块通用的流式输出辅助函数"""

import asyncio
import json
from typing import AsyncGenerator, Union

from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


def format_sse_event(data: dict) -> str:
    """把 dict 序列化为 SSE 格式的 data 行"""
    return f"data: {json.dumps(data, ensure_ascii=False)}\n\n"


def is_sse_event(value: str) -> bool:
    """判断字符串是否已经是 SSE 格式（data: 开头的 JSON）"""
    return value.startswith("data: ")


async def sse_stream(
    generator: AsyncGenerator[Union[str, dict], None],
    error_msg: str = "Stream error",
    keepalive_interval: int = 15,
) -> AsyncGenerator[str, None]:
    """遍历上游 async generator，将每个 chunk 包装为 SSE 事件并 yield，异常时 yield 错误事件

    使用 asyncio.Queue 实现独立的 keepalive 机制：当上游 generator 因模型加载等原因
    阻塞时，keepalive 任务在独立协程中定期发送心跳，防止客户端/代理超时断开连接。

    支持两种 yield 格式：
    - str：普通文本 chunk，包装为 {"code":200,"msg":"success","data":"chunk"}
    - str（SSE 格式）：已是 "data: {...}" 的事件，直接透传
    - dict：原始字典，直接序列化为 SSE 事件
    """
    queue: asyncio.Queue[Union[str, None]] = asyncio.Queue()
    stop_event = asyncio.Event()

    async def _produce():
        """消费上游 generator，将 chunk 放入队列"""
        try:
            async for chunk in generator:
                if isinstance(chunk, dict):
                    await queue.put(format_sse_event(chunk))
                elif isinstance(chunk, str) and is_sse_event(chunk):
                    await queue.put(chunk)
                else:
                    event = {"code": 200, "msg": "success", "data": chunk}
                    await queue.put(format_sse_event(event))
        except Exception as e:
            logger.error(f"{error_msg}: {e}")
            error = {"code": 500, "msg": str(e), "data": ""}
            await queue.put(format_sse_event(error))
        finally:
            await queue.put(None)  # 结束信号

    async def _keepalive():
        """定期发送 SSE keepalive 注释，防止超时"""
        while not stop_event.is_set():
            await asyncio.sleep(keepalive_interval)
            if not stop_event.is_set():
                await queue.put(": keepalive\n\n")

    # 启动生产者和 keepalive 任务
    producer_task = asyncio.create_task(_produce())
    keepalive_task = asyncio.create_task(_keepalive())

    try:
        while True:
            item = await queue.get()
            if item is None:
                break
            yield item
    finally:
        stop_event.set()
        keepalive_task.cancel()
        try:
            await keepalive_task
        except asyncio.CancelledError:
            pass


def format_suggestions_event(suggestions: list) -> str:
    """将推荐问题列表包装为 SSE 事件（code 201）"""
    data = {"code": 201, "msg": "suggestions", "data": suggestions}
    return format_sse_event(data)
