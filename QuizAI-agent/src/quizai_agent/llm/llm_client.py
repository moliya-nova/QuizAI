"""LLM Client — 仅负责 LLM 模型连接，不持有提示词，支持多提供商工厂模式"""

from quizai_agent.core.settings import settings
from quizai_agent.core.logger import get_logger

logger = get_logger(__name__)


class LLMClient:
    def __init__(self):
        self._sync_attrs()
        self.llm = self._create_llm()

    # 异步初始化（预留扩展点）
    async def init(self):
        logger.info("LLM Client 初始化完成")

    # 重新加载 LLM 配置
    def reload(self):
        self._sync_attrs()
        new_llm = self._create_llm()
        self.llm = new_llm
        logger.info(f"LLM client reloaded: provider={self.provider}, model={self.model}, base_url={self.base_url}")

    # 从 settings 同步配置属性
    def _sync_attrs(self):
        self.provider = settings.llm_provider
        self.api_key = settings.llm_api_key
        base_url = settings.llm_base_url.rstrip("/")
        if base_url and not base_url.startswith(("http://", "https://")):
            base_url = "https://" + base_url
        self.base_url = base_url
        self.model = settings.llm_model

    # 工厂方法：根据 provider 配置创建对应的 LLM 实例
    def _create_llm(self):
        provider = self.provider

        if provider == "openai":
            if not self.api_key:
                raise ValueError("OpenAI 协议需要配置 API Key")
            from langchain_openai import ChatOpenAI
            kwargs = {
                "api_key": self.api_key,
                "model": self.model,
                "timeout": 60,
                "streaming": True,
            }
            if self.base_url:
                kwargs["base_url"] = self.base_url
            return ChatOpenAI(**kwargs)

        elif provider == "anthropic":
            if not self.api_key:
                raise ValueError("Anthropic 协议需要配置 API Key")
            from langchain_anthropic import ChatAnthropic
            kwargs = {
                "api_key": self.api_key,
                "model": self.model,
                "timeout": 60,
                "streaming": True,
            }
            if self.base_url:
                kwargs["base_url"] = self.base_url
            return ChatAnthropic(**kwargs)

        elif provider == "ollama":
            from langchain_ollama import ChatOllama
            kwargs = {
                "model": self.model,
                "timeout": 60,
            }
            if self.base_url:
                kwargs["base_url"] = self.base_url
            return ChatOllama(**kwargs)

        else:
            raise ValueError(f"不支持的 LLM 提供商: {provider}，可选: openai / anthropic / ollama")
