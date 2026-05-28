<template>
  <div class="compare-page">
    <div class="page-head">
      <div>
        <h1>数据对比</h1>
        <p class="disclaimer">院校录取支持多生源省近五年（2021-2025）样本数据；专业对比含热度与门类就业趋势。仅供参考，以官方发布为准</p>
      </div>
      <el-button v-if="hasResult && isSchool && schoolItems.length >= 2" round :disabled="!reportOk" @click="exportSchoolReport">
        导出对比表 (Node)
      </el-button>
      <el-button v-if="hasResult" round @click="clearAll">清空选择</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="院校对比" name="school" />
      <el-tab-pane label="专业对比" name="major" />
    </el-tabs>

    <!-- ========== 院校对比 ========== -->
    <template v-if="isSchool">
      <div class="toolbar card">
        <div class="toolbar-row filters-row">
          <div class="filter-item">
            <span class="filter-label">所在省份</span>
            <el-select
              v-model="schoolLocation"
              filterable
              clearable
              placeholder="如：北京"
              style="width: 130px"
              @change="onSchoolLocationChange"
            >
              <el-option v-for="loc in schoolLocations" :key="loc" :label="loc" :value="loc" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">录取生源省</span>
            <el-select v-model="scoreProvince" filterable placeholder="看分省份" style="width: 130px">
              <el-option v-for="p in scoreProvinces" :key="p" :label="p" :value="p" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">科类</span>
            <el-select v-model="subjectType" style="width: 100px">
              <el-option label="理科" value="science" />
              <el-option label="文科" value="liberal" />
            </el-select>
          </div>
        </div>
        <CompareEntitySearch
          ref="schoolSearchRef"
          v-model="schoolSelectedList"
          require-location
          :location-filter="schoolLocation"
          placeholder="可选：再输入校名缩小范围（不填则列出该省全部高校）"
          :hint="schoolSearchHint"
          browse-path="/schools"
          :fetch-fn="searchSchools"
          @change="onSchoolPickChange"
        />
        <div class="toolbar-actions">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="schoolSelectedList.length < 2"
            @click="runSchoolCompare"
          >
            开始对比（已选 {{ schoolSelectedList.length }}/4）
          </el-button>
        </div>
        <div class="preset-row">
          <span class="preset-label">快捷对比：</span>
          <el-button
            v-for="p in schoolPresets"
            :key="p.name"
            size="small"
            round
            @click="applySchoolPreset(p)"
          >
            {{ p.name }}
          </el-button>
        </div>
      </div>

      <div v-if="schoolSelectedList.length" class="selected-chips">
        <div v-for="s in schoolSelectedList" :key="s.schoolCode" class="chip-card">
          <div class="chip-main">
            <strong>{{ s.schoolName }}</strong>
            <span class="chip-loc">{{ s.location }} · {{ s.city }}</span>
          </div>
          <el-button text type="danger" @click="removeSchool(s.schoolCode)">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <div v-loading="loading" class="result-area">
        <template v-if="schoolItems.length >= 2">
          <div class="summary-grid">
            <div
              v-for="item in schoolItems"
              :key="item.schoolCode"
              class="summary-card"
            >
              <h3>{{ item.schoolName }}</h3>
              <div class="tag-row">
                <span v-if="item.is985" class="tag t985">985</span>
                <span v-if="item.is211" class="tag t211">211</span>
                <span v-if="item.isDoubleFirst" class="tag tdf">双一流</span>
              </div>
              <p v-if="item.latestMinScore" class="highlight">
                {{ item.latestYear }}年最低分 <em>{{ item.latestMinScore }}</em>
                <span v-if="item.latestMinRank"> / 位次 {{ item.latestMinRank }}</span>
              </p>
              <p v-else class="muted">暂无该省录取数据</p>
              <p v-if="item.latestOverLineScore != null" class="over-line">
                超省控线
                <span :class="item.latestOverLineScore >= 0 ? 'up' : 'down'">
                  {{ item.latestOverLineScore > 0 ? '+' : '' }}{{ item.latestOverLineScore }}
                </span>
              </p>
              <div class="card-actions">
                <el-button size="small" text type="primary" @click="goSchool(item)">详情</el-button>
                <el-button size="small" text @click="askSchoolAI(item)">问 AI</el-button>
              </div>
            </div>
          </div>

          <div class="charts-row">
            <div ref="scoreChartRef" class="chart-box half" />
            <div ref="rankChartRef" class="chart-box half" />
          </div>
          <div ref="overLineChartRef" class="chart-box" />

          <h3 class="section-title">近五年录取分数线明细（{{ scoreProvince }}）</h3>
          <el-table :data="schoolScoreTableRows" border stripe class="compare-table" max-height="480">
            <el-table-column prop="label" label="年份 / 指标" width="120" fixed />
            <el-table-column
              v-for="col in schoolItems"
              :key="col.schoolCode"
              :label="col.schoolName"
              min-width="160"
            >
              <template #default="{ row }">
                <span :class="{ highlight: row.highlight }">{{ row.values[col.schoolCode] ?? '—' }}</span>
              </template>
            </el-table-column>
          </el-table>

          <h3 class="section-title">基本信息</h3>
          <el-table :data="schoolBaseTableRows" border stripe class="compare-table">
            <el-table-column prop="label" label="对比项" width="120" fixed />
            <el-table-column
              v-for="col in schoolItems"
              :key="col.schoolCode"
              :label="col.schoolName"
              min-width="160"
            >
              <template #default="{ row }">
                {{ row.values[col.schoolCode] ?? '—' }}
              </template>
            </el-table-column>
          </el-table>
        </template>

        <el-empty
          v-else-if="!loading"
          :description="emptySchoolHint"
        >
          <el-button type="primary" @click="applySchoolPreset(schoolPresets[0])">试试「陕西名校」</el-button>
        </el-empty>
      </div>
    </template>

    <!-- ========== 专业对比 ========== -->
    <template v-else>
      <div class="toolbar card">
        <CompareEntitySearch
          v-model="majorSelectedList"
          placeholder="输入专业名称，如：计算机、人工智能、会计"
          hint="支持专业名、门类、代码模糊搜索"
          browse-path="/majors"
          :fetch-fn="searchMajors"
          :get-title="(m) => m.majorName"
          :get-meta="(m) => `${m.majorCategory} · ${m.majorSubcategory || ''}`"
          @change="onMajorPickChange"
        />
        <div class="toolbar-actions">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="majorSelectedList.length < 2"
            @click="runMajorCompare"
          >
            开始对比（已选 {{ majorSelectedList.length }}/4）
          </el-button>
        </div>
        <div class="preset-row">
          <span class="preset-label">快捷对比：</span>
          <el-button
            v-for="p in majorPresets"
            :key="p.name"
            size="small"
            round
            @click="applyMajorPreset(p)"
          >
            {{ p.name }}
          </el-button>
        </div>
      </div>

      <div v-if="majorSelectedList.length" class="selected-chips">
        <div v-for="m in majorSelectedList" :key="m.majorCode" class="chip-card major-chip">
          <div class="chip-main">
            <strong>{{ m.majorName }}</strong>
            <span class="chip-loc">{{ m.majorCategory }}</span>
          </div>
          <el-button text type="danger" @click="removeMajor(m.majorCode)">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <div v-loading="loading" class="result-area">
        <template v-if="majorItems.length >= 2">
          <div class="summary-grid">
            <div v-for="item in majorItems" :key="item.majorCode" class="summary-card major-summary">
              <h3>{{ item.majorName }}</h3>
              <p class="chip-loc">{{ item.majorCategory }} · {{ item.majorSubcategory }}</p>
              <ul class="stat-list">
                <li>热度 <em>{{ item.hotIndex ?? '—' }}</em></li>
                <li>就业率 <em>{{ item.employmentRate != null ? item.employmentRate + '%' : '—' }}</em></li>
                <li>本科薪资 <em>{{ item.salaryAvg ? item.salaryAvg + ' 元' : '—' }}</em></li>
              </ul>
              <div class="card-actions">
                <el-button size="small" text type="primary" @click="goMajor(item)">详情</el-button>
                <el-button size="small" text @click="askMajorAI(item)">问 AI</el-button>
              </div>
            </div>
          </div>

          <div class="charts-row">
            <div ref="majorBarChartRef" class="chart-box half" />
            <div ref="majorRadarChartRef" class="chart-box half" />
          </div>
          <div class="charts-row">
            <div ref="majorHotChartRef" class="chart-box half" />
            <div ref="majorEmpChartRef" class="chart-box half" />
          </div>

          <h3 class="section-title">指标对比</h3>
          <el-table :data="majorTableRows" border stripe class="compare-table">
            <el-table-column prop="label" label="对比项" width="120" fixed />
            <el-table-column
              v-for="col in majorItems"
              :key="col.majorCode"
              :label="col.majorName"
              min-width="150"
            >
              <template #default="{ row }">
                {{ row.values[col.majorCode] ?? '—' }}
              </template>
            </el-table-column>
          </el-table>

          <h3 class="section-title">近五年报考热度</h3>
          <el-table :data="majorHotTableRows" border stripe class="compare-table" max-height="320">
            <el-table-column prop="label" label="年份 / 指标" width="120" fixed />
            <el-table-column
              v-for="col in majorItems"
              :key="col.majorCode"
              :label="col.majorName"
              min-width="140"
            >
              <template #default="{ row }">
                {{ row.values[col.majorCode] ?? '—' }}
              </template>
            </el-table-column>
          </el-table>

          <h3 class="section-title">专业介绍与就业去向</h3>
          <div class="career-grid">
            <div v-for="item in majorItems" :key="item.majorCode" class="career-card">
              <h4>{{ item.majorName }}</h4>

              <div class="career-block">
                <h5>专业介绍</h5>
                <p>{{ item.description || '暂无介绍' }}</p>
                <p v-if="item.courseList" class="sub-line"><span>核心课程</span>{{ item.courseList }}</p>
              </div>

              <div v-if="item.graduateDestinations" class="career-block">
                <h5>毕业生去向</h5>
                <p>{{ item.graduateDestinations }}</p>
              </div>

              <div v-if="item.typicalEmployers?.length" class="career-block">
                <h5>典型就业单位</h5>
                <div class="tag-row">
                  <el-tag
                    v-for="(c, i) in item.typicalEmployers"
                    :key="`${item.majorCode}-e-${i}`"
                    size="small"
                    effect="plain"
                    type="info"
                  >
                    {{ c }}
                  </el-tag>
                </div>
              </div>

              <div v-if="item.typicalCareers?.length" class="career-block">
                <h5>未来从事职业</h5>
                <div class="tag-row">
                  <el-tag
                    v-for="(c, i) in item.typicalCareers"
                    :key="`${item.majorCode}-j-${i}`"
                    size="small"
                    effect="plain"
                    type="success"
                  >
                    {{ c }}
                  </el-tag>
                </div>
              </div>

              <div v-if="item.careerDirection" class="career-block">
                <h5>就业方向概述</h5>
                <p>{{ item.careerDirection }}</p>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!loading" :description="emptyMajorHint">
          <el-button type="primary" @click="applyMajorPreset(majorPresets[0])">试试「计算机类」</el-button>
        </el-empty>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import CompareEntitySearch from '../components/data/CompareEntitySearch.vue'
