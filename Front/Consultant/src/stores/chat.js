import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'chat_sessions'

function generateId() {
  return `session_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

function generateMemoryId() {
  return `user_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

function generateMsgId() {
  return `msg_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`
}

function loadSessions() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveSessions(sessions) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(sessions))
  } catch { /* storage full */ }
}

export const useChatStore = defineStore('chat', () => {
  // ---- State ----
  const sessions = ref(loadSessions())
  const currentSessionId = ref(sessions.value[0]?.id || null)
  const isLoading = ref(false)
  const isThinking = ref(false)
  const searchEnabled = ref(false)
  const errorMsg = ref('')

  // ---- Getters ----
  const currentSession = computed(() =>
    sessions.value.find((s) => s.id === currentSessionId.value) || null
  )

  const currentMessages = computed(() => currentSession.value?.messages || [])

  const sortedSessions = computed(() =>
    [...sessions.value].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
  )

  const todaySessions = computed(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return sortedSessions.value.filter((s) => new Date(s.updatedAt) >= today)
  })

  const weekSessions = computed(() => {
    const weekAgo = new Date()
    weekAgo.setDate(weekAgo.getDate() - 7)
    weekAgo.setHours(0, 0, 0, 0)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return sortedSessions.value.filter((s) => {
      const d = new Date(s.updatedAt)
      return d >= weekAgo && d < today
    })
  })

  const olderSessions = computed(() => {
    const weekAgo = new Date()
    weekAgo.setDate(weekAgo.getDate() - 7)
    weekAgo.setHours(0, 0, 0, 0)
    return sortedSessions.value.filter((s) => new Date(s.updatedAt) < weekAgo)
  })

  // ---- Actions ----
  function createSession() {
    const session = {
      id: generateId(),
      title: '新对话',
      memoryId: generateMemoryId(),
      messages: [],
      messageCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    sessions.value.unshift(session)
    currentSessionId.value = session.id
    persist()
    return session
  }

  function switchSession(sessionId) {
    if (sessions.value.find((s) => s.id === sessionId)) {
      currentSessionId.value = sessionId
    }
  }

  function deleteSession(sessionId) {
    const idx = sessions.value.findIndex((s) => s.id === sessionId)
    if (idx === -1) return
    sessions.value.splice(idx, 1)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = sessions.value[0]?.id || null
    }
    persist()
  }

  function addMessage(role, content) {
    let session = currentSession.value
    if (!session) {
      session = createSession()
    }
    const msg = {
      id: generateMsgId(),
      role,
      content,
      timestamp: new Date().toISOString(),
    }
    session.messages.push(msg)
    session.messageCount = session.messages.filter((m) => m.role !== 'system').length
    session.updatedAt = new Date().toISOString()

    // Set title from first user message
    if (role === 'user' && session.title === '新对话') {
      session.title = content.substring(0, 40) + (content.length > 40 ? '...' : '')
    }

    persist()
    return msg
  }

  function updateLastMessage(content) {
    const session = currentSession.value
    if (!session) return
    const msgs = session.messages
    if (msgs.length > 0 && msgs[msgs.length - 1].role === 'assistant') {
      msgs[msgs.length - 1].content = content
    }
    session.updatedAt = new Date().toISOString()
  }

  function clearCurrentSession() {
    const session = currentSession.value
    if (!session) return
    session.messages = []
    session.messageCount = 0
    session.memoryId = generateMemoryId()
    session.title = '新对话'
    session.updatedAt = new Date().toISOString()
    persist()
  }

  function persist() {
    saveSessions(sessions.value)
  }

  // Initialize: ensure at least one session exists
  function init() {
    if (sessions.value.length === 0) {
      createSession()
    } else if (!currentSessionId.value) {
      currentSessionId.value = sessions.value[0].id
    }
  }

  return {
    // state
    sessions,
    currentSessionId,
    isLoading,
    isThinking,
    searchEnabled,
    errorMsg,
    // getters
    currentSession,
    currentMessages,
    sortedSessions,
    todaySessions,
    weekSessions,
    olderSessions,
    // actions
    createSession,
    switchSession,
    deleteSession,
    addMessage,
    updateLastMessage,
    clearCurrentSession,
    generateMemoryId,
    init,
    persist,
  }
})
