<template>
  <div class="wizard-page">
    <div class="page-head">
      <h1>志愿填报向导</h1>
      <p class="sub">每一步可查看、修改已填信息；也可前往对应功能页完成后再回到此同步</p>
    </div>

    <el-progress :percentage="progress?.progressPercent ?? 0" :stroke-width="10" striped />

    <div v-loading="loading" class="steps-wrap">
      <div
        v-for="detail in progress?.stepDetails || []"
        :key="detail.step"
        class="step-card"
        :class="{ expanded: expandedStep === detail.step, done: detail.completed, current: detail.current }"
      >
        <div class="step-header" @click="toggleStep(detail.step)">
          <div class="step-num" :class="{ done: detail.completed }">{{ detail.order }}</div>
          <div class="step-title">
            <strong>{{ detail.label }}</strong>
            <span v-if="detail.completed" class="badge done">已完成</span>
            <span v-else-if="detail.current" class="badge cur">当前</span>
            <span v-else-if="detail.hasData" class="badge draft">已填写</span>
          </div>
          <el-icon class="chevron"><ArrowDown /></el-icon>
        </div>

        <div v-show="expandedStep === detail.step" class="step-body">
          <ul v-if="detail.summary?.length" class="summary">
            <li v-for="(line, i) in detail.summary" :key="i">{{ line }}</li>
          </ul>
          <p v-else class="empty-tip">暂未填写，可在下方录入或前往功能页完成</p>

          <!-- 录入成绩 -->
          <el-form v-if="detail.step === 'ENTER_SCORE'" label-width="88px" class="step-form">
            <el-form-item label="录取生源省">
              <el-select v-model="forms.ENTER_SCORE.scoreProvince" filterable style="width: 100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
            <el-form-item label="科类">
              <el-select v-model="forms.ENTER_SCORE.subjectType" style="width: 100%">
                <el-option label="理科" value="science" />
                <el-option label="文科" value="liberal" />
              </el-select>
            </el-form-item>
            <el-form-item label="高考分数">
              <el-input-number v-model="forms.ENTER_SCORE.userScore" :min="200" :max="750" style="width: 100%" />
            </el-form-item>
          </el-form>

          <!-- 兴趣测评 -->
          <el-form v-else-if="detail.step === 'INTEREST_TEST'" label-width="88px" class="step-form">
            <el-form-item label="霍兰德代码">
              <el-input v-model="forms.INTEREST_TEST.hollandCode" placeholder="如 IRC" maxlength="6" />
            </el-form-item>
            <el-form-item label="主导类型">
              <el-input v-model="forms.INTEREST_TEST.hollandDominantType" placeholder="如 研究型" />
            </el-form-item>
          </el-form>

          <!-- 冲稳保 -->
          <el-form v-else-if="detail.step === 'MATCH'" label-width="88px" class="step-form">
            <el-form-item label="院校所在地">
              <el-select v-model="forms.MATCH.schoolProvince" filterable style="width: 100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
            <el-form-item label="录取生源省">
              <el-select v-model="forms.MATCH.scoreProvince" filterable style="width: 100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
            <el-form-item label="科类">
              <el-select v-model="forms.MATCH.subjectType" style="width: 100%">
                <el-option label="理科" value="science" />
                <el-option label="文科" value="liberal" />
              </el-select>
            </el-form-item>
            <el-form-item label="高考分数">
              <el-input-number v-model="forms.MATCH.userScore" :min="200" :max="750" style="width: 100%" />
            </el-form-item>
            <el-form-item label="测算结果">
              <div class="match-counts">
                <span>冲 <el-input-number v-model="forms.MATCH.matchRushCount" :min="0" size="small" /></span>
                <span>稳 <el-input-number v-model="forms.MATCH.matchStableCount" :min="0" size="small" /></span>
                <span>保 <el-input-number v-model="forms.MATCH.matchSafeCount" :min="0" size="small" /></span>
              </div>
            </el-form-item>
          </el-form>

          <!-- 收藏 -->
          <div v-else-if="detail.step === 'FAVORITES'" class="step-form">
            <p>当前收藏 <strong>{{ forms.FAVORITES.favoriteCount ?? 0 }}</strong> 所院校（保存时自动同步）</p>
          </div>

          <!-- 定稿方案 -->
          <el-form v-else-if="detail.step === 'FINALIZE'" label-width="88px" class="step-form">
            <el-form-item label="方案 ID">
              <el-input-number v-model="forms.FINALIZE.lastPlanId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="方案名称">
              <el-input v-model="forms.FINALIZE.lastPlanTitle" placeholder="与「我的志愿方案」一致" />
            </el-form-item>
          </el-form>

          <div class="step-actions">
            <el-button @click="goExternal(detail.step)">前往功能页</el-button>
            <el-button :loading="savingStep === detail.step" @click="saveOnly(detail.step)">保存修改</el-button>
            <el-button type="primary" :loading="savingStep === detail.step" @click="saveAndComplete(detail.step)">
              保存并完成
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { fetchWizardProgress, saveWizardStep, completeWizardStep, focusWizardStep } from '../api/wizard'
import { CHINA_PROVINCES } from '../utils/provinces'

