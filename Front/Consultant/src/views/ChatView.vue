<template>
  <div class="workspace">
    <!-- ===== SIDEBAR ===== -->
    <div class="sidebar-panel" :class="{ open: sidebarOpen }">
      <SessionSidebar
        :current-session-id="chatStore.currentSessionId"
        :sorted-sessions="chatStore.sortedSessions"
        :today-sessions="chatStore.todaySessions"
        :week-sessions="chatStore.weekSessions"
        :older-sessions="chatStore.olderSessions"
        @create="handleCreateSession"
        @switch="handleSwitchSession"
        @delete="handleDeleteSession"
        @close="sidebarOpen = false"
      />
    </div>
    <div v-if="sidebarOpen" class="sidebar-mask" @click="sidebarOpen = false"></div>

    <!-- ===== MAIN ===== -->
    <div class="main">
      <!-- Top Bar -->
      <header class="topbar">
        <div class="tb-l">
          <button class="tb-btn" @click="sidebarOpen = !sidebarOpen">
            <el-icon :size="20"><Expand /></el-icon>
          </button>
          <button class="tb-btn" @click="$router.push('/')">
            <el-icon :size="20"><HomeFilled /></el-icon>
          </button>
          <div class="tb-brand">
            <span class="tb-name">AI 志愿顾问</span>
            <span class="tb-badge">
              <span class="tb-dot" :class="{ thinking: chatStore.isThinking }"></span>
              DeepSeek-V4
            </span>
          </div>
        </div>
        <div class="tb-r">
          <button class="tb-btn" :disabled="chatStore.currentMessages.length === 0" @click="handleClear">
            <el-icon :size="18"><Delete /></el-icon>
          </button>
        </div>
      </header>

      <!-- Body -->
      <main class="body" ref="bodyRef">
        <WelcomeScreen v-if="chatStore.currentMessages.length === 0" @ask="quickAsk" />

        <div v-else class="msg-list">
          <div v-for="(msg, idx) in chatStore.currentMessages" :key="msg.id" class="msg-row" :class="msg.role">
            <div class="msg-avatar">
              <div v-if="msg.role === 'assistant'" class="av-ai">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2L2 8l10 6 10-6-10-6z"/><path d="M2 16l10 6 10-6"/><path d="M2 12l10 6 10-6"/>
                </svg>
              </div>
              <div v-else class="av-user">
                <el-icon :size="16"><UserFilled /></el-icon>
              </div>
            </div>

            <div class="msg-body">
              <!-- Thinking -->
              <div v-if="msg.role === 'assistant' && chatStore.isThinking && idx === chatStore.currentMessages.length - 1 && !msg.content" class="thinking">
                <span class="th-dot"></span><span class="th-dot"></span><span class="th-dot"></span>
              </div>

              <!-- Bubble -->
              <div class="msg-bubble" v-else>
                <div class="bubble-md markdown-body" v-html="renderMarkdown(msg.content)"></div>
                <span v-if="msg.role === 'assistant' && chatStore.isLoading && idx === chatStore.currentMessages.length - 1 && msg.content" class="cursor"></span>
                <button v-if="msg.role === 'assistant' && msg.content && !chatStore.isLoading" class="btn-copy" @click="copyMsg(msg.content)">
                  <el-icon :size="14"><component :is="copiedId === msg.id ? Check : CopyDocument" /></el-icon>
                </button>
              </div>
              <span class="msg-time">{{ fmt(msg.timestamp) }}</span>
            </div>
          </div>

          <div v-if="chatStore.errorMsg" class="err-toast">
            <el-icon><WarningFilled /></el-icon><span>{{ chatStore.errorMsg }}</span>
            <button @click="chatStore.errorMsg = ''"><el-icon :size="14"><Close /></el-icon></button>
          </div>
          <div ref="bottomRef"></div>
        </div>
      </main>

      <!-- ===== INPUT (Masterpiece) ===== -->
      <footer class="input-area">
        <!-- Search chip above input -->
        <div class="search-above">
          <button
            class="tb-chip" :class="{ on: chatStore.searchEnabled }"
            @click="chatStore.searchEnabled = !chatStore.searchEnabled"
          >
            <el-icon :size="16"><Search /></el-icon>
            <span>{{ chatStore.searchEnabled ? '联网搜索已开启' : '联网搜索：检索最新招生政策与资讯' }}</span>
          </button>
        </div>

        <div class="input-shell">
          <!-- Iridescent border ring -->
          <div class="shell-border"></div>

          <div class="shell-inner">
            <textarea
              ref="inputRef"
              v-model="inputText"
              :placeholder="chatStore.isLoading ? 'AI 正在回复...' : '输入问题，Enter 发送 · Shift+Enter 换行'"
              :disabled="chatStore.isLoading"
              rows="1"
              class="shell-input"
              @keydown.enter.exact.prevent="handleSend"
              @input="autoGrow"
            ></textarea>

            <button
              class="shell-send"
              :class="{ ready: inputText.trim() && !chatStore.isLoading, loading: chatStore.isLoading }"
              :disabled="!inputText.trim() || chatStore.isLoading"
              @click="handleSend"
            >
              <el-icon v-if="!chatStore.isLoading" :size="22"><Promotion /></el-icon>
              <span v-else class="spinner"></span>
            </button>
          </div>
        </div>
        <p class="input-note">AI 生成内容仅供参考，请核实关键信息</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Expand, HomeFilled, Search, Delete, UserFilled, Promotion, WarningFilled, Close, CopyDocument, Check } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat'
