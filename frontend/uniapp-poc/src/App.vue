<script setup lang="ts">
/**
 * App.vue (uni-app 版)
 * =====================
 * 全局认证守卫：onLaunch 检查 token，无 token 跳登录页
 * tabBar 页的 onShow 也做检查（防止 token 过期后仍停留）
 */
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { getAccessToken } from '@/utils/token'

onLaunch(() => {
  console.log('[uniapp-poc] App Launch')
  checkAuth()
})

onShow(() => {
  console.log('[uniapp-poc] App Show')
})

function checkAuth() {
  const token = getAccessToken()
  if (!token) {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    const currentPath = '/' + (current?.route ?? '')
    if (currentPath !== '/pages/login/index' && currentPath !== '/pages/register/index') {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
}
</script>

<style>
page {
  --qwu-primary: #f97316;
  --qwu-primary-light: #fff7ed;
  --qwu-primary-dark: #c2410c;
  --qwu-accent: #fbbf24;
  --qwu-accent-light: #fef9c3;
  --qwu-danger: #ef4444;
  --qwu-danger-light: #fef2f2;
  --qwu-success: #10b981;
  --qwu-success-light: #ecfdf5;
  --qwu-warning: #f59e0b;
  --qwu-warning-light: #fffbeb;
  --qwu-bg: #faf8f5;
  --qwu-card: #ffffff;
  --qwu-text: #1c1917;
  --qwu-text-secondary: #78716c;
  --qwu-text-muted: #a8a29e;
  --qwu-border: #e7e5e4;
  --qwu-border-light: #f5f5f4;
  --qwu-shadow: 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04);
  --qwu-shadow-md: 0 4px 6px -1px rgba(28,25,23,0.07), 0 2px 4px -2px rgba(28,25,23,0.05);
  --qwu-radius: 14px;
  --qwu-radius-sm: 10px;
  --qwu-radius-xs: 6px;

  background-color: var(--qwu-bg);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", "Microsoft YaHei", sans-serif;
  color: var(--qwu-text);
  font-size: 15px;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}
</style>
