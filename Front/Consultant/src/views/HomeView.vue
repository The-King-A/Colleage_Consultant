<template>
  <div class="homepage">
    <!-- Liquid mesh aurora background -->
    <div class="aurora">
      <div class="aurora-layer a1"></div>
      <div class="aurora-layer a2"></div>
      <div class="aurora-layer a3"></div>
      <div class="aurora-layer a4"></div>
    </div>
    <div class="dot-grid"></div>

    <!-- ===== TOP NAV ===== -->
    <header class="nav">
      <div class="nav-inner">
        <div class="nav-brand" @click="$router.push('/')">
          <div class="brand-mark">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2L2 8l10 6 10-6-10-6z"/><path d="M2 16l10 6 10-6"/><path d="M2 12l10 6 10-6"/>
            </svg>
          </div>
          <span>智选未来</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/chat">智能问答</router-link>
          <router-link to="/recommend">志愿推荐</router-link>
          <router-link to="/interest-test">兴趣测评</router-link>
          <router-link to="/schools">院校库</router-link>
          <router-link to="/compare">对比</router-link>
        </nav>
        <div class="nav-right">
          <template v-if="authStore.isLoggedIn">
            <span class="nav-username" @click="$router.push('/profile')">{{ authStore.nickname }}</span>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
          </template>
          <router-link to="/chat" class="nav-cta">开始使用</router-link>
        </div>
      </div>
    </header>

    <!-- ===== HERO ===== -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-chip">
          <span class="chip-dot"></span>
          DeepSeek-V4 · AI Native
        </div>

        <h1 class="hero-headline">
          <span class="line">让每一分</span>
          <span class="line accent">都找到最好的归宿</span>
        </h1>

        <p class="hero-sub">
          AI 驱动的智能志愿填报平台。深度融合大语言模型与百万级录取数据，<br/>为每一位考生生成精准、科学、个性化的志愿方案。
        </p>

        <div class="hero-actions">
          <router-link to="/chat" class="btn-glow">
            开始智能咨询
            <el-icon><ArrowRight /></el-icon>
          </router-link>
          <router-link to="/recommend" class="btn-ghost">
            <el-icon><Document /></el-icon>
            获取志愿推荐
          </router-link>
        </div>

        <div class="hero-metrics">
          <div class="metric-item">
            <span class="metric-num">12,580 <i>+</i></span>
            <span class="metric-label">累计服务考生</span>
          </div>
          <div class="metric-div"></div>
          <div class="metric-item">
            <span class="metric-num">2,867 <i>+</i></span>
            <span class="metric-label">覆盖院校数量</span>
          </div>
          <div class="metric-div"></div>
          <div class="metric-item">
            <span class="metric-num">98.6 <i>%</i></span>
            <span class="metric-label">用户满意度</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== SERVICE CARDS ===== -->
    <section class="services">
      <div class="services-head">
        <span class="section-chip">Core Capabilities</span>
        <h2>全方位 AI 志愿填报服务</h2>
        <p>从信息查询到方案生成，每一个环节都由 AI 深度驱动</p>
      </div>

      <div class="cards-row">
        <div class="glass-card" v-for="(c, i) in cards" :key="i">
          <div class="card-glow" :style="{ '--glow': c.glow }"></div>
          <div class="card-icon" :style="{ background: c.bg }">
            <el-icon :size="24"><component :is="c.icon" /></el-icon>
          </div>
          <h3>{{ c.title }}</h3>
          <p>{{ c.desc }}</p>
          <div class="card-tags">
            <span v-for="t in c.tags" :key="t">{{ t }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== AI CAPABILITIES ===== -->
    <section class="capabilities">
      <div class="services-head">
        <span class="section-chip">AI Powered</span>
        <h2>智能分析 · 精准决策</h2>
      </div>

      <div class="caps-grid">
        <div class="cap-card" v-for="(c, i) in capabilities" :key="i">
          <div class="cap-left">
            <div class="cap-badge" :style="{ background: c.bg }">
              <el-icon :size="26"><component :is="c.icon" /></el-icon>
            </div>
            <div class="cap-info">
              <h4>{{ c.title }}</h4>
              <p>{{ c.desc }}</p>
            </div>
          </div>
          <div class="cap-metrics">
            <div class="cap-m" v-for="m in c.metrics" :key="m.v">
              <span class="cap-mv">{{ m.v }}</span>
              <span class="cap-ml">{{ m.l }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== CTA ===== -->
    <section class="bottom-cta">
      <div class="cta-glass">
        <div class="cta-aurora"></div>
        <h2>准备好探索未来吗？</h2>
        <p>AI 大模型 × 百万级数据，为你的志愿填报保驾护航</p>
        <div class="cta-row">
          <router-link to="/chat" class="btn-glow">免费开始使用 <el-icon><ArrowRight /></el-icon></router-link>
          <router-link to="/recommend" class="btn-ghost">查看推荐示例</router-link>
        </div>
      </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="ftr">
      <div class="ftr-inner">
        <span>2026 智选未来 · AI 高考志愿助手</span>
        <span class="ftr-div">|</span>
        <span>数据仅供参考 · 请以官方发布为准</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ChatDotRound, TrendCharts, MagicStick, Search, DataAnalysis, Connection, Medal, Document, ArrowRight } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const cards = [
  {
    icon: ChatDotRound,
    bg: 'linear-gradient(135deg, #7c3aed, #6366f1)',
    glow: '#7c3aed',
    title: 'AI 智能问答',
    desc: '7×24h 在线顾问，基于 RAG 知识库检索，秒级响应院校专业问题，支持多轮深度对话。',
    tags: ['院校查询', '录取规则', '专业对比', '分数线'],
  },
  {
    icon: TrendCharts,
    bg: 'linear-gradient(135deg, #06b6d4, #0ea5e9)',
    glow: '#06b6d4',
    title: '志愿智能推荐',
    desc: '历年录取大数据 + AI 深度分析，按冲/稳/保三级梯度生成个性化志愿填报方案。',
    tags: ['冲稳保策略', '录取预测', '梯度推荐', '方案定制'],
  },
  {
    icon: MagicStick,
    bg: 'linear-gradient(135deg, #f97316, #ef4444)',
    glow: '#f97316',
    title: 'AI 兴趣测评',
    desc: '霍兰德职业兴趣模型，AI 对话式引导评估，精准匹配最适合的专业方向与职业路径。',
    tags: ['兴趣评估', '性格分析', '专业匹配', '测评报告'],
  },
  {
    icon: Search,
    bg: 'linear-gradient(135deg, #10b981, #059669)',
    glow: '#10b981',
    title: '院校大数据',
    desc: '覆盖全国 2800+ 高校，历年分数线、专业热度、就业率等关键指标一查便知。',
    tags: ['院校库', '分数线', '专业数据', '就业报告'],
  },
]

const capabilities = [
  {
    icon: DataAnalysis,
    bg: 'linear-gradient(135deg, #7c3aed, #6366f1)',
    title: '冲稳保智能分级',
    desc: 'AI 根据你的分数与历年数据自动划分冲刺、稳妥、保底三档院校，概率可视化呈现。',
    metrics: [{ v: '15%', l: '冲刺院校' }, { v: '55%', l: '稳妥院校' }, { v: '30%', l: '保底院校' }],
  },
  {
    icon: Connection,
    bg: 'linear-gradient(135deg, #06b6d4, #0ea5e9)',
    title: 'AI 职业发展预测',
    desc: '结合行业趋势与就业大数据，为每个专业方向提供清晰的职业发展路径与薪资预期。',
    metrics: [{ v: '95%', l: '预测准确率' }, { v: '50+', l: '行业覆盖' }, { v: '实时', l: '数据更新' }],
  },
  {
    icon: Medal,
    bg: 'linear-gradient(135deg, #f97316, #ef4444)',
    title: '一键生成 PDF 报告',
    desc: '含志愿方案、分数线分析、专业解读、职业规划的综合报告，支持下载与打印。',
    metrics: [{ v: 'PDF', l: '标准格式' }, { v: 'A4', l: '可打印' }, { v: '1键', l: '智能生成' }],
  },
  {
    icon: TrendCharts,
    bg: 'linear-gradient(135deg, #10b981, #059669)',
    title: 'ECharts 可视化分析',
    desc: '历年录取趋势、院校对比雷达图、专业热度变化曲线，关键数据一目了然。',
    metrics: [{ v: '5年', l: '历史数据' }, { v: '2800+', l: '院校覆盖' }, { v: '80+', l: '专业门类' }],
  },
]
</script>

<style scoped>
/* ============================================
   GEMINI-INSPIRED — Liquid Aurora + Glass
   ============================================ */

.homepage {
  position: relative;
  overflow-x: hidden;
  background: #f8fafd;
  color: #0f172a;
  min-height: 100vh;
}

/* ===== AURORA BACKGROUND ===== */
.aurora {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.aurora-layer {
  position: absolute;
  border-radius: 50%;
  filter: blur(180px);
  opacity: 0.55;
  animation: aurora-flow 18s ease-in-out infinite;
}

.a1 {
  width: 700px; height: 500px;
  background: conic-gradient(from 0deg, #a78bfa, #6366f1, #a78bfa);
  top: -15%; left: -15%;
}

.a2 {
  width: 600px; height: 400px;
  background: conic-gradient(from 120deg, #06b6d4, #22d3ee, #06b6d4);
  top: 45%; right: -10%;
  animation-delay: -6s;
}

.a3 {
  width: 500px; height: 350px;
  background: conic-gradient(from 240deg, #ec4899, #f97316, #ec4899);
  bottom: -10%; left: 30%;
  animation-delay: -12s;
}

.a4 {
  width: 400px; height: 300px;
  background: conic-gradient(from 60deg, #818cf8, #c084fc, #818cf8);
  top: 25%; left: 40%;
  animation-delay: -3s;
  opacity: 0.35;
}

@keyframes aurora-flow {
  0%, 100% { transform: translate(0, 0) rotate(0deg) scale(1); }
  25% { transform: translate(40px, -30px) rotate(5deg) scale(1.08); }
  50% { transform: translate(-20px, 20px) rotate(-3deg) scale(0.95); }
  75% { transform: translate(-35px, -15px) rotate(8deg) scale(1.04); }
}

.dot-grid {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.4;
}

/* ===== NAV ===== */
.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,0.65);
  backdrop-filter: saturate(180%) blur(24px);
  -webkit-backdrop-filter: saturate(180%) blur(24px);
  border-bottom: 1px solid rgba(0,0,0,0.06);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 28px;
  gap: 36px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
}

.brand-mark {
  width: 30px; height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(99,102,241,0.3);
}

.nav-menu {
  display: flex;
  gap: 2px;
}

.nav-menu a {
  padding: 8px 18px;
  border-radius: 10px;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  color: #475569;
  transition: all 0.2s;
}

.nav-menu a:hover { color: #6366f1; background: rgba(99,102,241,0.06); }

.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}

.nav-link {
  font-size: 0.85rem;
  font-weight: 500;
  color: #475569;
  text-decoration: none;
}

.nav-username {
  font-size: 0.85rem;
  font-weight: 600;
  color: #6366f1;
  cursor: pointer;
}

.nav-cta {
  display: inline-flex;
  align-items: center;
  padding: 9px 22px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  text-decoration: none;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  box-shadow: 0 2px 10px rgba(99,102,241,0.25);
  transition: all 0.3s;
}

.nav-cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 18px rgba(99,102,241,0.4);
}

/* ===== HERO ===== */
.hero {
  position: relative;
  z-index: 2;
  padding: 70px 24px 50px;
  text-align: center;
}

.hero-inner {
  max-width: 760px;
  margin: 0 auto;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 18px;
  border-radius: 24px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(99,102,241,0.15);
  backdrop-filter: blur(10px);
  color: #6366f1;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 28px;
}

.chip-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34,197,94,0.5);
}

.hero-headline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 24px;
}

