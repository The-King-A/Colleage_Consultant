/** AI 方案评审：复制到剪贴板 */

export async function copyReviewText(text) {
  const content = (text || '').trim()
  if (!content) {
    throw new Error('暂无评审内容可复制')
  }
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(content)
    return
  }
  const ta = document.createElement('textarea')
  ta.value = content
  ta.style.position = 'fixed'
  ta.style.left = '-9999px'
  document.body.appendChild(ta)
  ta.select()
  document.execCommand('copy')
  document.body.removeChild(ta)
}
