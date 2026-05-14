# 条件路由 — 两路分类：普通聊天 / 知识库检索


def route_after_classify(state) -> str:
    route_type = state.get("route_type", "normal_chat")
    if route_type == "knowledge":
        return "retrieve"
    return "llm"


def route_after_evaluate(state) -> str:
    if state.get("need_search", False):
        return "search"
    return "llm"
