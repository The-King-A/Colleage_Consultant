import request from './request'

export function fetchFavoriteSchools() {
  return request.get('/favorites/schools')
}

export function addFavoriteSchool(schoolCode, note) {
  return request.post(`/favorites/schools/${schoolCode}`, note ? { note } : {})
}

export function removeFavoriteSchool(schoolCode) {
  return request.delete(`/favorites/schools/${schoolCode}`)
}

export function checkFavoriteSchool(schoolCode) {
  return request.get(`/favorites/schools/${schoolCode}/status`)
}
