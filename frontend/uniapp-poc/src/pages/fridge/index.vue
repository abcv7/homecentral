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
  if (!item.expiryDate) return '#10b981'
  const days = Math.floor((new Date(item.expiryDate).getTime() - Date.now()) / 86400000)
  if (days < 0) return '#ef4444'
  if (days <= 3) return '#ef4444'
  if (days <= 7) return '#f59e0b'
  return '#10b981'
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
      <text class="title">🧊 冰箱</text>
      <text class="subtitle">共 {{ totalQty }} 件食材</text>
    </view>

    <view v-if="stats" class="stats">
      <view class="stat-card stat-expired">
        <text class="stat-num">{{ stats.expired }}</text>
        <text class="stat-label">已过期</text>
      </view>
      <view class="stat-card stat-expiring">
        <text class="stat-num">{{ stats.expiringSoon }}</text>
        <text class="stat-label">临期</text>
      </view>
      <view class="stat-card stat-fresh">
        <text class="stat-num">{{ stats.fresh }}</text>
        <text class="stat-label">新鲜</text>
      </view>
    </view>

    <view v-if="loading" class="loading">加载中…</view>
    <view v-else-if="error" class="loading error-text">错误: {{ error }}</view>

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
          <button class="btn-sm btn-warm" @click="onConsume(item.id)">吃了</button>
          <button class="btn-sm btn-danger" @click="onDiscard(item.id)">丢弃</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 16px 20px;
  padding-bottom: 80px;
  background: var(--qwu-bg, #faf8f5);
  min-height: 100vh;
}
.header {
  margin-bottom: 20px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  display: block;
  color: var(--qwu-text, #1c1917);
  letter-spacing: -0.5px;
}
.subtitle {
  font-size: 13px;
  color: var(--qwu-text-muted, #a8a29e);
  margin-top: 2px;
  display: block;
}
.stats {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.stat-card {
  flex: 1;
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 14px 12px;
  text-align: center;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
  border-left: 4px solid transparent;
}
.stat-expired {
  border-left-color: var(--qwu-danger, #ef4444);
}
.stat-expiring {
  border-left-color: var(--qwu-warning, #f59e0b);
}
.stat-fresh {
  border-left-color: var(--qwu-success, #10b981);
}
.stat-num {
  font-size: 22px;
  font-weight: 800;
  display: block;
  color: var(--qwu-text, #1c1917);
}
.stat-expired .stat-num { color: var(--qwu-danger, #ef4444); }
.stat-expiring .stat-num { color: var(--qwu-warning, #f59e0b); }
.stat-fresh .stat-num { color: var(--qwu-success, #10b981); }
.stat-label {
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
  margin-top: 4px;
  display: block;
}
.loading {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
}
.error-text {
  color: var(--qwu-danger, #ef4444);
}
.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item {
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 16px;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
}
.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--qwu-text, #1c1917);
}
.item-zone {
  font-size: 11px;
  color: var(--qwu-primary, #f97316);
  background: var(--qwu-primary-light, #fff7ed);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.item-expiry {
  font-size: 12px;
  color: var(--qwu-text-secondary, #78716c);
  margin-top: 4px;
  display: block;
}
.item-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.btn-sm {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: var(--qwu-radius-xs, 6px);
  font-weight: 500;
  border: none;
}
.btn-warm {
  background: var(--qwu-primary-light, #fff7ed);
  color: var(--qwu-primary, #f97316);
  border: 1px solid rgba(249, 115, 22, 0.2);
}
.btn-danger {
  background: var(--qwu-danger-light, #fef2f2);
  color: var(--qwu-danger, #ef4444);
  border: 1px solid rgba(239, 68, 68, 0.2);
}
</style>
