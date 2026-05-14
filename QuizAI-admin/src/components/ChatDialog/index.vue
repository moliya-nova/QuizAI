<template>
  <transition name="chat-slide">
    <div
      v-if="visible"
      class="chat-panel"
      :style="{ left: panelX + 'px', top: panelY + 'px' }"
    >
      <!-- 头部（可拖动） -->
      <div class="chat-panel-header" @mousedown="onDragStart">
        <div class="header-left">
          <div class="ai-avatar">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div>
            <div class="title">AI 刷题助手</div>
            <div class="status">
              <span class="status-dot"></span>
              在线
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-icon class="header-btn" @click.stop="handleNewChat" title="新对话"><Refresh /></el-icon>
          <el-icon class="header-btn" @click.stop="emit('update:modelValue', false)" title="收起"><Minus /></el-icon>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-avatar">
            <el-icon :size="28"><ChatDotRound /></el-icon>
          </div>
          <div class="welcome-title">你好，我是 QuizAI 刷题助手</div>
          <div class="welcome-subtitle">可以问我技术面试相关问题</div>
          <div class="welcome-chips">
            <div
              v-for="chip in welcomeChips"
              :key="chip"
              class="chat-chip"
              @click="sendMessage(chip)"
            >
              {{ chip }}
            </div>
          </div>
        </div>

        <template v-for="msg in messages" :key="msg.id">
          <div class="message-row" :class="msg.role">
            <div class="message-bubble">
              <div v-if="msg.id === typingMessageId" class="message-content">
                <span class="chat-markdown" v-html="renderMarkdown(displayedText)"></span>
                <span class="typing-cursor"></span>
              </div>
              <div v-else class="message-content">
                <span class="chat-markdown" v-html="renderMarkdown(msg.content)"></span>
              </div>
              <span
                v-if="msg.role === 'assistant' && msg.id !== typingMessageId"
                class="copy-btn"
                @click="handleCopy(msg.content)"
                title="复制"
              >
                <el-icon><CopyDocument /></el-icon>
              </span>
            </div>

            <div v-if="msg.role === 'assistant' && msg.suggestions?.length" class="suggestions-panel">
              <div class="suggestions-label">
                <el-icon><ChatLineSquare /></el-icon>
                <span>你可能还想问</span>
              </div>
              <div
                v-for="(s, idx) in msg.suggestions"
                :key="s"
                class="suggestion-card"
                :style="{ '--accent-color': suggestionColors[idx % suggestionColors.length] }"
                @click="sendMessage(s)"
              >
                <span class="suggestion-text">{{ s }}</span>
                <el-icon class="suggestion-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </template>

        <div v-if="waitingForReply" class="message-row assistant">
          <div class="message-bubble">
            <div class="typing-indicator">
              <div class="dot"></div>
              <div class="dot"></div>
              <div class="dot"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area" @mousedown.capture="focusInput">
        <div class="input-wrapper">
          <el-input
            ref="inputRef"
            class="chat-textarea"
            v-model="inputText"
            type="textarea"
            rows="1"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="handleSend"
            :disabled="streaming"
          />
          <button
            class="send-btn"
            :disabled="!inputText.trim() || streaming"
            @click="handleSend"
          >
            <el-icon :size="18"><Promotion /></el-icon>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { ChatDotRound, Refresh, Promotion, CopyDocument, Minus, ChatLineSquare, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { streamChat, deleteMemory, getHistory } from '@/api/agent/chat'

// ── 配置 marked ──
marked.setOptions({ breaks: true, gfm: true })
const renderer = new marked.Renderer()
renderer.code = ({ text, lang }) => {
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(text, { language }).value
  return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
}
marked.use({ renderer })

function renderMarkdown(text) {
  const clean = text.replace(/\n+$/, '')
  return (marked.parse(clean) || '').trim()
}

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

// ── 拖动 ──
const PANEL_W = 540
const PANEL_H = 720
const panelX = ref(window.innerWidth - PANEL_W - 28)
const panelY = ref(window.innerHeight - PANEL_H - 100)
let dragging = false
let dragOffsetX = 0
let dragOffsetY = 0

function onDragStart(e) {
  const target = e.target
  if (target.closest('.header-actions')) return
  dragging = true
  dragOffsetX = e.clientX - panelX.value
  dragOffsetY = e.clientY - panelY.value
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = 'none'
}

function onDragMove(e) {
  if (!dragging) return
  const x = e.clientX - dragOffsetX
  const y = e.clientY - dragOffsetY
  panelX.value = Math.max(0, Math.min(x, window.innerWidth - PANEL_W))
  panelY.value = Math.max(0, Math.min(y, window.innerHeight - 60))
}

function onDragEnd() {
  dragging = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = ''
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
})

