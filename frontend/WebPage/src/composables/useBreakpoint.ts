import { computed, onMounted, onUnmounted, ref, readonly, type Ref } from 'vue'

export type Breakpoint = 'mobile' | 'tablet' | 'desktop'

export interface UseBreakpointReturn {
  width: Readonly<Ref<number>>
  height: Readonly<Ref<number>>
  breakpoint: Readonly<Ref<Breakpoint>>
  isMobile: Readonly<Ref<boolean>>
  isTablet: Readonly<Ref<boolean>>
  isDesktop: Readonly<Ref<boolean>>
}

const MOBILE_MAX = 767
const TABLET_MAX = 1023

function classify(w: number): Breakpoint {
  if (w <= MOBILE_MAX) return 'mobile'
  if (w <= TABLET_MAX) return 'tablet'
  return 'desktop'
}

function readViewport(): { w: number; h: number } {
  if (typeof window === 'undefined') return { w: 1280, h: 800 }
  return {
    w: window.innerWidth || document.documentElement.clientWidth || 1280,
    h: window.innerHeight || document.documentElement.clientHeight || 800,
  }
}

let singleton: UseBreakpointReturn | null = null

export function useBreakpoint(): UseBreakpointReturn {
  if (singleton) return singleton

  const initial = readViewport()
  const width = ref(initial.w)
  const height = ref(initial.h)
  const breakpoint = ref<Breakpoint>(classify(initial.w))

  let mqlMobile: MediaQueryList | null = null
  let mqlTablet: MediaQueryList | null = null
  let ro: ResizeObserver | null = null

  function sync() {
    const { w, h } = readViewport()
    width.value = w
    height.value = h
    breakpoint.value = classify(w)
  }

  function onMqlChange() {
    sync()
  }

  function onResize() {
    sync()
  }

  onMounted(() => {
    if (typeof window === 'undefined') return
    sync()
    if (window.matchMedia) {
      mqlMobile = window.matchMedia(`(max-width: ${MOBILE_MAX}px)`)
      mqlTablet = window.matchMedia(`(min-width: ${MOBILE_MAX + 1}px) and (max-width: ${TABLET_MAX}px)`)
      if (mqlMobile.addEventListener) {
        mqlMobile.addEventListener('change', onMqlChange)
        mqlTablet.addEventListener('change', onMqlChange)
      } else {
        mqlMobile.addListener(onMqlChange)
        mqlTablet.addListener(onMqlChange)
      }
    }
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(onResize)
      ro.observe(document.documentElement)
    } else {
      window.addEventListener('resize', onResize, { passive: true })
    }
  })

  onUnmounted(() => {
    if (mqlMobile) {
      if (mqlMobile.removeEventListener) {
        mqlMobile.removeEventListener('change', onMqlChange)
        mqlTablet?.removeEventListener('change', onMqlChange)
      } else {
        mqlMobile.removeListener(onMqlChange)
        mqlTablet?.removeListener(onMqlChange)
      }
    }
    if (ro) {
      ro.disconnect()
      ro = null
    } else {
      window.removeEventListener('resize', onResize)
    }
  })

  const isMobile = readonly(computed(() => breakpoint.value === 'mobile'))
  const isTablet = readonly(computed(() => breakpoint.value === 'tablet'))
  const isDesktop = readonly(computed(() => breakpoint.value === 'desktop'))

  singleton = {
    width: readonly(width) as Readonly<Ref<number>>,
    height: readonly(height) as Readonly<Ref<number>>,
    breakpoint: readonly(breakpoint) as Readonly<Ref<Breakpoint>>,
    isMobile,
    isTablet,
    isDesktop,
  }
  return singleton
}