import { chatStream } from '../api/chat'
import { renderMarkdown } from '../utils/markdown'
import SessionSidebar from '../components/chat/SessionSidebar.vue'
import WelcomeScreen from '../components/chat/WelcomeScreen.vue'

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()

const inputText = ref('')
const inputRef = ref(null)
const bodyRef = ref(null)
const bottomRef = ref(null)
const sidebarOpen = ref(false)
const copiedId = ref(null)

onMounted(() => {
  chatStore.init()
  if (chatStore.currentMessages.length > 0) nextTick(() => scrollEnd(false))
  applyQueryQuestion()
})

watch(() => route.query.q, () => applyQueryQuestion())

watch(() => chatStore.currentMessages.length, () => nextTick(() => scrollEnd(true)))

function applyQueryQuestion() {
  const raw = route.query.q
  if (!raw || typeof raw !== 'string') return
  const text = decodeURIComponent(raw).trim()
  if (!text || chatStore.isLoading) return
  router.replace({ path: '/chat' })
  inputText.value = text
  nextTick(() => handleSend())
}

function scrollEnd(smooth = true) {
  nextTick(() => bottomRef.value?.scrollIntoView({ behavior: smooth ? 'smooth' : 'instant', block: 'end' }))
}

function autoGrow() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 150) + 'px'
}

function quickAsk(text) { inputText.value = text; nextTick(() => handleSend()) }

async function handleSend() {
  const content = inputText.value.trim()
  if (!content || chatStore.isLoading) return

  chatStore.addMessage('user', content)
  inputText.value = ''
  if (inputRef.value) inputRef.value.style.height = 'auto'

  chatStore.isLoading = true
  chatStore.isThinking = true
  chatStore.errorMsg = ''
  chatStore.addMessage('assistant', '')

  const session = chatStore.currentSession
  if (!session) return

  try {
    await chatStream(session.memoryId, content, (chunk) => {
      chatStore.isThinking = false
      const msgs = session.messages
      const last = msgs[msgs.length - 1]
      if (last?.role === 'assistant') last.content += chunk
      nextTick(() => scrollEnd(true))
    }, chatStore.searchEnabled)
  } catch (e) {
    chatStore.errorMsg = e.message || '请求失败'
    const msgs = session.messages
    if (msgs.length && msgs[msgs.length - 1]?.role === 'assistant' && !msgs[msgs.length - 1]?.content) msgs.pop()
  } finally {
    chatStore.isLoading = false
    chatStore.isThinking = false
    chatStore.persist()
    nextTick(() => scrollEnd(true))
  }
}

function handleCreateSession() { chatStore.createSession(); sidebarOpen.value = false; inputRef.value?.focus() }
function handleSwitchSession(id) { chatStore.switchSession(id); sidebarOpen.value = false; scrollEnd(false) }

async function handleDeleteSession(id) {
  try {
    await ElMessageBox.confirm('删除该对话？', '确认', { type: 'warning', confirmButtonText: '删除' })
    chatStore.deleteSession(id); ElMessage.success('已删除')
  } catch {}
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('清空当前对话？', '确认', { type: 'warning', confirmButtonText: '清空' })
    chatStore.clearCurrentSession(); ElMessage.success('已清空')
  } catch {}
}

async function copyMsg(content) {
  try {
    await navigator.clipboard.writeText(content.replace(/<[^>]*>/g, ''))
    const msgs = chatStore.currentMessages
    const last = [...msgs].reverse().find(m => m.role === 'assistant')
    if (last) { copiedId.value = last.id; setTimeout(() => copiedId.value = null, 2000) }
  } catch { ElMessage.error('复制失败') }
}

