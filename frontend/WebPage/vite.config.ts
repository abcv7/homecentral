import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // 自动按需注册 Naive UI 组件，移除 app.use(naive) 即可只打包实际用到的组件
    Components({
      resolvers: [NaiveUiResolver()],
      // 仅项目源码生效，不影响 node_modules
      dirs: ['src'],
      extensions: ['vue'],
      dts: false,
    }),
    VitePWA({
      registerType: 'autoUpdate',
      injectRegister: 'auto',
      includeAssets: [
        'favicon.svg',
        'icons/icon.svg',
        'icons/pwa-192x192.png',
        'icons/pwa-512x512.png',
        'icons/apple-touch-icon.png',
        'icons/favicon-16x16.png',
        'icons/favicon-32x32.png',
      ],
      manifest: {
        id: '/',
        name: 'HomeCentral · 栖物集',
        short_name: 'HomeCentral',
        description: '家庭中心控制系统：驿站 / 食材 / 备忘 / 好友',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'portrait',
        background_color: '#f8fafc',
        theme_color: '#667eea',
        lang: 'zh-CN',
        categories: ['lifestyle', 'utilities', 'productivity'],
        icons: [
          {
            src: 'icons/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'icons/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'icons/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
          {
            src: 'icons/icon.svg',
            sizes: 'any',
            type: 'image/svg+xml',
            purpose: 'any',
          },
        ],
      },
      workbox: {
        navigateFallback: '/index.html',
        globPatterns: ['**/*.{js,css,html,ico,png,svg,webp,woff,woff2}'],
        cleanupOutdatedCaches: true,
        clientsClaim: true,
        skipWaiting: false,
        maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,
        runtimeCaching: [
          {
            // API GET: stale-while-revalidate, 5min TTL
            urlPattern: ({ url }) => url.pathname.startsWith('/api/') && url.pathname.match(/\/(items|templates|categories|shopping|anniversaries|reminders|parcels|friends|groups|notifications|members|profile)$/),
            handler: 'StaleWhileRevalidate',
            options: {
              cacheName: 'api-get',
              expiration: { maxEntries: 200, maxAgeSeconds: 5 * 60 },
              cacheableResponse: { statuses: [0, 200] },
            },
          },
          {
            // 静态资源：cache-first
            urlPattern: ({ request }) => ['style', 'script', 'worker', 'image', 'font'].includes(request.destination),
            handler: 'CacheFirst',
            options: {
              cacheName: 'assets',
              expiration: { maxEntries: 200, maxAgeSeconds: 30 * 24 * 60 * 60 },
              cacheableResponse: { statuses: [0, 200] },
            },
          },
        ],
      },
      devOptions: {
        enabled: false,
        type: 'module',
      },
    }),
  ],
  server: {
    host: '0.0.0.0',
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE || 'http://localhost:18080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 提高 chunk 警告阈值（precache 模式下整体可接受）
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        // 手动分包：把 vendor libs 拆出独立 chunk，提升缓存命中率
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          // Naive UI + 全部传递依赖（保证不跨 chunk 互相引用）
          if (
            id.includes('naive-ui') ||
            id.includes('@css-render') ||
            id.includes('css-render') ||
            id.includes('@emotion') ||
            id.includes('@juggle') ||
            id.includes('date-fns') ||
            id.includes('evtd') ||
            id.includes('lodash-es') ||
            id.includes('seemly') ||
            id.includes('treemate') ||
            id.includes('vdirs') ||
            id.includes('vooks') ||
            id.includes('vueuc') ||
            id.includes('async-validator')
          ) {
            return 'vendor-naive'
          }
          // Vue 生态（runtime + router + pinia + shared）
          if (
            id.match(/[\\/]@vue[\\/]/) ||
            id.match(/[\\/]vue[\\/]/) ||
            id.match(/[\\/]vue-router[\\/]/) ||
            id.match(/[\\/]pinia[\\/]/) ||
            id.includes('vue-demi')
          ) {
            return 'vendor-vue'
          }
          // 图标库
          if (id.includes('@vicons') || id.includes('ionicons') || id.includes('svg-path-commander') || id.includes('isomorphic-dompurify')) {
            return 'vendor-icons'
          }
          // HTTP
          if (id.includes('axios') || id.includes('follow-redirects') || id.includes('form-data') || id.includes('proxy-from-env')) {
            return 'vendor-http'
          }
          // PWA / workbox
          if (id.includes('workbox')) {
            return 'vendor-pwa'
          }
          // 其他第三方：兜底
          return 'vendor'
        },
      },
    },
  },
})
