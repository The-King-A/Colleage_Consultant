/** Node.js Express 报告服务（经 Vite 代理 /report-api） */

async function postReport(path, payload) {
  const res = await fetch(`/report-api/api/v1/report/${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error('报告服务不可用，请启动 report-service (端口 3001)')
  }
  return res.json()
}

export async function generateVolunteerBrief(payload) {
  return postReport('volunteer-brief', payload)
}

export async function generateHollandBrief(payload) {
  return postReport('holland-brief', payload)
}

export async function generateSchoolCompareReport(payload) {
  return postReport('school-compare', payload)
}

export async function checkReportHealth() {
  try {
    const res = await fetch('/report-api/health')
    return res.ok
  } catch {
    return false
  }
}
