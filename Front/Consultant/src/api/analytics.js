/** Python FastAPI 分析服务（经 Vite 代理 /analytics） */

export async function matchAdmission({ schoolProvince, scoreProvince, subjectType, score, year }) {
  const body = {
    schoolProvince: schoolProvince || undefined,
    province: schoolProvince || undefined,
    scoreProvince: scoreProvince || schoolProvince || undefined,
    subjectType: subjectType || 'science',
    score: score != null && score !== '' ? Number(score) : undefined,
  }
  if (year != null && year !== '') {
    body.year = Number(year)
  }

  const res = await fetch('/analytics/api/v1/admission/match', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    const detail = err.detail
    const msg = typeof detail === 'string'
      ? detail
      : Array.isArray(detail)
        ? detail.map((d) => d.msg).join('; ')
        : '分析服务请求失败，请确认 analytics-service 已启动 (端口 8001)'
    throw new Error(msg)
  }
  return res.json()
}

export async function fetchScoreInsights({ scoreProvince, schoolProvince, subjectType, score, year }) {
  const body = {
    scoreProvince: scoreProvince || undefined,
    schoolProvince: schoolProvince || undefined,
    subjectType: subjectType || 'science',
    score: score != null && score !== '' ? Number(score) : undefined,
  }
  if (year != null) body.year = Number(year)

  const res = await fetch('/analytics/api/v1/admission/insights', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.detail || '分数线洞察请求失败')
  }
  return res.json()
}

export async function fetchHollandMajorMap(hollandCode, limit = 8) {
  const res = await fetch('/analytics/api/v1/holland/major-map', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ hollandCode, limit }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.detail || '霍兰德专业映射失败')
  }
  return res.json()
}

export async function checkAnalyticsHealth() {
  try {
    const res = await fetch('/analytics/health')
    return res.ok
  } catch {
    return false
  }
}