.hero-headline .line {
  font-size: clamp(2.8rem, 6vw, 4.5rem);
  font-weight: 800;
  color: #0f172a;
  line-height: 1.1;
  letter-spacing: -2px;
}

.hero-headline .accent {
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 40%, #06b6d4 70%, #0ea5e9 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: accent-shift 5s ease-in-out infinite;
}

@keyframes accent-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.hero-sub {
  font-size: 1.05rem;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 38px;
  max-width: 540px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 48px;
}

/* ===== PREMIUM BUTTONS (Gemini / ChatGPT style) ===== */

/* -- Primary: fluid glow shimmer -- */
.btn-glow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 38px;
  border-radius: 18px;
  font-size: 1.02rem;
  font-weight: 650;
  text-decoration: none;
  color: #fff;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  background: #4f3ed9;
  box-shadow:
    0 0 0 1px rgba(120, 90, 255, 0.3),
    0 2px 0 0 rgba(255,255,255,0.12) inset,
    0 6px 28px rgba(99, 70, 240, 0.45),
    0 0 60px rgba(99, 70, 240, 0.15);
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1),
              box-shadow 0.4s ease;
}

/* Internal fluid shimmer */
.btn-glow::before {
  content: '';
  position: absolute;
  inset: -2px;
  z-index: -1;
  border-radius: 20px;
  background: conic-gradient(
    from 0deg,
    #06b6d4, #6366f1, #a855f7, #ec4899,
    #6366f1, #06b6d4, #6366f1
  );
  background-size: 300% 300%;
  animation: btn-fluid-shimmer 4s linear infinite;
  opacity: 0;
  transition: opacity 0.5s ease;
}

