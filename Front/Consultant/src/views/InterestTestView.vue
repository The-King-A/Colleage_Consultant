<template>
  <div class="test-page">
    <header class="test-header">
      <div class="header-l">
        <button class="back-link" @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <div class="header-brand">
          <div class="brand-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
              <polygon points="12 2 15 9 22 9 16 14 18 21 12 17 6 21 8 14 2 9 9 9"/>
            </svg>
          </div>
          <div>
            <h2 class="brand-name">霍兰德兴趣测评</h2>
            <p class="brand-status">RIASEC · 科学量表 · 即时结果</p>
          </div>
        </div>
      </div>
      <div class="header-r">
        <el-button v-if="phase === 'quiz'" class="btn-reset" round :icon="RefreshLeft" @click="handleReset">
          重新测评
        </el-button>
      </div>
    </header>

    <div v-if="phase === 'quiz'" class="progress-bar">
      <div class="progress-inner">
        <span class="progress-label">测评进度</span>
        <span class="progress-num">
          第 {{ currentIndex + 1 }} 题 · 已答 {{ answeredCount }}/{{ questions.length }}
        </span>
      </div>
      <el-progress :percentage="progressPct" :stroke-width="6" :show-text="false" color="#6366f1" />
    </div>

    <main class="test-body">
      <!-- 欢迎 -->
      <div v-if="phase === 'welcome'" class="welcome-screen">
        <div class="welcome-card">
          <div class="welcome-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
              <polygon points="12 2 15 9 22 9 16 14 18 21 12 17 6 21 8 14 2 9 9 9"/>
            </svg>
          </div>
          <h2>霍兰德职业兴趣测评</h2>
          <p class="welcome-desc">
            采用国际通用的 RIASEC 六维模型，通过 30 道自陈题评估你的兴趣类型，并匹配适合的专业方向。
            结果由本地算法即时计算，稳定可复现。
          </p>
          <div class="test-meta">
            <div class="meta-item"><span class="meta-dot"></span><span>约 5 分钟</span></div>
            <div class="meta-item"><span class="meta-dot"></span><span>30 道题目</span></div>
            <div class="meta-item"><span class="meta-dot"></span><span>可选 AI 解读</span></div>
          </div>
          <el-button type="primary" size="large" round class="start-btn" :loading="loadingQuestions" @click="startTest">
            开始测评
          </el-button>
        </div>
      </div>

      <!-- 答题 -->
      <div v-else-if="phase === 'quiz'" class="quiz-screen">
        <div class="quiz-layout">
          <aside class="overview-panel" :class="{ collapsed: overviewCollapsed }">
            <div class="overview-head">
              <div>
                <h4>题目概览</h4>
                <p class="overview-stat">
                  已答 <strong>{{ answeredCount }}</strong> / {{ questions.length }}
                  <span v-if="unansweredCount > 0" class="unanswered-tip">，未答 {{ unansweredCount }} 题</span>
                </p>
              </div>
              <button type="button" class="overview-toggle" @click="overviewCollapsed = !overviewCollapsed">
                {{ overviewCollapsed ? '展开' : '收起' }}
              </button>
            </div>
            <div v-show="!overviewCollapsed" class="overview-body">
              <div class="overview-legend">
                <span><i class="dot current"></i>当前</span>
                <span><i class="dot done"></i>已答</span>
                <span><i class="dot todo"></i>未答</span>
              </div>
              <div class="overview-grid">
                <button
                  v-for="(q, idx) in questions"
                  :key="q.id"
                  type="button"
                  class="overview-cell"
                  :class="{
                    current: idx === currentIndex,
                    done: isAnswered(q.id),
                    todo: !isAnswered(q.id),
                  }"
                  :title="`第${idx + 1}题：${q.text}`"
                  @click="goToQuestion(idx)"
                >
                  {{ idx + 1 }}
                </button>
              </div>
              <p v-if="unansweredCount > 0" class="unanswered-list">
                未答题号：
                <button
                  v-for="n in unansweredNumbers"
                  :key="n"
                  type="button"
                  class="unanswered-link"
                  @click="goToQuestion(n - 1)"
                >
                  {{ n }}
                </button>
              </p>
              <p v-else class="all-done-tip">全部题目已作答，可以提交</p>
            </div>
          </aside>

          <div class="quiz-card">
            <p class="quiz-dim">{{ currentQuestion?.dimensionName }}</p>
            <h3 class="quiz-text">{{ currentQuestion?.text }}</h3>
            <p class="quiz-hint">请根据真实感受选择，没有标准答案</p>
            <div class="likert-grid">
              <button
                v-for="opt in likertOptions"
                :key="opt.value"
                type="button"
                class="likert-btn"
                :class="{ active: answers[currentQuestion?.id] === opt.value }"
                @click="selectScore(opt.value)"
              >
                <span class="likert-val">{{ opt.value }}</span>
                <span class="likert-label">{{ opt.label }}</span>
              </button>
            </div>
            <div class="quiz-nav">
              <el-button round :disabled="currentIndex === 0" @click="prevQuestion">上一题</el-button>
              <el-button
                v-if="currentIndex < questions.length - 1"
                type="primary"
                round
                :disabled="!currentAnswered"
                @click="nextQuestion"
              >
                下一题
              </el-button>
              <el-button
                v-else
                type="primary"
                round
                :loading="submitting"
                :disabled="!canSubmit"
                @click="submitTest"
              >
                提交并查看结果
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 结果 -->
      <div v-else-if="phase === 'result'" class="result-screen">
        <div class="result-card">
          <div class="result-header">
            <div class="code-badge">{{ result.hollandCode }}</div>
            <h2>测评完成</h2>
            <p>主导类型：{{ result.dominantType }}</p>
          </div>

          <div class="result-grid">
            <div class="chart-box">
              <div ref="radarRef" class="radar-chart"></div>
            </div>
            <div class="summary-box">
              <p class="summary-text">{{ result.personalitySummary }}</p>
              <h4>推荐专业方向</h4>
              <ul class="major-list">
                <li v-for="m in result.majorMatches" :key="m.majorName">
                  <span class="major-name">{{ m.majorName }}</span>
                  <el-tag size="small" type="success">{{ m.matchPercent }}%</el-tag>
                </li>
              </ul>
            </div>
          </div>

          <div v-if="aiInterpret" class="ai-box">
            <h4>AI 深度解读</h4>
            <p class="ai-text">{{ aiInterpret }}</p>
          </div>

          <div class="result-actions">
            <el-button type="primary" size="large" round :loading="interpreting" @click="generateAiInterpret">
              {{ aiInterpret ? '重新生成解读' : '生成 AI 解读（可选）' }}
            </el-button>
            <el-button type="success" size="large" round :icon="Document" @click="goRecommend">
              去志愿推荐
            </el-button>
            <el-button size="large" round :icon="Download" @click="downloadReport">下载报告</el-button>
            <el-button size="large" round :icon="RefreshLeft" @click="handleReset">重新测评</el-button>
            <el-button size="large" round :icon="HomeFilled" @click="goHome">返回首页</el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled, RefreshLeft, ArrowLeft, Download, Document
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { fetchHollandQuestions, submitHollandTest, interpretHollandStream } from '../api/interestTest'
import { completeWizardStep } from '../api/wizard'
import { useAuthStore } from '../stores/auth'
import { generateHollandBrief } from '../api/report'
import { downloadHtml } from '../utils/downloadHtml'

