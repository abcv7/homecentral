<template>
  <div class="card-view" :class="{ 'is-mobile': isMobile }">
    <!-- 顶部快捷入口（手机端：采购篮/历史；桌面端：摘要） -->
    <div v-if="!isMobile" class="cv-summary">
      <n-tag :type="basketItems.length > 0 ? 'warning' : 'default'" round>
        🛒 采购篮 {{ basketItems.length }} 件
      </n-tag>
      <n-tag type="info" round>🕘 历史 {{ history.length }} 批</n-tag>
    </div>
    <div v-else class="cv-quick-row">
      <button class="quick-btn" @click="$emit('open-basket')">
        <n-icon size="20" color="#f97316"><basket-outline /></n-icon>
        <span>采购篮</span>
        <span class="badge">{{ basketItems.length }}</span>
      </button>
      <button class="quick-btn" @click="$emit('open-history')">
        <n-icon size="20" color="#0ea5e9"><time-outline /></n-icon>
        <span>历史</span>
        <span class="badge">{{ history.length }}</span>
      </button>
    </div>

    <!-- 临期高亮 banner -->
    <div v-if="expiringItems.length > 0" class="cv-banner">
      <n-icon size="18" color="#f59e0b"><warning-outline /></n-icon>
      <span><b>{{ expiringItems.length }}</b> 项食材 {{ expiredCount > 0 ? `已过期 ${expiredCount} 项，` : '' }}3 天内到期 {{ expiringSoonCount }} 项</span>
    </div>

    <!-- 分区折叠面板 -->
    <n-collapse v-model:expanded-names="expanded" :default-expanded-names="defaultExpanded" :trigger-areas="['main', 'arrow']" arrow-placement="right">
      <n-collapse-item v-for="z in zonesToShow" :key="z.key" :name="z.key">
        <template #header-extra>
          <span class="zone-count">{{ z.items.length }} 件</span>
        </template>
        <template #header>
          <div class="zone-header-row">
            <span class="zone-emoji">{{ z.emoji }}</span>
            <span class="zone-name">{{ z.label }}</span>
          </div>
        </template>

        <div v-if="z.items.length === 0" class="empty-zone">
          <n-icon size="32" color="#cbd5e1"><leaf-outline /></n-icon>
          <p>暂无食材</p>
        </div>
        <div v-else class="cv-grid">
          <div
            v-for="(it, idx) in expandByQuantity(z.items)"
            :key="`${it.id}-${idx}`"
            class="cv-card"
            :class="expiryClass(it.daysToExpiry)"
            @click="$emit('item-click', it)"
          >
            <div class="cv-card-top">
              <span class="emoji">{{ it.categoryIcon || '🍽️' }}</span>
              <span class="name">{{ it.name }}</span>
              <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
            </div>
            <div class="cv-card-bot">
              <span v-if="it.daysToExpiry != null" class="days" :class="expiryClass(it.daysToExpiry)">
                <n-icon size="12"><calendar-outline /></n-icon>
                {{ it.daysToExpiry < 0 ? `已过${-it.daysToExpiry}天` : `${it.daysToExpiry}天` }}
              </span>
              <span v-else class="days muted">无保质期</span>
              <span v-if="it.subZone" class="loc">{{ it.subZone }}</span>
            </div>
          </div>
        </div>
      </n-collapse-item>
    </n-collapse>

    <!-- 历史（桌面端直接列出） -->
    <div v-if="!isMobile && history.length > 0" class="cv-history">
      <h4 class="section-title">🕘 采购历史</h4>
      <div class="cv-history-list">
        <div
          v-for="batch in history.slice(0, 6)"
          :key="batch.batchId"
          class="cv-history-card"
          @click="$emit('open-history-batch', batch)"
        >
          <div class="cv-history-head">
            <span class="cv-history-time">{{ formatTime(batch.purchasedAt) }}</span>
            <n-tag v-if="batch.emailSent" size="tiny" type="success" round>📧 已通知</n-tag>
          </div>
          <div class="cv-history-items">
            <span v-for="(it, i) in batch.items.slice(0, 4)" :key="i" class="cv-history-item">
              {{ emojiOf(it.categoryId) }} {{ it.name }}<span v-if="(it.quantity ?? 1) > 1">×{{ it.quantity }}</span>
            </span>
            <span v-if="batch.items.length > 4" class="cv-history-more">+{{ batch.items.length - 4 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { NTag, NCollapse, NCollapseItem, NIcon } from 'naive-ui'
import {
  BasketOutline, TimeOutline, LeafOutline, CalendarOutline, WarningOutline,
} from '@vicons/ionicons5'
import type { FridgeCategoryVO, FridgeItemVO, FridgeShoppingHistoryVO, FridgeTemplateVO, FridgeZone } from '../../types/fridge'
import { expandByQuantity } from '../../utils/fridgeExpand'
import { resolvePosition, type FridgeLocation } from '../../utils/subZoneMapping'

const props = defineProps<{
  items: FridgeItemVO[]
  categories: FridgeCategoryVO[]
  basketItems: FridgeItemVO[]
  history: FridgeShoppingHistoryVO[]
  isMobile: boolean
  template: FridgeTemplateVO | null
}>()

defineEmits<{
  'item-click': [item: FridgeItemVO]
  'open-history-batch': [batch: FridgeShoppingHistoryVO]
  'reuse-history-one': [item: { name: string; categoryId?: number; quantity?: number; unit?: string; source?: string }]
  'open-basket': []
  'open-history': []
}>()

const expanded = ref<string[]>(['REFRIGERATED', 'FROZEN'])
const defaultExpanded = ['REFRIGERATED', 'FROZEN']

interface ZoneCard {
  key: string
  label: string
  emoji: string
  items: FridgeItemVO[]
  zone: FridgeZone | null
  location: FridgeLocation
}

const zonesToShow = computed<ZoneCard[]>(() => {
  const t = props.template
  const showChiller = t?.layout === 'THREE_DOOR' && (t.chillerLayers ?? 0) > 0
  const list: ZoneCard[] = [
    { key: 'REFRIGERATED', label: '🥬 冷藏区', emoji: '🥬', items: [], zone: 'REFRIGERATED', location: 'fridge' },
    { key: 'FROZEN',       label: '❄️ 冷冻区', emoji: '❄️', items: [], zone: 'FROZEN',       location: 'freezer' },
  ]
  if (showChiller) {
    list.push({ key: 'CHILLER', label: '🧊 解冻区', emoji: '🧊', items: [], zone: null, location: 'chiller' })
  }
  for (const it of props.items) {
    if (it.status !== 'ACTIVE') continue
    const pos = resolvePosition(it)
    if (pos.location === 'fridge') list[0].items.push(it)
    else if (pos.location === 'freezer') list[1].items.push(it)
    else if (pos.location === 'chiller' && showChiller) list[2].items.push(it)
  }
  return list
})

const expiringItems = computed(() => props.items.filter((it) => {
  if (it.status !== 'ACTIVE' || it.daysToExpiry == null) return false
  return it.daysToExpiry <= 3
}))

const expiredCount = computed(() => expiringItems.value.filter((it) => (it.daysToExpiry ?? 0) < 0).length)
const expiringSoonCount = computed(() => expiringItems.value.filter((it) => (it.daysToExpiry ?? 0) >= 0).length)

function expiryClass(days?: number) {
  if (days == null) return ''
  if (days < 0) return 'exp-red'
  if (days <= 3) return 'exp-red'
  if (days <= 7) return 'exp-yellow'
  return 'exp-green'
}

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
.card-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cv-summary {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cv-quick-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.quick-btn {
  appearance: none;
  background: var(--bg-surface);
  border: 1px solid var(--border-2);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
  position: relative;
}
.quick-btn .badge {
  margin-left: auto;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  background: #fff7ed;
  color: #b45309;
  border-radius: 999px;
}
.cv-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #fcd34d;
  border-radius: 12px;
  font-size: 13px;
  color: #78350f;
  font-weight: 500;
}

.zone-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.zone-emoji {
  font-size: 18px;
}
.zone-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}
.zone-count {
  font-size: 12px;
  color: var(--text-3);
  font-weight: 600;
  margin-right: 4px;
}

.empty-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 0;
  color: var(--text-3);
  gap: 6px;
  font-size: 13px;
}

