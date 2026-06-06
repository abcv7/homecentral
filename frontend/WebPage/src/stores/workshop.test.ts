// @vitest-environment node
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useWorkshopStore } from './workshop'
import type { CocktailIndex, IngredientIndex } from '../types/workshop'
import { loadCocktails, loadIngredientsById, clearCache } from '../api/workshop'

const cocktailsFixture: CocktailIndex[] = [
  {
    id: 1, name_zh: 'A', name_en: '', name_alias: [], cover: null, views: 10,
    ingredient_ids: [10, 20], main_ingredient_ids: [10, 20],
    recipe_zh: '', method_zh: '', aroma: '', taste: '', style: '', scene: '', history: '',
  },
  {
    id: 2, name_zh: 'B', name_en: '', name_alias: [], cover: null, views: 5,
    ingredient_ids: [10, 30], main_ingredient_ids: [10, 30],
    recipe_zh: '', method_zh: '', aroma: '', taste: '', style: '', scene: '', history: '',
  },
]

const ingredientsFixture: IngredientIndex[] = [
  { id: 10, name_zh: '伏特加', name_en: 'Vodka', aliases: ['vodka'], cocktail_count: 2 },
  { id: 20, name_zh: '糖', name_en: 'Sugar', aliases: ['sugar'], cocktail_count: 1 },
  { id: 30, name_zh: '柠檬', name_en: 'Lemon', aliases: ['lemon'], cocktail_count: 1 },
]

beforeEach(() => {
  setActivePinia(createPinia())
  clearCache()
  globalThis.fetch = vi.fn(async (url: string) => {
    if (String(url).endsWith('cocktails_index.json')) {
      return { ok: true, json: async () => cocktailsFixture } as Response
    }
    return { ok: true, json: async () => ingredientsFixture } as Response
  }) as unknown as typeof fetch
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('workshop store', () => {
  it('ensureLoaded() fetches and caches data', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    expect(s.cocktails).toHaveLength(2)
    expect(s.ingredientsById.size).toBe(3)
    expect(s.loading).toBe(false)
  })

  it('toggleIngredient adds and removes', async () => {
    const s = useWorkshopStore()
    s.toggleIngredient(10)
    expect(s.selectedIds.has(10)).toBe(true)
    s.toggleIngredient(10)
    expect(s.selectedIds.has(10)).toBe(false)
  })

  it('clearSelected empties the set', async () => {
    const s = useWorkshopStore()
    s.addIngredient(10)
    s.addIngredient(20)
    s.clearSelected()
    expect(s.selectedCount).toBe(0)
  })

  it('results recompute on selection change', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('strict')
    s.addIngredient(10)
    s.addIngredient(20)
    // A: [10,20] full, B: [10,30] miss-1
    expect(s.results).toHaveLength(2)
    expect(s.results[0].cocktail.id).toBe(1)  // full wins
    expect(s.results[0].tier).toBe('full')
  })

  it('main mode: missing main_ingredient_id is OK', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('main')
    s.addIngredient(10)
    s.addIngredient(20)
    // Both cocktails have main_ingredient_ids set; user has 10 only
    // A: main=[10,20] -> matched=[10], missing=[20] -> miss-1
    // B: main=[10,30] -> matched=[10], missing=[30] -> miss-1
    expect(s.results).toHaveLength(2)
  })

  it('tierGroups splits by tier', async () => {
    const s = useWorkshopStore()
    await s.ensureLoaded()
    s.setMode('strict')
    s.addIngredient(10)
    // A: [10,20] -> miss-1 (missing 20)
    // B: [10,30] -> miss-1 (missing 30)
    expect(s.tierGroups['miss-1']).toHaveLength(2)
    expect(s.tierGroups.full).toHaveLength(0)
  })
})

// Unused import guard
void loadCocktails; void loadIngredientsById