/* Subtle internal glow overlay */
.btn-glow::after {
  content: '';
  position: absolute;
  inset: 1px;
  z-index: -1;
  border-radius: 17px;
  background: radial-gradient(
    ellipse 80% 60% at 30% 50%,
    rgba(6, 182, 212, 0.15),
    transparent 50%
  ),
  radial-gradient(
    ellipse 60% 80% at 70% 50%,
    rgba(236, 72, 153, 0.08),
    transparent 50%
  );
  opacity: 0;
  transition: opacity 0.5s ease;
}

.btn-glow:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow:
    0 0 0 1px rgba(120, 90, 255, 0.5),
    0 2px 0 0 rgba(255,255,255,0.15) inset,
    0 12px 40px rgba(99, 70, 240, 0.55),
    0 0 100px rgba(99, 70, 240, 0.25);
}

.btn-glow:hover::before { opacity: 1; }
.btn-glow:hover::after { opacity: 1; }

.btn-glow > * {
  position: relative;
  z-index: 2;
}

@keyframes btn-fluid-shimmer {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* -- Secondary: iridescent glass border -- */
.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 38px;
  border-radius: 18px;
  font-size: 1.02rem;
  font-weight: 650;
  text-decoration: none;
  position: relative;
  isolation: isolate;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: #334155;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1),
              box-shadow 0.4s ease,
              background 0.3s ease;
}

