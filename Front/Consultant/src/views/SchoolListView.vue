<template>
  <div class="data-page">
    <div class="page-head">
      <div>
        <h1>院校库</h1>
        <p>输入名称即可搜索，勾选后加入对比（最多 4 所）</p>
      </div>
      <el-button type="primary" round :disabled="compareList.length < 2" @click="goCompare">
        对比已选 ({{ compareList.length }}/4)
      </el-button>
      <el-button round @click="router.push('/compare')">进入对比页</el-button>
      <el-button round @click="router.push('/school-map')">地图选校</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索院校名称、城市、省份…"
        clearable
        size="large"
        class="search-main"
        @keyup.enter="onSearchNow"
        @clear="onSearchNow"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filters.location" placeholder="所在省份" clearable filterable style="width: 140px" @change="onSearchNow">
        <el-option v-for="loc in locations" :key="loc" :label="loc" :value="loc" />
      </el-select>
      <el-select v-model="filters.scoreProvince" filterable placeholder="看分省份" style="width: 130px" @change="onSearchNow">
        <el-option v-for="p in scoreProvinces" :key="p" :label="`${p}录取`" :value="p" />
      </el-select>
      <el-select v-model="filters.subjectType" style="width: 110px" @change="onSearchNow">
        <el-option label="理科" value="science" />
        <el-option label="文科" value="liberal" />
      </el-select>
    </div>

    <div class="quick-filters">
      <span class="qf-label">快捷筛选：</span>
      <el-button
        v-for="q in quickFilters"
        :key="q.key"
        :type="quickFilter === q.key ? 'primary' : 'default'"
        round
        size="small"
        @click="applyQuickFilter(q.key)"
      >
        {{ q.label }}
      </el-button>
    </div>

    <p v-if="total > 0" class="result-tip">共 {{ total }} 所院校，已输入关键字将实时筛选</p>

    <div v-loading="loading" class="card-grid">
      <div v-for="item in list" :key="item.schoolCode" class="school-card">
        <div class="card-top">
          <h3 @click="goDetail(item)">{{ item.schoolName }}</h3>
          <el-checkbox
            :model-value="isSelected(item.schoolCode)"
            :disabled="!isSelected(item.schoolCode) && compareList.length >= 4"
            @change="(v) => toggleCompare(item, v)"
          />
        </div>
        <div class="tags">
          <span v-if="item.is985" class="tag t985">985</span>
          <span v-if="item.is211" class="tag t211">211</span>
          <span v-if="item.isDoubleFirst" class="tag tdf">双一流</span>
          <span class="tag tloc">{{ item.location }} · {{ item.city }}</span>
        </div>
        <p class="meta">{{ item.schoolType || '高等院校' }}</p>
        <p v-if="item.latestMinScore" class="score">
          {{ filters.scoreProvince }}近年最低分 <strong>{{ item.latestMinScore }}</strong>
          <span v-if="item.latestMinRank"> / 位次 {{ item.latestMinRank }}</span>
        </p>
        <p v-else class="score muted">暂无该省录取数据</p>
        <div class="card-btns">
          <el-button text type="primary" @click="goDetail(item)">详情</el-button>
          <el-button
            v-if="authStore.isLoggedIn"
            text
            :type="favoriteSet.has(item.schoolCode) ? 'warning' : 'primary'"
            @click="toggleFavorite(item)"
          >
            {{ favoriteSet.has(item.schoolCode) ? '★ 已收藏' : '☆ 收藏' }}
          </el-button>
          <el-button
            v-if="!isSelected(item.schoolCode)"
            text
            type="primary"
            :disabled="compareList.length >= 4"
            @click="toggleCompare(item, true)"
          >
            + 加入对比
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !list.length" description="没有匹配的院校，换个关键词试试" />

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="onSearchNow"
      />
    </div>

    <transition name="dock">
      <div v-if="compareList.length" class="compare-dock">
        <div class="dock-inner">
          <span class="dock-label">已选 {{ compareList.length }} 所：</span>
          <el-tag
            v-for="s in compareList"
            :key="s.schoolCode"
            closable
            @close="toggleCompare(s, false)"
          >
            {{ s.schoolName }}
          </el-tag>
          <el-button type="primary" :disabled="compareList.length < 2" @click="goCompare">
            去对比
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { fetchSchoolPage, fetchSchoolLocations, fetchScoreProvinces } from '../api/school'
import { addFavoriteSchool, removeFavoriteSchool, fetchFavoriteSchools } from '../api/favorite'
import { useAuthStore } from '../stores/auth'
import { useDebounce } from '../composables/useDebounce'
import { sortProvinces } from '../utils/provinces'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const favoriteSet = ref(new Set())
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const locations = ref([])
const scoreProvinces = ref(['河南'])
const compareList = ref([])
const quickFilter = ref('all')

