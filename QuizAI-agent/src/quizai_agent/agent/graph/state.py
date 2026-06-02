"""LangGraph 图状态定义 — 三层记忆架构 + RAG 检索"""

from langgraph.graph import MessagesState


class ZhihuitongAgentState(MessagesState):
    # 校园智慧通 AI 智能体状态
    # - messages:       短期记忆，最多保留 16 条
    # - summary:        摘要记忆，80-100 字全局总结
    # - rag_context:    RAG 检索到的知识上下文
    # - rag_sources:    RAG 检索来源 [{title, distance}]
    # - route_type:     路由分类结果（normal_chat / knowledge）
    # - need_search:    RAG 质量评估后是否需要联网搜索
    # - search_results: 联网搜索结果
    # - suggestions:    AI 推荐的下一步问题
    # - MongoDB 自动持久化全量历史作为长期记忆

    summary: str = ""
    rag_context: str = ""
    rag_sources: list = []
    route_type: str = "normal_chat"
    need_search: bool = False
    search_results: str = ""
    suggestions: list = []