const router = useRouter()
const provinces = CHINA_PROVINCES
const loading = ref(false)
const savingStep = ref(null)
const progress = ref(null)
const expandedStep = ref('ENTER_SCORE')

const forms = reactive({
  ENTER_SCORE: { scoreProvince: '北京', subjectType: 'science', userScore: 550 },
  INTEREST_TEST: { hollandCode: '', hollandDominantType: '' },
  MATCH: {
    schoolProvince: '北京',
    scoreProvince: '北京',
    subjectType: 'science',
    userScore: 550,
    matchRushCount: 0,
    matchStableCount: 0,
    matchSafeCount: 0,
  },
  FAVORITES: { favoriteCount: 0 },
  FINALIZE: { lastPlanId: null, lastPlanTitle: '' },
})

const stepRoutes = {
  ENTER_SCORE: '/rank',
  INTEREST_TEST: '/interest-test',
  MATCH: '/matcher',
  FAVORITES: '/schools',
  FINALIZE: '/plans',
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await fetchWizardProgress()
    progress.value = res.data
    syncFormsFromProgress(res.data)
    if (res.data?.currentStep) {
      expandedStep.value = res.data.currentStep
    }
  } catch (e) {
    ElMessage.error(e.message || '请先登录')
    router.push({ path: '/login', query: { redirect: '/wizard' } })
  } finally {
    loading.value = false
  }
}

function syncFormsFromProgress(p) {
  if (!p) return
  forms.ENTER_SCORE.scoreProvince = p.scoreProvince || forms.ENTER_SCORE.scoreProvince
  forms.ENTER_SCORE.subjectType = p.subjectType || 'science'
  forms.ENTER_SCORE.userScore = p.userScore ?? forms.ENTER_SCORE.userScore

  forms.INTEREST_TEST.hollandCode = p.hollandCode || ''
  forms.INTEREST_TEST.hollandDominantType = p.hollandDominantType || ''

  forms.MATCH.schoolProvince = p.schoolProvince || forms.MATCH.schoolProvince
  forms.MATCH.scoreProvince = p.scoreProvince || forms.MATCH.scoreProvince
  forms.MATCH.subjectType = p.subjectType || 'science'
  forms.MATCH.userScore = p.userScore ?? forms.MATCH.userScore
  forms.MATCH.matchRushCount = p.matchRushCount ?? 0
  forms.MATCH.matchStableCount = p.matchStableCount ?? 0
  forms.MATCH.matchSafeCount = p.matchSafeCount ?? 0

  forms.FAVORITES.favoriteCount = p.favoriteCount ?? 0

  forms.FINALIZE.lastPlanId = p.lastPlanId ?? null
  forms.FINALIZE.lastPlanTitle = p.lastPlanTitle || ''

  for (const d of p.stepDetails || []) {
    const data = d.data || {}
    if (d.step === 'ENTER_SCORE' && data.userScore != null) {
      Object.assign(forms.ENTER_SCORE, data)
    }
    if (d.step === 'INTEREST_TEST' && data.hollandCode) {
      Object.assign(forms.INTEREST_TEST, data)
    }
    if (d.step === 'MATCH' && (data.schoolProvince || data.userScore != null)) {
      Object.assign(forms.MATCH, data)
    }
    if (d.step === 'FINALIZE' && data.lastPlanId) {
      Object.assign(forms.FINALIZE, data)
    }
  }
}

