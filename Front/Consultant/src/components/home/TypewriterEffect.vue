<template>
  <div class="typewriter" :class="{ 'typewriter-done': isDone }">
    <span class="typewriter-text">{{ displayText }}</span>
    <span v-if="!isDone" class="typewriter-cursor">|</span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  texts: { type: Array, required: true },
  typeSpeed: { type: Number, default: 80 },
  deleteSpeed: { type: Number, default: 40 },
  pauseDuration: { type: Number, default: 2000 },
  loop: { type: Boolean, default: true },
})

const emit = defineEmits(['done'])

const displayText = ref('')
const isDone = ref(false)
let timer = null
let currentIndex = 0
let charIndex = 0
let isDeleting = false

function type() {
  const currentText = props.texts[currentIndex]

  if (!isDeleting) {
    charIndex++
    displayText.value = currentText.substring(0, charIndex)

    if (charIndex === currentText.length) {
      if (currentIndex === props.texts.length - 1 && !props.loop) {
        isDone.value = true
        emit('done')
        return
      }
      isDeleting = true
      timer = setTimeout(type, props.pauseDuration)
      return
    }
  } else {
    charIndex--
    displayText.value = currentText.substring(0, charIndex)

    if (charIndex === 0) {
      isDeleting = false
      currentIndex = (currentIndex + 1) % props.texts.length
    }
  }

  const speed = isDeleting ? props.deleteSpeed : props.typeSpeed
  timer = setTimeout(type, speed)
}

onMounted(() => {
  timer = setTimeout(type, 500)
})

onUnmounted(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
.typewriter {
  display: inline-block;
  font-size: inherit;
  font-weight: inherit;
  color: inherit;
}

.typewriter-text {
  background: linear-gradient(135deg, #818cf8, #c084fc, #22d3ee);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.typewriter-cursor {
  display: inline-block;
  color: #818cf8;
  font-weight: 300;
  animation: cursor-blink 0.8s infinite;
  margin-left: 2px;
}

.typewriter-done .typewriter-cursor {
  display: none;
}

@keyframes cursor-blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
