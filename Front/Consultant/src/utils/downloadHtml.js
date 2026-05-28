/** 下载 HTML 报告（各页面共用，避免重复 Blob 逻辑） */

export function downloadHtml(html, filename) {
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}