import { fetchSchoolCompare, fetchSchoolOptions, fetchScoreProvinces, fetchSchoolLocations } from '../api/school'
import { fetchMajorCompare, fetchMajorOptions } from '../api/major'
import { generateSchoolCompareReport, checkReportHealth } from '../api/report'
import { downloadHtml } from '../utils/downloadHtml'
import { sortProvinces } from '../utils/provinces'

const route = useRoute()
const router = useRouter()

const COMPARE_YEARS = [2021, 2022, 2023, 2024, 2025]

const schoolPresets = [
  { name: '陕西名校', codes: ['10698', '10699', '10701'] },
  { name: '华南强校', codes: ['10558', '10561', '10590'] },
  { name: '河南本地', codes: ['10020', '10021', '10698'] },
]

const majorPresets = [
  { name: '计算机类', codes: ['080901', '080902', '080717T'] },
  { name: '工科热门', codes: ['080901', '080701', '080601'] },
  { name: '文理兼备', codes: ['080901', '020301K', '030101K'] },
]

const activeTab = ref(route.query.type === 'major' ? 'major' : 'school')
const isSchool = computed(() => activeTab.value === 'school')
const loading = ref(false)
const reportOk = ref(false)

const scoreProvinces = ref(['河南'])
const schoolLocations = ref([])
const schoolLocation = ref(route.query.schoolLocation || '')
const scoreProvince = ref(route.query.scoreProvince || '河南')
const subjectType = ref(route.query.subjectType || 'science')