function fmt(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
/* ============================================
   AI CHAT DASHBOARD — Gemini-Inspired Light
   ============================================ */

.workspace {
  height: 100vh;
  display: flex;
  background: #f7f8fc;
  overflow: hidden;
}

/* ===== SIDEBAR PANEL ===== */
.sidebar-panel { width: 280px; flex-shrink: 0; }

.sidebar-mask {
  display: none;
  position: fixed; inset: 0; z-index: 40;
  background: rgba(0,0,0,0.3); backdrop-filter: blur(4px);
}

/* ===== MAIN ===== */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f7f8fc;
}

/* ===== TOP BAR ===== */
.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(255,255,255,0.75);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid #e8ecf1;
  flex-shrink: 0;
}

.tb-l, .tb-r { display: flex; align-items: center; gap: 6px; }

.tb-btn {
  width: 34px; height: 34px;
  border-radius: 10px;
  border: 1px solid #e8ecf1;
  background: #fff;
  color: #64748b;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}

.tb-btn:hover { border-color: #cbd5e1; color: #334155; background: #f8fafc; }
.tb-btn:disabled { opacity: 0.4; cursor: default; }

.tb-brand { margin-left: 8px; display: flex; flex-direction: column; }
.tb-name { font-size: 0.88rem; font-weight: 700; color: #0f172a; line-height: 1.3; }
.tb-badge { font-size: 0.68rem; color: #64748b; display: flex; align-items: center; gap: 5px; }

.tb-dot { width: 5px; height: 5px; border-radius: 50%; background: #22c55e; }
.tb-dot.thinking { background: #f59e0b; animation: pulse-dot 1.2s ease-in-out infinite; }
@keyframes pulse-dot {
  0%,100% { box-shadow: 0 0 2px #f59e0b; }
  50% { box-shadow: 0 0 8px #f59e0b; }
}

.tb-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 5px 14px;
  border-radius: 20px;
  border: 1px solid #e8ecf1;
  background: #fff;
  color: #64748b;
  font-size: 0.78rem; font-weight: 500;
  cursor: pointer;
  transition: all 0.25s;
}

.tb-chip:hover { border-color: #cbd5e1; color: #334155; }
.tb-chip.on { background: rgba(99,102,241,0.06); border-color: rgba(99,102,241,0.3); color: #6366f1; }

/* ===== BODY ===== */
.body {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

.msg-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0 120px;
}

/* Message row */
.msg-row {
  display: flex; gap: 14px;
  margin-bottom: 22px;
  animation: in-msg 0.3s ease both;
}

@keyframes in-msg {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-row.user { flex-direction: row-reverse; }

.msg-avatar { flex-shrink: 0; padding-top: 1px; }

.av-ai, .av-user {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}

.av-ai {
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99,102,241,0.2);
}

.av-user {
  background: #e8ecf1;
  color: #64748b;
}

.msg-body { max-width: 74%; position: relative; }

/* Bubble */
.msg-bubble {
  position: relative;
  padding: 14px 20px;
  border-radius: 16px;
  font-size: 0.9rem;
  line-height: 1.75;
  word-break: break-word;
}

.msg-row.assistant .msg-bubble {
  background: #fff;
  border: 1px solid #e8ecf1;
  border-top-left-radius: 4px;
  color: #1e293b;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}

.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border-top-right-radius: 4px;
  color: #fff;
}

/* Markdown */
.bubble-md :deep(h2) { font-size: 1.1rem; margin: 12px 0 6px; color: #0f172a; }
.bubble-md :deep(h3) { font-size: 1rem; margin: 10px 0 4px; color: #1e293b; }
.bubble-md :deep(p) { margin: 4px 0 10px; }
.bubble-md :deep(ul), .bubble-md :deep(ol) { margin: 6px 0; padding-left: 20px; }
.bubble-md :deep(li) { margin-bottom: 3px; }
.bubble-md :deep(strong) { color: #6366f1; font-weight: 600; }
.bubble-md :deep(code) {
  background: #f1f5f9; padding: 2px 6px; border-radius: 4px;
  font-size: 0.85em; font-family: 'SF Mono','Fira Code','Consolas',monospace;
}
.bubble-md :deep(pre) {
  background: #1e293b; border-radius: 12px;
  padding: 16px 18px; margin: 12px 0; overflow-x: auto;
}
.bubble-md :deep(pre code) {
  background: none; padding: 0; color: #c9d1d9; font-size: 0.8rem;
}
.bubble-md :deep(blockquote) {
  border-left: 3px solid #6366f1; margin: 8px 0; padding: 2px 14px;
  color: #475569; background: rgba(99,102,241,0.03); border-radius: 0 8px 8px 0;
}
.bubble-md :deep(table) { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 0.82rem; }
.bubble-md :deep(th), .bubble-md :deep(td) { border: 1px solid #e8ecf1; padding: 7px 12px; text-align: left; }
.bubble-md :deep(th) { background: #f8fafc; font-weight: 600; }

.msg-row.user .bubble-md :deep(strong) { color: #fff; }
.msg-row.user .bubble-md :deep(pre) { background: rgba(0,0,0,0.25); }

/* Copy btn */
.btn-copy {
  position: absolute; top: 8px; right: 8px;
  width: 28px; height: 28px;
  border-radius: 6px; border: none;
  background: rgba(0,0,0,0.04);
  color: #94a3b8;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: all 0.2s;
}

.msg-bubble:hover .btn-copy { opacity: 1; }
.btn-copy:hover { background: rgba(0,0,0,0.08); color: #334155; }

/* Cursor */
.cursor {
  display: inline-block; width: 3px; height: 1.15em;
  background: #6366f1; margin-left: 2px;
  border-radius: 2px; animation: blink-cursor 0.8s steps(1) infinite;
}
@keyframes blink-cursor { 0%,100%{opacity:1} 50%{opacity:0} }

.msg-time { display: block; font-size: 0.7rem; color: #94a3b8; margin-top: 5px; padding: 0 4px; }

/* Thinking */
.thinking { display: flex; gap: 4px; padding: 10px 14px; }
.th-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #a5b4fc;
  animation: th-bounce 1.4s ease-in-out infinite;
}
.th-dot:nth-child(2) { animation-delay: 0.2s; }
.th-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes th-bounce { 0%,80%,100%{transform:scale(.6);opacity:.4} 40%{transform:scale(1);opacity:1} }

/* Error */
.err-toast {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px;
  background: #fef2f2; border: 1px solid #fecaca;
  border-radius: 10px; color: #ef4444; font-size: 0.85rem;
  margin-top: 8px;
}
.err-toast button { margin-left: auto; border: none; background: none; color: #ef4444; cursor: pointer; }

/* ===== INPUT (Masterpiece) ===== */
.input-area {
  flex-shrink: 0;
  padding: 0 24px 24px;
}

.search-above {
  max-width: 800px;
  margin: 0 auto 10px;
  display: flex;
  justify-content: center;
}

.input-shell {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  border-radius: 20px;
  background: #fff;
  box-shadow:
    0 1px 3px rgba(0,0,0,0.04),
    0 4px 16px rgba(0,0,0,0.03);
}

/* Iridescent gradient border */
.shell-border {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  padding: 1px;
  background: conic-gradient(
    from 0deg,
    #6366f1, #a855f7, #06b6d4,
    #ec4899, #6366f1, #0ea5e9,
    #6366f1
  );
  background-size: 300% 300%;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.5;
  transition: opacity 0.4s ease, background-position 0.6s ease;
}

/* Active: border glows & rotates */
.input-shell:focus-within .shell-border,
.input-shell:has(.shell-input:focus) .shell-border {
  opacity: 1;
  animation: border-rotate 4s linear infinite;
}

@keyframes border-rotate {
  0% { background-position: 0% 50%; }
  100% { background-position: 300% 50%; }
}

.shell-inner {
  position: relative;
  display: flex;
  align-items: flex-end;
  padding: 8px 9px 8px 20px;
}

.shell-input {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-size: 0.92rem;
  font-family: inherit;
  line-height: 1.55;
  color: #0f172a;
  padding: 8px 0;
  max-height: 150px;
  min-height: 24px;
}

.shell-input::placeholder { color: #94a3b8; }

.shell-input:disabled { opacity: 0.5; }

.shell-send {
  width: 40px; height: 40px;
  border-radius: 12px;
  border: none;
  background: #f1f5f9;
  color: #94a3b8;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  margin-left: 4px;
  transition: all 0.25s cubic-bezier(0.23, 1, 0.32, 1);
}

.shell-send.ready {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  box-shadow: 0 2px 12px rgba(99,102,241,0.3);
}

.shell-send.ready:hover {
  transform: scale(1.06);
  box-shadow: 0 4px 18px rgba(99,102,241,0.45);
}

.shell-send.loading {
  background: rgba(99,102,241,0.1);
  pointer-events: none;
}

.spinner {
  width: 18px; height: 18px;
  border: 2.5px solid rgba(99,102,241,0.2);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.input-note {
  text-align: center;
  font-size: 0.7rem;
  color: #94a3b8;
  margin-top: 10px;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .sidebar-panel {
    position: fixed; z-index: 50;
    left: 0; top: 0; bottom: 0;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }
  .sidebar-panel.open { transform: translateX(0); }
  .sidebar-mask { display: block; }

  .msg-body { max-width: 85%; }
  .body { padding: 0 12px; }
  .msg-list { padding: 16px 0 100px; }
  .input-area { padding: 0 12px 16px; }
  .tb-chip span { display: none; }
}
</style>
