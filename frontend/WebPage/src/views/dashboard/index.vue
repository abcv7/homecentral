<template>
  <n-space vertical :size="16">
    <n-h2>仪表盘</n-h2>
    <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
      <n-gi>
        <n-card title="驿站管理" size="small" hoverable @click="$router.push('/parcel')" style="cursor:pointer">
          <n-statistic :value="stats.parcelCount">
            <template #suffix>件</template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="购物清单" size="small" hoverable @click="$router.push('/life')" style="cursor:pointer">
          <n-statistic :value="stats.shoppingCount">
            <template #suffix>项</template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="纪念日" size="small" hoverable @click="$router.push('/life')" style="cursor:pointer">
          <n-statistic :value="stats.anniversaryCount">
            <template #suffix>个</template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="未读通知" size="small" hoverable @click="$router.push('/notification')" style="cursor:pointer">
          <n-statistic :value="stats.unreadNotification">
            <template #suffix>条</template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>
  </n-space>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { listParcels } from '../../api/parcel'
import { listShoppingMemos, listAnniversaries } from '../../api/life'
import { listNotifications } from '../../api/notification'

const stats = reactive({
  parcelCount: 0,
  shoppingCount: 0,
  anniversaryCount: 0,
  unreadNotification: 0,
})

onMounted(async () => {
  try { const r = await listParcels({ page: 1, size: 1 }); stats.parcelCount = r.data.data.totalElements } catch {}
  try { const r = await listShoppingMemos(); stats.shoppingCount = r.data.data.length } catch {}
  try { const r = await listAnniversaries(); stats.anniversaryCount = r.data.data.length } catch {}
  try { const r = await listNotifications(); stats.unreadNotification = r.data.data.filter((n) => !n.read).length } catch {}
})
</script>

<style scoped>
/* Tablet portrait (768-1023) → 2 cols */
@media (max-width: 1023px) {
  :deep(.n-grid) {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
/* Mobile (<=767) → 1 col */
@media (max-width: 767px) {
  :deep(.n-grid) {
    grid-template-columns: 1fr !important;
  }
}
</style>
