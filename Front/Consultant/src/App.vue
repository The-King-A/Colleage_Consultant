<template>
  <div id="app" :class="{ 'full-page': isFullPage }">
    <transition name="fade" mode="out-in">
      <router-view v-if="isFullPage" />
      <el-container v-else>
        <el-header class="app-header" :class="{ 'header-home': isHome }">
          <div class="header-inner">
            <div class="logo" @click="goHome">
              <div class="logo-icon-box">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2L2 8l10 6 10-6-10-6z"/>
                  <path d="M2 16l10 6 10-6"/>
                  <path d="M2 12l10 6 10-6"/>
                </svg>
              </div>
              <div class="logo-text">
                <span class="logo-title" :class="{ 'logo-title-dark': isHome }">智选未来</span>
                <span class="logo-sub">高考志愿助手</span>
              </div>
            </div>

            <nav class="nav-menu" :class="{ 'nav-menu-dark': isHome }">
              <div class="nav-scroll">
                <router-link
                  v-for="item in primaryNav"
                  :key="item.to"
                  :to="item.to"
                  class="nav-item"
                  :exact-active-class="item.exact ? 'active' : undefined"
                  :active-class="item.exact ? undefined : 'active'"
                >
                  <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
                  <span class="nav-label">{{ item.label }}</span>
                </router-link>

                <span class="nav-divider" aria-hidden="true" />

                <el-dropdown
                  trigger="hover"
                  placement="bottom-start"
                  popper-class="nav-tools-dropdown"
                  :show-timeout="80"
                  :hide-timeout="120"
                >
                  <button
                    type="button"
                    class="nav-item nav-more"
                    :class="{ active: isToolNavActive }"
                  >
                    <el-icon class="nav-icon"><Grid /></el-icon>
                    <span class="nav-label">志愿工具</span>
                    <el-icon class="nav-caret"><ArrowDown /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="item in toolNav"
                        :key="item.to"
                        :class="{ 'is-active-route': route.path === item.to }"
                        @click="router.push(item.to)"
                      >
                        <el-icon><component :is="item.icon" /></el-icon>
                        {{ item.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </nav>

            <div class="header-actions">
              <!-- Auth: logged in -->
              <template v-if="authStore.isLoggedIn">
                <el-dropdown trigger="click" popper-class="user-dropdown">
                  <div class="user-avatar-btn">
                    <el-avatar :size="32" :src="authStore.userInfo?.avatar">
                      {{ authStore.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                    <span class="user-name">{{ authStore.nickname }}</span>
                    <el-icon><ArrowDown /></el-icon>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="router.push('/profile')">
                        <el-icon><UserFilled /></el-icon>
                        个人中心
                      </el-dropdown-item>
                      <el-dropdown-item @click="router.push('/favorites')">
                        <el-icon><Star /></el-icon>
                        我的收藏
                      </el-dropdown-item>
                      <el-dropdown-item @click="router.push('/plans')">
                        <el-icon><Document /></el-icon>
                        志愿方案
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="handleLogout">
                        <el-icon><SwitchButton /></el-icon>
                        退出登录
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <!-- Auth: guest -->
              <template v-else>
                <el-button class="nav-login" size="default" round @click="router.push('/login')">
                  登录
                </el-button>
              </template>
              <el-button class="nav-cta" type="primary" size="default" round @click="router.push('/chat')">
                开始咨询
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </el-header>
        <el-main class="app-main">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled, ChatDotRound, Document, MagicStick, ArrowRight,
  ArrowDown, UserFilled, SwitchButton, School, DataAnalysis,
  TrendCharts, Histogram, Guide, DataLine, PieChart, Star, Grid, MapLocation,
} from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// Init theme from localStorage on load
const savedTheme = localStorage.getItem('app_theme') || 'dark'
document.documentElement.setAttribute('data-theme', savedTheme)

const fullPageRoutes = ['/', '/chat', '/interest-test']
const isFullPage = computed(() => fullPageRoutes.includes(route.path))
const isHome = computed(() => route.path === '/')

const primaryNav = [
  { to: '/', label: '首页', icon: HomeFilled, exact: true },
  { to: '/chat', label: '智能问答', icon: ChatDotRound },
  { to: '/schools', label: '院校库', icon: School },
  { to: '/matcher', label: '冲稳保', icon: TrendCharts },
  { to: '/compare', label: '对比', icon: DataAnalysis },
]

const toolNav = [
  { to: '/school-map', label: '地图选校', icon: MapLocation },
  { to: '/recommend', label: '志愿推荐', icon: Document },
  { to: '/interest-test', label: '兴趣测评', icon: MagicStick },
  { to: '/wizard', label: '填报向导', icon: Guide },
  { to: '/rank', label: '位次换算', icon: Histogram },
  { to: '/insights', label: '线差洞察', icon: DataLine },
  { to: '/dashboard', label: '数据看板', icon: PieChart },
]

const isToolNavActive = computed(() => toolNav.some((item) => route.path === item.to))

const goHome = () => router.push('/')

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
    authStore.logout()
    router.push('/')
  } catch { /* canceled */ }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f8fafc;
  color: #1e293b;
}

#app {
  min-height: 100vh;
}