/* Iridescent 1px border ring */
.btn-ghost::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  border-radius: 18px;
  padding: 1px;
  background: conic-gradient(
    from 0deg,
    rgba(99, 102, 241, 0.3),
    rgba(6, 182, 212, 0.4),
    rgba(168, 85, 247, 0.3),
    rgba(236, 72, 153, 0.2),
    rgba(99, 102, 241, 0.3)
  );
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.6;
  transition: opacity 0.4s ease;
}

/* Light-catching shimmer on hover */
.btn-ghost::after {
  content: '';
  position: absolute;
  inset: 2px;
  z-index: -1;
  border-radius: 16px;
  background: radial-gradient(
    ellipse 70% 50% at 50% 30%,
    rgba(255, 255, 255, 0.6),
    transparent 60%
  );
  opacity: 0;
  transition: opacity 0.4s ease;
}

.btn-ghost:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.8);
  color: #4f3ed9;
  box-shadow:
    0 4px 24px rgba(0,0,0,0.06),
    0 0 0 4px rgba(99,102,241,0.04);
}

.btn-ghost:hover::before { opacity: 1; }
.btn-ghost:hover::after { opacity: 1; }

.btn-ghost > * {
  position: relative;
  z-index: 2;
}

/* ===== HERO METRICS ===== */
.hero-metrics {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
}

.metric-item { text-align: center; }

.metric-num {
  display: block;
  font-size: 1.75rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -1px;
}