.cv-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  padding: 4px 0;
}
.card-view.is-mobile .cv-grid {
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.cv-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-2);
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  position: relative;
  overflow: hidden;
}
.cv-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #cbd5e1;
}
[data-theme='dark'] .cv-card::before { background: #475569; }
.cv-card.exp-red::before { background: #ef4444; }
.cv-card.exp-yellow::before { background: #f59e0b; }
.cv-card.exp-green::before { background: #10b981; }
.cv-card:active {
  transform: scale(0.98);
}
.cv-card-top {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cv-card .emoji {
  font-size: 22px;
}
.cv-card .name {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cv-card .qty {
  font-size: 10px;
  font-weight: 800;
  padding: 1px 6px;
  border-radius: 6px;
  background: #fef3c7;
  color: #b45309;
}
[data-theme='dark'] .cv-card .qty { background: #422006; color: #fcd34d; }
.cv-card-bot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.cv-card .days {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 6px;
}
.cv-card .days.exp-red { background: #fee2e2; color: #b91c1c; }
.cv-card .days.exp-yellow { background: #fef3c7; color: #b45309; }
.cv-card .days.exp-green { background: #dcfce7; color: #15803d; }
.cv-card .days.muted { background: var(--bg-hover); color: var(--text-3); }
[data-theme='dark'] .cv-card .days.exp-red { background: #450a0a; color: #fca5a5; }
[data-theme='dark'] .cv-card .days.exp-yellow { background: #422006; color: #fcd34d; }
[data-theme='dark'] .cv-card .days.exp-green { background: #052e16; color: #86efac; }
.cv-card .loc {
  font-size: 10px;
  color: var(--text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
}

/* 历史区（桌面） */
.cv-history {
  margin-top: 8px;
}
.section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  margin: 8px 0;
}
.cv-history-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}
.cv-history-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-1);
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.cv-history-card:hover {
  border-color: #7dd3fc;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(14, 165, 233, 0.1);
}
.cv-history-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.cv-history-time {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-1);
  opacity: 0.8;
}
.cv-history-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: var(--text-2);
}
.cv-history-item {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cv-history-more {
  font-size: 11px;
  color: var(--text-2);
  text-align: center;
}
</style>
