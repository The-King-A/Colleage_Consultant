<template>
  <div class="dash-page">
    <div class="page-head">
      <h1>数据看板</h1>
      <p>平台院校、专业、录取样本数据一览 · ECharts 可视化</p>
    </div>

    <div v-loading="loading" class="stat-cards">
      <div v-for="c in cards" :key="c.label" class="stat-card">
        <span class="stat-val">{{ c.value }}</span>
        <span class="stat-label">{{ c.label }}</span>
      </div>
    </div>

    <div class="charts">
      <div class="chart-card">
        <h3>专业热度 TOP（2024 样本）</h3>
        <div ref="hotChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <h3>院校所在地分布（样本）</h3>
        <div ref="provChartRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '../api/stats'

const loading = ref(false)
const cards = ref([])
const hotChartRef = ref(null)
const provChartRef = ref(null)
let hotChart = null
let provChart = null

async function load() {
  loading.value = true
  try {
    const res = await fetchDashboardStats()
    const d = res.data || {}
    cards.value = [
      { label: '院校总数', value: d.schoolCount ?? 0 },
      { label: '专业总数', value: d.majorCount ?? 0 },
      { label: '录取记录', value: d.admissionRecordCount ?? 0 },
      { label: '覆盖生源省', value: d.provinceWithScoreCount ?? 0 },
    ]
    await nextTick()
    renderHot(d.topHotMajors || [])
    renderProv(d.schoolsByProvince || [])
  } finally {
    loading.value = false
  }
}

function renderHot(rows) {
  if (!hotChartRef.value) return
  hotChart?.dispose()
  hotChart = echarts.init(hotChartRef.value)
  hotChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, bottom: 48, top: 24 },
    xAxis: { type: 'category', data: rows.map((r) => r.name), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '热度' },
    series: [{ type: 'bar', data: rows.map((r) => r.hotScore), itemStyle: { color: '#6366f1' } }],
  })
}

function renderProv(rows) {
  if (!provChartRef.value) return
  provChart?.dispose()
  provChart = echarts.init(provChartRef.value)
  provChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      data: rows.map((r) => ({ name: r.province, value: r.cnt })),
    }],
  })
}

function onResize() {
  hotChart?.resize()
  provChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  hotChart?.dispose()
  provChart?.dispose()
})
</script>

<style scoped>
.dash-page { max-width: 1100px; margin: 0 auto; padding: 24px; }
.page-head { margin-bottom: 24px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.page-head p { color: #64748b; margin-top: 6px; }
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; border-radius: 16px; padding: 24px; text-align: center; }
.stat-val { display: block; font-size: 2rem; font-weight: 800; }
.stat-label { font-size: 0.85rem; opacity: 0.9; }
.charts { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.chart-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; }
.chart-card h3 { font-size: 0.95rem; margin-bottom: 12px; color: #334155; }
.chart { height: 320px; }
@media (max-width: 768px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .charts { grid-template-columns: 1fr; }
}
</style>
