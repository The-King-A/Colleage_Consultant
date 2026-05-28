<template>
  <div class="insights-page">
    <div class="page-head">
      <div>
        <h1>分数线洞察</h1>
        <p class="sub">Python 分析服务 · 查看录取样本分布，理解冲稳保为何扎堆或冲档为空</p>
      </div>
      <el-tag :type="serviceOk ? 'success' : 'warning'" effect="plain">
        {{ serviceOk ? '分析服务在线' : '分析服务离线' }}
      </el-tag>
    </div>

    <div class="form-card">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="录取生源省">
          <el-select v-model="form.scoreProvince" filterable style="width: 130px">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="院校所在地（可选）">
          <el-select v-model="form.schoolProvince" clearable filterable style="width: 130px">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="科类">
          <el-select v-model="form.subjectType" style="width: 110px">
            <el-option label="理科" value="science" />
            <el-option label="文科" value="liberal" />
          </el-select>
        </el-form-item>
        <el-form-item label="你的分数（可选）">
          <el-input-number v-model="form.score" :min="200" :max="750" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="load">分析分布</el-button>
          <router-link to="/matcher" class="link-btn">去冲稳保测算 →</router-link>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="data" v-loading="loading" class="content">
      <div class="stat-row">
        <div class="stat"><b>{{ data.sampleCount }}</b><span>样本院校数</span></div>
        <div v-if="data.stats" class="stat"><b>{{ data.stats.min }}–{{ data.stats.max }}</b><span>最低分区间</span></div>
        <div v-if="data.stats" class="stat"><b>{{ data.stats.median }}</b><span>中位最低分</span></div>
        <div v-if="data.stats" class="stat"><b>{{ data.stats.spread }}</b><span>跨度（分）</span></div>
        <div v-if="data.userPosition" class="stat highlight">
          <b>{{ data.userPosition.percentile }}%</b><span>超过样本比例</span>
        </div>
      </div>

      <ul v-if="data.tips?.length" class="tips">
        <li v-for="(t, i) in data.tips" :key="i">{{ t }}</li>
      </ul>

      <div ref="chartRef" class="chart" />

      <div v-if="hollandCode" class="holland-box">
        <h3>霍兰德 {{ hollandCode }} · Python 专业门类映射</h3>
        <div class="chips">
          <span v-for="r in hollandRecs" :key="r.category" class="chip">{{ r.category }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { fetchScoreInsights, fetchHollandMajorMap, checkAnalyticsHealth } from '../api/analytics'
import { CHINA_PROVINCES } from '../utils/provinces'

const route = useRoute()
const provinces = CHINA_PROVINCES
const form = reactive({
  scoreProvince: '北京',
  schoolProvince: '',
  subjectType: 'science',
  score: 550,
})
const loading = ref(false)
const serviceOk = ref(false)
const data = ref(null)
const chartRef = ref(null)
const hollandCode = ref(route.query.holland || '')
const hollandRecs = ref([])
let chart = null

onMounted(async () => {
  serviceOk.value = await checkAnalyticsHealth()
  if (route.query.scoreProvince) form.scoreProvince = route.query.scoreProvince
  if (route.query.schoolProvince) form.schoolProvince = route.query.schoolProvince
  if (route.query.score) form.score = Number(route.query.score)
  if (serviceOk.value) load()
  if (hollandCode.value) loadHolland()
})

onBeforeUnmount(() => {
  chart?.dispose()
})

async function loadHolland() {
  try {
    const r = await fetchHollandMajorMap(hollandCode.value)
    hollandRecs.value = r.recommendations || []
  } catch { /* optional */ }
}

async function load() {
  loading.value = true
  try {
    data.value = await fetchScoreInsights({
      scoreProvince: form.scoreProvince,
      schoolProvince: form.schoolProvince || undefined,
      subjectType: form.subjectType,
      score: form.score,
    })
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error(e.message || '分析失败')
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || !data.value?.histogram?.length) return
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  const hist = data.value.histogram
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, bottom: 40, top: 24 },
    xAxis: {
      type: 'category',
      data: hist.map((h) => `${h.from}-${h.to}`),
      name: '最低分区间',
    },
    yAxis: { type: 'value', name: '院校数' },
    series: [{
      type: 'bar',
      data: hist.map((h) => h.count),
      itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] },
    }],
  })
}
</script>

<style scoped>
.insights-page { max-width: 960px; margin: 0 auto; padding: 24px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.sub { color: #64748b; font-size: 0.9rem; margin-top: 6px; }
.form-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; margin-bottom: 24px; }
.link-btn { margin-left: 12px; color: #6366f1; font-size: 0.9rem; text-decoration: none; }
.stat-row { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.stat { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 12px 20px; min-width: 120px; }
.stat b { display: block; font-size: 1.25rem; color: #1e293b; }
.stat span { font-size: 0.75rem; color: #94a3b8; }
.stat.highlight b { color: #6366f1; }
.tips { background: #f8fafc; border-radius: 12px; padding: 12px 20px; margin-bottom: 16px; color: #475569; font-size: 0.9rem; }
.chart { height: 320px; background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 12px; }
.holland-box { margin-top: 24px; padding: 16px; background: #faf5ff; border-radius: 12px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.chip { background: #fff; border: 1px solid #e9d5ff; padding: 4px 12px; border-radius: 8px; font-size: 0.85rem; }
</style>
