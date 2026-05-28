import request from './request'

export function loginApi(data) {
  return request.post('/auth/login', data)
}

export function registerApi(data) {
  return request.post('/auth/register', data)
}

export function getUserInfoApi() {
  return request.get('/user/info')
}

export function updateProfileApi(data) {
  return request.put('/user/profile', data)
}

export function updatePasswordApi(data) {
  return request.put('/user/password', data)
}
