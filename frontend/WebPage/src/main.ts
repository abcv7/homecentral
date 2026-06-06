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
