<template>
  <div class="detail-page">
    <header class="detail-header">
      <button class="back-link" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h2>专业详情</h2>
    </header>

    <main v-loading="loading" class="detail-body">
      <template v-if="major">
        <div class="major-hero">
          <h1>{{ major.majorName }}</h1>
          <p class="major-category">{{ major.majorCategory }} · {{ major.majorSubcategory }}</p>
          <div class="stat-row">
            <span>热度 <strong>{{ major.hotIndex ?? '—' }}</strong></span>
            <span>就业率 <strong>{{ major.employmentRate ?? '—' }}%</strong></span>
            <span>本科薪资 <strong>{{ major.salaryAvg ? major.salaryAvg + ' 元/月' : '—' }}</strong></span>
          </div>
        </div>

        <div class="detail-section">
          <h3>专业介绍</h3>
          <p>{{ major.description || '暂无介绍' }}</p>
          <p v-if="major.courseList" class="course-line">
            <strong>核心课程：</strong>{{ major.courseList }}
          </p>
        </div>

        <div v-if="major.graduateDestinations" class="detail-section">
          <h3>毕业生去向</h3>
          <p>{{ major.graduateDestinations }}</p>
        </div>

        <div v-if="major.typicalEmployers?.length" class="detail-section">
          <h3>典型就业单位</h3>
          <div class="tag-row">
            <el-tag v-for="(c, i) in major.typicalEmployers" :key="i" effect="plain" type="info">{{ c }}</el-tag>
          </div>
        </div>

        <div v-if="major.typicalCareers?.length" class="detail-section">
          <h3>未来从事职业</h3>
          <div class="tag-row">
            <el-tag v-for="(c, i) in major.typicalCareers" :key="i" effect="plain" type="success">{{ c }}</el-tag>
          </div>
        </div>

        <div v-if="major.careerDirection" class="detail-section">
          <h3>就业方向概述</h3>
          <p>{{ major.careerDirection }}</p>
        </div>

        <p class="data-note">以上就业单位与职业方向为平台整理的参考样本，非官方统计，请以院校就业质量报告为准。</p>

        <div class="detail-actions">
          <el-button type="primary" round @click="askAI">
            <el-icon><ChatDotRound /></el-icon>
            向 AI 咨询该专业
          </el-button>
          <el-button round @click="goCompare">加入对比</el-button>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="专业不存在" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { fetchMajorDetail } from '../api/major'

const route = useRoute()
const router = useRouter()

const majorCode = computed(() => route.params.code)
const loading = ref(false)
const major = ref(null)

function askAI() {
  router.push(`/chat?q=介绍一下${major.value?.majorName || '该专业'}的就业前景、毕业生去向和适合什么样的考生`)
}

function goCompare() {
  router.push({
    path: '/compare',
    query: { type: 'major', codes: majorCode.value },
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetchMajorDetail(majorCode.value)
    major.value = res.data
  } catch {
    major.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f8fafc;
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
}

.detail-body {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

.major-hero {
  background: linear-gradient(135deg, #312e81, #4c1d95);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 24px;
  color: #fff;
}

.major-hero h1 {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 8px;
}

.major-category {
  color: #c4b5fd;
  margin-bottom: 16px;
}

.stat-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 0.9rem;
  color: #e9d5ff;
}

.stat-row strong {
  color: #fff;
  font-size: 1.1rem;
}

.detail-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 20px;
}

.detail-section h3 {
  font-weight: 700;
  margin-bottom: 12px;
  color: #0f172a;
}

.detail-section p {
  color: #64748b;
  line-height: 1.8;
}

.course-line {
  margin-top: 12px;
  font-size: 0.9rem;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.data-note {
  font-size: 0.75rem;
  color: #94a3b8;
  margin: -8px 0 20px;
  line-height: 1.6;
}

.detail-actions {
  display: flex;
  gap: 12px;
}
</style>
