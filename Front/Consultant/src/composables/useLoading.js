import { ref } from 'vue'

export function useLoading(initialState = false) {
  const isLoading = ref(initialState)

  async function withLoading(fn) {
    isLoading.value = true
    try {
      return await fn()
    } finally {
      isLoading.value = false
    }
  }

  return { isLoading, withLoading }
}
