<template>
  <div class="matcher-page">
    <div class="page-head">
      <div>
        <h1>冲稳保测算</h1>
        <p class="sub">按<strong>院校所在地</strong>筛选学校，按<strong>录取生源省</strong>匹配历年分数线。同省组合点「补全样本」即可，一般数秒内完成。</p>
      </div>
      <el-tag :type="serviceOk ? 'success' : 'warning'" effect="plain">
        {{ serviceOk ? '分析服务在线' : '分析服务离线' }}
      </el-tag>
    </div>

    <div class="form-card">
      <el-form :inline="true" @submit.prevent="runMatch">
        <el-form-item label="院校所在地">
          <el-select v-model="form.schoolProvince" filterable style="width: 130px">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="录取生源省">
          <el-select v-model="form.scoreProvince" filterable style="width: 130px">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="科类">
          <el-select v-model="form.subjectType" style="width: 110px">
            <el-option label="理科" value="science" />
            <el-option label="文科" value="liberal" />
          </el-select>
        </el-form-item>
        <el-form-item label="高考分数">
          <el-input-number v-model="form.score" :min="200" :max="750" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="runMatch">开始测算</el-button>
          <el-button :loading="ensuringData" @click="ensureData">补全样本数据</el-button>
          <el-button v-if="result" :disabled="!reportOk" @click="downloadBrief">导出简报 (Node)</el-button>
          <el-button v-if="result" type="success" plain @click="openSavePlan">保存方案</el-button>
          <router-link v-if="result" :to="insightsLink" class="insight-link">分数线洞察 →</router-link>
        </el-form-item>
      </el-form>
    </div>

    <p v-if="rankInfo?.cumulativeRank" class="rank-banner">
      位次约 <strong>{{ rankInfo.cumulativeRank }}</strong> 名
      <span v-if="rankInfo.beatPercent != null"> · 约超过 {{ rankInfo.beatPercent }}% 考生</span>
      <router-link to="/rank" class="insight-link">位次详情</router-link>
    </p>

    <p v-if="result" class="result-tip">
      院校所在地 <strong>{{ result.schoolProvince }}</strong> · 录取生源省 <strong>{{ result.scoreProvince }}</strong>
      · 共匹配 {{ result.summary?.totalMatched ?? 0 }} 所
      <span v-if="result.tierMode === 'relative'" class="tier-mode-tip">
        （样本线较集中，冲/稳/保按录取难度相对划分）
      </span>
      <span v-else-if="result.tierMode === 'balanced'" class="tier-mode-tip">
        （已按线差均衡划分，保证冲/稳/保均有院校）
      </span>
      <span v-if="result.summary?.dataSource === 'synthetic'" class="tier-mode-tip">
        （库内样本不足，已按省控线+院校层次推算）
      </span>
    </p>

    <div v-if="result && result.summary?.totalMatched < 15" class="empty-hint">
      匹配院校较少，可点击「补全样本数据」写入该省录取线后再测算（首次约需 10～30 秒）。
    </div>

    <div v-if="result && result.summary?.totalMatched === 0" class="empty-hint">
      该条件下暂无院校录取样本。可尝试：换录取生源省、调整分数，或确认 analytics-service 已连接数据库。
    </div>

    <div v-if="result" v-loading="loading" class="tiers">
      <div v-for="tier in tierMeta" :key="tier.key" class="tier-col">
        <h3 :style="{ color: tier.color }">{{ tier.label }}（{{ result[tier.key]?.length || 0 }}）</h3>
        <div v-if="!result[tier.key]?.length" class="empty-tier">暂无匹配院校</div>
        <div v-for="s in result[tier.key]" :key="s.schoolCode" class="school-item">
          <strong>{{ s.schoolName }}</strong>
          <p>{{ s.location }} · {{ s.city }}</p>
          <p class="score">最低 {{ s.minScore }} · 线差 <b>{{ s.scoreDiff > 0 ? '+' : '' }}{{ s.scoreDiff }}</b></p>
          <div class="tags">
            <span v-if="s.is985" class="t985">985</span>
            <span v-if="s.is211" class="t211">211</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="saveDialog" title="保存志愿方案" width="400px">
      <el-input v-model="planTitle" placeholder="方案名称，如：福建-北京-550分" />
      <el-input v-model="planNote" type="textarea" placeholder="备注（可选）" rows="2" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="saveDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { matchAdmission, checkAnalyticsHealth } from '../api/analytics'
import { ensureMatcherData } from '../api/school'
import { generateVolunteerBrief } from '../api/report'
import { saveVolunteerPlan } from '../api/plan'
import { lookupScoreRank } from '../api/rank'
import { completeWizardStep } from '../api/wizard'
import { downloadHtml } from '../utils/downloadHtml'
import { CHINA_PROVINCES } from '../utils/provinces'

const router = useRouter()
const authStore = useAuthStore()
const rankInfo = ref(null)

const provinces = CHINA_PROVINCES
const form = reactive({
  schoolProvince: '北京',
  scoreProvince: '北京',
  subjectType: 'science',
  score: 550,
})
const loading = ref(false)
const ensuringData = ref(false)
const result = ref(null)
const serviceOk = ref(false)
const reportOk = ref(false)
const saveDialog = ref(false)
const planTitle = ref('')
const planNote = ref('')
const saving = ref(false)

const insightsLink = computed(() => ({
  path: '/insights',
  query: {
    scoreProvince: form.scoreProvince,
    schoolProvince: form.schoolProvince,
    score: form.score,
  },
}))

const tierMeta = [
  { key: 'rush', label: '冲', color: '#dc2626' },
  { key: 'stable', label: '稳', color: '#2563eb' },
  { key: 'safe', label: '保', color: '#16a34a' },
]

