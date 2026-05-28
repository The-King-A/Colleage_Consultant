<template>
  <div class="plan-page">
    <div class="page-head">
      <h1>我的志愿方案</h1>
      <p class="sub">Java 后端持久化 · 保存冲稳保测算结果，随时回看</p>
    </div>

    <el-empty v-if="!loading && !plans.length" description="暂无方案">
      <el-button type="primary" @click="$router.push('/matcher')">去测算并保存</el-button>
    </el-empty>

    <div v-loading="loading" class="plan-list">
      <div v-for="p in plans" :key="p.id" class="plan-card">
        <div class="plan-head">
          <h3>{{ p.title }}</h3>
          <el-button text type="danger" @click="remove(p.id)">删除</el-button>
        </div>
        <p class="meta">
          {{ p.schoolProvince }} · 生源 {{ p.scoreProvince }} · {{ p.subjectType === 'science' ? '理科' : '文科' }}
          · {{ p.userScore }} 分
        </p>
        <p class="counts">
          冲 <b>{{ p.rushCount ?? 0 }}</b> · 稳 <b>{{ p.stableCount ?? 0 }}</b> · 保 <b>{{ p.safeCount ?? 0 }}</b>
        </p>
        <p v-if="p.note" class="note">{{ p.note }}</p>
        <p class="time">{{ formatTime(p.createTime) }}</p>
        <div class="actions">
          <el-button size="small" @click="showDetail(p)">查看详情</el-button>
          <el-button size="small" type="primary" plain :loading="reviewingId === p.id" @click="aiReview(p)">
            AI 评审
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="方案详情" width="720px">
      <pre v-if="detailJson" class="json-preview">{{ detailJson }}</pre>
    </el-dialog>

    <el-dialog
      v-model="reviewVisible"
      title="AI 方案评审"
      width="640px"
      destroy-on-close
      :close-on-click-modal="!isReviewing"
    >
      <p v-if="reviewPlanMeta" class="review-meta-hint">{{ reviewPlanMeta }}</p>
      <div class="review-toolbar">
        <el-button
          size="small"
          :disabled="!hasReviewContent"
          :icon="copiedReview ? Check : CopyDocument"
          @click="copyReview"
        >
          {{ copiedReview ? '已复制' : '复制' }}
        </el-button>
        <el-button
          size="small"
          type="primary"
          :disabled="!hasReviewContent"
          :loading="exportingPdf"
          :icon="Download"
          @click="exportPdf"
        >
          导出 PDF
        </el-button>
        <span v-if="isReviewing" class="review-stream-tip">生成中，可先复制已输出内容</span>
        <span v-else-if="!hasReviewContent" class="review-stream-tip">等待评审内容输出…</span>
      </div>
      <div v-loading="isReviewing && !hasReviewContent" class="review-body">
        {{ reviewText || '正在生成评审…' }}
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, Check, Download } from '@element-plus/icons-vue'
import { fetchVolunteerPlans, deleteVolunteerPlan, reviewPlanStream, downloadReviewPdf } from '../api/plan'
import { copyReviewText } from '../utils/reviewExport'

const loading = ref(false)
const plans = ref([])
const detailVisible = ref(false)
const detailJson = ref('')
const reviewVisible = ref(false)
const reviewText = ref('')
const reviewingId = ref(null)
const reviewPlanTitle = ref('')
const reviewPlanMeta = ref('')
const copiedReview = ref(false)
const isReviewing = ref(false)
const exportingPdf = ref(false)

/** 有任意评审文字即可复制/导出（不必等流式结束） */
const hasReviewContent = computed(() => reviewText.value.trim().length > 0)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await fetchVolunteerPlans()
    plans.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

function showDetail(p) {
  try {
    const obj = JSON.parse(p.planJson || '{}')
    detailJson.value = JSON.stringify(obj, null, 2)
  } catch {
    detailJson.value = p.planJson || ''
  }
  detailVisible.value = true
}

function buildPlanMeta(p) {
  const subject = p.subjectType === 'science' ? '理科' : '文科'
  return `${p.schoolProvince} · 生源 ${p.scoreProvince} · ${subject} · ${p.userScore} 分 · 冲${p.rushCount ?? 0}/稳${p.stableCount ?? 0}/保${p.safeCount ?? 0}`
}

async function aiReview(p) {
  reviewingId.value = p.id
  isReviewing.value = true
  reviewText.value = ''
  reviewPlanTitle.value = p.title || '志愿方案'
  reviewPlanMeta.value = buildPlanMeta(p)
  copiedReview.value = false
  reviewVisible.value = true
  try {
    await reviewPlanStream(p.id, (chunk) => {
      reviewText.value += chunk
    })
    if (!reviewText.value.trim()) {
      ElMessage.warning('未收到评审内容，请检查后端与 DeepSeek 配置')
    } else {
      ElMessage.success('评审完成')
    }
  } catch (e) {
    ElMessage.error(e.message || 'AI 评审失败')
    if (!reviewText.value.trim()) {
      reviewVisible.value = false
    }
  } finally {
    reviewingId.value = null
    isReviewing.value = false
  }
}

async function copyReview() {
  try {
    await copyReviewText(reviewText.value)
    copiedReview.value = true
    ElMessage.success('评审内容已复制到剪贴板')
    setTimeout(() => {
      copiedReview.value = false
    }, 2000)
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  }
}

async function exportPdf() {
  exportingPdf.value = true
  try {
    await downloadReviewPdf({
      title: `AI 方案评审 · ${reviewPlanTitle.value}`,
      meta: reviewPlanMeta.value,
      content: reviewText.value,
    })
    ElMessage.success('PDF 已开始下载')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  } finally {
    exportingPdf.value = false
  }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该方案？', '提示', { type: 'warning' })
    await deleteVolunteerPlan(id)
    ElMessage.success('已删除')
    load()
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.plan-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page-head { margin-bottom: 24px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.sub { color: #64748b; font-size: 0.9rem; margin-top: 6px; }
.plan-list { display: flex; flex-direction: column; gap: 16px; }
.plan-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; }
.plan-head { display: flex; justify-content: space-between; align-items: center; }
.plan-head h3 { margin: 0; font-size: 1.1rem; }
.meta, .counts { color: #64748b; font-size: 0.9rem; margin: 8px 0; }
.counts b { color: #6366f1; }
.note { color: #475569; font-size: 0.85rem; }
.time { font-size: 0.75rem; color: #94a3b8; margin-bottom: 12px; }
.actions { display: flex; gap: 8px; margin-top: 8px; }
.json-preview { max-height: 400px; overflow: auto; font-size: 0.75rem; background: #f8fafc; padding: 12px; border-radius: 8px; }
.review-meta-hint { font-size: 0.8rem; color: #64748b; margin: 0 0 12px; line-height: 1.5; }
.review-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}
.review-stream-tip { font-size: 0.75rem; color: #94a3b8; margin-left: 4px; }
.review-body { min-height: 120px; max-height: 50vh; overflow-y: auto; line-height: 1.8; color: #334155; white-space: pre-wrap; }
</style>