const router = useRouter()
const phase = ref('welcome')
const questions = ref([])
/** 用 reactive 保证动态题号写入后 allAnswered 能即时更新 */
const answers = reactive({})
const currentIndex = ref(0)
const loadingQuestions = ref(false)
const submitting = ref(false)
const interpreting = ref(false)
const result = ref(null)
const aiInterpret = ref('')
const radarRef = ref(null)
let radarChart = null

const likertOptions = [
  { value: 1, label: '非常不符合' },
  { value: 2, label: '不太符合' },
  { value: 3, label: '一般' },
  { value: 4, label: '比较符合' },
  { value: 5, label: '非常符合' },
]

const currentQuestion = computed(() => questions.value[currentIndex.value])
const progressPct = computed(() =>
  questions.value.length ? Math.round(((currentIndex.value + 1) / questions.value.length) * 100) : 0
)
function isAnswered(questionId) {
  const score = answers[questionId]
  return typeof score === 'number' && score >= 1 && score <= 5
}

const currentAnswered = computed(() =>
  currentQuestion.value ? isAnswered(currentQuestion.value.id) : false
)

const allAnswered = computed(() =>
  questions.value.length > 0 && questions.value.every((q) => isAnswered(q.id))
)

const canSubmit = computed(() => allAnswered.value && currentAnswered.value)

