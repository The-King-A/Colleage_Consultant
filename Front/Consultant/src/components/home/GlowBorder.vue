<template>
  <div class="glow-border-wrapper" ref="targetRef">
    <div class="glow-border-inner">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useScrollReveal } from '../../composables/useScrollReveal'

const { targetRef } = useScrollReveal({
  from: { y: 40, opacity: 0, scale: 0.95 },
  to: { y: 0, opacity: 1, scale: 1 },
  duration: 0.6,
})
</script>

<style scoped>
.glow-border-wrapper {
  position: relative;
  border-radius: 20px;
  padding: 2px;
  background: conic-gradient(
    from 0deg,
    rgba(99, 102, 241, 0.4),
    rgba(139, 92, 246, 0.4),
    rgba(34, 211, 238, 0.4),
    rgba(99, 102, 241, 0.4)
  );
  animation: border-rotate 4s linear infinite;
}

.glow-border-wrapper:hover {
  background: conic-gradient(
    from 0deg,
    rgba(99, 102, 241, 0.7),
    rgba(139, 92, 246, 0.7),
    rgba(34, 211, 238, 0.7),
    rgba(99, 102, 241, 0.7)
  );
}

.glow-border-inner {
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(20px);
  height: 100%;
  overflow: hidden;
}

@keyframes border-rotate {
  from { filter: hue-rotate(0deg); }
  to { filter: hue-rotate(360deg); }
}
</style>
