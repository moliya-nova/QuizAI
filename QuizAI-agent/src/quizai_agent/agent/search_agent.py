"""搜索 Agent — 调用 Tavily 联网搜索"""

from quizai_agent.agent.base_agent import BaseAgent
from quizai_agent.utils.search_util import search_tool


class SearchAgent(BaseAgent):

    async def run(self, query: str) -> str:
        """执行联网搜索，返回格式化结果"""
        return search_tool.web_search(query)
