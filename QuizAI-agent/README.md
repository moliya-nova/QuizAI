# 校园智慧通AI智能体服务

基于LangChain和LangGraph的AI智能体服务，为校园智慧通系统提供智能对话、RAG检索、联网搜索等功能。

## 功能特性

- **智能对话**：支持多轮对话，自动记忆管理
- **RAG检索**：基于向量数据库的知识检索
- **联网搜索**：通过Tavily API获取实时信息
- **记忆管理**：短期记忆、摘要记忆、长期记忆三层架构
- **流式输出**：SSE流式响应，实时对话体验

## 快速开始

### 1. 安装依赖

```bash
# 使用uv安装依赖
uv sync

# 或者使用pip
pip install -e .
```

### 2. 配置环境变量

复制`.env.example`为`.env`，并填写配置：

```bash
cp .env.example .env
```

编辑`.env`文件，配置以下必要参数：

- `LLM_API_KEY`：LLM API密钥
- `LLM_BASE_URL`：LLM API地址
- `LLM_MODEL`：使用的模型名称

### 3. 启动服务

```bash
# 使用uv运行
uv run zhihuitong-agent

# 或者直接运行Python模块
python -m zhihuitong_agent.main
```

服务将在`http://0.0.0.0:8000`启动。

## API接口

### 对话接口

- `POST /chat/stream`：流式对话
- `DELETE /chat/memory/{thread_id}`：清除会话记忆
- `GET /chat/threads`：获取活跃会话列表
- `GET /chat/history/{thread_id}`：获取会话历史

### 配置接口

- `GET /config`：获取当前配置
- `PUT /config`：更新配置

### RAG接口

- `POST /rag/index`：索引单篇文章
- `DELETE /rag/index/{article_id}`：删除文章索引
- `POST /rag/index/all`：批量重建索引

## 项目结构

```
zhihuitong-agent-python/
├── pyproject.toml             # 项目配置
├── .env.example               # 环境变量示例
├── README.md                  # 使用说明
├── src/
│   └── zhihuitong_agent/      # 主包
│       ├── __init__.py
│       ├── main.py            # 应用入口
│       ├── core/              # 核心模块
│       ├── llm/               # LLM模块
│       ├── agent/             # Agent模块
│       ├── rag/               # RAG模块
│       ├── tools/             # 工具模块
│       ├── api/               # API模块
│       ├── models/            # 数据模型
│       └── utils/             # 工具类
└── data/                      # 数据目录
    ├── vector_store/          # ChromaDB向量存储
    └── docstore.db            # SQLite文档存储
```

## 与Java后端集成

Python智能体服务通过REST API与Java后端集成：

1. Java后端调用Python服务的API接口
2. Python服务处理AI相关的业务逻辑
3. 结果通过API返回给Java后端

## 开发说明

### 添加新的Agent

1. 在`agent/`目录下创建新的Agent类
2. 继承`BaseAgent`基类
3. 实现`run`方法
4. 在`core/lifecycle.py`中注册新的Agent

### 添加新的工具

1. 在`tools/`目录下创建新的工具类
2. 使用`@tool`装饰器定义工具
3. 在Agent中调用工具

### 修改提示词

编辑`agent/prompt/`目录下的提示词文件：

- `system_prompt.py`：系统提示词
- `chat_prompt.py`：对话提示词
- `rag_prompt.py`：RAG提示词
- `search_prompt.py`：搜索提示词

## 常见问题

### Q: 如何更换LLM模型？

A: 修改`.env`文件中的`LLM_MODEL`参数，支持所有OpenAI API兼容的模型。

### Q: 如何添加自定义知识库？

A: 使用`POST /rag/index`接口索引文章，或使用`POST /rag/index/all`接口批量索引。

### Q: 如何查看日志？

A: 日志会输出到控制台，格式为`时间 [级别] 模块名: 消息`。

## 许可证

MIT License
