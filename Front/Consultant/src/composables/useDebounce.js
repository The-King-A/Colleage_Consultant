/**
 * @param {(...args: unknown[]) => void} fn
 * @param {number} delay ms
 */
export function useDebounce(fn, delay = 300) {
  let timer = null
  const debounced = (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
  debounced.cancel = () => {
    if (timer) clearTimeout(timer)
    timer = null
  }
  return debounced
}
