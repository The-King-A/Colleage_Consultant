import request from './request'

export function fetchWizardProgress() {
  return request.get('/wizard/progress')
}

export function completeWizardStep(payload) {
  return request.post('/wizard/complete-step', payload)
}

/** 仅保存本步数据，不标记完成、不自动推进 */
export function saveWizardStep(payload) {
  return request.post('/wizard/save-step', payload)
}

/** 切换到某步进行查看/修改 */
export function focusWizardStep(step) {
  return request.post('/wizard/save-step', { setCurrentStep: step })
}
