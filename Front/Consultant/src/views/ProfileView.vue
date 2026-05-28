<template>
  <div class="profile-page">
    <header class="profile-header">
      <button class="back-link" @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h2>个人中心</h2>
    </header>

    <main class="profile-body">
      <!-- User Info Card -->
      <div class="info-card">
        <div class="avatar-section">
          <el-avatar :size="80" :src="authStore.userInfo?.avatar">
            {{ authStore.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-info">
            <h3>{{ authStore.nickname }}</h3>
            <p>@{{ authStore.username }}</p>
            <span class="role-badge">{{ authStore.userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ authStore.userInfo?.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ authStore.userInfo?.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所在地</span>
            <span class="info-value">{{ authStore.userInfo?.province || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(authStore.userInfo?.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="action-list">
        <div class="action-item" @click="$router.push('/recommend')">
          <el-icon><Document /></el-icon>
          <span>我的志愿推荐</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="action-item" @click="$router.push('/interest-test')">
          <el-icon><MagicStick /></el-icon>
          <span>兴趣测评记录</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="action-item" @click="handleLogout">
          <el-icon color="#ef4444"><SwitchButton /></el-icon>
          <span style="color:#ef4444">退出登录</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Document, MagicStick, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function formatDate(dateStr) {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
    })
    authStore.logout()
    router.push('/')
  } catch { /* canceled */ }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-body);
}

.profile-header {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid #e2e8f0;
}

.profile-header h2 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
}

.back-link {
  width: 36px; height: 36px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: var(--bg-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
}

.profile-body {
  max-width: 700px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

.info-card {
  background: var(--bg-surface);
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 36px;
  margin-bottom: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.avatar-info h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
}

.avatar-info p {
  font-size: 0.875rem;
  color: #94a3b8;
  margin: 4px 0 8px;
}

.role-badge {
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 8px;
  background: #eef2ff;
  color: #4f46e5;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 0.8rem;
  color: #94a3b8;
}

.info-value {
  font-size: 0.95rem;
  color: #1e293b;
  font-weight: 500;
}

.action-list {
  background: var(--bg-surface);
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f1f5f9;
}

.action-item:last-child {
  border-bottom: none;
}

.action-item:hover {
  background: var(--bg-body);
}

.action-item span {
  flex: 1;
  font-size: 0.95rem;
  color: #334155;
  font-weight: 500;
}

.action-item .arrow {
  color: #cbd5e1;
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
