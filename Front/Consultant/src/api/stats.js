import request from './request'

export function fetchDashboardStats() {
  return request.get('/stats/dashboard')
}
