import request from './request'

export function fetchSchoolPage(params) {
  return request.get('/schools/page', { params })
}

export function fetchSchoolOptions(params) {
  return request.get('/schools/options', { params })
}

export function fetchSchoolLocations() {
  return request.get('/schools/locations')
}

export function fetchScoreProvinces() {
  return request.get('/schools/score-provinces')
}

export function fetchSchoolDetail(code, params) {
  return request.get(`/schools/${code}`, { params })
}

export function fetchSchoolCompare(params) {
  return request.get('/schools/compare', { params })
}

export function fetchSchoolMapMarkers(params) {
  return request.get('/schools/map-markers', { params })
}

/** 补全冲稳保测算录取样本 */
export function ensureMatcherData({ schoolProvince, scoreProvince } = {}) {
  const params = {}
  if (schoolProvince) params.schoolProvince = schoolProvince
  if (scoreProvince) params.scoreProvince = scoreProvince
  return request.post('/schools/ensure-matcher-data', null, {
    params,
    timeout: 90000,
  })
}
