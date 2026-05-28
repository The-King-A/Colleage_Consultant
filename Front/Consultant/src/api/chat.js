/**
 * 聊天 API — GET 流式请求
 * 后端同时支持 GET (query params) 和 POST (JSON body)
 */
export const chatStream = async (memoryId, message, onMessage, search = false) => {
  try {
    const url = `/api/chat?memoryId=${encodeURIComponent(memoryId)}&message=${encodeURIComponent(message)}&search=${search}`
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Accept': 'text/html;charset=utf-8' },
    })

    if (!response.ok) {
      const errorText = await response.text().catch(() => '')
      console.error('后端错误:', response.status, errorText)
      const msg = response.status === 500 ? '服务器内部错误，请检查后端日志'
        : response.status === 404 ? '接口不存在，请确认后端已启动'
        : `请求失败 (${response.status})`
      throw new Error(msg)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      if (chunk && onMessage) {
        for (let i = 0; i < chunk.length; i++) {
          onMessage(chunk[i])
          const c = chunk[i]
          let d = 30
          if (/[一-龥]/.test(c)) d = 40
          else if (/[。！？\n]/.test(c)) d = 100
          else if (/[，,]/.test(c)) d = 60
          else if (/[a-zA-Z\s]/.test(c)) d = 20
          await new Promise(r => setTimeout(r, d))
        }
      }
    }
  } catch (error) {
    console.error('chatStream 失败:', error.message || error)
    throw error
  }
}

export const interestTestStream = async (memoryId, message, onMessage) => {
  try {
    const url = `/api/interest-test?memoryId=${encodeURIComponent(memoryId)}&message=${encodeURIComponent(message)}`
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Accept': 'text/html;charset=utf-8' },
    })

    if (!response.ok) {
      const errorText = await response.text().catch(() => '')
      console.error('后端错误:', response.status, errorText)
      const msg = response.status === 500 ? '服务器内部错误'
        : response.status === 404 ? '接口不存在，请确认后端已启动'
        : `请求失败 (${response.status})`
      throw new Error(msg)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      if (chunk && onMessage) {
        for (let i = 0; i < chunk.length; i++) {
          onMessage(chunk[i])
          const c = chunk[i]
          let d = 30
          if (/[一-龥]/.test(c)) d = 40
          else if (/[。！？\n]/.test(c)) d = 100
          else if (/[，,]/.test(c)) d = 60
          else if (/[a-zA-Z\s]/.test(c)) d = 20
          await new Promise(r => setTimeout(r, d))
        }
      }
    }
  } catch (error) {
    console.error('interestTestStream 失败:', error.message || error)
    throw error
  }
}
