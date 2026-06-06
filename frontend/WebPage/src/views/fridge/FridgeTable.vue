<template>
  <n-space vertical :size="16">
    <n-card :bordered="true" size="small">
      <n-space align="center" wrap>
        <n-radio-group v-model:value="zoneTab" @update:value="reload">
          <n-radio-button value="ALL">全部</n-radio-button>
          <n-radio-button value="REFRIGERATED">冷藏</n-radio-button>
          <n-radio-button value="FROZEN">冷冻</n-radio-button>
          <n-radio-button value="PENDING">采购篮</n-radio-button>
        </n-radio-group>
        <n-select
          v-model:value="filter.categoryId"
          :options="categoryOptions"
          placeholder="全部分类"
          clearable
          style="width:160px"
          @update:value="reload"
        />
        <n-select
          v-model:value="filter.status"
          :options="statusOptions"
          placeholder="全部状态"
          clearable
          style="width:140px"
          @update:value="reload"
        />
        <n-input
          v-model:value="filter.keyword"
          placeholder="搜索食材名..."
          clearable
          style="width:200px"
          @keyup.enter="reload"
        />
        <n-button @click="reload">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
      </n-space>
    </n-card>

    <n-data-table
      :columns="columns"
      :data="expanded"
      :loading="loading"
      :bordered="true"
      :row-key="rowKey"
    />
  </n-space>
</template>

<script setup lang="ts">
import { ref, h, computed, onMounted, watch } from 'vue'
import { NSpace, NCard, NRadioGroup, NRadioButton, NSelect, NInput, NButton, NDataTable, NImage, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import {
  listItems,
} from '../../api/fridge'
import type { FridgeCategoryVO, FridgeItemVO, FridgeItemStatus, FridgeZone } from '../../types/fridge'
import { expandByQuantity } from '../../utils/fridgeExpand'

const props = defineProps<{
  categories: FridgeCategoryVO[]
  refreshKey: number
}>()

const message = useMessage()
const loading = ref(false)
const items = ref<FridgeItemVO[]>([])
const zoneTab = ref<'ALL' | 'REFRIGERATED' | 'FROZEN' | 'PENDING'>('ALL')
const filter = ref<{ categoryId?: number; status?: FridgeItemStatus; keyword?: string }>({})

const categoryOptions = computed(() =>
  props.categories.map((c) => ({ label: `${c.icon ?? ''} ${c.name}`.trim(), value: c.id })),
)

const statusOptions = [
  { label: '在用', value: 'ACTIVE' as FridgeItemStatus },
  { label: '已消耗', value: 'CONSUMED' as FridgeItemStatus },
  { label: '已丢弃', value: 'DISCARDED' as FridgeItemStatus },
  { label: '采购篮', value: 'PENDING' as FridgeItemStatus },
]

function rowKey(row: FridgeItemVO & { __idx?: number }) {
  return `${row.id}-${row.__idx ?? 0}`
}

const expanded = computed(() =>
  expandByQuantity(items.value).map((it, i) => ({ ...it, __idx: i })),
)

function expiryColor(days?: number): string {
  if (days == null) return '#666'
  if (days < 0) return '#d03050'
  if (days <= 3) return '#d03050'
  if (days <= 7) return '#f0a020'
  return '#18a058'
}

function statusTagType(status: string): 'success' | 'info' | 'warning' {
  if (status === 'ACTIVE') return 'success'
  if (status === 'CONSUMED') return 'info'
  if (status === 'PENDING') return 'warning'
  return 'warning'
}

function statusLabel(status: string): string {
  if (status === 'ACTIVE') return '在用'
  if (status === 'CONSUMED') return '已消耗'
  if (status === 'DISCARDED') return '已丢弃'
  if (status === 'PENDING') return '采购篮'
  return status
}

function zoneLabel(zone: string | null): string {
  if (zone === 'REFRIGERATED') return '冷藏'
  if (zone === 'FROZEN') return '冷冻'
  return '-'
}

const columns: DataTableColumns<FridgeItemVO> = [
  {
    title: '食材',
    key: 'name',
    width: 220,
    render: (row) =>
      h(NSpace, { align: 'center', size: 8 }, () => [
        row.imageUrl
          ? h(NImage, { src: row.imageUrl, width: 36, height: 36, objectFit: 'cover', previewDisabled: true, style: 'border-radius:4px' })
          : h('span', { style: 'font-size:24px' }, row.categoryIcon ?? '🍽️'),
        h('div', [
          h('div', { style: 'font-weight:500' }, row.name),
          row.subZone ? h('div', { style: 'font-size:11px;color:#999' }, `位置: ${row.subZone}`) : null,
        ]),
      ]),
  },
  {
    title: '区域',
    key: 'zone',
    width: 80,
    render: (row) => h(NTag, { size: 'small', type: row.zone === 'FROZEN' ? 'info' : 'primary' }, () => zoneLabel(row.zone)),
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 120,
    render: (row) => (row.categoryName ? `${row.categoryIcon ?? ''} ${row.categoryName}`.trim() : '-'),
  },
  {
    title: '数量',
    key: 'quantity',
    width: 80,
    render: (row) => (row.quantity != null ? `${row.quantity}${row.unit ?? ''}` : '-'),
  },
  {
    title: '过期日期',
    key: 'expiryDate',
    width: 130,
    render: (row) =>
      h('div', [
        h('div', { style: `color:${expiryColor(row.daysToExpiry)};font-weight:500` }, row.expiryDate ?? '未设置'),
        row.daysToExpiry != null
          ? h('div', { style: `font-size:11px;color:${expiryColor(row.daysToExpiry)}` }, `${row.daysToExpiry >= 0 ? row.daysToExpiry + ' 天后' : '已过 ' + Math.abs(row.daysToExpiry) + ' 天'}`)
          : null,
      ]),
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => h(NTag, { size: 'small', type: statusTagType(row.status) }, () => statusLabel(row.status)),
  },
  {
    title: '来源',
    key: 'source',
    width: 70,
    render: (row) => h(NTag, { size: 'small', type: row.source === 'AI' ? 'info' : 'default', bordered: false }, () => row.source === 'AI' ? 'AI' : '手动'),
  },
]

async function reload() {
  loading.value = true
  try {
    const params: any = {}
    if (zoneTab.value === 'PENDING') {
      params.status = 'PENDING'
    } else if (zoneTab.value !== 'ALL') {
      params.zone = zoneTab.value as FridgeZone
    }
    if (filter.value.categoryId) params.categoryId = filter.value.categoryId
    if (filter.value.status) params.status = filter.value.status
    if (filter.value.keyword) params.keyword = filter.value.keyword
    // 默认 ACTIVE 当未指定
    if (!params.status) {
      params.includePending = true
    }
    const res = await listItems(params)
    if (res.data?.success) {
      items.value = res.data.data ?? []
    } else {
      message.error(res.data?.message ?? '加载失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  zoneTab.value = 'ALL'
  filter.value = {}
  reload()
}

defineExpose({ reload })

watch(() => props.refreshKey, () => reload())
onMounted(reload)
</script>
