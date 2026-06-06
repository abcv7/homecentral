<template>
  <n-drawer
    :show="show"
    :height="'auto'"
    placement="bottom"
    :native-scrollbar="false"
    @update:show="(v: boolean) => $emit('update:show', v)"
  >
    <n-drawer-content :show-icon="false" :native-scrollbar="false" :closable="false">
      <div v-if="item" class="sheet">
        <div class="sheet-handle" />
        <div class="sheet-head">
          <span class="emoji">{{ item.categoryIcon || '🍽️' }}</span>
          <div class="meta">
            <div class="name">{{ item.name }}</div>
            <div class="sub">
              <n-tag size="tiny" :type="item.zone === 'FROZEN' ? 'info' : item.zone === 'REFRIGERATED' ? 'primary' : 'default'" round>
                {{ zoneLabel(item.zone) }}
              </n-tag>
              <span v-if="item.subZone" class="loc">{{ item.subZone }}</span>
              <span v-if="(item.quantity ?? 1) > 1" class="qty">×{{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <div class="sheet-actions">
          <button class="sheet-action" :disabled="item.status !== 'ACTIVE'" @click="onConsume">
            <n-icon size="22" color="#10b981"><checkmark-circle-outline /></n-icon>
            <span class="lbl">消耗 1 份</span>
            <span class="hint">点一次少一份</span>
          </button>
          <button class="sheet-action" @click="onEdit">
            <n-icon size="22" color="#8b5cf6"><create-outline /></n-icon>
            <span class="lbl">编辑</span>
            <span class="hint">改名称 / 数量 / 过期</span>
          </button>
          <button class="sheet-action" :disabled="item.status === 'PENDING'" @click="openMove">
            <n-icon size="22" color="#3b82f6"><move-outline /></n-icon>
            <span class="lbl">移动位置</span>
            <span class="hint">换冷藏 / 冷冻 / 解冻</span>
          </button>
          <button class="sheet-action danger" @click="onDelete">
            <n-icon size="22" color="#ef4444"><trash-outline /></n-icon>
            <span class="lbl">删除</span>
            <span class="hint">软删，不可恢复</span>
          </button>
        </div>

        <n-button block size="large" @click="$emit('update:show', false)">取消</n-button>
      </div>
    </n-drawer-content>
  </n-drawer>

  <ZonePickerSheet
    v-model:show="showZonePicker"
    :item="item"
    :template="template"
    @confirm="onMoveConfirm"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { NDrawer, NDrawerContent, NTag, NButton, NIcon } from 'naive-ui'
import {
  CheckmarkCircleOutline, MoveOutline, TrashOutline, CreateOutline,
} from '@vicons/ionicons5'
import type { FridgeItemVO, FridgeTemplateVO, FridgeZone } from '../../types/fridge'
import ZonePickerSheet from './ZonePickerSheet.vue'

const props = defineProps<{
  show: boolean
  item: FridgeItemVO | null
  template: FridgeTemplateVO | null
}>()

const emit = defineEmits<{
  'update:show': [v: boolean]
  consume: [item: FridgeItemVO]
  edit: [item: FridgeItemVO]
  delete: [item: FridgeItemVO]
  moved: [payload: { item: FridgeItemVO; zone: FridgeZone | null; subZone: string | null }]
}>()

const showZonePicker = ref(false)

watch(() => props.show, (v) => {
  if (!v) showZonePicker.value = false
})

function zoneLabel(zone: string | null) {
  if (zone === 'REFRIGERATED') return '冷藏'
  if (zone === 'FROZEN') return '冷冻'
  if (zone === null && props.item?.status === 'PENDING') return '采购篮'
  return '-'
}

function onConsume() {
  if (!props.item) return
  emit('consume', props.item)
  emit('update:show', false)
}

function openMove() {
  if (!props.item) return
  showZonePicker.value = true
}

function onEdit() {
  if (!props.item) return
  emit('edit', props.item)
  emit('update:show', false)
}

function onMoveConfirm(payload: { zone: FridgeZone | null; subZone: string | null }) {
  if (!props.item) return
  emit('moved', { item: props.item, zone: payload.zone, subZone: payload.subZone })
  emit('update:show', false)
}

function onDelete() {
  if (!props.item) return
  emit('delete', props.item)
  emit('update:show', false)
}
</script>

<style scoped>
.sheet {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
.sheet-handle {
  width: 40px;
  height: 4px;
  background: #e2e8f0;
  border-radius: 999px;
  margin: 4px auto 4px;
}
.sheet-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 4px 8px;
  border-bottom: 1px solid #f1f5f9;
}
.sheet-head .emoji {
  font-size: 36px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 14px;
}
.sheet-head .meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.sheet-head .name {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
}
.sheet-head .sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  flex-wrap: wrap;
}
.sheet-head .sub .qty {
  font-size: 11px;
  font-weight: 800;
  padding: 1px 6px;
  border-radius: 6px;
  background: #fef3c7;
  color: #b45309;
}
.sheet-head .sub .loc {
  font-size: 11px;
  color: #94a3b8;
}
.sheet-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.sheet-action {
  appearance: none;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px 16px;
  display: grid;
  grid-template-columns: 32px 1fr auto;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s, border-color 0.15s;
}
.sheet-action:active {
  background: #f1f5f9;
}
.sheet-action:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.sheet-action.danger {
  border-color: #fecaca;
  background: #fff7f7;
}
.sheet-action .lbl {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}
.sheet-action .hint {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
  text-align: right;
}
</style>
