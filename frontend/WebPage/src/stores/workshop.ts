/**
 * 调酒台 Pinia store
 * ===================
 * 后端化 (Phase N.2)：所有计算走 /api/workshop/recommend，
 * 调酒台索引从 /api/workshop/ingredients 拉。
 *
 * 输出 WorkshopResult 保持现有契约 (snake_case 字段 + ingredient_ids 数组)
 * 让 components (CocktailResultCard / CocktailDetailModal) 无需改动。
 */

import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import type { CocktailIndex, IngredientIndex, WorkshopMode, WorkshopResult } from '../types/workshop'
import { TIER_ORDER } from '../types/workshop'
import {
  loadIngredientsById,
  recommendCocktails,
  type BackendCocktail,
  type BackendRecommendResult,
} from '../api/workshop'

/** 后端 VO → 现有 CocktailIndex (snake_case + ingredient_ids 数组) */
function toCocktailIndex(bc: BackendCocktail): CocktailIndex {
  const ings = bc.ingredients ?? []
  return {
    id: bc.id,
    name_zh: bc.nameZh,
    name_en: bc.nameEn,
    name_alias: bc.nameAlias ?? [],
    cover: bc.cover,
    views: bc.views,
    ingredient_ids: ings.map((i) => i.id),
    main_ingredient_ids: ings.filter((i) => i.isMain === true).map((i) => i.id),
    recipe_zh: bc.recipeZh,
    method_zh: bc.methodZh,
    aroma: bc.aroma,
    taste: bc.taste,
    style: bc.style,
    scene: bc.scene,
    history: bc.history,
  }
}

function toWorkshopResult(rr: BackendRecommendResult): WorkshopResult {
  const matched: number[] = (rr.matchedIngredients ?? []).map((i) => i.id)
  const missing: number[] = (rr.missingIngredients ?? []).map((i) => i.id)
  const total = matched.length + missing.length
  const ratio = total > 0 ? matched.length / total : 0
  return {
    cocktail: toCocktailIndex(rr.cocktail),
    required_ids: (rr.cocktail.ingredients ?? []).map((i) => i.id),
    matched,
    missing,
    ratio,
    tier: rr.tier,
  }
}

export const useWorkshopStore = defineStore('workshop', () => {
  const selectedIds = ref<Set<number>>(new Set())
  const mode = ref<WorkshopMode>('strict')
  const ingredientsById = ref<Map<number, IngredientIndex>>(new Map())
  const results = ref<WorkshopResult[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const selectedCount = computed(() => selectedIds.value.size)
  const ingredientCount = computed(() => ingredientsById.value.size)

  const tierGroups = computed(() => {
    const groups: Record<keyof typeof TIER_ORDER, WorkshopResult[]> = {
      full: [],
      'miss-1': [],
      'miss-2': [],
      'miss-3+': [],
    }
    for (const r of results.value) groups[r.tier].push(r)
    return groups
  })

  async function ensureLoaded(): Promise<void> {
    if (ingredientsById.value.size > 0) return
    loading.value = true
    error.value = null
    try {
      const ing = await loadIngredientsById()
      ingredientsById.value = ing
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  async function refreshResults(): Promise<void> {
    if (selectedIds.value.size === 0) {
      results.value = []
      return
    }
    loading.value = true
    error.value = null
    try {
      const backendMode = mode.value === 'strict' ? 'STRICT' : 'MAIN'
      const ids = Array.from(selectedIds.value)
      const raw = await recommendCocktails(ids, backendMode)
      results.value = raw.map(toWorkshopResult)
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  // 任一依赖变化 -> 调后端
  watch([selectedIds, mode], () => {
    void refreshResults()
  }, { flush: 'post' })

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
    ingredientsById,
    loading,
    error,
    selectedCount,
    ingredientCount,
    results,
    tierGroups,
    ensureLoaded,
    refreshResults,
    addIngredient,
    removeIngredient,
    toggleIngredient,
    clearSelected,
    setMode,
  }
})
