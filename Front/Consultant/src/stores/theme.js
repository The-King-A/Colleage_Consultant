import { defineStore } from 'pinia'
import { ref } from 'vue'

const KEY = 'app_theme'

function applyTheme(t) {
  document.documentElement.setAttribute('data-theme', t)
  localStorage.setItem(KEY, t)
}

export const useThemeStore = defineStore('theme', () => {
  const saved = localStorage.getItem(KEY) || 'dark'
  const theme = ref(saved)

  // Apply on first load
  applyTheme(saved)

  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(theme.value)
  }

  function setTheme(t) {
    theme.value = t
    applyTheme(t)
  }

  return { theme, toggle, setTheme }
})
