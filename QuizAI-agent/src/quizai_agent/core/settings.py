import os
import threading
from pathlib import Path
from dotenv import load_dotenv, set_key

# 从项目根目录加载 .env（无论从哪里启动）
_PROJECT_DIR = Path(__file__).resolve().parent.parent.parent.parent
load_dotenv(_PROJECT_DIR / ".env")

ENV_FILE = str(_PROJECT_DIR / ".env")

# 字段名到环境变量名的映射
_ENV_MAPPING = {
    "llm_provider": "LLM_PROVIDER",
    "llm_api_key": "LLM_API_KEY",
    "llm_base_url": "LLM_BASE_URL",
    "llm_model": "LLM_MODEL",
    "vector_store_path": "VECTOR_STORE_PATH",
    "chunk_size": "CHUNK_SIZE",
    "chunk_overlap": "CHUNK_OVERLAP",
    "host": "HOST",
    "port": "PORT",
    "system_prompt": "SYSTEM_PROMPT",
    "docstore_path": "DOCSTORE_PATH",
    # 搜索工具配置
    "tavily_api_key": "TAVILY_API_KEY",
    "tavily_api_url": "TAVILY_API_URL",
    # Java后端配置
    "java_backend_url": "JAVA_BACKEND_URL",
    # Embedding模型配置
    "embedding_model": "EMBEDDING_MODEL",
    "embedding_device": "EMBEDDING_DEVICE",
}


# 应用配置，支持动态重载和持久化到 .env 文件
class Settings:

    def __init__(self):
        self.lock = threading.RLock()  # 可重入锁，update() 内调用 reload() 不会死锁
        self.reload()

    # 从环境变量重新加载所有配置
    def reload(self):
        with self.lock:
            self.llm_provider = os.getenv("LLM_PROVIDER", "openai")
            self.llm_api_key = os.getenv("LLM_API_KEY", "")
            self.llm_base_url = os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
            self.llm_model = os.getenv("LLM_MODEL", "gpt-4o")
            self.vector_store_path = os.getenv("VECTOR_STORE_PATH", "./data/vector_store")
            self.chunk_size = int(os.getenv("CHUNK_SIZE", "1500"))
            self.chunk_overlap = int(os.getenv("CHUNK_OVERLAP", "100"))
            self.host = os.getenv("HOST", "0.0.0.0")
            self.port = int(os.getenv("PORT", "8000"))
            self.system_prompt = os.getenv("SYSTEM_PROMPT", "")
            self.docstore_path = os.getenv("DOCSTORE_PATH", "./data/docstore.db")

            # 搜索工具配置
            self.tavily_api_key = os.getenv("TAVILY_API_KEY", "")
            self.tavily_api_url = os.getenv("TAVILY_API_URL", "https://api.tavily.com/v1/search")

            # Java后端配置
            self.java_backend_url = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")

            # Embedding模型配置
            self.embedding_model = os.getenv("EMBEDDING_MODEL", "BAAI/bge-large-zh-v1.5")
            self.embedding_device = os.getenv("EMBEDDING_DEVICE", "cpu")

    # 更新配置并持久化到 .env 文件
    def update(self, updates: dict):
        with self.lock:
            # 过滤掉掩码化的敏感字段（含 *** 的值是脱敏后的占位符，不应覆盖真实值）
            if "llm_api_key" in updates and "***" in str(updates.get("llm_api_key", "")):
                del updates["llm_api_key"]
            if "tavily_api_key" in updates and "***" in str(updates.get("tavily_api_key", "")):
                del updates["tavily_api_key"]

            for key, value in updates.items():
                env_key = _ENV_MAPPING.get(key)
                if env_key:
                    os.environ[env_key] = str(value)
                    if ENV_FILE:
                        set_key(ENV_FILE, env_key, str(value))
            self.reload()

    # 返回当前配置（API key 脱敏）
    def to_dict(self) -> dict:
        with self.lock:
            masked_key = "***"
            if self.llm_api_key and len(self.llm_api_key) > 8:
                masked_key = self.llm_api_key[:8] + "***"
            return {
                "llm_provider": self.llm_provider,
                "llm_api_key": masked_key,
                "llm_base_url": self.llm_base_url,
                "llm_model": self.llm_model,
                "vector_store_path": self.vector_store_path,
                "chunk_size": self.chunk_size,
                "chunk_overlap": self.chunk_overlap,
                "host": self.host,
                "port": self.port,
                "system_prompt": self.system_prompt,
                "docstore_path": self.docstore_path,
                # 搜索工具配置
                "tavily_api_key": self.tavily_api_key[:8] + "***" if self.tavily_api_key and len(self.tavily_api_key) > 8 else "***",
                "tavily_api_url": self.tavily_api_url,
                # Java后端配置
                "java_backend_url": self.java_backend_url,
                # Embedding模型配置
                "embedding_model": self.embedding_model,
                "embedding_device": self.embedding_device,
            }


settings = Settings()
