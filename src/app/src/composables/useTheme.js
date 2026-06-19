import { ref } from 'vue'
import { useColors } from 'vuestic-ui'

const STORAGE_KEY = 'memowand-theme'

export function getInitialTheme() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === 'light' || saved === 'dark') return saved
  } catch {
    // localStorage may be unavailable
  }
  const prefersDark =
    typeof window !== 'undefined' &&
    window.matchMedia &&
    window.matchMedia('(prefers-color-scheme: dark)').matches
  return prefersDark ? 'dark' : 'light'
}

// Module-singleton so every component shares the same reactive value.
const theme = ref(getInitialTheme())

export function useTheme() {
  const { applyPreset } = useColors()

  function apply(value) {
    theme.value = value
    document.documentElement.classList.toggle('dark', value === 'dark')
    applyPreset(value)
    try {
      localStorage.setItem(STORAGE_KEY, value)
    } catch {
      // best-effort persistence
    }
  }

  function init() {
    apply(theme.value)
  }

  function setTheme(value) {
    apply(value === 'dark' ? 'dark' : 'light')
  }

  function toggleTheme() {
    apply(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, init, setTheme, toggleTheme }
}
