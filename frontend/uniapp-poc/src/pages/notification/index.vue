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
      <view class="header-left">
        <text class="title">🔔 通知</text>
        <view v-if="unreadCount > 0" class="unread-badge">
          <text class="unread-text">{{ unreadCount }}</text>
        </view>
      </view>
      <button v-if="unreadCount > 0" class="read-all-btn" @click="handleReadAll">全部已读</button>
    </view>

    <view v-if="loading" class="loading">加载中…</view>
    <view v-if="!loading && list.length === 0" class="empty">暂无通知</view>

    <view v-if="!loading" class="list">
      <view v-for="item in list" :key="item.id" :class="['item', item.read ? 'item-read' : 'item-unread']">
        <view class="item-row">
          <view v-if="!item.read" class="dot" />
          <text class="item-title">{{ item.title }}</text>
          <text class="item-time">{{ formatTime(item.createdAt) }}</text>
        </view>
        <text v-if="item.content" class="item-content">{{ item.content }}</text>
        <button v-if="!item.read" class="btn-sm btn-warm" @click="handleRead(item.id)">标为已读</button>
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  color: var(--qwu-text, #1c1917);
  letter-spacing: -0.5px;
}
.unread-badge {
  background: var(--qwu-primary, #f97316);
  padding: 2px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.unread-text {
  font-size: 11px;
  color: #fff;
  font-weight: 700;
}
.read-all-btn {
  font-size: 12px;
  padding: 6px 14px;
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-xs, 6px);
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.loading {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
}
.empty {
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
  border-left: 4px solid transparent;
  transition: all 0.2s;
}
.item-unread {
  border-left-color: var(--qwu-primary, #f97316);
  background: var(--qwu-primary-light, #fff7ed);
}
.item-read {
  opacity: 0.7;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 8px;
  height: 8px;
  background: var(--qwu-primary, #f97316);
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.15);
}
.item-title {
  font-size: 14px;
  font-weight: 600;
  flex: 1;
  color: var(--qwu-text, #1c1917);
}
.item-time {
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
  flex-shrink: 0;
}
.item-content {
  font-size: 13px;
  color: var(--qwu-text-secondary, #78716c);
  margin-top: 6px;
  display: block;
  margin-left: 16px;
}
.btn-sm {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: var(--qwu-radius-xs, 6px);
  font-weight: 500;
  border: none;
  margin-top: 10px;
}
.btn-warm {
  background: var(--qwu-primary-light, #fff7ed);
  color: var(--qwu-primary, #f97316);
  border: 1px solid rgba(249, 115, 22, 0.2);
}
</style>