.metric-num i {
  font-style: normal;
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.metric-label {
  font-size: 0.78rem;
  color: #64748b;
  font-weight: 500;
}

.metric-div {
  width: 1px; height: 32px;
  background: #e2e8f0;
}

/* ===== SERVICES ===== */
.services {
  position: relative;
  z-index: 2;
  padding: 50px 24px 40px;
}

.services-head {
  text-align: center;
  margin-bottom: 40px;
}

.section-chip {
  display: inline-block;
  font-size: 0.72rem;
  font-weight: 700;
  color: #6366f1;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 14px;
  padding: 4px 14px;
  border-radius: 20px;
  background: rgba(99,102,241,0.06);
  border: 1px solid rgba(99,102,241,0.12);
}

.services-head h2 {
  font-size: clamp(1.5rem, 2.5vw, 2rem);
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.5px;
  margin-bottom: 8px;
}

.services-head p {
  font-size: 0.95rem;
  color: #64748b;
}

.cards-row {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 22px;
}

/* ===== GLASS CARD ===== */
.glass-card {
  position: relative;
  background: rgba(255,255,255,0.55);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 20px;
  padding: 34px 26px 26px;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  overflow: hidden;
}

.glass-card:hover {
  background: rgba(255,255,255,0.85);
  border-color: rgba(99,102,241,0.2);
  box-shadow: 0 16px 48px rgba(0,0,0,0.05), 0 0 0 1px rgba(99,102,241,0.08);
  transform: translateY(-4px);
}

.card-glow {
  position: absolute;
  top: -60px; right: -60px;
  width: 140px; height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--glow) 0%, transparent 70%);
  opacity: 0.12;
  transition: opacity 0.4s;
}

.glass-card:hover .card-glow { opacity: 0.22; }

.card-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.15);
}

.glass-card h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.glass-card p {
  font-size: 0.87rem;
  color: #475569;
  line-height: 1.65;
  margin-bottom: 18px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.card-tags span {
  font-size: 0.72rem;
  padding: 4px 10px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #475569;
  font-weight: 500;
}

/* ===== CAPABILITIES ===== */
.capabilities {
  position: relative;
  z-index: 2;
  padding: 20px 24px 50px;
}

.caps-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.cap-card {
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0,0,0,0.04);
  border-radius: 16px;
  padding: 26px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  transition: all 0.35s ease;
}

.cap-card:hover {
  background: rgba(255,255,255,0.85);
  border-color: rgba(99,102,241,0.12);
  box-shadow: 0 8px 28px rgba(0,0,0,0.03);
  transform: translateY(-2px);
}

.cap-left {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.cap-badge {
  width: 54px; height: 54px;
  border-radius: 14px;
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
}

.cap-info h4 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.cap-info p {
  font-size: 0.83rem;
  color: #475569;
  line-height: 1.55;
  max-width: 340px;
}

.cap-metrics {
  display: flex;
  gap: 20px;
  flex-shrink: 0;
}

.cap-m { text-align: center; }

.cap-mv {
  display: block;
  font-size: 1rem;
  font-weight: 700;
  color: #6366f1;
}

.cap-ml {
  font-size: 0.68rem;
  color: #64748b;
}

/* ===== BOTTOM CTA ===== */
.bottom-cta {
  position: relative;
  z-index: 2;
  padding: 30px 24px 50px;
}

.cta-glass {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
  padding: 50px 40px;
  border-radius: 24px;
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(99,102,241,0.1);
  position: relative;
  overflow: hidden;
}

.cta-aurora {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(99,102,241,0.06), transparent 60%),
              radial-gradient(ellipse at 80% 100%, rgba(6,182,212,0.04), transparent 60%);
  pointer-events: none;
}

.cta-glass h2 {
  position: relative;
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.cta-glass p {
  position: relative;
  font-size: 0.95rem;
  color: #475569;
  margin-bottom: 28px;
}

.cta-row {
  position: relative;
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

/* ===== FOOTER ===== */
.ftr {
  position: relative;
  z-index: 2;
  border-top: 1px solid rgba(0,0,0,0.05);
  background: rgba(255,255,255,0.4);
}

.ftr-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
  text-align: center;
  font-size: 0.78rem;
  color: #94a3b8;
}

.ftr-div { margin: 0 8px; }

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .nav-menu { display: none; }
  .hero { padding: 50px 20px 36px; }
  .hero-headline .line { font-size: 2.2rem; }
  .hero-sub br { display: none; }
  .hero-metrics { gap: 24px; }
  .cards-row { grid-template-columns: 1fr; }
  .caps-grid { grid-template-columns: 1fr; }
  .cap-card { flex-direction: column; text-align: center; }
  .cap-left { flex-direction: column; align-items: center; }
  .cap-metrics { justify-content: center; }
  .cta-glass { padding: 36px 24px; }
}
</style>