const schoolSearchHint = computed(() => {
  if (schoolLocation.value) {
    return `已选所在地「${schoolLocation.value}」，直接点「搜索」即可列出该省高校；「录取生源省」仅影响分数线显示`
  }
  return '请先选择「院校所在地」（如天津），再点搜索；或输入校名模糊查找'
})

const schoolSelectedList = ref([])
const majorSelectedList = ref([])

const selectedCodes = computed(() => schoolSelectedList.value.map((s) => s.schoolCode))
const selectedMajorCodes = computed(() => majorSelectedList.value.map((m) => m.majorCode))
const schoolItems = ref([])
const majorItems = ref([])

const scoreChartRef = ref(null)
const rankChartRef = ref(null)
const overLineChartRef = ref(null)
const majorBarChartRef = ref(null)
const majorRadarChartRef = ref(null)
const majorHotChartRef = ref(null)
const majorEmpChartRef = ref(null)
const schoolSearchRef = ref(null)

let scoreChart = null
let rankChart = null
let overLineChart = null
let majorBarChart = null
let majorRadarChart = null
let majorHotChart = null
let majorEmpChart = null

const hasResult = computed(() =>
  (isSchool.value && schoolItems.value.length >= 2) ||
  (!isSchool.value && majorItems.value.length >= 2)
)

