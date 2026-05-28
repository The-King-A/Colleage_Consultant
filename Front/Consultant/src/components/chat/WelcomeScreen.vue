<template>
  <div class="welcome">
    <!-- Brand icon with ambient glow -->
    <div class="brand-glow">
      <div class="glow-orb g1"></div>
      <div class="glow-orb g2"></div>
      <div class="brand-icon">
        <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 8l10 6 10-6-10-6z"/>
          <path d="M2 16l10 6 10-6"/>
          <path d="M2 12l10 6 10-6"/>
        </svg>
      </div>
    </div>

    <h1 class="greeting">你好，我是智选未来</h1>
    <p class="subtitle">AI 高考志愿顾问 · DeepSeek-V4 驱动</p>

    <!-- Prompt cards grid -->
    <div class="prompt-grid">
      <button
        v-for="p in prompts"
        :key="p.text"
        class="prompt-card"
        @click="$emit('ask', p.text)"
      >
        <div class="prompt-icon" :style="{ background: p.bg }">
          <el-icon :size="18"><component :is="p.icon" /></el-icon>
        </div>
        <div class="prompt-body">
          <span class="prompt-title">{{ p.title }}</span>
          <span class="prompt-text">{{ p.text }}</span>
        </div>
        <el-icon class="prompt-arrow" :size="16"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, TrendCharts, Search, DataAnalysis, ArrowRight } from '@element-plus/icons-vue'

defineEmits(['ask'])

const prompts = [
  {
    icon: ChatDotRound,
    bg: 'linear-gradient(135deg, #7c3aed, #6366f1)',
    title: '院校咨询',
    text: '介绍一下西安交通大学的特色专业与录取分数线',
  },
  {
    icon: TrendCharts,
    bg: 'linear-gradient(135deg, #06b6d4, #0ea5e9)',
    title: '志愿策略',
    text: '理科600分左右，帮我用冲稳保策略推荐学校',
  },
  {
    icon: Search,
    bg: 'linear-gradient(135deg, #f97316, #ef4444)',
    title: '专业对比',
    text: '计算机、软件工程、人工智能有什么区别和前景',
  },
  {
    icon: DataAnalysis,
    bg: 'linear-gradient(135deg, #10b981, #059669)',
    title: '数据分析',
    text: '哪些专业就业率高？近年薪资趋势如何',
  },
  {
    icon: ChatDotRound,
    bg: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
    title: '录取规则',
    text: '平行志愿是什么？填报时需要注意哪些事项',
  },
  {
    icon: TrendCharts,
    bg: 'linear-gradient(135deg, #ec4899, #f97316)',
    title: '就业趋势',
    text: '分析人工智能专业的就业趋势和薪资水平',
  },
]
</script>

<style scoped>
.welcome {
  max-width: 780px;
  margin: 0 auto;
  padding: 56px 0 80px;
  text-align: center;
}

/* Brand with glow */
.brand-glow {
  position: relative;
  display: inline-block;
  margin-bottom: 28px;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.g1 {
  width: 140px; height: 140px;
  background: rgba(124, 58, 237, 0.2);
  animation: orb-breathe 3s ease-in-out infinite;
}

.g2 {
  width: 100px; height: 100px;
  background: rgba(6, 182, 212, 0.18);
  animation: orb-breathe 3s ease-in-out 1s infinite;
}

@keyframes orb-breathe {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.6; }
  50% { transform: translate(-50%, -50%) scale(1.3); opacity: 1; }
}

.brand-icon {
  position: relative;
  width: 80px; height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 32px rgba(99,102,241,0.25);
}

.greeting {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin-bottom: 40px;
}

/* Prompt grid */
.prompt-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  text-align: left;
}

.prompt-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border-radius: 16px;
  border: 1px solid #e8ecf1;
  background: #fff;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.prompt-card:hover {
  border-color: rgba(99,102,241,0.2);
  box-shadow:
    0 1px 2px rgba(0,0,0,0.04),
    0 4px 16px rgba(99,102,241,0.06);
  transform: translateY(-1px);
}

.prompt-card:active {
  transform: translateY(0);
}

.prompt-icon {
  width: 42px; height: 42px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.prompt-body {
  flex: 1;
  min-width: 0;
}

.prompt-title {
  display: block;
  font-size: 0.8rem;
  font-weight: 600;
  color: #6366f1;
  margin-bottom: 2px;
}

.prompt-text {
  display: block;
  font-size: 0.85rem;
  color: #334155;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prompt-arrow {
  color: #cbd5e1;
  flex-shrink: 0;
  transition: all 0.25s;
}

.prompt-card:hover .prompt-arrow {
  color: #6366f1;
  transform: translateX(3px);
}

@media (max-width: 640px) {
  .prompt-grid {
    grid-template-columns: 1fr;
  }
  .welcome {
    padding: 40px 0 60px;
  }
}
</style>
