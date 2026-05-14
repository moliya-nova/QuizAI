const baseUrl = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080/api'

/**
 * SSE 流式聊天
 */
export function streamChat(message, onChunk, threadId, signal) {
  return new Promise(async (resolve, reject) => {
    try {
      const token = localStorage.getItem('adminToken')
      const resp = await fetch(`${baseUrl}/agent/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify({ message, threadId }),
        signal
      })

      if (!resp.ok) {
        reject(new Error(`HTTP ${resp.status}`))
        return
      }

      const reader = resp.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { value, done } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (!line || line.startsWith('event:') || line.startsWith('id:') || line.startsWith('retry:')) continue
          if (!line.startsWith('data:')) continue

          let data = line.substring(5)
          if (data.startsWith(' ')) data = data.substring(1)
          if (!data || data === '[DONE]') continue

          try {
            const parsed = JSON.parse(data)
            onChunk(parsed)
          } catch {
            // 非 JSON 数据，忽略
          }
        }
      }
      resolve()
    } catch (e) {
      reject(e)
    }
  })
}

/**
 * 删除会话记忆
 */
export function deleteMemory(threadId) {
  const token = localStorage.getItem('adminToken')
  return fetch(`${baseUrl}/agent/chat/memory/${threadId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  })
}

/**
 * 获取会话历史
 */
export async function getHistory(threadId) {
  const token = localStorage.getItem('adminToken')
  const resp = await fetch(`${baseUrl}/agent/chat/history/${threadId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  const json = await resp.json()
  if (json.code === 200 && json.data) {
    return typeof json.data === 'string' ? JSON.parse(json.data) : json.data
  }
  return []
}
