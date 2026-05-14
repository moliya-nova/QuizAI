"""对话智能体 — 多轮对话 + 提示词拼接 + 记忆压缩"""

from langchain_core.messages import HumanMessage, SystemMessage

from quizai_agent.agent.base_agent import BaseAgent


def extract_content_text(content) -> str:
    """从 LLM 响应中提取文本内容，兼容 OpenAI 和 Anthropic 格式"""
    if isinstance(content, str):
        return content
    elif isinstance(content, list):
        # Anthropic 格式: [{"type": "text", "text": "..."}]
        texts = []
        for item in content:
            if isinstance(item, dict) and item.get("type") == "text":
                texts.append(item.get("text", ""))
            elif isinstance(item, str):
                texts.append(item)
        return "".join(texts)
    else:
        return str(content)
from quizai_agent.agent.prompt.chat_prompt import (
    CLASSIFY_PROMPT,
    FALLBACK_SUMMARY,
    HISTORY_SUMMARY_PREFIX,
    SUGGESTIONS_PROMPT,
    SUMMARY_COMPRESSION,
)
from quizai_agent.agent.prompt.rag_prompt import RAG_CONTEXT_PREFIX
from quizai_agent.agent.prompt.search_prompt import SEARCH_CONTEXT_PREFIX
from quizai_agent.agent.prompt.system_prompt import SYSTEM_DEFAULT
from quizai_agent.core.settings import settings
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


class ChatAgent(BaseAgent):
    def __init__(self, llm_client):
        super().__init__(llm_client)
        self.system_prompt = settings.system_prompt or SYSTEM_DEFAULT

    def reload(self):
        if settings.system_prompt is not None:
            self.system_prompt = settings.system_prompt

    # 调用 LLM 判断用户问题的路由类型：normal_chat / knowledge
    async def classify(self, query: str) -> str:
        prompt = CLASSIFY_PROMPT.format(user_query=query)
        response = await self.llm.ainvoke([HumanMessage(content=prompt)])
        result = extract_content_text(response.content).strip()
        if result in ("normal_chat", "knowledge"):
            return result
        return "normal_chat"  # 默认走普通聊天

    # 组装完整上下文：系统提示词 → 历史摘要 → RAG/搜索上下文 → 最新消息
    def build_context_messages(self, state: dict, max_messages: int = 16) -> list:
        messages = list(state.get("messages", []))
        summary = state.get("summary", "")
        rag_context = state.get("rag_context", "")

        context_messages = [SystemMessage(content=self.system_prompt)]

        if summary:
            context_messages.append(
                SystemMessage(content=HISTORY_SUMMARY_PREFIX.format(summary=summary))
            )

        if rag_context:
            context_messages.append(
                SystemMessage(content=RAG_CONTEXT_PREFIX.format(rag_context=rag_context))
            )

        search_results = state.get("search_results", "")
        if search_results:
            context_messages.append(
                SystemMessage(content=SEARCH_CONTEXT_PREFIX.format(search_results=search_results))
            )

        if len(messages) > max_messages:
            messages = messages[-max_messages:]

        context_messages.extend(messages)
        return context_messages

    # 调用 LLM 生成推荐问题
    async def generate_suggestions(self, query: str, ai_answer: str) -> list:
        prompt = SUGGESTIONS_PROMPT.format(user_query=query, ai_answer=ai_answer[:500])
        try:
            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            content_text = extract_content_text(response.content).strip()
            suggestions = [
                line.strip()
                for line in content_text.split("\n")
                if line.strip() and len(line.strip()) <= 30
            ]
            return suggestions[:3]
        except Exception:
            return []

    # 调用 LLM 生成对话摘要，含异常回退
    async def generate_summary(self, old_messages: list, old_summary: str) -> str:
        text_parts = []
        for msg in old_messages:
            role = "用户" if isinstance(msg, HumanMessage) else "助手"
            content = extract_content_text(msg.content)
            text_parts.append(f"{role}: {content}")

        prompt = SUMMARY_COMPRESSION.format(
            old_summary=old_summary if old_summary else "无",
            text_parts="\n".join(text_parts),
        )

        try:
            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            new_summary = extract_content_text(response.content).strip()
            if len(new_summary) > 100:
                new_summary = new_summary[:100]
            return new_summary
        except Exception:
            return FALLBACK_SUMMARY.format(count=len(old_messages))
