import request from './request'

export function fetchHollandQuestions() {
  return request.get('/interest-test/questions')
}

export function submitHollandTest(answers) {
  return request.post('/interest-test/submit', { answers })
}

/** 流式 AI 解读（无逐字延迟） */
export async function interpretHollandStream(result, onChunk) {
  const response = await fetch('/api/interest-test/interpret', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'text/html;charset=utf-8' },
    body: JSON.stringify(result),
  })
  if (!response.ok) {
    const text = await response.text().catch(() => '')
    throw new Error(text || `请求失败 (${response.status})`)
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    const chunk = decoder.decode(value, { stream: true })
    if (chunk && onChunk) onChunk(chunk)
  }
}
