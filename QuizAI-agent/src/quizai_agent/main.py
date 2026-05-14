"""FastAPI 应用入口"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from quizai_agent.core.lifecycle import lifespan
from quizai_agent.api.chat_router import router as chat_router
from quizai_agent.api.config_router import router as config_router
from quizai_agent.api.rag_router import router as rag_router


app = FastAPI(
    title="QuizAI Agent API",
    version="1.0.0",
    description="QuizAI 刷题助手 AI 接口服务，提供对话、RAG检索功能",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat_router)
app.include_router(config_router)
app.include_router(rag_router)

def main():
    """启动服务"""
    import uvicorn
    uvicorn.run(
        "quizai_agent.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
    )


if __name__ == "__main__":
    main()
