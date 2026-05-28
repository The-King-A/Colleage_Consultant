<template>
  <div class="mouse-glow" :style="glowStyle"></div>
</template>

<script setup>
import { computed } from 'vue'
import { useMousePosition } from '../../composables/useMousePosition'

const { x, y } = useMousePosition()

const glowStyle = computed(() => ({
  background: `radial-gradient(600px circle at ${x.value}px ${y.value}px, rgba(99, 102, 241, 0.08), transparent 60%)`,
}))
</script>

<style scoped>
.mouse-glow {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  transition: background 0.3s ease;
}
</style>
