<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('[uniapp-poc] App Launch')
})
onShow(() => {
  console.log('[uniapp-poc] App Show')
})
</script>

<style>
/* 全局样式 */
page {
  background-color: #f4f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif;
}
</style>
