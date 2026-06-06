// @vitest-environment node
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { nextTick } from 'vue'
import { useWorkshopStore } from './workshop'
import type { IngredientIndex } from '../types/workshop'

vi.mock('../api/workshop', () => {
  const ingredientsFixture: IngredientIndex[] = [
    { id: 10, name_zh: '伏特加', name_en: 'Vodka', aliases: ['vodka'], cocktail_count: 2 },
    { id: 20, name_zh: '糖', name_en: 'Sugar', aliases: ['sugar'], cocktail_count: 1 },
    { id: 30, name_zh: '柠檬', name_en: 'Lemon', aliases: ['lemon'], cocktail_count: 1 },
  ]
  return {
    loadIngredientsById: vi.fn(async () => new Map(ingredientsFixture.map((i) => [i.id, i]))),
    recommendCocktails: vi.fn(async (ids: number[], mode: string) => {
      // 简化版 recommend 模拟: 命中计算
      const owned = new Set(ids)
      const cocktails = [
        { id: 1, nameZh: 'A', nameEn: '', ingredients: [{ id: 10, isMain: true }, { id: 20, isMain: true }] },
        { id: 2, nameZh: 'B', nameEn: '', ingredients: [{ id: 10, isMain: true }, { id: 30, isMain: true }] },
      ]
      const results = []
      for (const c of cocktails) {
        const required = mode === 'STRICT' ? c.ingredients : c.ingredients
        const matched = required.filter((i) => owned.has(i.id))
        const missing = required.filter((i) => !owned.has(i.id))
        if (matched.length === 0) continue
        const m = missing.length
        const tier = m === 0 ? 'full' : m === 1 ? 'miss-1' : m === 2 ? 'miss-2' : 'miss-3+'
        results.push({
          tier,
          cocktail: {
            ...c,
            nameAlias: [],
            cover: null,
            views: 0,
            recipeZh: '', methodZh: '', aroma: '', taste: '', style: '', scene: '', history: '',
          },
          matchedIngredients: matched.map((i) => ({ id: i.id, isMain: i.isMain })),
          missingIngredients: missing.map((i) => ({ id: i.id, isMain: i.isMain })),
          missingCount: missing.length,
        })
      }
      return results
    }),
    clearCache: vi.fn(),
  }
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('workshop store (后端化)', () => {
  it('ensureLoaded() fetches and caches ingredients', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    expect(s.ingredientsById.size).toBe(3)
    expect(s.ingredientCount).toBe(3)
    expect(s.loading).toBe(false)
  })

  it('toggleIngredient adds and removes', () => {
    const s = useWorkshopStore()
    s.toggleIngredient(10)
    expect(s.selectedIds.has(10)).toBe(true)
    s.toggleIngredient(10)
    expect(s.selectedIds.has(10)).toBe(false)
  })

  it('clearSelected empties the set', () => {
    const s = useWorkshopStore()
    s.addIngredient(10)
    s.addIngredient(20)
    s.clearSelected()
    expect(s.selectedCount).toBe(0)
  })

  it('results refetch on selection change (strict)', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('strict')
    s.addIngredient(10)
    s.addIngredient(20)
    await nextTick()
    await new Promise((r) => setTimeout(r, 10))
    // A: [10,20] full, B: [10,30] miss-1
    expect(s.results).toHaveLength(2)
    expect(s.results[0].cocktail.id).toBe(1)
    expect(s.results[0].tier).toBe('full')
    expect(s.results[1].tier).toBe('miss-1')
  })

  it('results refetch on mode change (main)', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('main')
    s.addIngredient(10)
    s.addIngredient(20)
    await nextTick()
    await new Promise((r) => setTimeout(r, 10))
    // Both cocktails have main ingredients; user has 10 only
    // A: main=[10,20] -> matched=[10], missing=[20] -> miss-1
    // B: main=[10,30] -> matched=[10], missing=[30] -> miss-1
    expect(s.results).toHaveLength(2)
  })

  it('tierGroups splits by tier', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('strict')
    s.addIngredient(10)
    await nextTick()
    await new Promise((r) => setTimeout(r, 10))
    // A: [10,20] -> miss-1 (missing 20)
    // B: [10,30] -> miss-1 (missing 30)
    expect(s.tierGroups['miss-1']).toHaveLength(2)
    expect(s.tierGroups.full).toHaveLength(0)
  })
})
