<script setup lang="ts">
/**
 * 包裹 view (uni-app 版)
 * 简化版:只展示列表 + 状态过滤
 * 拍照识别/分享留待完整 H5 页
 */
import { computed, ref } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import { listParcels, pickUpParcel, receiveParcel } from '@/api/parcel'
import type { ParcelVO, ParcelStatus } from '@/types/api'

const list = ref<ParcelVO[]>([])
const loading = ref(false)
const statusFilter = ref<ParcelStatus | ''>('')

async function load() {
  loading.value = true
  try {
    const res = await listParcels(statusFilter.value ? { status: statusFilter.value } : undefined)
    const data = res.data.data
    if (Array.isArray(data)) list.value = data
    else if (data && typeof data === 'object' && 'records' in data && Array.isArray((data as { records: unknown }).records)) {
      list.value = (data as { records: ParcelVO[] }).records
    } else {
      list.value = []
    }
  } finally {
    loading.value = false
  }
}

uniOnShow(() => load())

function setStatus(s: ParcelStatus | '') {
  statusFilter.value = s
  load()
}

const total = computed(() => list.value.length)
const pendingCount = computed(() => list.value.filter((p: ParcelVO) => p.status === 'PENDING_PICKUP').length)
const pickedUpCount = computed(() => list.value.filter((p: ParcelVO) => p.status === 'PICKED_UP').length)

async function onPickUp(id: number) {
  await pickUpParcel(id)
  await load()
}

async function onReceive(id: number) {
  await receiveParcel(id)
  await load()
}

const STATUS_MAP: Record<ParcelStatus, { label: string; color: string }> = {
  PENDING_PICKUP: { label: '待取件', color: '#f59e0b' },
  PICKED_UP: { label: '已取件', color: '#f97316' },
  RECEIVED: { label: '已收货', color: '#10b981' },
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">📦 驿站</text>
      <text class="subtitle">共 {{ total }} 件 · 待取 {{ pendingCount }} · 已取 {{ pickedUpCount }}</text>
    </view>

    <view class="filter">
      <button :class="['filter-btn', statusFilter === '' ? 'active' : '']" @click="setStatus('')">全部</button>
      <button :class="['filter-btn', statusFilter === 'PENDING_PICKUP' ? 'active' : '']" @click="setStatus('PENDING_PICKUP')">待取件</button>
      <button :class="['filter-btn', statusFilter === 'PICKED_UP' ? 'active' : '']" @click="setStatus('PICKED_UP')">已取件</button>
      <button :class="['filter-btn', statusFilter === 'RECEIVED' ? 'active' : '']" @click="setStatus('RECEIVED')">已收货</button>
    </view>

    <view v-if="loading" class="loading">加载中…</view>

    <view v-else class="list">
      <view
        v-for="p in list"
        :key="p.id"
        class="item"
        :style="{ borderLeft: `4px solid ${STATUS_MAP[p.status].color}` }"
      >
        <view class="item-row">
          <text class="item-courier">{{ p.courierCompany || '未知快递' }}</text>
          <view class="status" :style="{ background: STATUS_MAP[p.status].color }">
            <text class="status-text">{{ STATUS_MAP[p.status].label }}</text>
          </view>
        </view>
        <text class="item-tracking">运单: {{ p.trackingNumber }}</text>
        <view class="item-meta">
          <text class="item-owner">归属: {{ p.ownerName || '我' }}</text>
          <text v-if="p.daysAtStation !== undefined" class="item-days">{{ p.daysAtStation }} 天</text>
        </view>
        <view v-if="p.status !== 'RECEIVED'" class="item-actions">
          <button v-if="p.status === 'PENDING_PICKUP'" class="btn-sm btn-warm" @click="onPickUp(p.id)">取件</button>
          <button v-if="p.status === 'PICKED_UP'" class="btn-sm btn-primary" @click="onReceive(p.id)">收货</button>
        </view>
      </view>
      <view v-if="list.length === 0" class="empty">暂无包裹</view>
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
  margin-bottom: 16px;
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
.filter {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.filter-btn {
  font-size: 13px;
  padding: 6px 14px;
  background: var(--qwu-card, #ffffff);
  color: var(--qwu-text-secondary, #78716c);
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: 20px;
  font-weight: 500;
}
.filter-btn.active {
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border-color: var(--qwu-primary, #f97316);
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.loading {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
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
  margin-bottom: 6px;
}
.item-courier {
  font-size: 15px;
  font-weight: 600;
  color: var(--qwu-text, #1c1917);
}
.status {
  padding: 3px 10px;
  border-radius: 10px;
}
.status-text {
  font-size: 11px;
  color: #fff;
  font-weight: 600;
}
.item-tracking {
  font-size: 13px;
  color: var(--qwu-text-secondary, #78716c);
  display: block;
  margin-top: 4px;
}
.item-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.item-owner, .item-days {
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
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
.btn-primary {
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.empty {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
}
</style>
