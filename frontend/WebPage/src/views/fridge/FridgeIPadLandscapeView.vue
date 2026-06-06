<template>
  <div class="ipad-landscape">
    <!-- 顶部：紧凑工具栏 -->
    <div class="ipad-toolbar">
      <n-space align="center" :wrap="false" :size="10">
        <n-radio-group :value="viewMode" size="small" @update:value="onViewModeChange">
          <n-radio-button value="visual">🧊 3D</n-radio-button>
          <n-radio-button value="card">📦 卡片</n-radio-button>
          <n-radio-button value="table">📋 列表</n-radio-button>
        </n-radio-group>
        <n-button size="small" type="primary" strong @click="$emit('add-item')">
          <template #icon><n-icon><add-outline /></n-icon></template>
          新增
        </n-button>
        <n-button size="small" type="info" strong @click="$emit('recognize')">
          <template #icon><n-icon><camera-outline /></n-icon></template>
          拍照
        </n-button>
        <n-button size="small" quaternary @click="$emit('refresh')">
          <template #icon><n-icon><refresh-outline /></n-icon></template>
        </n-button>
        <n-button size="small" quaternary @click="$emit('open-settings')">
          <template #icon><n-icon><settings-outline /></n-icon></template>
        </n-button>
      </n-space>
      <span style="flex:1" />
      <n-tag v-if="currentTemplate" :type="currentTemplate.default ? 'success' : 'default'" size="small" round>
        {{ currentTemplate.name }}
      </n-tag>
    </div>

    <!-- 3D 视图模式：3-col grid -->
    <div v-if="viewMode === 'visual'" class="ipad-grid-3col">
      <div class="col col-left">
        <FridgeBasket
          :items="basketItems"
          embedded
          @drop-basket="$emit('drop-basket')"
          @item-click="$emit('item-click', $event)"
        />
      </div>
      <div class="col col-center">
        <FridgeVisual
          :template="currentTemplate"
          :items="activeItems"
          :is-mobile="false"
          @drop="$emit('drop-visual', $event)"
          @item-click="$emit('item-click', $event)"
        />
      </div>
      <div class="col col-right">
        <FridgeShoppingHistory
          :items="history"
          :categories="categories"
          embedded
          @open-batch="$emit('open-history-batch', $event)"
          @reuse-one="$emit('reuse-history-one', $event)"
        />
      </div>
    </div>

    <!-- 卡片视图模式：单列居中 -->
    <div v-else-if="viewMode === 'card'" class="ipad-card">
      <FridgeCardView
        :items="activeItems"
        :categories="categories"
        :basket-items="basketItems"
        :history="history"
        :is-mobile="false"
        :template="currentTemplate"
        @item-click="$emit('item-click', $event)"
        @open-history-batch="$emit('open-history-batch', $event)"
        @reuse-history-one="$emit('reuse-history-one', $event)"
      />
    </div>

    <!-- 表格视图模式 -->
    <div v-else class="ipad-table">
      <FridgeTable
        :categories="categories"
        :refresh-key="tableRefreshKey"
      />
    </div>

    <!-- 浮动确认购买按钮（采购篮有内容时显示） -->
    <n-button
      v-if="viewMode === 'visual' && basketItems.length > 0"
      class="ipad-confirm-fab"
      type="primary"
      size="large"
      strong
      @click="$emit('confirm-purchase')"
    >
      🛒 确认购买 ({{ basketItems.length }})
    </n-button>
  </div>
</template>

<script setup lang="ts">
import { NButton, NIcon, NRadioButton, NRadioGroup, NSpace, NTag } from 'naive-ui'
import { AddOutline, CameraOutline, RefreshOutline, SettingsOutline } from '@vicons/ionicons5'
import FridgeBasket from './FridgeBasket.vue'
import FridgeVisual from './FridgeVisual.vue'
import FridgeShoppingHistory from './FridgeShoppingHistory.vue'
import FridgeCardView from './FridgeCardView.vue'
import FridgeTable from './FridgeTable.vue'
import type {
  FridgeCategoryVO,
  FridgeItemVO,
  FridgeShoppingHistoryVO,
  FridgeTemplateVO,
  FridgeZone,
} from '../../types/fridge'

defineProps<{
  viewMode: 'visual' | 'card' | 'table'
  basketItems: FridgeItemVO[]
  activeItems: FridgeItemVO[]
  history: FridgeShoppingHistoryVO[]
  categories: FridgeCategoryVO[]
  currentTemplate: FridgeTemplateVO | null
  tableRefreshKey: number
}>()

const emit = defineEmits<{
  'update:viewMode': [value: 'visual' | 'card' | 'table']
  'add-item': []
  recognize: []
  refresh: []
  'open-settings': []
  'drop-basket': []
  'drop-visual': [payload: { itemId: number; zone: FridgeZone | null; subZone: string | null }]
  'item-click': [item: FridgeItemVO]
  'open-history-batch': [batch: FridgeShoppingHistoryVO]
  'reuse-history-one': [item: { name: string; categoryId?: number; quantity?: number; unit?: string; source?: string }]
  'confirm-purchase': []
}>()

function onViewModeChange(v: 'visual' | 'card' | 'table') {
  emit('update:viewMode', v)
}
</script>

<style scoped>
.ipad-landscape {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  height: 100%;
}
.ipad-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: var(--bg-surface);
  border-radius: 14px;
  border: 1px solid var(--border-2);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}
[data-theme='dark'] .ipad-toolbar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
.ipad-grid-3col {
  display: grid;
  grid-template-columns: 200px 1fr 260px;
  gap: 12px;
  flex: 1;
  min-height: 0;
  /* iPad 横屏 1024-1366 适配 */
}
.col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.col-center {
  align-items: center;
  justify-content: center;
}
.col-left,
.col-right {
  overflow: hidden;
}
.ipad-card,
.ipad-table {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
.ipad-confirm-fab {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 50;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* iPad mini 1024 较窄：缩窄右列 */
@media (max-width: 1100px) {
  .ipad-grid-3col {
    grid-template-columns: 180px 1fr 220px;
  }
}
</style>
