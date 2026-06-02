"""FastAPI 应用生命周期 — 启动初始化 / 关闭清理"""

from contextlib import asynccontextmanager

from fastapi import FastAPI

from quizai_agent.agent.chat_agent import ChatAgent
from quizai_agent.agent.rag_agent import RagAgent
from quizai_agent.agent.memory.mongo_memory import MemoryManager
from quizai_agent.agent.graph.workflow import AgentWorkflow
from quizai_agent.core.settings import settings
from quizai_agent.agent.search_agent import SearchAgent

from quizai_agent.api import chat_router as chat_module
from quizai_agent.api import config_router as config_module
from quizai_agent.llm import llm_client
from quizai_agent.rag.store_factory import close_doc_store, init_doc_store


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 初始化 LLM Client
    await llm_client.init()

    # 创建 Agent 实例
    chat_agent = ChatAgent(llm_client)
    rag_agent = RagAgent(llm_client)
    search_agent = SearchAgent(llm_client)

    # 初始化记忆管理器（MongoDB）
    memory_manager = MemoryManager(mongo_uri=settings.mongo_uri)
    await memory_manager.init()

    # 初始化 Agent 编排
    workflow = AgentWorkflow(chat_agent, rag_agent, memory_manager, search_agent)
    workflow.build()

    # 注入到 API 模块
    chat_module.set_workflow(workflow)
    config_module.set_workflow(workflow)

    # 初始化 RAG 文档存储
    await init_doc_store()

    # 应用运行阶段
    yield

    # 关闭 RAG 文档存储
    await close_doc_store()