const answeredCount = computed(() =>
  questions.value.filter((q) => isAnswered(q.id)).length
)

const unansweredCount = computed(() => questions.value.length - answeredCount.value)

const unansweredNumbers = computed(() =>
  questions.value
    .map((q, idx) => ({ id: q.id, num: idx + 1 }))
    .filter((item) => !isAnswered(item.id))
    .map((item) => item.num)
)

const overviewCollapsed = ref(false)

function goToQuestion(index) {
  if (index < 0 || index >= questions.value.length) return
  currentIndex.value = index
  overviewCollapsed.value = false
}

function goHome() {
  router.push('/')
}

function goRecommend() {
  const majors = result.value?.recommendedMajors?.slice(0, 5) || []
  router.push({
    path: '/recommend',
    query: majors.length ? { majors: majors.join(',') } : {},
  })
}

async function loadQuestions() {
  loadingQuestions.value = true
  try {
    const res = await fetchHollandQuestions()
    questions.value = res.data || []
    if (!questions.value.length) throw new Error('empty')
  } catch {
    ElMessage.error('加载题目失败，请确认后端已启动')
  } finally {
    loadingQuestions.value = false
  }
}

async function startTest() {
  if (!questions.value.length) await loadQuestions()
  if (!questions.value.length) return
  Object.keys(answers).forEach((k) => delete answers[k])
  currentIndex.value = 0
  aiInterpret.value = ''
  result.value = null
  phase.value = 'quiz'
}

function selectScore(score) {
  const id = currentQuestion.value?.id
  if (id == null) return
  answers[id] = score
  if (currentIndex.value < questions.value.length - 1) {
    setTimeout(() => { currentIndex.value++ }, 200)
  }
}

function prevQuestion() {
  if (currentIndex.value > 0) currentIndex.value--
}

function nextQuestion() {
  if (!currentAnswered.value) {
    ElMessage.warning('请先选择一项')
    return
  }
  if (currentIndex.value < questions.value.length - 1) currentIndex.value++
}

