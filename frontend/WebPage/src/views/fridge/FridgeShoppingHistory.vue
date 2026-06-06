<template>
  <div class="history" :class="{ 'is-mobile': isMobile }">
    <div class="history-header">
      <div class="title-row">
        <div class="icon-box">
          <n-icon size="18" color="#0ea5e9"><time-outline /></n-icon>
        </div>
        <div>
          <div class="title">采购历史</div>
          <div class="subtitle">点击批次加入采购篮</div>
        </div>
      </div>
      <n-tag :type="items.length > 0 ? 'info' : 'default'" size="small" round>
        {{ items.length }} 批
      </n-tag>
    </div>
    <div class="history-list" :class="{ 'h5-hide-scrollbar': isMobile }">
      <div v-if="!items.length" class="empty">
        <n-icon size="32" color="#cbd5e1"><archive-outline /></n-icon>
        <p>暂无购买历史</p>
        <p class="hint">点击底部"确认购买"后会记录在这里</p>
      </div>
      <div
        v-for="batch in items"
        :key="batch.batchId"
        class="batch-card"
        @click="$emit('open-batch', batch)"
      >
        <div class="batch-head">
          <div class="batch-time">{{ formatTime(batch.purchasedAt) }}</div>
          <n-tag v-if="batch.emailSent" size="tiny" type="success" round>📧 已通知</n-tag>
          <n-tag v-else-if="batch.partnerEmail" size="tiny" type="warning" round>📧 待发送</n-tag>
        </div>
        <div class="batch-items">
          <div v-for="(it, idx) in batch.items.slice(0, 4)" :key="idx" class="batch-item">
            <span class="emoji">{{ emojiOf(it.categoryId) }}</span>
            <span class="name">{{ it.name }}</span>
            <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
          </div>
          <div v-if="batch.items.length > 4" class="batch-more">+{{ batch.items.length - 4 }} 更多</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NIcon, NTag } from 'naive-ui'
import { TimeOutline, ArchiveOutline } from '@vicons/ionicons5'
import type { FridgeCategoryVO, FridgeShoppingHistoryVO } from '../../types/fridge'

const props = defineProps<{
  items: FridgeShoppingHistoryVO[]
  categories: FridgeCategoryVO[]
  isMobile?: boolean
}>()

defineEmits<{
  'open-batch': [batch: FridgeShoppingHistoryVO]
  'reuse-one': [item: { name: string; categoryId?: number; quantity?: number; unit?: string; source?: string }]
}>()

function emojiOf(categoryId?: number) {
  const cat = props.categories.find((c) => c.id === categoryId)
  return cat?.icon ?? '🍽️'
}

function formatTime(iso?: string) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
  if (sameDay) {
    return `今天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) {
    return `昨天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.history {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid #f1f5f9;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 480px;
  max-height: calc(100vh - 240px);
  position: sticky;
  top: 16px;
}
.history.is-mobile {
  background: transparent;
  border: 0;
  box-shadow: none;
  border-radius: 0;
  padding: 0;
  min-height: 0;
  max-height: none;
  position: static;
  gap: 8px;
}
.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.history.is-mobile .history-header {
  display: none;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.icon-box {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #f0f9ff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #bae6fd;
}
.title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}
.subtitle {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}
.history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;
}
.history.is-mobile .history-list {
  padding-right: 0;
}
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  text-align: center;
  gap: 6px;
  font-size: 12px;
}
.empty p {
  margin: 0;
}
.empty .hint {
  font-size: 11px;
  max-width: 220px;
  line-height: 1.5;
}
.batch-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.batch-card:hover {
  background: #f0f9ff;
  border-color: #7dd3fc;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(14, 165, 233, 0.1);
}
.batch-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.batch-time {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
}
.batch-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.batch-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #1e293b;
}
.batch-item .emoji {
  font-size: 14px;
}
.batch-item .name {
  flex: 1;
  font-weight: 500;
}
.batch-item .qty {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 6px;
  font-weight: 700;
  background: #fef3c7;
  color: #b45309;
}
.batch-more {
  font-size: 11px;
  color: #64748b;
  text-align: center;
  padding-top: 2px;
}
</style>
