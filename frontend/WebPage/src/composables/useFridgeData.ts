import { ref, computed, type Ref } from 'vue'
import { useMessage } from 'naive-ui'
import type {
  FridgeItemVO, FridgeCategoryVO, FridgeTemplateVO, FridgeShoppingHistoryVO,
  FridgeExpiringVO,
} from '../types/fridge'
import {
  listItems, listCategories, listTemplates, getDefaultTemplate, listShoppingHistory,
  getExpiringStats,
} from '../api/fridge'

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`
}

export function nowHHmmss(): string {
  const d = new Date()
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const EMPTY_STATS = { total: 0, expired: 0, expiringSoon: 0, fresh: 0 }

export interface UseFridgeDataReturn {
  items: Ref<FridgeItemVO[]>
  activeItems: Ref<FridgeItemVO[]>
  categories: Ref<FridgeCategoryVO[]>
  templates: Ref<FridgeTemplateVO[]>
  currentTemplate: Ref<FridgeTemplateVO | null>
  history: Ref<FridgeShoppingHistoryVO[]>
  stats: Ref<FridgeExpiringVO>
  lastSavedAt: Ref<string | null>
  tableRefreshKey: Ref<number>
  loadItems: () => Promise<void>
  loadCategories: () => Promise<void>
  loadTemplates: () => Promise<void>
  loadHistory: () => Promise<void>
  refreshAll: () => void
}

export function useFridgeData(): UseFridgeDataReturn {
  const message = useMessage()
  const items = ref<FridgeItemVO[]>([])
  const categories = ref<FridgeCategoryVO[]>([])
  const templates = ref<FridgeTemplateVO[]>([])
  const currentTemplate = ref<FridgeTemplateVO | null>(null)
  const history = ref<FridgeShoppingHistoryVO[]>([])
  const stats = ref<FridgeExpiringVO>({ ...EMPTY_STATS, items: [] })
  const lastSavedAt = ref<string | null>(null)
  const tableRefreshKey = ref(0)

  const activeItems = computed(() =>
    items.value.filter((it) => it.status === 'ACTIVE' || it.status === 'PENDING'),
  )

  async function loadItems(): Promise<void> {
    const [itemsRes, statsRes] = await Promise.all([
      listItems({ includePending: true }),
      getExpiringStats(),
    ])
    if (itemsRes.data?.success) items.value = itemsRes.data.data ?? []
    else message.error(itemsRes.data?.message ?? '加载失败')
    if (statsRes.data?.success && statsRes.data.data) {
      stats.value = statsRes.data.data
    }
  }

  async function loadCategories(): Promise<void> {
    const res = await listCategories()
    if (res.data?.success) categories.value = res.data.data ?? []
  }

  async function loadTemplates(): Promise<void> {
    const [listRes, defRes] = await Promise.all([listTemplates(), getDefaultTemplate()])
    if (listRes.data?.success) templates.value = listRes.data.data ?? []
    if (defRes.data?.success) currentTemplate.value = defRes.data.data
  }

  async function loadHistory(): Promise<void> {
    const res = await listShoppingHistory(50)
    if (res.data?.success) history.value = res.data.data ?? []
  }

  function refreshAll(): void {
    loadItems()
    loadTemplates()
    loadHistory()
    tableRefreshKey.value++
  }

  return {
    items, activeItems, categories, templates, currentTemplate, history,
    stats, lastSavedAt, tableRefreshKey,
    loadItems, loadCategories, loadTemplates, loadHistory, refreshAll,
  }
}
