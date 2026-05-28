<template>
  <div class="detail-page">
    <header class="detail-header">
      <button class="back-link" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h2>院校详情</h2>
    </header>

    <main v-loading="loading" class="detail-body">
      <template v-if="school">
        <div class="school-hero">
          <div class="school-hero-content">
            <h1>{{ school.schoolName }}</h1>
            <p class="loc">{{ school.location }} · {{ school.city }}</p>
            <div class="school-tags">
              <span v-if="school.is985" class="tag tag-985">985</span>
              <span v-if="school.is211" class="tag tag-211">211</span>
              <span v-if="school.isDoubleFirst" class="tag tag-df">双一流</span>
              <span class="tag tag-type">{{ school.schoolType }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>院校简介</h3>
          <p>{{ school.description || '暂无简介' }}</p>
        </div>

        <div v-if="school.admissionScores?.length" class="detail-section">
          <h3>录取分数线（{{ scoreProvince }} · {{ subjectLabel }}）</h3>
          <el-table :data="school.admissionScores" stripe size="small">
            <el-table-column prop="year" label="年份" width="80" />
            <el-table-column prop="batch" label="批次" width="100" />
            <el-table-column prop="minScore" label="最低分" width="90" />
            <el-table-column prop="avgScore" label="平均分" width="90" />
            <el-table-column prop="minRank" label="最低位次" width="100" />
            <el-table-column prop="overLineScore" label="超线分" width="90">
              <template #default="{ row }">
                <span v-if="row.overLineScore != null" :class="row.overLineScore >= 0 ? 'over' : 'under'">
                  {{ row.overLineScore > 0 ? '+' : '' }}{{ row.overLineScore }}
                </span>
                <span v-else>—</span>
              </template>
            </el-table-column>
          </el-table>
          <p class="hint">近五年（2021-2025）样本数据，仅供参考</p>
        </div>

        <div class="detail-actions">
          <el-button type="primary" round @click="askAI">
            <el-icon><ChatDotRound /></el-icon>
            向 AI 咨询该校
          </el-button>
          <el-button round @click="goCompare">加入对比</el-button>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="院校不存在" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { fetchSchoolDetail } from '../api/school'

const route = useRoute()
const router = useRouter()

const schoolCode = computed(() => route.params.code)
const scoreProvince = ref(route.query.scoreProvince || '河南')
const subjectType = ref(route.query.subjectType || 'science')
const subjectLabel = computed(() => (subjectType.value === 'science' ? '理科' : '文科'))

const loading = ref(false)
const school = ref(null)

function askAI() {
  router.push(`/chat?q=介绍一下${school.value?.schoolName || '该院校'}`)
}

function goCompare() {
  router.push({
    path: '/compare',
    query: {
      type: 'school',
      codes: schoolCode.value,
      scoreProvince: scoreProvince.value,
      subjectType: subjectType.value,
    },
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetchSchoolDetail(schoolCode.value, {
      scoreProvince: scoreProvince.value,
      subjectType: subjectType.value,
    })
    school.value = res.data
  } catch {
    school.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: var(--bg-body, #f8fafc);
}

.detail-header {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid #e2e8f0;
}

.detail-header h2 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
}

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

.detail-body {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

.school-hero {
  background: linear-gradient(135deg, #1e293b, #0f172a);
  border-radius: 20px;
  padding: 48px;
  margin-bottom: 24px;
}

.school-hero-content h1 {
  font-size: 2rem;
  font-weight: 800;
  color: #fff;
  margin-bottom: 8px;
}

.loc {
  color: #94a3b8;
  margin-bottom: 12px;
}

.school-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.tag-985 { background: rgba(239, 68, 68, 0.2); color: #fca5a5; }
.tag-211 { background: rgba(59, 130, 246, 0.2); color: #93c5fd; }
.tag-df { background: rgba(34, 197, 94, 0.2); color: #86efac; }
.tag-type { background: rgba(255,255,255,0.1); color: #cbd5e1; }

.detail-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: 16px;
  color: #0f172a;
}

.detail-section p {
  color: #64748b;
  line-height: 1.8;
}

.hint {
  font-size: 0.8rem;
  color: #94a3b8 !important;
  margin-top: 12px;
}

.over { color: #16a34a; font-weight: 600; }
.under { color: #dc2626; font-weight: 600; }

.detail-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
