import request from './request'
import { useAuthStore } from '../stores/auth'

export function fetchVolunteerPlans() {
  return request.get('/plans')
}

export function fetchVolunteerPlan(id) {
  return request.get(`/plans/${id}`)
}

export function saveVolunteerPlan(data) {
  return request.post('/plans', data)
}

export function deleteVolunteerPlan(id) {
  return request.delete(`/plans/${id}`)
}

/** 流式 AI 方案评审 */
export async function reviewPlanStream(planId, onChunk) {
  const authStore = useAuthStore()
  const response = await fetch(`/api/plans/${planId}/ai-review`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/html;charset=utf-8',
      ...(authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {}),
    },
  })
  if (!response.ok) {
    const text = await response.text().catch(() => '')
    throw new Error(text || `评审失败 (${response.status})`)
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

/** 导出 AI 评审 PDF（Java + Apache PDFBox） */
export async function downloadReviewPdf({ title, meta, content }) {
  const authStore = useAuthStore()
  const response = await fetch('/api/plans/export-review-pdf', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/pdf',
      ...(authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {}),
    },
    body: JSON.stringify({ title, meta, content }),
  })
  if (!response.ok) {
    const text = await response.text().catch(() => '')
    let message = text || `导出失败 (${response.status})`
    try {
      const json = JSON.parse(text)
      if (json.message) message = json.message
    } catch {
      /* plain text */
    }
    throw new Error(message)
  }
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  const safeName = (title || 'AI方案评审').replace(/[/\\?%*:|"<>]/g, '_').slice(0, 40)
  a.href = url
  a.download = `${safeName}.pdf`
  a.click()
  URL.revokeObjectURL(url)
}
