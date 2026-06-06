import { createApp } from 'vue'
import naive from 'naive-ui'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

// 在挂载前同步应用主题，避免首屏闪烁
;(function applyInitialTheme() {
  try {
    const stored = localStorage.getItem('homecentral.theme')
    const mode = stored === 'light' || stored === 'dark' || stored === 'auto' ? stored : 'auto'
    const sysDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const resolved = mode === 'auto' ? (sysDark ? 'dark' : 'light') : mode
    document.documentElement.setAttribute('data-theme', resolved)
    const meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement | null
    if (meta) meta.setAttribute('content', resolved === 'dark' ? '#0f172a' : '#f8fafc')
  } catch {}
})()

const app = createApp(App)
app.use(naive)
app.use(createPinia())
app.use(router)
app.mount('#app')

// PWA：注册 Service Worker (autoUpdate) + 监听更新事件
if ('serviceWorker' in navigator) {
  import('virtual:pwa-register')
    .then(({ registerSW }) => {
      registerSW({
        immediate: true,
        onNeedRefresh() {
          const ev = new CustomEvent('pwa:need-refresh')
          window.dispatchEvent(ev)
        },
        onOfflineReady() {
          const ev = new CustomEvent('pwa:offline-ready')
          window.dispatchEvent(ev)
        },
        onRegisteredSW(_swUrl, registration) {
          // 注册成功：30 分钟轮询一次检查更新
          if (registration) {
            setInterval(() => {
              registration.update().catch(() => {})
            }, 30 * 60 * 1000)
          }
        },
        onRegisterError(err) {
          console.warn('[PWA] SW register failed:', err)
        },
      })
    })
    .catch(() => {
      // dev 模式或 vite-plugin-pwa 未启用时静默失败
    })
}