#app.full-page {
  background: #f8fafc;
}

/* ---- Header ---- */
.app-header {
  --header-height: 72px;
  height: var(--header-height);
  padding: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 24px 0 28px;
}

.header-actions {
  flex-shrink: 0;
}

/* ---- Logo ---- */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.logo-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: transform 0.3s, box-shadow 0.3s;
}

.logo:hover .logo-icon-box {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.5px;
}

.logo-sub {
  font-size: 0.7rem;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 1px;
}

/* ---- Nav ---- */
.nav-menu {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: center;
}

.nav-scroll {
  display: flex;
  align-items: center;
  gap: 2px;
  max-width: 100%;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 2px 0;
}

.nav-scroll::-webkit-scrollbar {
  display: none;
}

.nav-divider {
  width: 1px;
  height: 18px;
  margin: 0 6px;
  background: #e2e8f0;
  flex-shrink: 0;
}

.nav-item {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  border: none;
  border-radius: 8px;
  text-decoration: none;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 1.25;
  transition: color 0.2s ease, background 0.2s ease;
  position: relative;
}

.nav-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.nav-label {
  letter-spacing: 0.02em;
}

.nav-caret {
  font-size: 12px;
  margin-left: -2px;
  opacity: 0.55;
  transition: transform 0.2s ease;
}

.nav-more:hover .nav-caret,
.nav-more.active .nav-caret {
  opacity: 0.9;
}

.nav-item:hover {
  color: #334155;
  background: #f1f5f9;
}

.nav-item.active {
  color: #4f46e5;
  font-weight: 600;
  background: #eef2ff;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 4px;
  height: 2px;
  border-radius: 1px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* User avatar dropdown */
.user-avatar-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 14px 4px 4px;
  border-radius: 28px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.user-avatar-btn:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-name {
  font-size: 0.85rem;
  font-weight: 500;
  color: #334155;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-login {
  font-weight: 500;
  color: #64748b;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.nav-login:hover {
  color: #4f46e5;
  border-color: #c7d2fe;
}

.nav-cta {
  font-weight: 600;
  padding: 10px 22px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
  transition: all 0.3s ease;
}

.nav-cta:hover {
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

/* ---- Main ---- */
.app-main {
  padding: 0;
  min-height: calc(100vh - var(--header-height));
}

/* ---- Page transitions ---- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-leave-to {
  opacity: 0;
}

/* ---- Dark header variant (homepage) ---- */
.header-home {
  background: rgba(11, 15, 26, 0.7) !important;
  backdrop-filter: saturate(180%) blur(20px) !important;
  -webkit-backdrop-filter: saturate(180%) blur(20px) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
}

.logo-title-dark {
  color: #f1f5f9 !important;
}

.nav-menu-dark .nav-divider {
  background: rgba(255, 255, 255, 0.12);
}

.nav-menu-dark .nav-item {
  color: #94a3b8 !important;
}

.nav-menu-dark .nav-item:hover {
  color: #e2e8f0 !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.nav-menu-dark .nav-item.active {
  color: #c7d2fe !important;
  background: rgba(99, 102, 241, 0.18) !important;
}

.nav-menu-dark .nav-item.active::after {
  background: linear-gradient(90deg, #a5b4fc, #c4b5fd);
}

/* ---- Responsive ---- */
@media (max-width: 1100px) {
  .nav-label {
    display: none;
  }

  .nav-item {
    padding: 8px 10px;
  }

  .nav-more .nav-caret {
    display: none;
  }

  .nav-item.active::after {
    left: 8px;
    right: 8px;
  }
}

@media (max-width: 768px) {
  .header-inner {
    padding: 0 12px;
    gap: 8px;
  }

  .logo-title {
    font-size: 1rem;
  }

  .logo-sub {
    display: none;
  }

  .nav-divider {
    display: none;
  }

  .user-name {
    display: none;
  }

  .nav-cta {
    padding: 8px 14px !important;
  }
}

/* 志愿工具下拉 */
.nav-tools-dropdown .el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  padding: 8px 16px;
}

.nav-tools-dropdown .el-dropdown-menu__item.is-active-route {
  color: #4f46e5;
  background: #eef2ff;
  font-weight: 600;
}
</style>