const emptySchoolHint = computed(() =>
  schoolSelectedList.value.length < 2
    ? '请至少选择 2 所院校，然后点击「开始对比」'
    : '暂无对比数据，请检查所选院校是否有该省录取记录'
)

const emptyMajorHint = computed(() =>
  majorSelectedList.value.length < 2
    ? '请至少选择 2 个专业，然后点击「开始对比」'
    : '暂无对比数据'
)

const schoolBaseTableRows = computed(() => {
  if (!schoolItems.value.length) return []
  const defs = [
    { label: '所在地', key: 'location' },
    { label: '城市', key: 'city' },
    { label: '院校类型', key: 'schoolType' },
    { label: '985', key: 'is985', bool: true },
    { label: '211', key: 'is211', bool: true },
    { label: '双一流', key: 'isDoubleFirst', bool: true },
  ]
  return defs.map((d) => ({
    label: d.label,
    values: Object.fromEntries(
      schoolItems.value.map((s) => {
        let v = s[d.key]
        if (d.bool) v = v ? '是' : '否'
        return [s.schoolCode, v ?? '—']
      })
    ),
  }))
})

const schoolScoreTableRows = computed(() => {
  if (!schoolItems.value.length) return []
  const rows = []
  for (const year of COMPARE_YEARS) {
    rows.push({
      label: `${year} 最低分`,
      highlight: true,
      values: Object.fromEntries(
        schoolItems.value.map((s) => {
          const sc = (s.scores || []).find((x) => x.year === year)
          return [s.schoolCode, sc?.minScore ?? '—']
        })
      ),
    })
    rows.push({
      label: `${year} 位次`,
      values: Object.fromEntries(
        schoolItems.value.map((s) => {
          const sc = (s.scores || []).find((x) => x.year === year)
          return [s.schoolCode, sc?.minRank ?? '—']
        })
      ),
    })
    rows.push({
      label: `${year} 超线分`,
      values: Object.fromEntries(
        schoolItems.value.map((s) => {
          const sc = (s.scores || []).find((x) => x.year === year)
          if (sc?.overLineScore == null) return [s.schoolCode, '—']
          const v = sc.overLineScore
          return [s.schoolCode, (v > 0 ? '+' : '') + v]
        })
      ),
    })
  }
  return rows
})

const majorTableRows = computed(() => {
  const defs = [
    { label: '学科门类', key: 'majorCategory' },
    { label: '专业类', key: 'majorSubcategory' },
    { label: '学位', key: 'degreeType' },
    { label: '学制(年)', key: 'studyDuration' },
    { label: '学习难度', key: 'difficultyLevel' },
    { label: '热度指数', key: 'hotIndex' },
    { label: '就业率(%)', key: 'employmentRate' },
    { label: '本科薪资(元)', key: 'salaryAvg' },
    { label: '五年薪资(元)', key: 'salary5year' },
  ]
  const rows = defs.map((d) => ({
    label: d.label,
    values: Object.fromEntries(
      majorItems.value.map((m) => [m.majorCode, m[d.key] ?? '—'])
    ),
  }))
  rows.push({
    label: '典型单位（节选）',
    values: Object.fromEntries(
      majorItems.value.map((m) => [
        m.majorCode,
        (m.typicalEmployers || []).slice(0, 4).join('、') || '—',
      ])
    ),
  })
  rows.push({
    label: '典型职业（节选）',
    values: Object.fromEntries(
      majorItems.value.map((m) => [
        m.majorCode,
        (m.typicalCareers || []).slice(0, 4).join('、') || '—',
      ])
    ),
  })
  return rows
})

const majorHotTableRows = computed(() => {
  if (!majorItems.value.length) return []
  const rows = []
  for (const year of COMPARE_YEARS) {
    rows.push({
      label: `${year} 热度分`,
      values: Object.fromEntries(
        majorItems.value.map((m) => {
          const t = (m.hotTrends || []).find((x) => x.year === year)
          return [m.majorCode, t?.hotScore ?? '—']
        })
      ),
    })
    rows.push({
      label: `${year} 热度排名`,
      values: Object.fromEntries(
        majorItems.value.map((m) => {
          const t = (m.hotTrends || []).find((x) => x.year === year)
          return [m.majorCode, t?.rank ?? '—']
        })
      ),
    })
  }
  return rows
})

