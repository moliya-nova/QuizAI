from langchain.tools import tool
from quizai_agent.utils.search_util import search_tool


@tool
def internet_search(query: str) -> str:
    """用于联网搜索，获取最新资讯、外部知识、实时信息时使用"""
    return search_tool.web_search(query)
