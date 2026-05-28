import { onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

export function useScrollReveal(options = {}) {
  const {
    from = { y: 60, opacity: 0 },
    to = { y: 0, opacity: 1 },
    duration = 0.8,
    delay = 0,
    ease = 'power3.out',
    stagger = 0,
    start = 'top 85%',
    once = true,
  } = options

  const targetRef = ref(null)
  let trigger = null
  let tween = null

  onMounted(() => {
    if (!targetRef.value) return
    tween = gsap.fromTo(targetRef.value, from, {
      ...to,
      duration,
      delay,
      ease,
      stagger,
      scrollTrigger: {
        trigger: targetRef.value,
        start,
        toggleActions: once ? 'play none none none' : 'play none none reverse',
      },
    })
    trigger = tween.scrollTrigger
  })

  onUnmounted(() => {
    if (trigger) trigger.kill()
    if (tween) tween.kill()
  })

  return { targetRef }
}
