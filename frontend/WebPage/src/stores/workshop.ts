/**
 * 调酒台 Pinia store
 * ===================
 * 状态:
 *   - selectedIds: Set<number>  用户已选原料 id
 *   - mode: WorkshopMode
 *   - cocktailIndex: CocktailIndex[] 加载后缓存
 *   - ingredientIndexById: Map  原料字典
 *   - loading / error
 *
 * 计算:
 *   - selectedCount
 *   - results: WorkshopResult[]  (recommend 算法输出)
 *   - tierGroups
 *
 * 通用:本 store 不引用任何冰箱/外部模块,跨页面安全。
 */

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type {
  CocktailIndex,
  IngredientIndex,
  WorkshopMode,
  WorkshopResult,
} from '../types/workshop'
import {
  loadCocktails,
  loadIngredientsById,
} from '../api/workshop'
import { groupByTier, recommend } from '../utils/workshop-recommend'

export const useWorkshopStore = defineStore('workshop', () => {
  const selectedIds = ref<Set<number>>(new Set())
  const mode = ref<WorkshopMode>('strict')
  const cocktails = ref<CocktailIndex[]>([])
  const ingredientsById = ref<Map<number, IngredientIndex>>(new Map())
  const loading = ref(false)
  const error = ref<string | null>(null)

  const selectedCount = computed(() => selectedIds.value.size)

  const results = computed<WorkshopResult[]>(() => {
    if (cocktails.value.length === 0) return []
    return recommend(cocktails.value, selectedIds.value, mode.value)
  })

  const tierGroups = computed(() => groupByTier(results.value))

  async function ensureLoaded(): Promise<void> {
    if (cocktails.value.length > 0) return
    loading.value = true
    error.value = null
    try {
      const [c, ing] = await Promise.all([
        loadCocktails(),
        loadIngredientsById(),
      ])
      cocktails.value = c
      ingredientsById.value = ing
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  function addIngredient(id: number): void {
    if (selectedIds.value.has(id)) return
    const next = new Set(selectedIds.value)
    next.add(id)
    selectedIds.value = next
  }

  function removeIngredient(id: number): void {
    if (!selectedIds.value.has(id)) return
    const next = new Set(selectedIds.value)
    next.delete(id)
    selectedIds.value = next
  }

  function toggleIngredient(id: number): void {
    if (selectedIds.value.has(id)) removeIngredient(id)
    else addIngredient(id)
  }

  function clearSelected(): void {
    selectedIds.value = new Set()
  }

  function setMode(m: WorkshopMode): void {
    mode.value = m
  }

  return {
    selectedIds,
    mode,
    cocktails,
    ingredientsById,
    loading,
    error,
    selectedCount,
    results,
    tierGroups,
    ensureLoaded,
    addIngredient,
    removeIngredient,
    toggleIngredient,
    clearSelected,
    setMode,
  }
})