onMounted(async () => {
  serviceOk.value = await checkAnalyticsHealth()
  try {
    const r = await fetch('/report-api/health')
    reportOk.value = r.ok
  } catch {
    reportOk.value = false
  }
})

async function ensureData() {
  ensuringData.value = true
  try {
    const res = await ensureMatcherData({
      schoolProvince: form.schoolProvince,
      scoreProvince: form.scoreProvince,
    })
    const n = res.data?.inserted ?? 0
    ElMessage.success(res.data?.message || `已处理，新增 ${n} 条样本`)
    if (n > 0) {
      await runMatch()
    }
  } catch (e) {
    ElMessage.error(e.message || '补全失败，请确认后端已启动')
  } finally {
    ensuringData.value = false
  }
}

async function runMatch() {
  if (!form.schoolProvince) {
    ElMessage.warning('请选择院校所在地')
    return
  }
  if (form.score == null || form.score === '') {
    ElMessage.warning('请输入高考分数')
    return
  }
  loading.value = true
  try {
    result.value = await matchAdmission({
      schoolProvince: form.schoolProvince,
      scoreProvince: form.scoreProvince,
      subjectType: form.subjectType,
      score: form.score,
    })
    loadRank()
    if (authStore.isLoggedIn) {
      completeWizardStep({
        step: 'MATCH',
        userScore: form.score,
        scoreProvince: form.scoreProvince,
        subjectType: form.subjectType,
        schoolProvince: form.schoolProvince,
        matchRushCount: result.value?.summary?.rushCount,
        matchStableCount: result.value?.summary?.stableCount,
        matchSafeCount: result.value?.summary?.safeCount,
        markComplete: false,
        advance: false,
      }).catch(() => {})
    }
  } catch (e) {
    ElMessage.error(e.message || '测算失败')
  } finally {
    loading.value = false
  }
}

async function downloadBrief() {
  if (!result.value) return
  try {
    const { html } = await generateVolunteerBrief({
      title: '冲稳保测算简报',
      province: form.scoreProvince,
      score: form.score,
      matchSummary: result.value.summary,
      rush: result.value.rush,
      stable: result.value.stable,
      safe: result.value.safe,
    })
    downloadHtml(html, `冲稳保简报_${form.schoolProvince}_${form.score}.html`)
    ElMessage.success('简报已生成（Node.js 服务）')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  }
}

function openSavePlan() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后保存方案')
    router.push({ path: '/login', query: { redirect: '/matcher' } })
    return
  }
  planTitle.value = `${form.schoolProvince}-${form.scoreProvince}-${form.score}分`
  planNote.value = ''
  saveDialog.value = true
}

async function loadRank() {
  try {
    const res = await lookupScoreRank({
      province: form.scoreProvince,
      subjectType: form.subjectType,
      score: form.score,
    })
    rankInfo.value = res.data
  } catch {
    rankInfo.value = null
  }
}

async function savePlan() {
  if (!planTitle.value.trim()) {
    ElMessage.warning('请填写方案名称')
    return
  }
  saving.value = true
  try {
    const saveRes = await saveVolunteerPlan({
      title: planTitle.value.trim(),
      schoolProvince: form.schoolProvince,
      scoreProvince: form.scoreProvince,
      subjectType: form.subjectType,
      userScore: form.score,
      planJson: JSON.stringify(result.value),
      note: planNote.value || undefined,
    })
    const planId = saveRes.data?.id
    if (planId) {
      await completeWizardStep({
        step: 'FINALIZE',
        lastPlanId: planId,
        lastPlanTitle: planTitle.value.trim(),
        userScore: form.score,
        scoreProvince: form.scoreProvince,
        subjectType: form.subjectType,
        schoolProvince: form.schoolProvince,
        matchRushCount: result.value?.summary?.rushCount,
        matchStableCount: result.value?.summary?.stableCount,
        matchSafeCount: result.value?.summary?.safeCount,
      }).catch(() => {})
    }
    ElMessage.success('方案已保存')
    saveDialog.value = false
    router.push('/plans')
  } catch (e) {
    ElMessage.error(e.message || '保存失败，请确认已执行 volunteer_plan.sql 建表')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.matcher-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.sub { color: #64748b; font-size: 0.9rem; margin-top: 6px; }
.sub strong { color: #475569; }
.result-tip { font-size: 0.85rem; color: #64748b; margin-bottom: 12px; }
.tier-mode-tip { color: #8b5cf6; font-size: 0.8rem; }
.empty-hint {
  background: #fffbeb; border: 1px solid #fde68a; color: #92400e;
  padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; font-size: 0.9rem;
}
.form-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; margin-bottom: 24px; }
.tiers { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.tier-col { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; max-height: 70vh; overflow-y: auto; }
.tier-col h3 { margin-bottom: 12px; font-size: 1rem; }
.school-item { border-bottom: 1px solid #f1f5f9; padding: 12px 0; font-size: 0.85rem; }
.school-item p { color: #64748b; margin: 4px 0; }
.score b { color: #6366f1; }
.tags span { font-size: 0.65rem; padding: 2px 6px; border-radius: 4px; margin-right: 4px; }
.t985 { background: #fee2e2; color: #dc2626; }
.t211 { background: #dbeafe; color: #2563eb; }
.empty-tier { color: #94a3b8; font-size: 0.85rem; }
.rank-banner {
  background: #eef2ff; color: #4338ca; padding: 10px 16px; border-radius: 10px;
  margin-bottom: 12px; font-size: 0.9rem;
}
.insight-link { margin-left: 8px; color: #6366f1; font-size: 0.85rem; text-decoration: none; }
@media (max-width: 900px) { .tiers { grid-template-columns: 1fr; } }
</style>
