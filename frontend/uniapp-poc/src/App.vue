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
/* 全局样式 */
page {
  background-color: #f4f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif;
}
</style>
