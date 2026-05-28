<template>
  <div class="rank-page">
    <div class="page-head">
      <h1>位次换算</h1>
      <p class="sub">Java 一分一段查表 · 累计位次与等效往年分（样本数据，仅供参考）</p>
    </div>

    <div class="form-card">
      <el-form :inline="true" @submit.prevent="lookup">
        <el-form-item label="生源省">
          <el-select v-model="form.province" filterable style="width: 130px">
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
          <el-button type="primary" :loading="loading" @click="lookup">查询位次</el-button>
          <router-link class="link" :to="matcherLink">去冲稳保测算 →</router-link>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="result" class="result-card">
      <p v-if="result.tip" class="tip">{{ result.tip }}</p>
      <div v-if="result.cumulativeRank" class="stats">
        <div class="stat main">
          <b>{{ result.cumulativeRank }}</b>
          <span>累计位次（约）</span>
        </div>
        <div v-if="result.beatPercent != null" class="stat">
          <b>{{ result.beatPercent }}%</b>
          <span>约超过同省考生</span>
        </div>
        <div class="stat">
          <b>{{ result.matchedScore }}</b>
          <span>查表分数</span>
        </div>
      </div>
      <div v-if="result.equivalents?.length" class="equiv">
        <h3>等效往年分（按位次接近）</h3>
        <el-table :data="result.equivalents" size="small" stripe>
          <el-table-column prop="year" label="年份" width="80" />
          <el-table-column prop="equivalentScore" label="等效分" width="90" />
          <el-table-column prop="cumulativeRankAtYear" label="该年位次" width="100" />
          <el-table-column prop="note" label="说明" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { lookupScoreRank } from '../api/rank'
import { completeWizardStep } from '../api/wizard'
import { useAuthStore } from '../stores/auth'
import { CHINA_PROVINCES } from '../utils/provinces'

const authStore = useAuthStore()

const provinces = CHINA_PROVINCES
const form = reactive({
  province: '北京',
  subjectType: 'science',
  score: 550,
})
const loading = ref(false)
const result = ref(null)

const matcherLink = computed(() => ({
  path: '/matcher',
  query: {
    scoreProvince: form.province,
    score: form.score,
  },
}))

async function lookup() {
  loading.value = true
  try {
    const res = await lookupScoreRank({
      province: form.province,
      subjectType: form.subjectType,
      score: form.score,
    })
    result.value = res.data
    if (authStore.isLoggedIn) {
      completeWizardStep({
        step: 'ENTER_SCORE',
        userScore: form.score,
        scoreProvince: form.province,
        subjectType: form.subjectType,
      }).catch(() => {})
    }
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.rank-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.sub { color: #64748b; font-size: 0.9rem; margin-top: 6px; }
.form-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; margin: 20px 0; }
.link { margin-left: 12px; color: #6366f1; text-decoration: none; font-size: 0.9rem; }
.result-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 24px; }
.tip { color: #475569; line-height: 1.7; margin-bottom: 16px; }
.stats { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; }
.stat { background: #f8fafc; padding: 16px 24px; border-radius: 12px; min-width: 120px; }
.stat b { display: block; font-size: 1.5rem; color: #6366f1; }
.stat span { font-size: 0.75rem; color: #94a3b8; }
.stat.main b { font-size: 2rem; }
.equiv h3 { font-size: 1rem; margin-bottom: 12px; color: #334155; }
</style>
