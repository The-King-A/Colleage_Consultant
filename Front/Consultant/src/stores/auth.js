import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getUserInfoApi } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // Actions
  async function login(credentials) {
    const res = await loginApi(credentials)
    const data = res.data
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    return data
  }

  async function register(formData) {
    const res = await registerApi(formData)
    const data = res.data
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    return data
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfoApi()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // Initialize: restore from localStorage
  function init() {
    const savedToken = localStorage.getItem('token')
    const savedInfo = localStorage.getItem('userInfo')
    if (savedToken) {
      token.value = savedToken
      if (savedInfo) {
        try { userInfo.value = JSON.parse(savedInfo) } catch { /* ignore */ }
      }
    }
  }

  return {
    token, userInfo, isLoggedIn, username, nickname,
    login, register, fetchUserInfo, logout, init,
  }
})
