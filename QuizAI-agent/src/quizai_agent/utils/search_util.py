"""联网搜索工具 — Tavily API 封装"""

from tavily import TavilyClient

from quizai_agent.core.settings import settings


class SearchTool:
    def __init__(self):
        self.client = TavilyClient(api_key=settings.tavily_api_key)

    def web_search(self, query: str, max_results=3):
        """执行联网搜索，返回干净正文"""
        response = self.client.search(
            query=query,
            search_depth="basic",
            max_results=max_results
        )

        context = ""
        for res in response["results"]:
            context += f"【标题】{res['title']}\n"
            context += f"【内容】{res['content']}\n"
            context += f"【链接】{res['url']}\n\n"

        return context


search_tool = SearchTool()
