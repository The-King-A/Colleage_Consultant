<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  particleCount: { type: Number, default: 80 },
  particleColor: { type: String, default: '99, 102, 241' },
  lineColor: { type: String, default: '99, 102, 241' },
  lineWidth: { type: Number, default: 0.5 },
  particleSize: { type: Number, default: 2 },
  speed: { type: Number, default: 0.3 },
  connectDistance: { type: Number, default: 150 },
})

const canvasRef = ref(null)
let animationId = null
let particles = []
let mouse = { x: null, y: null, radius: 120 }

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.vx = (Math.random() - 0.5) * props.speed
    this.vy = (Math.random() - 0.5) * props.speed
    this.size = Math.random() * props.particleSize + 0.5
    this.opacity = Math.random() * 0.5 + 0.2
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    if (this.x < 0 || this.x > this.canvas.width) this.vx *= -1
    if (this.y < 0 || this.y > this.canvas.height) this.vy *= -1

    if (mouse.x != null && mouse.y != null) {
      const dx = this.x - mouse.x
      const dy = this.y - mouse.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < mouse.radius) {
        const force = (mouse.radius - dist) / mouse.radius
        this.x += dx * force * 0.03
        this.y += dy * force * 0.03
      }
    }
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${props.particleColor}, ${this.opacity})`
    ctx.fill()
  }
}

function initParticles(canvas) {
  particles = []
  for (let i = 0; i < props.particleCount; i++) {
    particles.push(new Particle(canvas))
  }
}

function animate(ctx, canvas) {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach((p, i) => {
    p.update()
    p.draw(ctx)

    for (let j = i + 1; j < particles.length; j++) {
      const dx = p.x - particles[j].x
      const dy = p.y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < props.connectDistance) {
        const opacity = (1 - dist / props.connectDistance) * 0.15
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(${props.lineColor}, ${opacity})`
        ctx.lineWidth = props.lineWidth
        ctx.stroke()
      }
    }

    if (mouse.x != null && mouse.y != null) {
      const dx = p.x - mouse.x
      const dy = p.y - mouse.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < mouse.radius) {
        const opacity = (1 - dist / mouse.radius) * 0.3
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(mouse.x, mouse.y)
        ctx.strokeStyle = `rgba(${props.particleColor}, ${opacity})`
        ctx.lineWidth = props.lineWidth + 0.5
        ctx.stroke()
      }
    }
  })

  animationId = requestAnimationFrame(() => animate(ctx, canvas))
}

function resizeCanvas(canvas) {
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

function onMouseMove(e) {
  const rect = canvasRef.value.getBoundingClientRect()
  mouse.x = e.clientX - rect.left
  mouse.y = e.clientY - rect.top
}

function onMouseLeave() {
  mouse.x = null
  mouse.y = null
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  resizeCanvas(canvas)
  initParticles(canvas)
  animate(ctx, canvas)

  window.addEventListener('resize', () => resizeCanvas(canvas))
  canvas.addEventListener('mousemove', onMouseMove)
  canvas.addEventListener('mouseleave', onMouseLeave)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})
</script>

<style scoped>
.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