function parseCodes(raw) {
  if (!raw) return []
  return raw.toString().split(',').map((s) => s.trim()).filter(Boolean).slice(0, 4)
}

function syncUrl() {
  const query = { type: activeTab.value }
  if (isSchool.value && selectedCodes.value.length) {
    query.codes = selectedCodes.value.join(',')
    if (schoolLocation.value) query.schoolLocation = schoolLocation.value
    query.scoreProvince = scoreProvince.value
    query.subjectType = subjectType.value
  } else if (!isSchool.value && selectedMajorCodes.value.length) {
    query.codes = selectedMajorCodes.value.join(',')
  }
  router.replace({ query })
}

async function searchSchools({ keyword, location }) {
  if (!location) {
    return []
  }
  const res = await fetchSchoolOptions({
    keyword: keyword || undefined,
    location,
    scoreProvince: scoreProvince.value,
    subjectType: subjectType.value,
    limit: 50,
  })
  let list = res.data || []
  // 前端二次校验，防止接口未带 location 时混入外地院校
  list = list.filter((s) => s.location === location)
  return list
}

async function searchMajors({ keyword }) {
  const res = await fetchMajorOptions({ keyword: keyword || undefined, limit: 40 })
  return res.data || []
}

function onSchoolLocationChange() {
  schoolSearchRef.value?.reload()
}

async function loadSchoolsByCodes(codes) {
  if (!codes.length) {
    schoolSelectedList.value = []
    return
  }
  const res = await fetchSchoolOptions({ codes: codes.join(','), limit: 10 })
  schoolSelectedList.value = res.data || []
}

async function loadMajorsByCodes(codes) {
  if (!codes.length) {
    majorSelectedList.value = []
    return
  }
  const res = await fetchMajorOptions({ codes: codes.join(','), limit: 10 })
  majorSelectedList.value = res.data || []
}

function onSchoolPickChange(list) {
  if (list.length >= 2 && list.length === schoolSelectedList.value.length) {
    syncUrl()
  }
}

function onMajorPickChange(list) {
  if (list.length >= 2) {
    syncUrl()
  }
}

function onTabChange() {
  schoolItems.value = []
  majorItems.value = []
  syncUrl()
}

function removeSchool(code) {
  schoolSelectedList.value = schoolSelectedList.value.filter((s) => s.schoolCode !== code)
  if (schoolSelectedList.value.length < 2) schoolItems.value = []
  syncUrl()
}

function removeMajor(code) {
  majorSelectedList.value = majorSelectedList.value.filter((m) => m.majorCode !== code)
  if (majorSelectedList.value.length < 2) majorItems.value = []
  syncUrl()
}

function clearAll() {
  if (isSchool.value) {
    schoolSelectedList.value = []
    schoolItems.value = []
  } else {
    majorSelectedList.value = []
    majorItems.value = []
  }
  syncUrl()
  disposeCharts()
}

async function applySchoolPreset(preset) {
  await loadSchoolsByCodes(preset.codes)
  runSchoolCompare()
}

async function applyMajorPreset(preset) {
  await loadMajorsByCodes(preset.codes)
  runMajorCompare()
}

function goSchool(item) {
  router.push({ path: `/school/${item.schoolCode}`, query: { name: item.schoolName } })
}

function goMajor(item) {
  router.push({
    path: `/major/${item.majorCode}`,
    query: { name: item.majorName, category: item.majorCategory },
  })
}

function askSchoolAI(item) {
  router.push(`/chat?q=${encodeURIComponent(`对比分析 ${item.schoolName} 的优劣势和适合什么样的考生`)}`)
}

function askMajorAI(item) {
  router.push(`/chat?q=${encodeURIComponent(`对比 ${item.majorName} 与其他相近专业的就业前景`)}`)
}

function disposeCharts() {
  scoreChart?.dispose()
  rankChart?.dispose()
  overLineChart?.dispose()
  majorBarChart?.dispose()
  majorRadarChart?.dispose()
  majorHotChart?.dispose()
  majorEmpChart?.dispose()
  scoreChart = rankChart = overLineChart = majorBarChart = majorRadarChart = majorHotChart = majorEmpChart = null
}

function initChart(el, instance) {
  if (!el) return null
  if (instance) instance.dispose()
  return echarts.init(el)
}

