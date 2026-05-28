<template>
  <div class="floating-card" :style="floatStyle">
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  duration: { type: Number, default: 6 },
  yOffset: { type: Number, default: 12 },
  delay: { type: Number, default: 0 },
})

const floatStyle = computed(() => ({
  animation: `float-${props.delay} ${props.duration}s ease-in-out infinite`,
  animationDelay: `${props.delay}s`,
}))
</script>

<style scoped>
.floating-card {
  will-change: transform;
}

@keyframes float-0 {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-12px) rotate(0.5deg); }
}

@keyframes float-1 {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(-0.5deg); }
}

@keyframes float-2 {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-14px) rotate(0.3deg); }
}

@keyframes float-3 {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-8px) rotate(-0.3deg); }
}

@keyframes float-4 {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-16px) rotate(0.6deg); }
}
</style>
