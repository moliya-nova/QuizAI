"""存储单例工厂 — DocStore / VectorStore 延迟初始化"""

from quizai_agent.core.settings import settings
from quizai_agent.rag.vector_store import VectorStore
from quizai_agent.rag.docstore import DocStore
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)

_doc_store: DocStore | None = None
_vector_store: VectorStore | None = None


def get_doc_store() -> DocStore:
    global _doc_store
    if _doc_store is None:
        _doc_store = DocStore(settings.docstore_path)
    return _doc_store


def get_vector_store() -> VectorStore:
    global _vector_store
    if _vector_store is None:
        _vector_store = VectorStore()
    return _vector_store


# 在 FastAPI lifespan 中调用，初始化 DocStore 和 VectorStore（含 Embedding 模型预加载）
async def init_doc_store():
    import asyncio
    ds = get_doc_store()
    await ds.init()
    # 预加载 VectorStore（Embedding 模型），避免首次 RAG 请求时阻塞 event loop
    await asyncio.to_thread(get_vector_store)


async def close_doc_store():
    global _doc_store, _vector_store
    if _doc_store:
        await _doc_store.close()
    _doc_store = None
    _vector_store = None