function renderSchoolCharts() {
  if (!schoolItems.value.length) return

  scoreChart = initChart(scoreChartRef.value, scoreChart)
  rankChart = initChart(rankChartRef.value, rankChart)
  overLineChart = initChart(overLineChartRef.value, overLineChart)

  const names = schoolItems.value.map((s) => s.schoolName)
  const yearLabels = COMPARE_YEARS.map(String)
  const subjectLabel = subjectType.value === 'science' ? '理科' : '文科'

  scoreChart?.setOption({
    title: { text: `${scoreProvince.value} ${subjectLabel} · 最低分`, left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, type: 'scroll' },
    grid: { left: 50, right: 20, top: 40, bottom: 56 },
    xAxis: { type: 'category', data: yearLabels },
    yAxis: { type: 'value', name: '分', scale: true },
    series: schoolItems.value.map((s) => ({
      name: s.schoolName,
      type: 'line',
      smooth: true,
      connectNulls: false,
      data: COMPARE_YEARS.map((y) => (s.scores || []).find((x) => x.year === y)?.minScore ?? null),
    })),
  })

  rankChart?.setOption({
    title: { text: '最低位次趋势（数值越小越好）', left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, type: 'scroll' },
    grid: { left: 56, right: 20, top: 40, bottom: 56 },
    xAxis: { type: 'category', data: yearLabels },
    yAxis: { type: 'value', name: '位次', inverse: true },
    series: schoolItems.value.map((s) => ({
      name: s.schoolName,
      type: 'line',
      smooth: true,
      data: COMPARE_YEARS.map((y) => (s.scores || []).find((x) => x.year === y)?.minRank ?? null),
    })),
  })

  const latestYear = COMPARE_YEARS[COMPARE_YEARS.length - 1]
  overLineChart?.setOption({
    title: { text: `${latestYear} 年超省控线分`, left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 40, bottom: 32 },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: names.length > 3 ? 15 : 0 } },
    yAxis: { type: 'value', name: '分' },
    series: [{
      type: 'bar',
      data: schoolItems.value.map((s) => {
        const sc = (s.scores || []).find((x) => x.year === latestYear)
        return sc?.overLineScore ?? 0
      }),
      itemStyle: {
        color: (p) => (p.value >= 0 ? '#6366f1' : '#ef4444'),
      },
      label: { show: true, position: 'top' },
    }],
  })
}

function renderMajorCharts() {
  if (!majorItems.value.length) return

  majorBarChart = initChart(majorBarChartRef.value, majorBarChart)
  majorRadarChart = initChart(majorRadarChartRef.value, majorRadarChart)

  const names = majorItems.value.map((m) => m.majorName)

  majorBarChart?.setOption({
    title: { text: '热度与薪资', left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 50, right: 50, top: 40, bottom: 48 },
    xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate: names.length > 2 ? 12 : 0 } },
    yAxis: [{ type: 'value', name: '热度' }, { type: 'value', name: '薪资' }],
    series: [
      { name: '热度', type: 'bar', data: majorItems.value.map((m) => m.hotIndex), itemStyle: { color: '#6366f1' } },
      { name: '薪资', type: 'bar', yAxisIndex: 1, data: majorItems.value.map((m) => m.salaryAvg), itemStyle: { color: '#22c55e' } },
    ],
  })

  const maxHot = Math.max(...majorItems.value.map((m) => m.hotIndex || 0), 1)
  const maxEmp = Math.max(...majorItems.value.map((m) => Number(m.employmentRate) || 0), 1)
  const maxSal = Math.max(...majorItems.value.map((m) => m.salaryAvg || 0), 1)

  majorRadarChart?.setOption({
    title: { text: '综合指标雷达（归一化）', left: 'center', textStyle: { fontSize: 13 } },
    tooltip: {},
    legend: { bottom: 0, type: 'scroll' },
    radar: {
      indicator: [
        { name: '热度', max: 100 },
        { name: '就业率', max: 100 },
        { name: '本科薪资', max: 100 },
        { name: '五年薪资', max: 100 },
      ],
    },
    series: [{
      type: 'radar',
      data: majorItems.value.map((m) => ({
        name: m.majorName,
        value: [
          ((m.hotIndex || 0) / maxHot) * 100,
          ((Number(m.employmentRate) || 0) / maxEmp) * 100,
          ((m.salaryAvg || 0) / maxSal) * 100,
          ((m.salary5year || m.salaryAvg || 0) / maxSal) * 100,
        ],
      })),
    }],
  })
}

