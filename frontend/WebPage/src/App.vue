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
import { computed } from 'vue'
import { NConfigProvider, darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import { useTheme } from './composables/useTheme'

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
</script>
