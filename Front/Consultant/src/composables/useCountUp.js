import { ref, onMounted, onUnmounted } from 'vue'

export function useCountUp(endValue, duration = 2000, options = {}) {
  const { prefix = '', suffix = '', decimals = 0, separator = ',' } = options
  const displayValue = ref(prefix + '0' + suffix)
  const isCounting = ref(false)
  let animationId = null
  let observer = null
  const targetRef = ref(null)

  function formatNumber(num) {
    const fixed = num.toFixed(decimals)
    const [intPart, decPart] = fixed.split('.')
    const formatted = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, separator)
    return prefix + formatted + (decPart ? '.' + decPart : '') + suffix
  }

  function easeOutExpo(t) {
    return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
  }

  function startCount() {
    if (isCounting.value) return
    isCounting.value = true
    const startTime = performance.now()

    function animate(currentTime) {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easeOutExpo(progress)
      const currentValue = easedProgress * endValue
      displayValue.value = formatNumber(currentValue)

      if (progress < 1) {
        animationId = requestAnimationFrame(animate)
      } else {
        displayValue.value = formatNumber(endValue)
        isCounting.value = false
      }
    }

    animationId = requestAnimationFrame(animate)
  }

  function reset() {
    if (animationId) cancelAnimationFrame(animationId)
    isCounting.value = false
    displayValue.value = formatNumber(0)
  }

  onMounted(() => {
    if (!targetRef.value) return
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            startCount()
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.3 }
    )
    observer.observe(targetRef.value)
  })

  onUnmounted(() => {
    if (animationId) cancelAnimationFrame(animationId)
    if (observer) observer.disconnect()
  })

  return { displayValue, targetRef, isCounting }
}
