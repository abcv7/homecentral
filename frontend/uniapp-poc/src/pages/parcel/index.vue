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
  PICKED_UP: { label: '已取件', color: '#3b82f6' },
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
          <button v-if="p.status === 'PENDING_PICKUP'" class="btn-sm" @click="onPickUp(p.id)">取件</button>
          <button v-if="p.status === 'PICKED_UP'" class="btn-sm primary" @click="onReceive(p.id)">收货</button>
        </view>
      </view>
      <view v-if="list.length === 0" class="empty">暂无包裹</view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 16px; }
.header { margin-bottom: 12px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.subtitle { font-size: 13px; color: #888; margin-top: 4px; display: block; }
.filter { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-btn { font-size: 13px; padding: 6px 12px; background: #f5f5f5; color: #333; border: 1px solid #e5e5e5; border-radius: 16px; }
.filter-btn.active { background: #1e293b; color: #fff; border-color: #1e293b; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.list { display: flex; flex-direction: column; gap: 8px; }
.item { background: #fff; border-radius: 8px; padding: 12px; }
.item-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.item-courier { font-size: 15px; font-weight: 600; }
.status { padding: 2px 8px; border-radius: 12px; }
.status-text { font-size: 11px; color: #fff; }
.item-tracking { font-size: 13px; color: #333; display: block; margin-top: 4px; }
.item-meta { display: flex; gap: 8px; margin-top: 4px; }
.item-owner, .item-days { font-size: 12px; color: #888; }
.item-actions { display: flex; gap: 8px; margin-top: 8px; }
.btn-sm { font-size: 12px; padding: 4px 10px; background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; border-radius: 4px; }
.btn-sm.primary { background: #1e293b; color: #fff; border-color: #1e293b; }
.empty { text-align: center; color: #888; padding: 40px 0; }
</style>
