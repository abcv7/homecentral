<script setup lang="ts">
/**
 * 冰箱 view (uni-app 版)
 * 复刻 web 端 useFridgeData + FridgeView 的核心展示
 * - 去掉 Naive UI 组件,改用 <view>/<text>/<button> + scoped CSS
 * - 数据走本地 api/fridge (基于 uni.request, 支持 H5 + 微信小程序)
 */
import { computed, ref } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import { listItems, getExpiringStats, consumeItem, discardItem } from '@/api/fridge'
import type { FridgeItemVO, FridgeExpiringVO } from '@/types/fridge'

const items = ref<FridgeItemVO[]>([])
const stats = ref<FridgeExpiringVO | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

function extractList(data: unknown): FridgeItemVO[] {
  if (Array.isArray(data)) return data as FridgeItemVO[]
  if (data && typeof data === 'object' && 'records' in data && Array.isArray((data as { records: unknown }).records)) {
    return (data as { records: FridgeItemVO[] }).records
  }
  return []
}

async function load() {
  loading.value = true
  error.value = null
  try {
    const [itemsRes, statsRes] = await Promise.all([
      listItems(),
      getExpiringStats(),
    ])
    items.value = extractList(itemsRes.data.data)
    stats.value = statsRes.data.data
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

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

async function onConsume(id: number) {
  await consumeItem(id)
  await load()
}

async function onDiscard(id: number) {
  await discardItem(id)
  await load()
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">🥶 冰箱</text>
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
    <view v-else-if="error" class="loading">错误: {{ error }}</view>

    <view v-else class="list">
      <view
        v-for="item in items"
        :key="item.id"
        class="item"
        :style="{ borderLeft: `4px solid ${highlightColor(item)}` }"
      >
        <view class="item-row">
          <text class="item-name">{{ item.name }}</text>
          <text v-if="item.zone" class="item-zone">{{ item.zone }}</text>
        </view>
        <text v-if="item.expiryDate" class="item-expiry">
          过期: {{ item.expiryDate }}
        </text>
        <view class="item-actions">
          <button class="btn-sm" @click="onConsume(item.id)">吃了</button>
          <button class="btn-sm danger" @click="onDiscard(item.id)">丢弃</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 16px; }
.header { margin-bottom: 16px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.subtitle { font-size: 14px; color: #888; margin-top: 4px; display: block; }
.stats { display: flex; gap: 12px; margin-bottom: 16px; }
.stat-card { flex: 1; background: #fff; border-radius: 8px; padding: 12px; text-align: center; }
.stat-num { font-size: 20px; font-weight: 700; display: block; }
.stat-label { font-size: 12px; color: #888; margin-top: 4px; display: block; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.list { display: flex; flex-direction: column; gap: 8px; }
.item { background: #fff; border-radius: 8px; padding: 12px; }
.item-row { display: flex; justify-content: space-between; align-items: center; }
.item-name { font-size: 15px; font-weight: 600; }
.item-zone { font-size: 11px; color: #999; background: #f5f5f5; padding: 2px 6px; border-radius: 4px; }
.item-expiry { font-size: 12px; color: #888; margin-top: 2px; display: block; }
.item-actions { display: flex; gap: 8px; margin-top: 8px; }
.btn-sm { font-size: 12px; padding: 4px 10px; background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; border-radius: 4px; }
.btn-sm.danger { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
</style>
