<template>
  <div ref="targetRef" class="scroll-reveal">
    <slot />
  </div>
</template>

<script setup>
import { useScrollReveal } from '../../composables/useScrollReveal'

const props = defineProps({
  from: { type: Object, default: () => ({ y: 60, opacity: 0 }) },
  to: { type: Object, default: () => ({ y: 0, opacity: 1 }) },
  duration: { type: Number, default: 0.8 },
  delay: { type: Number, default: 0 },
  ease: { type: String, default: 'power3.out' },
  stagger: { type: Number, default: 0 },
  start: { type: String, default: 'top 85%' },
  once: { type: Boolean, default: true },
})

const { targetRef } = useScrollReveal({
  from: props.from,
  to: props.to,
  duration: props.duration,
  delay: props.delay,
  ease: props.ease,
  stagger: props.stagger,
  start: props.start,
  once: props.once,
})
</script>

<style scoped>
.scroll-reveal {
  /* ensure block-level reveal */
}
</style>
