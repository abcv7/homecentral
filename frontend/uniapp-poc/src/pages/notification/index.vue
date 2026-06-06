<script setup lang="ts">
/**
 * 通知页 (uni-app 版)
 * 通知列表 + 标已读 + 全部标已读
 */
import { ref, computed } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import { listNotifications, readNotification, readAllNotifications } from '@/api/notification'
import type { NotificationVO } from '@/types/api'

const list = ref<NotificationVO[]>([])
const loading = ref(false)
const unreadCount = computed(() => list.value.filter((n) => !n.read).length)

async function fetchList() {
  loading.value = true
  try {
    const res = await listNotifications()
    list.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

uniOnShow(() => fetchList())

async function handleRead(id: number) {
  try {
    await readNotification(id)
    await fetchList()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReadAll() {
  try {
    await readAllNotifications()
    await fetchList()
    uni.showToast({ title: '全部已读', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function formatTime(time: string | undefined): string {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">🔔 通知</text>
      <button v-if="unreadCount > 0" class="read-all-btn" @click="handleReadAll">全部标为已读</button>
    </view>

    <view v-if="loading" class="loading">加载中…</view>
    <view v-if="!loading && list.length === 0" class="empty">暂无通知</view>

    <view v-if="!loading" class="list">
      <view v-for="item in list" :key="item.id" class="item">
        <view class="item-row">
          <view v-if="!item.read" class="dot" />
          <text class="item-title">{{ item.title }}</text>
          <text class="item-time">{{ formatTime(item.createdAt) }}</text>
        </view>
        <text v-if="item.content" class="item-content">{{ item.content }}</text>
        <button v-if="!item.read" class="btn-sm" @click="handleRead(item.id)">标为已读</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 24px; font-weight: 700; }
.read-all-btn { font-size: 12px; padding: 4px 12px; background: #667eea; color: #fff; border: none; border-radius: 6px; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.empty { text-align: center; color: #888; padding: 40px 0; }
.list { display: flex; flex-direction: column; gap: 8px; }
.item { background: #fff; border-radius: 8px; padding: 12px; }
.item-row { display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; background: #f56c6c; border-radius: 4px; flex-shrink: 0; }
.item-title { font-size: 14px; font-weight: 600; flex: 1; }
.item-time { font-size: 12px; color: #888; }
.item-content { font-size: 13px; color: #666; margin-top: 4px; display: block; }
.btn-sm { font-size: 12px; padding: 4px 10px; background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; border-radius: 4px; margin-top: 8px; }
</style>