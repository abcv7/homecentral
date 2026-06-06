<template>
  <div class="basket" :class="{ 'is-mobile': isMobile }">
    <div class="basket-header">
      <div class="title-row">
        <div class="icon-box">
          <n-icon size="18" color="#f97316"><basket-outline /></n-icon>
        </div>
        <div>
          <div class="title">采购篮</div>
          <div class="subtitle">{{ isMobile ? '点击食材移除' : '拖至冰箱内归位（PENDING）' }}</div>
        </div>
      </div>
      <n-tag :type="uniqueCount > 0 ? 'warning' : 'default'" size="small" round>
        {{ uniqueCount }} 件
      </n-tag>
    </div>
    <div
      class="basket-drop"
      :class="{ 'is-over': isOver }"
      @dragover.prevent="onDragOverDesktop"
      @dragleave="isOver = false"
      @drop.prevent="onDrop"
    >
      <div v-if="expanded.length === 0" class="empty">
        <n-icon size="32" color="#cbd5e1"><leaf-outline /></n-icon>
        <p>篮子空空如也~</p>
        <p class="hint">新增食材 / 拍照识别 / 从冰箱退回的食材都会出现在这里</p>
      </div>
      <div v-else class="chips">
        <div
          v-for="(it, idx) in expanded"
          :key="`${it.id}-${idx}`"
          class="chip"
          :class="{ 'mobile-tap': isMobile }"
          :draggable="!isMobile"
          @dragstart="onDragStart($event, it.id)"
          @dragend="$emit('drag-end')"
          @click="handleClick(it)"
        >
          <span class="emoji">{{ it.categoryIcon || '🍽️' }}</span>
          <span class="name">{{ it.name }}</span>
          <span v-if="it.daysToExpiry != null" class="expiry" :class="expiryClass(it.daysToExpiry)">
            {{ it.daysToExpiry < 0 ? `已过${-it.daysToExpiry}天` : `${it.daysToExpiry}天` }}
          </span>
          <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { NIcon, NTag } from 'naive-ui'
import { BasketOutline, LeafOutline } from '@vicons/ionicons5'
import type { FridgeItemVO } from '../../types/fridge'
import { expandByQuantity } from '../../utils/fridgeExpand'

const props = defineProps<{
  items: FridgeItemVO[]
  isMobile?: boolean
}>()

const emit = defineEmits<{
  'drag-start': [id: number]
  'drag-end': []
  'item-click': [item: FridgeItemVO]
  'item-action': [item: FridgeItemVO]
  'drop-basket': []
}>()

const isOver = ref(false)

const expanded = computed(() => expandByQuantity(props.items))
const uniqueCount = computed(() => props.items.length)
const isMobile = computed(() => !!props.isMobile)

function onDragStart(e: DragEvent, id: number) {
  if (!e.dataTransfer) return
  e.dataTransfer.setData('text/plain', String(id))
  e.dataTransfer.effectAllowed = 'move'
  emit('drag-start', id)
}

function onDragOverDesktop() {
  if (isMobile.value) return
  isOver.value = true
}

function onDrop(e: DragEvent) {
  isOver.value = false
  if (isMobile.value) return
  if (e.dataTransfer) {
    const id = e.dataTransfer.getData('text/plain')
    if (id) emit('drop-basket')
  }
}

function handleClick(it: FridgeItemVO) {
  if (isMobile.value) {
    // 移动端：点 PENDING 卡片直接弹出 ActionSheet（删除/移动）
    emit('item-action', it)
  } else {
    emit('item-click', it)
  }
}

function expiryClass(days: number) {
  if (days < 0) return 'expired'
  if (days <= 3) return 'red'
  if (days <= 7) return 'yellow'
  return 'green'
}
</script>

<style scoped>
.basket {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid #f1f5f9;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 480px;
}
.basket.is-mobile {
  min-height: 0;
  border-radius: 0;
  box-shadow: none;
  border: 0;
  padding: 4px 0;
  background: transparent;
  gap: 8px;
}
.basket-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.basket.is-mobile .basket-header {
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
  background: #fff7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #fed7aa;
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
.basket-drop {
  flex: 1;
  background: #f8fafc;
  border: 2px dashed #e2e8f0;
  border-radius: 16px;
  padding: 14px;
  min-height: 360px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s;
}
.basket.is-mobile .basket-drop {
  background: transparent;
  border: 0;
  padding: 0;
  min-height: 0;
}
.basket-drop.is-over {
  background: #fff7ed;
  border-color: #f97316;
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
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-content: flex-start;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: #ffffff;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  cursor: grab;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05);
  transition: transform 0.15s, box-shadow 0.15s;
}
.basket.is-mobile .chip {
  cursor: pointer;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 13px;
  background: #fff;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}
.basket.is-mobile .chip.mobile-tap:active {
  transform: scale(0.98);
  background: #f8fafc;
}
.chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.1);
}
.chip:active {
  cursor: grabbing;
}
.emoji {
  font-size: 14px;
}
.basket.is-mobile .emoji {
  font-size: 18px;
}
.name {
  color: #1e293b;
}
.expiry {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 6px;
  font-weight: 700;
}
.expiry.expired,
.expiry.red {
  background: #fee2e2;
  color: #dc2626;
}
.expiry.yellow {
  background: #fef3c7;
  color: #b45309;
}
.expiry.green {
  background: #dcfce7;
  color: #15803d;
}
.qty {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 6px;
  font-weight: 700;
  background: #fef3c7;
  color: #b45309;
}
</style>
