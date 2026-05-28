/** 统一 HTML 报告模板（避免各页面重复拼接） */

export function esc(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

export function volunteerBriefHtml(body) {
  const {
    title = '志愿规划简报',
    studentName = '考生',
    province = '',
    score = '',
    hollandCode = '',
    dominantType = '',
    recommendedMajors = [],
    matchSummary = null,
    rush = [],
    stable = [],
    safe = [],
    note = '',
  } = body

  const schoolList = (items, label) => {
    if (!items?.length) return `<p class="muted">暂无${label}院校</p>`
    return `<ul>${items
      .slice(0, 12)
      .map(
        (s) =>
          `<li><strong>${esc(s.schoolName)}</strong> · ${esc(s.location || '')} ${esc(s.city || '')} · 近年最低 ${s.minScore ?? '-'} 分（差值 ${s.scoreDiff ?? '-'}）</li>`
      )
      .join('')}</ul>`
  }

  return `<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="UTF-8"><title>${esc(title)}</title>
<style>
body{font-family:"PingFang SC","Microsoft YaHei",sans-serif;max-width:800px;margin:40px auto;padding:0 20px;color:#334155;line-height:1.8}
h1{color:#1e293b;border-bottom:3px solid #6366f1;padding-bottom:12px}
h2{color:#475569;margin-top:28px;font-size:1.1rem}
.tag{display:inline-block;background:#eef2ff;color:#6366f1;padding:4px 12px;border-radius:8px;font-weight:700;margin:8px 0}
.muted{color:#94a3b8}.footer{margin-top:48px;font-size:12px;color:#94a3b8;text-align:center}
</style></head><body>
<h1>${esc(title)}</h1>
<p>考生：${esc(studentName)} · 省份：${esc(province)} · 分数：${esc(score)}</p>
${hollandCode ? `<p class="tag">霍兰德 ${esc(hollandCode)} · ${esc(dominantType)}</p>` : ''}
${recommendedMajors?.length ? `<h2>意向专业</h2><p>${recommendedMajors.map(esc).join('、')}</p>` : ''}
${matchSummary ? `<h2>测算概览</h2><p>冲 ${matchSummary.rushCount ?? 0} 所 · 稳 ${matchSummary.stableCount ?? 0} 所 · 保 ${matchSummary.safeCount ?? 0} 所</p>` : ''}
<h2>冲（冲刺）</h2>${schoolList(rush, '冲刺')}
<h2>稳（稳妥）</h2>${schoolList(stable, '稳妥')}
<h2>保（保底）</h2>${schoolList(safe, '保底')}
${note ? `<h2>备注</h2><p>${esc(note)}</p>` : ''}
<div class="footer">由 Node.js 报告服务生成 · 智选未来志愿助手 · ${new Date().toLocaleString('zh-CN')}</div>
</body></html>`
}

export function hollandBriefHtml(body) {
  const {
    hollandCode = '',
    dominantType = '',
    personalitySummary = '',
    majorMatches = [],
    aiInterpret = '',
    scores = {},
  } = body

  const majorRows = (majorMatches || [])
    .map(
      (m) =>
        `<li><strong>${esc(m.majorName || m.category)}</strong>${m.matchPercent != null ? `（${m.matchPercent}%）` : ''} — ${esc(m.reason || '')}</li>`
    )
    .join('')

  const scoreRows = Object.entries(scores || {})
    .map(([k, v]) => `<span class="pill">${esc(k)}: ${v}</span>`)
    .join(' ')

  return `<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="UTF-8"><title>霍兰德测评报告</title>
<style>
body{font-family:"PingFang SC","Microsoft YaHei",sans-serif;max-width:720px;margin:40px auto;padding:0 20px;color:#334155;line-height:1.8}
h1{text-align:center;color:#1e293b}.code{font-size:2rem;color:#6366f1;text-align:center;font-weight:800}
.pill{display:inline-block;background:#eef2ff;color:#6366f1;padding:4px 10px;border-radius:6px;margin:4px;font-size:0.9rem}
ul{margin-left:20px}.footer{margin-top:40px;text-align:center;color:#94a3b8;font-size:12px}
</style></head><body>
<h1>霍兰德兴趣测评报告</h1>
<p class="code">${esc(hollandCode)} · ${esc(dominantType)}</p>
${scoreRows ? `<p>${scoreRows}</p>` : ''}
<p>${esc(personalitySummary)}</p>
<h2>推荐专业</h2><ul>${majorRows || '<li class="muted">暂无</li>'}</ul>
${aiInterpret ? `<h2>AI 解读</h2><p>${esc(aiInterpret)}</p>` : ''}
<div class="footer">Node.js 报告服务 · 智选未来 · ${new Date().toLocaleString('zh-CN')}</div>
</body></html>`
}

export function schoolCompareHtml(body) {
  const { title = '院校对比报告', schools = [], scoreProvince = '', subjectType = '' } = body
  if (!schools?.length) {
    return `<!DOCTYPE html><html><body><p>暂无对比数据</p></body></html>`
  }

  const headers = ['院校', '所在地', '985', '211', '近年最低分', '线差']
  const head = `<tr>${headers.map((h) => `<th>${esc(h)}</th>`).join('')}</tr>`
  const rows = schools
    .map(
      (s) => `<tr>
<td><strong>${esc(s.schoolName)}</strong></td>
<td>${esc(s.location || s.province || '')}</td>
<td>${s.is985 ? '是' : '-'}</td>
<td>${s.is211 ? '是' : '-'}</td>
<td>${s.minScore ?? '-'}</td>
<td>${s.scoreDiff != null ? s.scoreDiff : '-'}</td>
</tr>`
    )
    .join('')

  return `<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="UTF-8"><title>${esc(title)}</title>
<style>
body{font-family:"PingFang SC","Microsoft YaHei",sans-serif;margin:32px;color:#334155}
h1{font-size:1.25rem;color:#1e293b}table{border-collapse:collapse;width:100%;margin-top:16px}
th,td{border:1px solid #e2e8f0;padding:10px 12px;text-align:left}th{background:#f8fafc}
.meta{color:#64748b;font-size:0.9rem;margin-bottom:8px}
</style></head><body>
<h1>${esc(title)}</h1>
<p class="meta">录取生源省：${esc(scoreProvince)} · 科类：${esc(subjectType)}</p>
<table><thead>${head}</thead><tbody>${rows}</tbody></table>
<div class="meta" style="margin-top:24px">Node.js 报告服务 · ${new Date().toLocaleString('zh-CN')}</div>
</body></html>`
}