async function submitTest() {
  if (!allAnswered.value) {
    const first = questions.value.findIndex((q) => !isAnswered(q.id))
    if (first >= 0) {
      goToQuestion(first)
      ElMessage.warning(`还有 ${unansweredCount.value} 题未答，已跳转到第 ${first + 1} 题`)
    } else {
      ElMessage.warning('请完成全部题目')
    }
    return
  }
  submitting.value = true
  try {
    const payload = questions.value.map((q) => ({
      questionId: q.id,
      score: answers[q.id],
    }))
    const res = await submitHollandTest(payload)
    result.value = res.data
    phase.value = 'result'
    if (useAuthStore().isLoggedIn && res.data?.hollandCode) {
      completeWizardStep({
        step: 'INTEREST_TEST',
        hollandCode: res.data.hollandCode,
        hollandDominantType: res.data.dominantType,
      }).catch(() => {})
    }
    await nextTick()
    renderRadar()
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function renderRadar() {
  if (!radarRef.value || !result.value?.dimensionScores) return
  if (radarChart) radarChart.dispose()
  radarChart = echarts.init(radarRef.value)
  const names = result.value.dimensionNames || {}
  const scores = result.value.dimensionScores || {}
  const order = ['R', 'I', 'A', 'S', 'E', 'C']
  radarChart.setOption({
    color: ['#6366f1'],
    radar: {
      indicator: order.map((k) => ({ name: names[k] || k, max: 100 })),
      radius: '62%',
    },
    series: [{
      type: 'radar',
      data: [{
        value: order.map((k) => scores[k] || 0),
        areaStyle: { color: 'rgba(99,102,241,0.25)' },
        lineStyle: { width: 2 },
      }],
    }],
  })
}

async function generateAiInterpret() {
  if (!result.value) return
  interpreting.value = true
  aiInterpret.value = ''
  try {
    await interpretHollandStream(result.value, (chunk) => {
      aiInterpret.value += chunk
    })
    ElMessage.success('AI 解读已生成')
  } catch {
    ElMessage.error('AI 解读生成失败，请稍后重试')
  } finally {
    interpreting.value = false
  }
}

async function downloadReport() {
  if (!result.value) return
  const r = result.value
  try {
    const { html } = await generateHollandBrief({
      hollandCode: r.hollandCode,
      dominantType: r.dominantType,
      personalitySummary: r.personalitySummary,
      majorMatches: r.majorMatches,
      aiInterpret: aiInterpret.value,
      scores: r.scores,
    })
    downloadHtml(html, `霍兰德测评_${r.hollandCode}_${Date.now()}.html`)
    ElMessage.success('报告已下载（Node 统一模板）')
  } catch {
    ElMessage.error('报告服务不可用，请启动 report-service')
  }
}

async function handleReset() {
  try {
    await ElMessageBox.confirm('确定重新开始？当前结果将丢失。', '提示', { type: 'warning' })
    phase.value = 'welcome'
    Object.keys(answers).forEach((k) => delete answers[k])
    currentIndex.value = 0
    result.value = null
    aiInterpret.value = ''
    if (radarChart) {
      radarChart.dispose()
      radarChart = null
    }
  } catch { /* cancelled */ }
}

function onResize() {
  radarChart?.resize()
}

watch(phase, (p) => {
  if (p === 'result') nextTick(() => renderRadar())
})

onMounted(() => {
  loadQuestions()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  radarChart?.dispose()
})
</script>

<style scoped>
.test-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
}

.test-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.header-l { display: flex; align-items: center; gap: 14px; }

.back-link {
  width: 36px; height: 36px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
}

.header-brand { display: flex; align-items: center; gap: 10px; }

.brand-icon {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name { font-size: 0.975rem; font-weight: 700; color: #0f172a; }
.brand-status { font-size: 0.72rem; color: #8b5cf6; }

.btn-reset { border: 1px solid #e2e8f0; background: #fff; color: #64748b; }

.progress-bar {
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}

.progress-inner {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #64748b;
}

.progress-num { color: #6366f1; }

.test-body { flex: 1; overflow-y: auto; }

.welcome-screen, .result-screen {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
}

.quiz-screen {
  min-height: 100%;
  padding: 24px;
  align-items: flex-start;
}

.quiz-layout {
  display: flex;
  gap: 20px;
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  align-items: flex-start;
}

.overview-panel {
  flex: 0 0 280px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 18px;
  position: sticky;
  top: 12px;
}

.overview-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}

.overview-head h4 {
  font-size: 0.95rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.overview-stat {
  font-size: 0.8rem;
  color: #64748b;
}

.overview-stat strong { color: #6366f1; }
.unanswered-tip { color: #dc2626; }

.overview-toggle {
  flex-shrink: 0;
  border: none;
  background: #f1f5f9;
  color: #6366f1;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;
  cursor: pointer;
}

.overview-legend {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 0.72rem;
  color: #64748b;
  margin-bottom: 12px;
}

.overview-legend span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  display: inline-block;
}

.dot.current { background: #6366f1; }
.dot.done { background: #22c55e; }
.dot.todo { background: #e2e8f0; border: 1px solid #cbd5e1; }

.overview-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.overview-cell {
  aspect-ratio: 1;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  font-size: 0.8rem;
  font-weight: 700;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
}

.overview-cell.todo {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

.overview-cell.done {
  background: #f0fdf4;
  border-color: #86efac;
  color: #15803d;
}

.overview-cell.current {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25);
}

.overview-cell:hover:not(.current) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
}

.unanswered-list {
  margin-top: 14px;
  font-size: 0.8rem;
  color: #64748b;
  line-height: 1.8;
}

.unanswered-link {
  border: none;
  background: #fee2e2;
  color: #b91c1c;
  font-weight: 700;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 6px;
  margin: 0 4px 4px 0;
  cursor: pointer;
}

.unanswered-link:hover { background: #fecaca; }

.all-done-tip {
  margin-top: 14px;
  font-size: 0.8rem;
  color: #16a34a;
  font-weight: 600;
}

.welcome-card, .result-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  width: 100%;
  max-width: 720px;
  padding: 48px 40px;
}

.quiz-card {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  padding: 40px 36px;
}

.welcome-icon {
  width: 88px; height: 88px;
  margin: 0 auto 24px;
  border-radius: 20px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-card h2 { text-align: center; font-size: 1.5rem; font-weight: 700; margin-bottom: 12px; }

.welcome-desc { text-align: center; color: #64748b; line-height: 1.8; margin-bottom: 24px; }

.test-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 28px;
  font-size: 0.875rem;
  color: #64748b;
}

.meta-item { display: flex; align-items: center; gap: 6px; }
.meta-dot { width: 8px; height: 8px; border-radius: 50%; background: #a5b4fc; }

.start-btn {
  display: block;
  margin: 0 auto;
  padding: 14px 48px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border: none;
}

.quiz-dim { font-size: 0.8rem; color: #8b5cf6; font-weight: 600; margin-bottom: 8px; }
.quiz-text { font-size: 1.25rem; font-weight: 700; color: #0f172a; line-height: 1.6; margin-bottom: 8px; }
.quiz-hint { font-size: 0.85rem; color: #94a3b8; margin-bottom: 28px; }

.likert-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 32px; }

.likert-btn {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}

.likert-btn:hover { border-color: #c7d2fe; background: #f8fafc; }
.likert-btn.active { border-color: #6366f1; background: #eef2ff; }

.likert-val {
  width: 28px; height: 28px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #6366f1;
  flex-shrink: 0;
}

.likert-btn.active .likert-val { background: #6366f1; color: #fff; }
.likert-label { font-size: 0.95rem; color: #334155; }

.quiz-nav { display: flex; justify-content: space-between; gap: 12px; }

.result-card { max-width: 900px; padding: 40px; }

.result-header { text-align: center; margin-bottom: 32px; }

.code-badge {
  display: inline-block;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 0.2em;
  color: #6366f1;
  background: #eef2ff;
  padding: 8px 24px;
  border-radius: 12px;
  margin-bottom: 12px;
}

.result-header h2 { font-size: 1.35rem; margin-bottom: 6px; }
.result-header p { color: #64748b; }

.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 28px;
}

.chart-box, .summary-box {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.radar-chart { width: 100%; height: 280px; }

.summary-text { font-size: 0.9rem; color: #475569; line-height: 1.8; margin-bottom: 16px; }

.summary-box h4 { font-size: 0.95rem; margin-bottom: 12px; color: #0f172a; }

.major-list { list-style: none; padding: 0; margin: 0; }

.major-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.875rem;
}

.ai-box {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.ai-box h4 { margin-bottom: 10px; color: #166534; }
.ai-text { line-height: 1.85; color: #334155; white-space: pre-wrap; }

.result-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

@media (max-width: 900px) {
  .quiz-layout { flex-direction: column; }
  .overview-panel {
    flex: none;
    width: 100%;
    position: static;
  }
  .overview-panel.collapsed .overview-body { display: none; }
}

@media (max-width: 768px) {
  .welcome-card, .quiz-card, .result-card { padding: 28px 20px; }
  .overview-grid { grid-template-columns: repeat(5, 1fr); }
  .result-grid { grid-template-columns: 1fr; }
  .result-actions .el-button { width: 100%; }
}
</style>
