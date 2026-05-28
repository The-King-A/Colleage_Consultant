<template>
  <div class="fav-page">
    <div class="page-head">
      <h1>我的收藏</h1>
      <p>Spring Boot + MySQL 持久化 · 登录后同步云端</p>
    </div>

    <div v-loading="loading" class="fav-list">
      <el-empty v-if="!loading && !list.length" description="还没有收藏院校，去院校库添加吧">
        <el-button type="primary" @click="$router.push('/schools')">去院校库</el-button>
      </el-empty>

      <div v-for="item in list" :key="item.schoolCode" class="fav-card">
        <div class="fav-main">
          <h3 @click="goDetail(item)">{{ item.schoolName }}</h3>
          <p>{{ item.location }} · {{ item.city }}</p>
          <div class="tags">
            <span v-if="item.is985" class="t985">985</span>
            <span v-if="item.is211" class="t211">211</span>
          </div>
          <p v-if="item.note" class="note">备注：{{ item.note }}</p>
        </div>
        <div class="fav-actions">
          <el-button text type="primary" @click="goDetail(item)">详情</el-button>
          <el-button text type="danger" @click="remove(item)">取消收藏</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchFavoriteSchools, removeFavoriteSchool } from '../api/favorite'

const router = useRouter()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await fetchFavoriteSchools()
    list.value = res.data || []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  router.push({ path: `/school/${item.schoolCode}`, query: { name: item.schoolName } })
}

async function remove(item) {
  try {
    await ElMessageBox.confirm(`取消收藏「${item.schoolName}」？`, '提示', { type: 'warning' })
    await removeFavoriteSchool(item.schoolCode)
    ElMessage.success('已取消收藏')
    load()
  } catch { /* cancelled */ }
}

onMounted(load)
</script>

<style scoped>
.fav-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page-head { margin-bottom: 24px; }
.page-head h1 { font-size: 1.75rem; font-weight: 800; }
.page-head p { color: #64748b; margin-top: 6px; font-size: 0.9rem; }
.fav-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  padding: 20px; margin-bottom: 12px;
}
.fav-main h3 { cursor: pointer; font-size: 1.1rem; font-weight: 700; }
.fav-main h3:hover { color: #6366f1; }
.fav-main p { color: #64748b; font-size: 0.85rem; margin-top: 6px; }
.tags { margin-top: 8px; }
.tags span { font-size: 0.7rem; padding: 2px 8px; border-radius: 6px; margin-right: 6px; font-weight: 600; }
.t985 { background: #fee2e2; color: #dc2626; }
.t211 { background: #dbeafe; color: #2563eb; }
.note { color: #94a3b8; font-style: italic; }
</style>