// ── 状态 ──
const messagesRef = ref(null)
const inputRef = ref(null)
const inputText = ref('')
const streaming = ref(false)
const waitingForReply = ref(false)
const messages = ref([])
const threadId = ref('')
const typingMessageId = ref(null)
const displayedText = ref('')
const abortController = ref(null)

const welcomeChips = ['Java 中 HashMap 的原理是什么？', '如何优化 MySQL 查询？', 'Spring Boot 的核心注解有哪些？']
const suggestionColors = ['#6366f1', '#8b5cf6', '#06b6d4']

function focusInput() {
  setTimeout(() => {
    const textarea = inputRef.value?.$el?.querySelector('textarea')
    if (textarea) textarea.focus()
  }, 0)
}

// ── 会话管理 ──
function getSessionKey() {
  return 'chat_session_quizai'
}

function loadSession() {
  const saved = localStorage.getItem(getSessionKey())
  if (saved) {
    try {
      const data = JSON.parse(saved)
      threadId.value = data.threadId || ''
      messages.value = data.messages || []
    } catch { initSession() }
  } else { initSession() }
}

function saveSession() {
  localStorage.setItem(getSessionKey(), JSON.stringify({
    threadId: threadId.value,
    messages: messages.value
  }))
}

function initSession() {
  threadId.value = crypto.randomUUID()
  messages.value = []
  saveSession()
}

// ── 打字机 ──
function typewriterEffect(text, startIndex, onComplete) {
  let index = startIndex
  const timer = setInterval(() => {
    if (index < text.length) {
      index = Math.min(index + 2, text.length)
      displayedText.value = text.substring(0, index)
      scrollToBottom()
    } else {
      clearInterval(timer)
      onComplete()
    }
  }, 18)
  return timer
}

// ── 发送 ──
function handleSend() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return
  inputText.value = ''
  sendMessage(text)
}

async function sendMessage(text) {
  const userMsg = {
    id: crypto.randomUUID(),
    role: 'user',
    content: text,
    timestamp: Date.now()
  }
  messages.value.push(userMsg)
  saveSession()
  scrollToBottom()

  streaming.value = true
  waitingForReply.value = true
  abortController.value = new AbortController()

  const assistantMsg = {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '',
    timestamp: Date.now()
  }

  let fullText = ''
  let typewriterTimer = null

  try {
    await streamChat(
      text,
      (resp) => {
        if (resp.code === 200 && typeof resp.data === 'string') {
          if (waitingForReply.value) {
            waitingForReply.value = false
            typingMessageId.value = assistantMsg.id
            displayedText.value = ''
            messages.value.push(assistantMsg)
          }
          const currentLength = displayedText.value.length
          if (typewriterTimer) clearInterval(typewriterTimer)
          fullText += resp.data
          typewriterTimer = typewriterEffect(fullText, currentLength, () => {
            assistantMsg.content = fullText
            typewriterTimer = null
            saveSession()
          })
        } else if (resp.code === 201 && Array.isArray(resp.data)) {
          assistantMsg.suggestions = resp.data
          saveSession()
        } else if (resp.msg) {
          if (waitingForReply.value) waitingForReply.value = false
          assistantMsg.content += resp.msg
          messages.value.push(assistantMsg)
          saveSession()
        }
      },
      threadId.value,
      abortController.value.signal
    )
  } catch (e) {
    if (e.name !== 'AbortError') {
      if (!assistantMsg.content) {
        assistantMsg.content = '请求失败，请稍后重试。'
      }
    }
  } finally {
    if (typewriterTimer) clearInterval(typewriterTimer)
    if (fullText) assistantMsg.content = fullText
    if (!messages.value.find(m => m.id === assistantMsg.id)) {
      if (!assistantMsg.content) {
        assistantMsg.content = '抱歉，未能获取到回答，请重试。'
      }
      messages.value.push(assistantMsg)
    }
    typingMessageId.value = null
    streaming.value = false
    waitingForReply.value = false
    abortController.value = null
    saveSession()
    scrollToBottom()
    setTimeout(() => {
      const textarea = inputRef.value?.$el?.querySelector('textarea')
      if (textarea) textarea.focus()
    }, 50)
  }
}

// ── 新对话 ──
async function handleNewChat() {
  if (streaming.value) abortController.value?.abort()
  if (threadId.value && messages.value.length > 0) {
    try { await deleteMemory(threadId.value) } catch {}
  }
  initSession()
}

