const { request } = require('../../utils/request');
const { parseMarkdown } = require('../../utils/markdown');
const app = getApp();

Page({
  data: {
    messages: [],
    inputText: '',
    isLoading: false,
    isStreaming: false,
    suggestions: [],
    scrollToView: '',
    showWelcome: true,
    threadId: 'default',
    userInfo: null
  },

  requestTask: null,
  bufferString: '',

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    const threadId = `thread_${userInfo?.id || 'anonymous'}_${Date.now()}`;
    this.setData({
      userInfo,
      threadId
    });
  },

  onShow() {
    if (typeof this.getTabBar === 'function') {
      this.getTabBar()?.setSelected(1)
    }
  },

  onUnload() {
    this.cancelStream();
  },

  // 输入框内容变化
  onInputChange(e) {
    this.setData({ inputText: e.detail.value });
  },

  // 发送消息
  sendMessage(e) {
    let content = '';
    if (e.currentTarget.dataset.content) {
      content = e.currentTarget.dataset.content;
    } else {
      content = this.data.inputText.trim();
    }

    if (!content || this.data.isStreaming) return;

    // 添加用户消息
    const userMessage = {
      id: `msg_${Date.now()}`,
      role: 'user',
      content: content,
      timestamp: Date.now()
    };

    const messages = [...this.data.messages, userMessage];

    this.setData({
      messages,
      inputText: '',
      showWelcome: false,
      suggestions: [],
      isLoading: true
    });

    this.scrollToBottom();

    // 调用 AI 接口
    this.streamChat(content);
  },

  // 流式对话
  streamChat(content) {
    const agentBaseUrl = app.globalData.agentBaseUrl || 'http://localhost:8000';

    // 添加 AI 消息占位
    const aiMessage = {
      id: `msg_${Date.now()}_ai`,
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isTyping: true
    };

    const messages = [...this.data.messages, aiMessage];
    this.setData({
      messages,
      isStreaming: true
    });

    this.bufferString = '';

    this.requestTask = wx.request({
      url: `${agentBaseUrl}/chat/stream`,
      method: 'POST',
      enableChunked: true,
      header: {
        'Content-Type': 'application/json'
      },
      data: {
        message: content,
        thread_id: this.data.threadId,
        username: this.data.userInfo?.nickname || ''
      },
      success: (res) => {
        // 流式请求完成
        this.finishStreaming();
      },
      fail: (err) => {
        console.error('请求失败:', err);
        this.handleStreamError();
      }
    });

    // 监听流式数据
    this.requestTask.onChunkReceived((res) => {
      this.handleChunk(res.data);
    });
  },

  // 处理流式数据块
  handleChunk(buffer) {
    const uint8Array = new Uint8Array(buffer);
    let text = '';
    for (let i = 0; i < uint8Array.length; i++) {
      text += '%' + ('00' + uint8Array[i].toString(16)).slice(-2);
    }

    try {
      text = decodeURIComponent(text);
    } catch (e) {
      text = String.fromCharCode.apply(null, uint8Array);
    }

    this.bufferString += text;

    // 按 SSE 事件分割
    const parts = this.bufferString.split('\n\n');
    this.bufferString = parts.pop() || '';

    for (let part of parts) {
      part = part.trim();
      if (!part || !part.startsWith('data:')) continue;

      const dataStr = part.substring(5).trim();

      try {
        const data = JSON.parse(dataStr);

        if (data.code === 200 && data.data) {
          // 普通文本 chunk
          this.appendAIMessage(data.data);
        } else if (data.code === 201 && data.data) {
          // 推荐问题
          this.setData({ suggestions: data.data });
        }
      } catch (e) {
        // 如果不是 JSON，直接作为文本处理
        if (dataStr !== '[DONE]') {
          this.appendAIMessage(dataStr);
        }
      }
    }
  },

  // 追加 AI 消息内容
  appendAIMessage(text) {
    const messages = this.data.messages;
    const lastIndex = messages.length - 1;

    if (lastIndex >= 0 && messages[lastIndex].role === 'assistant') {
      messages[lastIndex].content += text;
      messages[lastIndex].htmlContent = parseMarkdown(messages[lastIndex].content);
      this.setData({ messages });
      this.scrollToBottom();
    }
  },

  // 完成流式输出
  finishStreaming() {
    const messages = this.data.messages;
    const lastIndex = messages.length - 1;

    if (lastIndex >= 0 && messages[lastIndex].role === 'assistant') {
      messages[lastIndex].isTyping = false;
    }

    this.setData({
      messages,
      isLoading: false,
      isStreaming: false
    });
  },

  // 处理流式错误
  handleStreamError() {
    const messages = this.data.messages;
    const lastIndex = messages.length - 1;

    if (lastIndex >= 0 && messages[lastIndex].role === 'assistant') {
      if (!messages[lastIndex].content) {
        messages[lastIndex].content = '抱歉，请求失败，请稍后再试。';
        messages[lastIndex].htmlContent = '<p style="font-size:28rpx;color:#1f2937;line-height:1.8;">抱歉，请求失败，请稍后再试。</p>';
      }
      messages[lastIndex].isTyping = false;
    }

    this.setData({
      messages,
      isLoading: false,
      isStreaming: false
    });

    wx.showToast({ title: '网络请求失败', icon: 'none' });
  },

  // 取消流式请求
  cancelStream() {
    if (this.requestTask) {
      this.requestTask.abort();
      this.requestTask = null;
    }
  },

  // 停止生成
  stopGenerating() {
    this.cancelStream();
    this.finishStreaming();
  },

  // 清空对话
  clearChat() {
    wx.showModal({
      title: '提示',
      content: '确定要清空对话记录吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            messages: [],
            showWelcome: true,
            suggestions: [],
            isStreaming: false,
            isLoading: false
          });

          // 更新 threadId
          const threadId = `thread_${this.data.userInfo?.id || 'anonymous'}_${Date.now()}`;
          this.setData({ threadId });
        }
      }
    });
  },

  // 滚动到底部
  scrollToBottom() {
    const messages = this.data.messages;
    if (messages.length > 0) {
      setTimeout(() => {
        this.setData({
          scrollToView: messages[messages.length - 1].id
        });
      }, 100);
    }
  },

  // 返回首页
  goBack() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