function renderMajorTrendCharts() {
  if (!majorItems.value.length) return

  majorHotChart = initChart(majorHotChartRef.value, majorHotChart)
  majorEmpChart = initChart(majorEmpChartRef.value, majorEmpChart)

  const yearLabels = COMPARE_YEARS.map(String)

  majorHotChart?.setOption({
    title: { text: '近五年报考热度', left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, type: 'scroll' },
    grid: { left: 50, right: 20, top: 40, bottom: 56 },
    xAxis: { type: 'category', data: yearLabels },
    yAxis: { type: 'value', name: '热度分', scale: true },
    series: majorItems.value.map((m) => ({
      name: m.majorName,
      type: 'line',
      smooth: true,
      data: COMPARE_YEARS.map((y) => (m.hotTrends || []).find((x) => x.year === y)?.hotScore ?? null),
    })),
  })

  const empCategory = majorItems.value[0]?.majorCategory || '门类'
  majorEmpChart?.setOption({
    title: { text: `${empCategory}门类 · 近五年就业率`, left: 'center', textStyle: { fontSize: 13 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 48 },
    xAxis: { type: 'category', data: yearLabels },
    yAxis: { type: 'value', name: '%', max: 100 },
    series: [{
      name: `${empCategory}就业率`,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.08 },
      data: COMPARE_YEARS.map((y) => {
        const trends = majorItems.value[0]?.employmentTrends || []
        const t = trends.find((x) => x.year === y)
        return t?.employmentRate != null ? Number(t.employmentRate) : null
      }),
      itemStyle: { color: '#22c55e' },
    }],
  })
}

async function runSchoolCompare() {
  if (selectedCodes.value.length < 2) {
    ElMessage.warning('请至少选择 2 所院校')
    return
  }
  loading.value = true
  disposeCharts()
  try {
    const res = await fetchSchoolCompare({
      codes: selectedCodes.value.join(','),
      scoreProvince: scoreProvince.value,
      subjectType: subjectType.value,
      years: COMPARE_YEARS.join(','),
    })
    schoolItems.value = res.data || []
    if (schoolItems.value.length < 2) {
      ElMessage.info('部分院校暂无录取数据，请更换院校或省份')
    }
    syncUrl()
    await nextTick()
    renderSchoolCharts()
    handleResize()
  } catch {
    schoolItems.value = []
    ElMessage.error('对比加载失败')
  } finally {
    loading.value = false
  }
}

async function runMajorCompare() {
  if (selectedMajorCodes.value.length < 2) {
    ElMessage.warning('请至少选择 2 个专业')
    return
  }
  loading.value = true
  disposeCharts()
  try {
    const res = await fetchMajorCompare({ codes: selectedMajorCodes.value.join(',') })
    majorItems.value = res.data || []
    syncUrl()
    await nextTick()
    renderMajorCharts()
    renderMajorTrendCharts()
    handleResize()
  } catch {
    majorItems.value = []
    ElMessage.error('对比加载失败')
  } finally {
    loading.value = false
  }
}

function handleResize() {
  scoreChart?.resize()
  rankChart?.resize()
  overLineChart?.resize()
  majorBarChart?.resize()
  majorRadarChart?.resize()
  majorHotChart?.resize()
  majorEmpChart?.resize()
}

watch([scoreProvince, subjectType], () => {
  if (isSchool.value && schoolSelectedList.value.length >= 2 && schoolItems.value.length >= 2) {
    runSchoolCompare()
  }
})

async function exportSchoolReport() {
  if (schoolItems.value.length < 2) return
  try {
    const schools = schoolItems.value.map((item) => ({
      schoolName: item.schoolName,
      location: item.location,
      is985: item.is985,
      is211: item.is211,
      minScore: item.latestMinScore,
      scoreDiff: item.latestOverLineScore,
    }))
    const { html } = await generateSchoolCompareReport({
      title: '院校对比报告',
      scoreProvince: scoreProvince.value,
      subjectType: subjectType.value,
      schools,
    })
    downloadHtml(html, `院校对比_${Date.now()}.html`)
    ElMessage.success('对比表已导出')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  }
}

