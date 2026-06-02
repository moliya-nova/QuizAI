# 小程序 AI 对话 — 语音输入 + 图片理解功能实现计划

## 背景

当前 QuizAI 小程序 AI 对话页面仅支持文本输入。需要新增：
1. **语音输入**：长按说话 → Whisper 转文字 → 回显到输入框（用户可编辑再发送）
2. **图片理解**：用户上传图片 → 多模态 LLM 转义为文字描述 → 与用户问题拼接后走原有流程

核心原则：**不破坏现有 LangGraph 图结构**，通过新增前置节点和独立配置实现。

## 架构设计

```
                    ┌─────────────────────────────────────────────┐
                    │           Python Agent Service              │
                    │                                             │
                    │  ┌─────────┐    ┌──────────────────────┐   │
  语音录音 ──────────►│  Whisper  │───►│  /asr/transcribe     │   │
  (mp3上传)          │  ASR      │    │  返回文字             │   │──► 小程序输入框
                    │  └─────────┘    └──────────────────────┘   │
                    │                                             │
                    │  ┌─────────┐    ┌──────────────────────┐   │
  图片+文字 ─────────►│  Vision  │───►│  preprocess 节点     │   │
  (chat/stream)      │  LLM     │    │  图片→文字描述        │   │
                    │  (独立配置)│    └────────┬─────────────┘   │
                    │  └─────────┘             │                 │
                    │                    ┌─────▼──────┐          │
                    │                    │  classify   │          │
                    │                    │  → llm      │          │
                    │                    │  → suggest  │          │
                    │                    │  → summarize│          │
                    │                    └────────────┘          │
                    └─────────────────────────────────────────────┘
```

**关键设计**：
- 语音识别是**独立 API**，不进 LangGraph，返回文字给小程序
- 图片理解在 LangGraph 图入口加一个 **preprocess 节点**，其余节点不动
- Vision LLM 有**独立配置**（provider/key/url/model），与主 LLM 分开，方便切换

## 需要修改的文件

### Python Agent（QuizAI-agent/）

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | `src/quizai_agent/api/asr_router.py` | ASR 端点 `POST /asr/transcribe` |
| 新建 | `src/quizai_agent/llm/vision_client.py` | Vision LLM 客户端（独立配置） |
| 新建 | `src/quizai_agent/agent/graph/preprocess.py` | 图片预处理节点 |
| 修改 | `src/quizai_agent/core/settings.py` | 新增 4 个 VISION_ 配置项 |
| 修改 | `src/quizai_agent/agent/graph/state.py` | 新增 `image_description` 字段 |
| 修改 | `src/quizai_agent/agent/graph/workflow.py` | 注册 preprocess 节点 |
| 修改 | `src/quizai_agent/models/schemas/chat.py` | ChatRequest 新增 `image_base64` 字段 |
| 修改 | `src/quizai_agent/main.py` | 注册 asr_router |
| 修改 | `pyproject.toml` | 新增 `openai-whisper` 依赖 |

### Spring Boot 后端（QuizAI-backend/）

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | `quizai-agent/.../controller/AsrController.java` | ASR 接口代理 |
| 新建 | `quizai-agent/.../service/AsrService.java` | 转发到 Python Whisper |

### 小程序（QuizAI-minipro/）

| 操作 | 文件 | 说明 |
|------|------|------|
| 修改 | `pages/chat/chat.js` | 录音逻辑 + 图片选择 + 上传 |
| 修改 | `pages/chat/chat.wxml` | 输入区域改造（语音按钮 + 图片按钮） |
| 修改 | `pages/chat/chat.wxss` | 新增样式 |

## 详细实现步骤

### Step 1：Python — Vision LLM 独立配置和客户端

**settings.py** 新增配置项：
```python
# Vision 多模态模型配置（独立于主 LLM）
self.vision_provider = os.getenv("VISION_LLM_PROVIDER", "openai")
self.vision_api_key = os.getenv("VISION_LLM_API_KEY", "")
self.vision_base_url = os.getenv("VISION_LLM_BASE_URL", "https://api.deepseek.com")
self.vision_model = os.getenv("VISION_LLM_MODEL", "deepseek-vl2")
```

**vision_client.py**：复用 LLMClient 的工厂模式，创建独立的 vision LLM 实例。
- 支持 openai / anthropic / ollama 三种协议
- 默认用 DeepSeek VL2（通过 OpenAI 兼容协议）
- .env 中配置 `VISION_LLM_*` 四个变量

### Step 2：Python — LangGraph 新增 preprocess 节点

