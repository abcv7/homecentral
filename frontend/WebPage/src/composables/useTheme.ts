import { ref, computed, watch, onMounted, onUnmounted, readonly } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'
export type ResolvedTheme = 'light' | 'dark'

const STORAGE_KEY = 'homecentral.theme'

const mode = ref<ThemeMode>('auto')
const systemPrefersDark = ref(false)
let initialized = false
let mediaQuery: MediaQueryList | null = null

function readStoredMode(): ThemeMode {
  try {
    const v = localStorage.getItem(STORAGE_KEY)
    if (v === 'light' || v === 'dark' || v === 'auto') return v
  } catch {}
  return 'auto'
}

function writeStoredMode(v: ThemeMode) {
  try {
    localStorage.setItem(STORAGE_KEY, v)
  } catch {}
}

function applyThemeAttribute(resolved: ResolvedTheme) {
  if (typeof document === 'undefined') return
  document.documentElement.setAttribute('data-theme', resolved)
  // 同步 iOS Safari 顶部栏 / Android Chrome 主题色
  const meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement | null
  if (meta) {
    meta.setAttribute('content', resolved === 'dark' ? '#0f172a' : '#f8fafc')
  }
}

function systemThemeHandler(e: MediaQueryListEvent | MediaQueryList) {
  systemPrefersDark.value = e.matches
}

export function useTheme() {
  if (!initialized) {
    initialized = true
    if (typeof window !== 'undefined') {
      mode.value = readStoredMode()
      mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      systemPrefersDark.value = mediaQuery.matches
      mediaQuery.addEventListener('change', systemThemeHandler)
    }
  }

  const resolved = computed<ResolvedTheme>(() => {
    if (mode.value === 'auto') return systemPrefersDark.value ? 'dark' : 'light'
    return mode.value
  })

  // 首次 mount 时立即应用，避免闪烁
  onMounted(() => {
    applyThemeAttribute(resolved.value)
  })

  // 监听 resolved 变化 → 写 data-theme 与 theme-color
  watch(resolved, (v) => {
    applyThemeAttribute(v)
  }, { immediate: true })

  // 监听 mode 变化 → 持久化
  watch(mode, (v) => {
    writeStoredMode(v)
  })

  onUnmounted(() => {
    // 全局单例，不要解绑 mediaQuery（其它组件可能仍在用）
  })

  function setMode(v: ThemeMode) {
    mode.value = v
  }

  function cycleMode() {
    const order: ThemeMode[] = ['light', 'dark', 'auto']
    const idx = order.indexOf(mode.value)
    mode.value = order[(idx + 1) % order.length]
  }

  return {
    mode: readonly(mode),
    resolved,
    setMode,
    cycleMode,
  }
}
