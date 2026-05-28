<template>
  <div class="feature-card" @mousemove="onMouseMove" @mouseleave="onMouseLeave" ref="cardRef">
    <div class="card-glow" :style="glowStyle"></div>
    <div class="card-content">
      <div class="card-icon" :style="{ background: iconBg }">
        <el-icon :size="24">
          <component :is="icon" />
        </el-icon>
      </div>
      <h3 class="card-title">{{ title }}</h3>
      <p class="card-desc">{{ desc }}</p>
      <div class="card-tags">
        <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  icon: { type: Object, required: true },
  iconBg: { type: String, default: 'linear-gradient(135deg, #6366f1, #8b5cf6)' },
  title: { type: String, required: true },
  desc: { type: String, required: true },
  tags: { type: Array, default: () => [] },
})

const cardRef = ref(null)
const rotateX = ref(0)
const rotateY = ref(0)
const glowX = ref(50)
const glowY = ref(50)

function onMouseMove(e) {
  if (!cardRef.value) return
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  rotateY.value = ((x - centerX) / centerX) * 8
  rotateX.value = -((y - centerY) / centerY) * 8
  glowX.value = (x / rect.width) * 100
  glowY.value = (y / rect.height) * 100
}

function onMouseLeave() {
  rotateX.value = 0
  rotateY.value = 0
  glowX.value = 50
  glowY.value = 50
}

const glowStyle = computed(() => ({
  background: `radial-gradient(circle at ${glowX.value}% ${glowY.value}%, rgba(99, 102, 241, 0.15), transparent 60%)`,
}))

const cardStyle = computed(() => ({
  transform: `perspective(800px) rotateX(${rotateX.value}deg) rotateY(${rotateY.value}deg)`,
}))
</script>

<style scoped>
.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.feature-card:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 40px rgba(99, 102, 241, 0.1);
}

.card-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.feature-card:hover .card-glow {
  opacity: 1;
}

.card-content {
  position: relative;
  z-index: 1;
  padding: 36px 28px 28px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 22px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.2);
}

.card-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #e2e8f0;
  margin-bottom: 10px;
}

.card-desc {
  font-size: 0.9rem;
  color: #94a3b8;
  line-height: 1.7;
  margin-bottom: 18px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 0.75rem;
  padding: 4px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: #94a3b8;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.06);
}
</style>
