<template>
  <n-modal
    :show="show"
    preset="card"
    :title="pending ? '温区变更确认' : '操作确认'"
    :style="isMobile ? { width: '90vw', maxWidth: '380px' } : { width: '420px' }"
    :mask-closable="false"
    @update:show="(v) => $emit('update:show', v)"
  >
    <div v-if="pending" class="confirm-body">
      <div class="confirm-icon">
        <n-icon size="48" color="#f59e0b"><warning-outline /></n-icon>
      </div>
      <p class="confirm-text">
        即将移动
        <span class="item-chip">
          <span v-if="pending.item.categoryIcon" class="emoji">{{ pending.item.categoryIcon }}</span>
          <span class="name">{{ pending.item.name }}</span>
        </span>
        <br />
        从 <b class="from">{{ fromLabel }}</b>
        转移至 <b class="to">{{ toLabel }}</b> 吗？
      </p>
      <p class="confirm-hint">
        跨温区移动可能影响食材的保鲜期，请确认操作无误。
      </p>
    </div>
    <div v-else class="confirm-body">
      <p class="confirm-text">{{ message }}</p>
    </div>
    <template #action>
      <n-space justify="end">
        <n-button @click="onCancel">取消</n-button>
        <n-button type="primary" @click="onConfirm">确认</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NModal, NSpace, NButton, NIcon } from 'naive-ui'
import { WarningOutline } from '@vicons/ionicons5'
import type { FridgeItemVO, FridgeZone } from '../../types/fridge'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

interface PendingMove {
  item: FridgeItemVO
  fromZone: FridgeZone | null
  fromSubZone: string | null
  toZone: FridgeZone | null
  toSubZone: string | null
  toLabel: string
  fromLabel: string
}

const props = defineProps<{
  show: boolean
  pending: PendingMove | null
  message?: string
}>()

const emit = defineEmits<{
  'update:show': [v: boolean]
  confirm: []
  cancel: []
}>()

const pending = computed(() => props.pending)

const fromLabel = computed(() => props.pending?.fromLabel ?? '-')
const toLabel = computed(() => props.pending?.toLabel ?? '-')

function onConfirm() {
  emit('confirm')
}

function onCancel() {
  emit('cancel')
  emit('update:show', false)
}
</script>

<style scoped>
.confirm-body {
  text-align: center;
  padding: 8px 0;
}
.confirm-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}
.confirm-text {
  font-size: 14px;
  line-height: 1.8;
  color: #475569;
  margin: 0;
}
.confirm-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}
.item-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 8px;
  margin: 4px 2px;
  font-weight: 600;
  color: #1e293b;
}
.emoji {
  font-size: 16px;
}
.from {
  color: #2563eb;
}
.to {
  color: #f59e0b;
}
</style>
