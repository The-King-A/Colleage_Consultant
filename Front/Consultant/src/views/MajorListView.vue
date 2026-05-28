<template>
  <div class="data-page">
    <div class="page-head">
      <div>
        <h1>专业库</h1>
        <p>输入专业名搜索，支持门类筛选，勾选后加入对比</p>
      </div>
      <el-button type="primary" round :disabled="compareList.length < 2" @click="goCompare">
        对比已选 ({{ compareList.length }}/4)
      </el-button>
      <el-button round @click="router.push({ path: '/compare', query: { type: 'major' } })">进入对比页</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索专业名称、代码、学科门类…"
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
      <el-select
        v-model="filters.category"
        placeholder="学科门类"
        clearable
        filterable
        style="width: 160px"
        @change="onSearchNow"
      >
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>

    <div v-if="hotCategories.length" class="quick-filters">
      <span class="qf-label">热门门类：</span>
      <el-button
        v-for="c in hotCategories"
        :key="c"
        :type="filters.category === c ? 'primary' : 'default'"
        round
        size="small"
        @click="pickCategory(c)"
      >
        {{ c }}
      </el-button>
      <el-button v-if="filters.category" round size="small" @click="pickCategory('')">清除</el-button>
    </div>

    <p v-if="total > 0" class="result-tip">共 {{ total }} 个专业</p>

    <div v-loading="loading" class="card-grid">
      <div v-for="item in list" :key="item.majorCode" class="major-card">
        <div class="card-top">
          <h3 @click="goDetail(item)">{{ item.majorName }}</h3>
          <el-checkbox
            :model-value="isSelected(item.majorCode)"
            :disabled="!isSelected(item.majorCode) && compareList.length >= 4"
            @change="(v) => toggleCompare(item, v)"
          />
        </div>
        <p class="cat">{{ item.majorCategory }} · {{ item.majorSubcategory }}</p>
        <div class="stats">
          <span>热度 <strong>{{ item.hotIndex ?? '—' }}</strong></span>
          <span>就业率 <strong>{{ item.employmentRate ?? '—' }}%</strong></span>
          <span>薪资 <strong>{{ item.salaryAvg ? item.salaryAvg + '元' : '—' }}</strong></span>
        </div>
        <div class="card-btns">
          <el-button text type="primary" @click="goDetail(item)">详情</el-button>
          <el-button
            v-if="!isSelected(item.majorCode)"
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

    <el-empty v-if="!loading && !list.length" description="没有匹配的专业" />

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
          <span class="dock-label">已选 {{ compareList.length }} 个：</span>
          <el-tag
            v-for="m in compareList"
            :key="m.majorCode"
            closable
            @close="toggleCompare(m, false)"
          >
            {{ m.majorName }}
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
import { ref, reactive, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { fetchMajorPage, fetchMajorCategories } from '../api/major'
import { useDebounce } from '../composables/useDebounce'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const categories = ref([])
const compareList = ref([])

const hotCategories = computed(() => {
  const prefer = ['工学', '管理学', '经济学', '医学', '法学', '理学', '文学']
  return prefer.filter((c) => categories.value.includes(c))
})

const filters = reactive({
  keyword: '',
  category: '',
})

const debouncedSearch = useDebounce(() => {
  page.value = 1
  loadData()
}, 400)

watch(() => filters.keyword, () => debouncedSearch())

function isSelected(code) {
  return compareList.value.some((m) => m.majorCode === code)
}

function toggleCompare(item, checked) {
  if (checked) {
    if (compareList.value.length >= 4) {
      ElMessage.warning('最多选择 4 个专业')
      return
    }
    if (!isSelected(item.majorCode)) {
      compareList.value.push({
        majorCode: item.majorCode,
        majorName: item.majorName,
        majorCategory: item.majorCategory,
      })
    }
  } else {
    const code = item.majorCode || item
    compareList.value = compareList.value.filter((m) => m.majorCode !== code)
  }
}

function pickCategory(c) {
  filters.category = c
  onSearchNow()
}

function onSearchNow() {
  debouncedSearch.cancel()
  page.value = 1
  loadData()
}

function goDetail(item) {
  router.push({
    path: `/major/${item.majorCode}`,
    query: { name: item.majorName, category: item.majorCategory },
  })
}

function goCompare() {
  const codes = compareList.value.map((m) => m.majorCode).join(',')
  router.push({ path: '/compare', query: { type: 'major', codes } })
}

async function loadData() {
  loading.value = true
  try {
    const res = await fetchMajorPage({
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
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

onMounted(async () => {
  try {
    const res = await fetchMajorCategories()
    categories.value = res.data || []
  } catch { /* ignore */ }
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

.major-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
}

.major-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.08);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-top h3 {
  font-size: 1.05rem;
  font-weight: 700;
  cursor: pointer;
  color: #0f172a;
}

.card-top h3:hover {
  color: #6366f1;
}

.cat {
  font-size: 0.85rem;
  color: #64748b;
  margin: 8px 0 12px;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.85rem;
  color: #475569;
  margin-bottom: 8px;
}

.stats strong {
  color: #6366f1;
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