onMounted(async () => {
  reportOk.value = await checkReportHealth()
  window.addEventListener('resize', handleResize)
  const initCodes = parseCodes(route.query.codes)
  try {
    const [provinceRes, locRes] = await Promise.all([
      fetchScoreProvinces(),
      fetchSchoolLocations(),
    ])
    const provinces = sortProvinces(provinceRes.data || [])
    if (provinces.length) {
      scoreProvinces.value = provinces
      if (!provinces.includes(scoreProvince.value)) {
        scoreProvince.value = provinces.includes('河南') ? '河南' : provinces[0]
      }
    }
    schoolLocations.value = sortProvinces(locRes.data || [])
    if (route.query.schoolLocation && schoolLocations.value.includes(route.query.schoolLocation)) {
      schoolLocation.value = route.query.schoolLocation
    }
  } catch { /* ignore */ }

  if (activeTab.value === 'school' && schoolLocation.value) {
    await nextTick()
    schoolSearchRef.value?.reload()
  }

  if (activeTab.value === 'school') {
    await loadSchoolsByCodes(initCodes)
    if (schoolSelectedList.value.length >= 2) runSchoolCompare()
  } else {
    await loadMajorsByCodes(initCodes)
    if (majorSelectedList.value.length >= 2) runMajorCompare()
  }
})

watch(
  () => route.query,
  (q) => {
    if (q.type) activeTab.value = q.type === 'major' ? 'major' : 'school'
    if (q.codes) {
      const codes = parseCodes(q.codes)
      if (activeTab.value === 'school') {
        loadSchoolsByCodes(codes).then(() => {
          if (codes.length >= 2) runSchoolCompare()
        })
      } else {
        loadMajorsByCodes(codes).then(() => {
          if (codes.length >= 2) runMajorCompare()
        })
      }
    }
    if (q.schoolLocation) schoolLocation.value = q.schoolLocation
    if (q.scoreProvince) scoreProvince.value = q.scoreProvince
    if (q.subjectType) subjectType.value = q.subjectType
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.compare-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 24px 64px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.page-head h1 {
  font-size: 1.75rem;
  font-weight: 800;
  color: #0f172a;
}

.disclaimer {
  font-size: 0.8rem;
  color: #94a3b8;
  margin-top: 6px;
}

.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  margin: 16px 0;
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filters-row {
  margin-bottom: 12px;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
}

.toolbar-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.preset-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.preset-label {
  font-size: 0.85rem;
  color: #64748b;
}

.selected-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.chip-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px 12px;
}

.chip-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chip-main strong {
  font-size: 0.9rem;
  color: #0f172a;
}

.chip-loc {
  font-size: 0.75rem;
  color: #94a3b8;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: linear-gradient(145deg, #f8fafc, #fff);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
}

.summary-card h3 {
  font-size: 1.05rem;
  font-weight: 700;
  margin-bottom: 8px;
  color: #0f172a;
}

.major-summary {
  background: linear-gradient(145deg, #faf5ff, #fff);
}

.tag-row {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.tag {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 600;
}

.t985 { background: #fee2e2; color: #dc2626; }
.t211 { background: #dbeafe; color: #2563eb; }
.tdf { background: #dcfce7; color: #16a34a; }

.highlight {
  font-size: 0.9rem;
  color: #475569;
}

.highlight em {
  font-style: normal;
  font-size: 1.25rem;
  font-weight: 800;
  color: #6366f1;
}

.muted {
  color: #94a3b8;
  font-size: 0.85rem;
}

.over-line {
  font-size: 0.85rem;
  margin-top: 6px;
  color: #64748b;
}

.over-line .up { color: #16a34a; font-weight: 700; }
.over-line .down { color: #dc2626; font-weight: 700; }

.stat-list {
  list-style: none;
  padding: 0;
  margin: 8px 0;
  font-size: 0.85rem;
  color: #64748b;
}

.stat-list em {
  font-style: normal;
  color: #6366f1;
  font-weight: 700;
}

.card-actions {
  margin-top: 12px;
  display: flex;
  gap: 4px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

.chart-box {
  width: 100%;
  height: 360px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 8px;
  margin-bottom: 16px;
}

.chart-box.half {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 24px 0 12px;
  color: #0f172a;
}

.compare-table {
  margin-bottom: 8px;
}

.compare-table .highlight {
  font-weight: 700;
  color: #6366f1;
}

.career-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.career-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
}

.career-card > h4 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
}

.career-block {
  margin-bottom: 16px;
}

.career-block:last-child {
  margin-bottom: 0;
}

.career-block h5 {
  font-size: 0.8rem;
  font-weight: 700;
  color: #6366f1;
  margin-bottom: 8px;
  text-transform: none;
}

.career-block p {
  font-size: 0.875rem;
  color: #64748b;
  line-height: 1.75;
}

.career-block .sub-line {
  margin-top: 8px;
  font-size: 0.8rem;
}

.career-block .sub-line span {
  display: inline-block;
  font-weight: 600;
  color: #475569;
  margin-right: 6px;
}

.career-card .tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
