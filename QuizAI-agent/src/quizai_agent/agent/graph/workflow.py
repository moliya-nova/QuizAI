"""Agent 编排 — 构建 LangGraph 条件路由图 + 流式聊天入口"""

from langchain_core.messages import AIMessage, HumanMessage
from langgraph.graph import END, StateGraph

from quizai_agent.agent.chat_agent import extract_content_text
from quizai_agent.agent.memory.sqlite_memory import MemoryManager
from quizai_agent.agent.graph.edges import route_after_classify, route_after_evaluate
from quizai_agent.agent.graph.nodes import (
    make_classify_node, make_llm_node, make_retrieve_node,
    make_search_node, make_summarize_node, make_suggest_node,
)
from quizai_agent.agent.graph.state import ZhihuitongAgentState
from quizai_agent.core.logger import get_logger
from quizai_agent.utils.sse import format_suggestions_event

logger = get_logger(__name__)


class AgentWorkflow:
    def __init__(self, chat_agent, rag_agent, memory_manager: MemoryManager, search_agent):
        self.chat_agent = chat_agent
        self.rag_agent = rag_agent
        self.memory = memory_manager
        self.search_agent = search_agent
        self.agent = None

    # 构建 LangGraph 图
    def build(self):
        builder = StateGraph(ZhihuitongAgentState)

        # ── 注册所有节点 ──
        builder.add_node("classify", make_classify_node(self.chat_agent))
        builder.add_node("retrieve", make_retrieve_node(self.rag_agent, self.chat_agent))
        builder.add_node("search", make_search_node(self.search_agent))
        builder.add_node("llm", make_llm_node(self.chat_agent))
        builder.add_node("suggest", make_suggest_node(self.chat_agent))
        builder.add_node("summarize", make_summarize_node(self.chat_agent))

        # ── 路由：classify → 两路分支 ──
        builder.set_entry_point("classify")
        builder.add_conditional_edges(
            "classify",
            route_after_classify,
            {
                "retrieve": "retrieve",
                "llm": "llm",
            },
        )

        # ── 路由：retrieve → 质量评估后决定是否联网搜索 ──
        builder.add_conditional_edges(
            "retrieve",
            route_after_evaluate,
            {
                "search": "search",
                "llm": "llm",
            },
        )

        # ── 搜索后直接到 LLM ──
        builder.add_edge("search", "llm")

        # ── LLM → 推荐 → 摘要 → 结束 ──
        builder.add_edge("llm", "suggest")
        builder.add_edge("suggest", "summarize")
        builder.add_edge("summarize", END)

        self.agent = builder.compile(checkpointer=self.memory.checkpointer)
        logger.info("AgentWorkflow 图构建完成（两路路由 + RAG 质量评估 + 联网搜索 fallback）")

    # 热重载：重新加载 Agent 配置并重建图
    def rebuild(self):
        self.chat_agent.reload()
        self.rag_agent.reload()
        self.build()

    async def stream_chat(self, message: str, thread_id: str = "default", username: str = ""):
        logger.info(f"Sending message to LLM model={self.chat_agent.llm_client.model}")

        if not self.chat_agent.llm_client.api_key:
            logger.warning("LLM_API_KEY 未配置")
            for chunk in ["Echo: ", message]:
                yield chunk
            return

        self.memory.touch_thread(thread_id)

        await self.memory.ensure_threads_loaded()

        if username:
            await self.memory.save_thread_user(thread_id, username)

        await self.memory.save_message(thread_id, "user", message)

        assistant_content = ""

        async for chunk in self.agent.astream(
            {"messages": [HumanMessage(content=message)]},
            config={"configurable": {"thread_id": thread_id}},
            stream_mode="messages",
        ):
            if isinstance(chunk, tuple) and len(chunk) == 2:
                msg, metadata = chunk
                if isinstance(msg, AIMessage) and msg.content:
                    if metadata.get("langgraph_node") == "llm":
                        content = extract_content_text(msg.content)
                        assistant_content += content
                        yield content
            elif isinstance(chunk, AIMessage) and chunk.content:
                content = extract_content_text(chunk.content)
                assistant_content += content
                yield content

        # 获取最终状态，追加来源标记和推荐问题
        try:
            snapshot = await self.agent.aget_state(
                config={"configurable": {"thread_id": thread_id}}
            )

            route_type = snapshot.values.get("route_type", "normal_chat")
            search_results = snapshot.values.get("search_results", "")

            # 仅知识库路由时追加来源
            if route_type == "knowledge":
                if search_results:
                    # 联网搜索结果：标注来源于网络
                    search_marker = "\n\n---\n*此回答信息来源于网络*"
                    assistant_content += search_marker
                    yield search_marker
                else:
                    # RAG 检索结果：标注参考文章来源
                    sources = snapshot.values.get("rag_sources", [])
                    if sources:
                        seen = set()
                        source_lines = []
                        for s in sources:
                            title = s.get("title", "")
                            if title and title not in seen:
                                seen.add(title)
                                source_lines.append(f"- {title}")
                        if source_lines:
                            sources_text = "\n\n---\n**参考来源：**\n" + "\n".join(source_lines)
                            assistant_content += sources_text
                            yield sources_text

            # 推荐问题发送
            suggestions = snapshot.values.get("suggestions", [])
            if suggestions:
                yield format_suggestions_event(suggestions)

        except Exception as e:
            logger.warning(f"获取最终状态失败: {e}")

        if assistant_content:
            await self.memory.save_message(thread_id, "assistant", assistant_content)

        self.memory.touch_thread(thread_id)