// ── 复制 ──
async function handleCopy(content) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制')
  } catch { ElMessage.warning('复制失败') }
}

// ── 滚动 ──
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

// ── 历史 ──
async function loadHistory() {
  if (!threadId.value) return
  try {
    const history = await getHistory(threadId.value)
    if (history.length > 0) {
      messages.value = history.map((h, i) => ({
        id: `history_${i}`,
        role: h.role,
        content: h.content,
        timestamp: h.created_at ? new Date(h.created_at).getTime() : undefined
      }))
      saveSession()
      scrollToBottom()
    }
  } catch { /* 无历史 */ }
}

// ── 打开时加载 ──
watch(visible, (v) => {
  if (v) {
    panelX.value = window.innerWidth - PANEL_W - 28
    panelY.value = window.innerHeight - PANEL_H - 100
    loadSession()
    loadHistory()
  }
})
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$primary-light: #818cf8;
$surface: #ffffff;
$surface-dim: #f8fafc;
$border: #e2e8f0;
$text: #1e293b;
$text-secondary: #64748b;
$text-muted: #94a3b8;
$radius-lg: 20px;
$radius-md: 14px;
$radius-sm: 10px;

.chat-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.chat-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.chat-slide-enter-from {
  opacity: 0;
  transform: scale(0.92);
}
.chat-slide-leave-to {
  opacity: 0;
  transform: scale(0.96);
}

.chat-panel {
  position: fixed;
  width: 540px;
  height: 720px;
  background: $surface;
  border-radius: $radius-lg;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.14), 0 8px 24px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9998;
}

.chat-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, $primary, $primary-light);
  color: #fff;
  cursor: grab;
  user-select: none;

  &:active { cursor: grabbing; }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    pointer-events: none;

    .ai-avatar {
      width: 38px;
      height: 38px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 18px;
    }

    .title { font-size: 15px; font-weight: 600; }
    .status {
      font-size: 12px; opacity: 0.85; display: flex; align-items: center; gap: 5px; margin-top: 1px;
      .status-dot {
        width: 6px; height: 6px; border-radius: 50%; background: #4ade80;
        box-shadow: 0 0 6px rgba(74, 222, 128, 0.5);
      }
    }
  }

  .header-actions {
    display: flex; gap: 4px;
    .header-btn {
      width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center;
      cursor: pointer; color: rgba(255, 255, 255, 0.7); transition: all 0.2s;
      &:hover { background: rgba(255, 255, 255, 0.15); color: #fff; }
    }
  }
}

.chat-messages {
  flex: 1; min-height: 0; overflow-y: auto; padding: 20px 24px; background: $surface-dim; scroll-behavior: smooth;
  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
}

.welcome-screen {
  display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; padding: 20px;
  .welcome-avatar {
    width: 56px; height: 56px; border-radius: 16px;
    background: linear-gradient(135deg, $primary, $primary-light);
    display: flex; align-items: center; justify-content: center; color: #fff;
    margin-bottom: 14px; box-shadow: 0 8px 24px rgba(99, 102, 241, 0.25);
  }
  .welcome-title { font-size: 17px; font-weight: 600; color: $text; margin-bottom: 4px; }
  .welcome-subtitle { font-size: 13px; color: $text-muted; margin-bottom: 20px; }
  .welcome-chips { display: flex; flex-direction: column; gap: 8px; width: 100%; max-width: 300px; }
  .chat-chip {
    padding: 10px 16px; border: 1px solid $border; border-radius: $radius-sm; background: $surface;
    color: $text-secondary; font-size: 13px; cursor: pointer; transition: all 0.2s; text-align: center;
    &:hover {
      border-color: $primary; color: $primary; background: rgba(99, 102, 241, 0.04);
      transform: translateY(-1px); box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
    }
  }
}

.message-row {
  display: flex; margin-bottom: 12px; animation: fadeSlideIn 0.3s ease;
  &.user {
    justify-content: flex-end;
    .message-bubble {
      background: linear-gradient(135deg, $primary, $primary-light);
      color: #fff; border-radius: $radius-md $radius-md 4px $radius-md; max-width: 80%;
    }
  }
  &.assistant {
    justify-content: flex-start; flex-wrap: wrap;
    .message-bubble {
      background: $surface; color: $text; border-radius: $radius-md $radius-md $radius-md 4px;
      max-width: 85%; border: 1px solid $border; position: relative;
      .copy-btn {
        position: absolute; top: 6px; right: 6px; opacity: 0; transition: opacity 0.2s;
        padding: 4px; font-size: 13px; color: $text-muted; cursor: pointer;
        background: $surface; border: 1px solid $border; border-radius: 6px; display: flex;
        &:hover { color: $primary; border-color: $primary; }
      }
      &:hover .copy-btn { opacity: 1; }
    }
    .suggestions-panel { flex-basis: 100%; }
  }
  .message-content { padding: 10px 14px; font-size: 13.5px; line-height: 1.65; overflow-wrap: break-word; }
}