const quickFilters = [
  { key: 'all', label: '全部' },
  { key: '985', label: '985' },
  { key: '211', label: '211' },
  { key: 'both', label: '985且211' },
]

const filters = reactive({
  keyword: '',
  location: '',
  scoreProvince: '河南',
  subjectType: 'science',
  is985: false,
  is211: false,
})

const debouncedSearch = useDebounce(() => {
  page.value = 1
  loadData()
}, 400)

watch(() => filters.keyword, () => debouncedSearch())

function isSelected(code) {
  return compareList.value.some((s) => s.schoolCode === code)
}

function toggleCompare(item, checked) {
  if (checked) {
    if (compareList.value.length >= 4) {
      ElMessage.warning('最多选择 4 所院校')
      return
    }
    if (!isSelected(item.schoolCode)) {
      compareList.value.push({
        schoolCode: item.schoolCode,
        schoolName: item.schoolName,
        location: item.location,
        city: item.city,
      })
    }
  } else {
    const code = item.schoolCode || item
    compareList.value = compareList.value.filter((s) => s.schoolCode !== code)
  }
}

function applyQuickFilter(key) {
  quickFilter.value = key
  filters.is985 = key === '985' || key === 'both'
  filters.is211 = key === '211' || key === 'both'
  onSearchNow()
}

function onSearchNow() {
  debouncedSearch.cancel()
  page.value = 1
  loadData()
}

function goDetail(item) {
  router.push({ path: `/school/${item.schoolCode}`, query: { name: item.schoolName } })
}

function goCompare() {
  const codes = compareList.value.map((s) => s.schoolCode).join(',')
  router.push({
    path: '/compare',
    query: {
      type: 'school',
      codes,
      scoreProvince: filters.scoreProvince,
      subjectType: filters.subjectType,
    },
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await fetchSchoolPage({
      keyword: filters.keyword || undefined,
      location: filters.location || undefined,
      scoreProvince: filters.scoreProvince,
      subjectType: filters.subjectType,
      is985: filters.is985 || undefined,
      is211: filters.is211 || undefined,
      page: page.value,
      size: pageSize.value,
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadFavorites() {
  if (!authStore.isLoggedIn) {
    favoriteSet.value = new Set()
    return
  }
  try {
    const res = await fetchFavoriteSchools()
    favoriteSet.value = new Set((res.data || []).map((s) => s.schoolCode))
  } catch {
    favoriteSet.value = new Set()
  }
}

async function toggleFavorite(item) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (favoriteSet.value.has(item.schoolCode)) {
      await removeFavoriteSchool(item.schoolCode)
      favoriteSet.value.delete(item.schoolCode)
      favoriteSet.value = new Set(favoriteSet.value)
      ElMessage.success('已取消收藏')
    } else {
      await addFavoriteSchool(item.schoolCode)
      favoriteSet.value.add(item.schoolCode)
      favoriteSet.value = new Set(favoriteSet.value)
      ElMessage.success('已加入收藏')
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(async () => {
  try {
    const [locRes, provRes] = await Promise.all([
      fetchSchoolLocations(),
      fetchScoreProvinces(),
    ])
    locations.value = sortProvinces(locRes.data || [])
    const provinces = sortProvinces(provRes.data || [])
    if (provinces.length) {
      scoreProvinces.value = provinces
    }
  } catch { /* ignore */ }
  await loadFavorites()
  loadData()
})

onBeforeUnmount(() => debouncedSearch.cancel())
</script>

<style scoped>
.data-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 100px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.page-head h1 {
  font-size: 1.75rem;
  font-weight: 800;
  color: #0f172a;
}

.page-head p {
  color: #64748b;
  margin-top: 6px;
  font-size: 0.9rem;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  margin-bottom: 12px;
}

.search-main {
  flex: 1;
  min-width: 260px;
}

.quick-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.qf-label {
  font-size: 0.85rem;
  color: #64748b;
}

.result-tip {
  font-size: 0.8rem;
  color: #94a3b8;
  margin-bottom: 12px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.school-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.school-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.1);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.card-top h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
  cursor: pointer;
}

.card-top h3:hover {
  color: #6366f1;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0;
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
.tloc { background: #f1f5f9; color: #64748b; }

.meta {
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 8px;
}

.score {
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 8px;
}

.score strong {
  color: #6366f1;
  font-size: 1.1rem;
}

.score.muted {
  color: #94a3b8;
}

.card-btns {
  display: flex;
  gap: 4px;
}

.pager {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.compare-dock {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid #e2e8f0;
  box-shadow: 0 -8px 32px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
}

.dock-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.dock-label {
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
}

.dock-enter-active,
.dock-leave-active {
  transition: transform 0.25s ease, opacity 0.25s;
}

.dock-enter-from,
.dock-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
