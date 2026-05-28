<template>
  <div class="entity-search">
    <div class="search-row">
      <el-input
        v-model="keyword"
        :placeholder="placeholder"
        clearable
        size="large"
        class="search-input"
        @input="onKeywordInput"
        @keyup.enter="runSearch"
        @clear="onClear"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" :loading="searching" @click="runSearch">搜索</el-button>
      <el-button v-if="browsePath" size="large" @click="goBrowse">去库中挑选</el-button>
    </div>

    <p v-if="hint" class="search-hint">{{ hint }}</p>

    <div v-loading="searching" class="result-panel">
      <div v-if="!results.length && !searching" class="empty-tip">
        {{ emptyTip }}
      </div>
      <button
        v-for="item in results"
        :key="getCode(item)"
        type="button"
        class="result-item"
        :class="{ added: isAdded(item), disabled: isDisabled(item) }"
        :disabled="isDisabled(item)"
        @click="toggleItem(item)"
      >
        <div class="result-main">
          <strong>{{ getTitle(item) }}</strong>
          <span class="result-meta">{{ getMeta(item) }}</span>
        </div>
        <span class="result-action">{{ isAdded(item) ? '已选' : isDisabled(item) ? '已满' : '+ 加入' }}</span>
      </button>
    </div>

    <div class="slot-count">
      已选 <em>{{ modelValue.length }}</em> / {{ max }}
      <span v-if="modelValue.length >= 2" class="ready-tip">· 可开始对比</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useDebounce } from '../../composables/useDebounce'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 4 },
  placeholder: { type: String, default: '输入名称搜索' },
  hint: { type: String, default: '' },
  browsePath: { type: String, default: '' },
  /** async ({ keyword, location }) => items[] */
  fetchFn: { type: Function, required: true },
  /** 院校所在地（与筛选框双向绑定） */
  locationFilter: { type: String, default: '' },
  /** 为 true 时必须先选所在地，避免列出全国院校 */
  requireLocation: { type: Boolean, default: false },
  getCode: { type: Function, default: (item) => item.majorCode || item.schoolCode },
  getTitle: { type: Function, default: (item) => item.majorName || item.schoolName },
  getMeta: {
    type: Function,
    default: (item) => {
      if (item.majorCategory) return `${item.majorCategory} · ${item.majorSubcategory || ''}`
      const tags = []
      if (item.is985) tags.push('985')
      if (item.is211) tags.push('211')
      if (item.location) tags.push(`${item.location} · ${item.city || ''}`)
      return tags.join(' · ') || item.schoolType || ''
    },
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const router = useRouter()
const keyword = ref('')
const results = ref([])
const searching = ref(false)

const debouncedSearch = useDebounce(() => runSearch(), 350)

const emptyTip = computed(() => {
  if (props.requireLocation && !props.locationFilter) {
    return '请先在上方选择「院校所在地」，再点搜索'
  }
  if (keyword.value) return '未找到匹配项，请换关键词或调整所在地'
  if (props.locationFilter) {
    return `「${props.locationFilter}」暂无匹配院校，可尝试换关键词或到院校库查看`
  }
  return '请输入关键词后搜索'
})

function isAdded(item) {
  const code = props.getCode(item)
  return props.modelValue.some((x) => props.getCode(x) === code)
}

function isDisabled(item) {
  return !isAdded(item) && props.modelValue.length >= props.max
}

function toggleItem(item) {
  if (isAdded(item)) {
    const next = props.modelValue.filter((x) => props.getCode(x) !== props.getCode(item))
    emit('update:modelValue', next)
    emit('change', next)
    return
  }
  if (props.modelValue.length >= props.max) {
    ElMessage.warning(`最多选择 ${props.max} 项`)
    return
  }
  const next = [...props.modelValue, item]
  emit('update:modelValue', next)
  emit('change', next)
}

function onKeywordInput() {
  debouncedSearch()
}

function onClear() {
  debouncedSearch.cancel()
  runSearch()
}

async function runSearch() {
  if (props.requireLocation && !props.locationFilter) {
    results.value = []
    return
  }

  searching.value = true
  results.value = []
  try {
    const list = await props.fetchFn({
      keyword: keyword.value.trim(),
      location: props.locationFilter || '',
    })
    results.value = list || []
  } catch {
    results.value = []
  } finally {
    searching.value = false
  }
}

function goBrowse() {
  if (props.browsePath) router.push(props.browsePath)
}

watch(
  () => props.locationFilter,
  () => runSearch()
)

defineExpose({ reload: runSearch })

onMounted(() => {
  if (!props.requireLocation) {
    runSearch()
  }
})

onBeforeUnmount(() => debouncedSearch.cancel())
</script>

<style scoped>
.entity-search {
  width: 100%;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 220px;
}

.search-hint {
  font-size: 0.8rem;
  color: #94a3b8;
  margin: 8px 0 0;
}

.result-panel {
  margin-top: 12px;
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fafbfc;
}

.empty-tip {
  padding: 24px;
  text-align: center;
  color: #94a3b8;
  font-size: 0.875rem;
}

.result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover:not(.disabled):not(.added) {
  background: #f5f3ff;
}

.result-item.added {
  background: #eef2ff;
}

.result-item.disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.result-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.result-main strong {
  font-size: 0.95rem;
  color: #0f172a;
}

.result-meta {
  font-size: 0.75rem;
  color: #64748b;
}

.result-action {
  flex-shrink: 0;
  font-size: 0.8rem;
  font-weight: 600;
  color: #6366f1;
  margin-left: 12px;
}

.result-item.added .result-action {
  color: #16a34a;
}

.slot-count {
  margin-top: 10px;
  font-size: 0.85rem;
  color: #64748b;
}

.slot-count em {
  font-style: normal;
  font-weight: 800;
  color: #6366f1;
}

.ready-tip {
  color: #16a34a;
  font-weight: 600;
}
</style>