.typing-cursor {
  display: inline-block; width: 2px; height: 14px; background: $primary;
  margin-left: 2px; vertical-align: text-bottom; animation: blink 0.8s infinite;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.typing-indicator {
  display: flex; gap: 5px; padding: 8px 4px;
  .dot {
    width: 7px; height: 7px; border-radius: 50%; background: #cbd5e1;
    animation: bounce 1.2s ease-in-out infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}
@keyframes bounce { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-5px); } }
@keyframes fadeSlideIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }

.suggestions-panel {
  margin-top: 6px; margin-bottom: 4px; width: fit-content;
  display: flex; flex-direction: column; gap: 4px;
  .suggestions-label {
    display: flex; align-items: center; gap: 5px; font-size: 11.5px; color: $text-muted; padding-left: 2px;
    .el-icon { font-size: 13px; }
  }
  .suggestion-card {
    display: inline-flex; align-items: center; gap: 6px; padding: 6px 10px;
    background: $surface; border: 1px solid $border;
    border-left: 3px solid var(--accent-color, $primary);
    border-radius: 6px; cursor: pointer; transition: all 0.2s ease; width: fit-content;
    .suggestion-text { font-size: 12.5px; color: $text-secondary; line-height: 1.4; white-space: nowrap; }
    .suggestion-arrow { font-size: 11px; color: $text-muted; transition: transform 0.2s; flex-shrink: 0; }
    &:hover {
      border-color: var(--accent-color, $primary); background: rgba(99, 102, 241, 0.03);
      transform: translateX(2px); box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
      .suggestion-text { color: var(--accent-color, $primary); }
      .suggestion-arrow { color: var(--accent-color, $primary); transform: translateX(2px); }
    }
  }
}

.chat-input-area {
  padding: 12px 16px; background: $surface; border-top: 1px solid $border; flex-shrink: 0; position: relative; z-index: 1;
  .input-wrapper { display: flex; align-items: flex-end; gap: 10px; }
  .chat-textarea {
    flex: 1;
    :deep(.el-textarea__inner) {
      resize: none; min-height: 36px; max-height: 100px; overflow-y: auto;
      border-radius: $radius-sm; padding: 10px 14px; font-size: 13.5px; line-height: 1.5;
      box-shadow: none; border-color: $border; transition: border-color 0.2s;
      &:focus { border-color: $primary; }
    }
  }
  .send-btn {
    width: 38px; height: 38px; border-radius: 12px; border: none;
    background: linear-gradient(135deg, $primary, $primary-light);
    color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center;
    flex-shrink: 0; transition: all 0.2s; box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
    &:hover:not(:disabled) {
      transform: translateY(-1px); box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
    }
    &:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }
  }
}
</style>

<!-- 非 scoped 样式：v-html 注入的 markdown -->
<style lang="scss">
.chat-markdown {
  text-align: justify;
  p { margin: 0 0 2px; }
  p:last-child { margin-bottom: 0; }
  ul, ol { margin: 2px 0; padding-left: 20px; }
  li { margin: 1px 0; }
  h1, h2, h3, h4, h5, h6 { margin: 4px 0 2px; line-height: 1.4; }
  h1:first-child, h2:first-child, h3:first-child, h4:first-child, h5:first-child, h6:first-child { margin-top: 0; }
  code { background: #f1f5f9; padding: 2px 6px; border-radius: 5px; font-size: 12.5px; color: #e11d48; }
  pre {
    background: #1e1e2e; border-radius: 8px; padding: 12px; overflow-x: auto; margin: 8px 0;
    code { background: none; color: #cdd6f4; padding: 0; font-size: 12.5px; }
  }
  blockquote {
    border-left: 3px solid #6366f1; margin: 8px 0; padding: 6px 12px;
    color: #64748b; background: rgba(99, 102, 241, 0.04); border-radius: 0 8px 8px 0;
  }
  table {
    border-collapse: collapse; width: 100%; margin: 8px 0;
    th, td { border: 1px solid #e2e8f0; padding: 6px 10px; text-align: left; }
    th { background: #f1f5f9; font-weight: 600; }
  }
  a { color: #6366f1; text-decoration: none; }
  a:hover { text-decoration: underline; }
  hr { border: none; border-top: 1px solid #e2e8f0; margin: 10px 0; }
}
</style>
