import request from './request'

export function fetchMajorPage(params) {
  return request.get('/majors/page', { params })
}

export function fetchMajorCategories() {
  return request.get('/majors/categories')
}

export function fetchMajorOptions(params) {
  return request.get('/majors/options', { params })
}

export function fetchMajorDetail(code) {
  return request.get(`/majors/${code}`)
}

export function fetchMajorCompare(params) {
  return request.get('/majors/compare', { params })
}