function toggleStep(step) {
  expandedStep.value = expandedStep.value === step ? '' : step
  focusWizardStep(step).catch(() => {})
}

function payloadForStep(step) {
  const base = { step }
  if (step === 'ENTER_SCORE') {
    return { ...base, ...forms.ENTER_SCORE }
  }
  if (step === 'INTEREST_TEST') {
    return { ...base, hollandCode: forms.INTEREST_TEST.hollandCode, hollandDominantType: forms.INTEREST_TEST.hollandDominantType }
  }
  if (step === 'MATCH') {
    return { ...base, ...forms.MATCH }
  }
  if (step === 'FAVORITES') {
    return { ...base, favoriteCount: forms.FAVORITES.favoriteCount }
  }
  if (step === 'FINALIZE') {
    return { ...base, lastPlanId: forms.FINALIZE.lastPlanId, lastPlanTitle: forms.FINALIZE.lastPlanTitle }
  }
  return base
}

async function saveOnly(step) {
  savingStep.value = step
  try {
    const res = await saveWizardStep(payloadForStep(step))
    progress.value = res.data
    syncFormsFromProgress(res.data)
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    savingStep.value = null
  }
}

async function saveAndComplete(step) {
  savingStep.value = step
  try {
    const res = await completeWizardStep(payloadForStep(step))
    progress.value = res.data
    syncFormsFromProgress(res.data)
    ElMessage.success('已保存并完成本步')
    if (res.data?.currentStep) {
      expandedStep.value = res.data.currentStep
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    savingStep.value = null
  }
}

function goExternal(step) {
  const path = stepRoutes[step]
  if (!path) return
  const query = {}
  if (step === 'MATCH' || step === 'ENTER_SCORE') {
    const f = step === 'MATCH' ? forms.MATCH : forms.ENTER_SCORE
    if (f.scoreProvince) query.scoreProvince = f.scoreProvince
    if (f.schoolProvince) query.schoolProvince = f.schoolProvince
    if (f.userScore) query.score = f.userScore
  }
  router.push({ path, query })
}
</script>

<style scoped>
.wizard-page { max-width: 720px; margin: 0 auto; padding: 24px; }
.page-head { margin-bottom: 20px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.sub { color: #64748b; font-size: 0.9rem; margin-top: 6px; line-height: 1.5; }
.steps-wrap { margin-top: 24px; display: flex; flex-direction: column; gap: 12px; }
.step-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.step-card.current { border-color: #a5b4fc; }
.step-card.expanded { box-shadow: 0 4px 20px rgba(99, 102, 241, 0.08); }
.step-header {
  display: flex; align-items: center; gap: 14px; padding: 16px 20px; cursor: pointer; user-select: none;
}
.step-header:hover { background: #fafafa; }
.step-num {
  width: 32px; height: 32px; border-radius: 50%; background: #f1f5f9; color: #64748b;
  display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem; flex-shrink: 0;
}
.step-num.done { background: #dcfce7; color: #16a34a; }
.step-title { flex: 1; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.step-title strong { font-size: 1rem; color: #1e293b; }
.badge { font-size: 0.7rem; padding: 2px 8px; border-radius: 6px; font-weight: 500; }
.badge.done { background: #dcfce7; color: #16a34a; }
.badge.cur { background: #eef2ff; color: #4f46e5; }
.badge.draft { background: #fef3c7; color: #b45309; }
.chevron { color: #94a3b8; transition: transform 0.2s; }
.step-card.expanded .chevron { transform: rotate(180deg); }
.step-body { padding: 0 20px 20px; border-top: 1px solid #f1f5f9; }
.summary { margin: 16px 0; padding-left: 18px; color: #475569; font-size: 0.9rem; line-height: 1.7; }
.empty-tip { color: #94a3b8; font-size: 0.85rem; margin: 12px 0; }
.step-form { margin-top: 12px; }
.match-counts { display: flex; flex-wrap: wrap; gap: 16px; align-items: center; font-size: 0.85rem; color: #64748b; }
.step-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; padding-top: 16px; border-top: 1px dashed #e2e8f0; }
</style>
