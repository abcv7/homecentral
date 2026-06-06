<template>
  <n-config-provider :theme="theme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue'
import { NConfigProvider, darkTheme, useNotification, type GlobalThemeOverrides } from 'naive-ui'
import { useTheme } from './composables/useTheme'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
authStore.hydrate()

const { resolved } = useTheme()

const theme = computed(() => (resolved.value === 'dark' ? darkTheme : null))

// 主题色微调：暗色下主色稍亮，文字更柔和
const themeOverrides = computed<GlobalThemeOverrides>(() => {
  if (resolved.value === 'dark') {
    return {
      common: {
        primaryColor: '#818cf8',
        primaryColorHover: '#a5b4fc',
        primaryColorPressed: '#6366f1',
        bodyColor: '#0f172a',
        cardColor: '#1e293b',
        modalColor: '#1e293b',
        popoverColor: '#1e293b',
        textColorBase: '#f1f5f9',
        placeholderColor: 'rgba(241, 245, 249, 0.4)',
        dividerColor: 'rgba(241, 245, 249, 0.12)',
        borderColor: '#334155',
        inputColor: '#0f172a',
        tableHeaderColor: '#1e293b',
        actionColor: '#1e293b',
      },
    }
  }
  return {}
})

// PWA：监听 SW 更新与离线就绪事件
let notificationApi: ReturnType<typeof useNotification> | null = null

function bindNotificationApi() {
  // useNotification 必须在 n-notification-provider 子树内调用
  // 顶层 setup 中暂不可用，等 router-view 挂载后再取
  try {
    notificationApi = useNotification()
  } catch {}
}

function onNeedRefresh() {
  if (!notificationApi) bindNotificationApi()
  if (!notificationApi) return
  notificationApi.create({
    title: '🔄 有新版本可用',
    content: '关闭应用后下次打开将自动应用最新版本',
    type: 'info',
    duration: 8000,
    closable: true,
  })
}

function onOfflineReady() {
  if (!notificationApi) bindNotificationApi()
  if (!notificationApi) return
  notificationApi.create({
    title: '📦 离线缓存已就绪',
    content: '断网后仍可访问已缓存的页面',
    type: 'success',
    duration: 5000,
    closable: true,
  })
}

onMounted(() => {
  bindNotificationApi()
  window.addEventListener('pwa:need-refresh', onNeedRefresh)
  window.addEventListener('pwa:offline-ready', onOfflineReady)
})

onBeforeUnmount(() => {
  window.removeEventListener('pwa:need-refresh', onNeedRefresh)
  window.removeEventListener('pwa:offline-ready', onOfflineReady)
})
</script>
