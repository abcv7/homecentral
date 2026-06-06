<script setup lang="ts">
/**
 * uni-app 改写版 (POC)
 * 复用了主项目 useFridgeData composable + 部分 utils
 * uni-app 组件命名: <view> <text> <button> 等内置组件
 */
import { computed, ref, onMounted } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import {
  listItems,
  getExpiringStats,
} from '@shared-api/fridge'
import type { FridgeItemVO, FridgeExpiringVO } from '@shared-types/fridge'

const items = ref<FridgeItemVO[]>([])
const stats = ref<FridgeExpiringVO | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const [list, stat] = await Promise.all([
      listItems(),
      getExpiringStats(),
    ])
    items.value = list
    stats.value = stat
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
uniOnShow(() => load())

const totalQty = computed(() => items.value.length)

function highlightColor(item: FridgeItemVO): string {
  if (!item.expiryDate) return '#67c23a'
  const days = Math.floor((new Date(item.expiryDate).getTime() - Date.now()) / 86400000)
  if (days < 0) return '#f56c6c'
  if (days <= 3) return '#f56c6c'
  if (days <= 7) return '#e6a23c'
  return '#67c23a'
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">🥶 冰箱 (uni-app POC)</text>
      <text class="subtitle">共 {{ totalQty }} 件</text>
    </view>

    <view v-if="stats" class="stats">
      <view class="stat-card">
        <text class="stat-num">{{ stats.expired }}</text>
        <text class="stat-label">已过期</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.expiringSoon }}</text>
        <text class="stat-label">临期</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.fresh }}</text>
        <text class="stat-label">新鲜</text>
      </view>
    </view>

    <view v-if="loading" class="loading">加载中…</view>

    <view v-else class="list">
      <view
        v-for="item in items"
        :key="item.id"
        class="item"
        :style="{ borderLeft: `4px solid ${highlightColor(item)}` }"
      >
        <text class="item-name">{{ item.name }}</text>
        <text v-if="item.expiryDate" class="item-expiry">
          过期: {{ item.expiryDate }}
        </text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 16px;
}
.header {
  margin-bottom: 16px;
}
.title {
  font-size: 24px;
  font-weight: 700;
  display: block;
}
.subtitle {
  font-size: 14px;
  color: #888;
  margin-top: 4px;
  display: block;
}
.stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  display: block;
}
.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  display: block;
}
.loading {
  text-align: center;
  color: #888;
  padding: 40px 0;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
}
.item-name {
  font-size: 15px;
  font-weight: 600;
  display: block;
}
.item-expiry {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
  display: block;
}
</style>