**state.py** 新增字段：
```python
image_description: str = ""  # 图片转义后的文字描述
```

**preprocess.py** 新增节点：
```python
def make_preprocess_node(vision_client):
    async def preprocess_node(state):
        # 提取最后一条用户消息
        # 如果消息中包含图片（content 为 list 格式含 image_url）
        # → 调用 vision_client 生成图片描述
        # → 将描述存入 state["image_description"]
        # 如果没有图片，直接返回空
        ...
    return preprocess_node
```

**workflow.py** 修改图结构：
```python
# 原有：set_entry_point("classify")
# 改为：set_entry_point("preprocess")
# 新增：builder.add_edge("preprocess", "classify")
# 其余所有节点和边不变
```

**chat_agent.py** 的 `build_context_messages` 方法：
- 如果 `state` 中有 `image_description`，在用户最新消息前插入图片描述

### Step 3：Python — ChatRequest 扩展

**chat.py**：
```python
class ChatRequest(BaseModel):
    message: str
    thread_id: str = "default"
    username: str = ""
    image_base64: str = ""  # 新增：base64 编码的图片（可选）
```

### Step 4：Python — ASR 端点

**asr_router.py**：
```python
@router.post("/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    # 保存临时文件 → whisper.transcribe() → 返回文字 → 删除临时文件
```

**main.py** 注册：`app.include_router(asr_router)`

### Step 5：Spring Boot — ASR 代理

**AsrController.java**：`POST api/agent/asr/transcribe`
- 接收小程序上传的 mp3 文件
- 校验 JWT + 文件大小（≤25MB）
- 通过 WebClient 转发到 Python `/asr/transcribe`

**AsrService.java**：复用已有的 `agentWebClient`

### Step 6：小程序 — 语音输入

**chat.js** 新增：
- `voiceState`（idle/recording/recognizing）、`isVoiceMode`、`recordingTime`
- `initRecorder()`：初始化 RecorderManager，监听 onStart/onStop/onError
- `onVoiceTouchStart/Move/End`：长按录音，上滑取消
- `uploadAndRecognize()`：wx.uploadFile 上传到后端 ASR 接口
- 识别结果**回显到 inputText**（不自动发送，用户可编辑）

**chat.wxml** 输入区域改造：
- 左侧：🎤 按钮切换语音模式
- 键盘模式：现有 input + 发送按钮（不变）
- 语音模式：按住说话 / 录音中（红色脉冲）/ 识别中（loading）

### Step 7：小程序 — 图片输入

**chat.js** 新增：
- `chooseImage()`：wx.chooseImage 或 wx.chooseMedia 选择图片
- 将图片转为 base64，附加到 streamChat 请求的 `image_base64` 字段
- 修改 `streamChat()` 方法，请求体新增 `image_base64` 字段

**chat.wxml** 输入区域：
- 键盘模式下，输入框左侧新增 📷 图片按钮
- 选中图片后显示缩略图预览（可点击删除）

## .env 配置示例

```bash
# 主 LLM（对话用，已有）
LLM_PROVIDER=anthropic
LLM_API_KEY=sk-xxx
LLM_BASE_URL=https://api.deepseek.com/anthropic
LLM_MODEL=deepseek-v4-flash

# Vision LLM（图片转义用，新增）
VISION_LLM_PROVIDER=openai
VISION_LLM_API_KEY=sk-xxx           # 可以复用 DeepSeek Key
VISION_LLM_BASE_URL=https://api.deepseek.com
VISION_LLM_MODEL=deepseek-vl2
```

## 异常处理

| 场景 | 处理 |
|------|------|
| 麦克风权限被拒 | 弹窗引导去设置 |
| 录音太短（<1秒） | Toast "说话时间太短" |
| 上滑取消录音 | 静默取消 |
| ASR 服务异常 | Toast 错误信息 |
| Vision LLM 不可用 | 跳过图片转义，仅用文字部分 |
| 图片过大 | 压缩后上传（小程序端 wx.compressImage） |

## 验证方案

1. **Whisper 单测**：curl 上传 mp3 到 Python `/asr/transcribe`，验证中文/英文/混合识别
2. **Vision 单测**：curl 发送含图片的 chat 请求，验证 preprocess 节点输出图片描述
3. **后端代理测试**：Postman 测试 ASR 接口的 JWT 鉴权和转发
4. **小程序语音测试**：长按录音 → 识别 → 回显输入框 → 编辑 → 发送
5. **小程序图片测试**：选图 → 发送 → AI 回复中体现图片内容理解
6. **边界测试**：短按、超时、断网、权限拒绝、超大图片
