<template>
  <n-modal
    :show="show"
    preset="card"
    :style="isMobile ? { width: '94vw', maxWidth: '480px' } : { width: '560px' }"
    :title="title"
    @update:show="$emit('update:show', $event)"
  >
    <div v-if="batch">
      <div class="batch-meta">
        <n-tag size="small" type="info" round>{{ formatTime(batch.purchasedAt) }}</n-tag>
        <n-tag v-if="batch.emailSent" size="small" type="success" round>📧 已通知 {{ batch.partnerEmail }}</n-tag>
        <n-tag v-else-if="batch.partnerEmail" size="small" type="warning" round>📧 未通知 {{ batch.partnerEmail }}</n-tag>
      </div>

      <n-space vertical :size="8" style="margin-top:14px">
        <n-checkbox
          v-model:checked="selectAll"
          :indeterminate="selectedCount > 0 && selectedCount < batch.items.length"
          @update:checked="onToggleAll"
        >
          全选 / 已选 {{ selectedCount }} / 共 {{ batch.items.length }} 项
        </n-checkbox>
        <div
          v-for="(it, idx) in batch.items"
          :key="it.id ?? idx"
          class="item-row"
        >
          <n-checkbox
            :checked="selected.has(it.id ?? -idx)"
            @update:checked="onToggleItem(it, $event)"
          />
          <span class="emoji">{{ emojiOf(it.categoryId) }}</span>
          <span class="name">{{ it.name }}</span>
          <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
          <n-button text size="tiny" type="primary" @click="onReuseOne(it)">仅加这一项</n-button>
        </div>
      </n-space>
    </div>
    <template #footer>
      <n-space justify="end">
        <n-button @click="$emit('update:show', false)">关闭</n-button>
        <n-button
          type="primary"
          :disabled="selectedCount === 0"
          @click="onReuseSelected"
        >
          加入采购篮 ({{ selectedCount }})
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NModal, NTag, NSpace, NCheckbox, NButton } from 'naive-ui'
import type { FridgeCategoryVO, FridgeShoppingHistoryVO } from '../../types/fridge'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

const props = defineProps<{
  show: boolean
  batch: FridgeShoppingHistoryVO | null
  categories: FridgeCategoryVO[]
}>()

const emit = defineEmits<{
  'update:show': [v: boolean]
  'reuse-batch': [batch: FridgeShoppingHistoryVO]
  'reuse-one': [item: { name: string; categoryId?: number; quantity?: number; unit?: string; source?: string }]
}>()

const selected = ref<Set<number>>(new Set())
const selectAll = ref(false)

const selectedCount = computed(() => selected.value.size)

const title = computed(() => `历史批次（${formatShortTime(props.batch?.purchasedAt)}）`)

watch(
  () => props.show,
  (v) => {
    if (v) {
      selected.value = new Set()
      selectAll.value = false
    }
  },
)

function emojiOf(categoryId?: number) {
  const cat = props.categories.find((c) => c.id === categoryId)
  return cat?.icon ?? '🍽️'
}

function onToggleAll(v: boolean) {
  if (!props.batch) return
  if (v) {
    selected.value = new Set(props.batch.items.map((it, idx) => it.id ?? -idx))
  } else {
    selected.value = new Set()
  }
}

function onToggleItem(item: any, v: boolean) {
  const idx = props.batch!.items.indexOf(item)
  const key = item.id ?? -idx
  if (v) selected.value.add(key)
  else selected.value.delete(key)
  selected.value = new Set(selected.value)
}

function onReuseOne(item: any) {
  emit('reuse-one', {
    name: item.name,
    categoryId: item.categoryId,
    quantity: item.quantity,
    unit: item.unit,
    source: item.source,
  })
}

function onReuseSelected() {
  if (!props.batch) return
  // 当前实现：直接复用整批；后续可优化为只复用 selected 子集
  emit('reuse-batch', props.batch)
}

function formatTime(iso?: string) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('zh-CN', { hour12: false })
}

function formatShortTime(iso?: string) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.batch-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.emoji {
  font-size: 18px;
}
.name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.qty {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 6px;
  font-weight: 700;
  background: #fef3c7;
  color: #b45309;
}
</style>
