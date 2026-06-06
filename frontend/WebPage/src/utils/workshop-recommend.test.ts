import { describe, expect, it } from 'vitest'
import { recommend, groupByTier } from './workshop-recommend'
import type { CocktailIndex } from '../types/workshop'

function mk(over: Partial<CocktailIndex> & { id: number; ingredient_ids: number[]; main_ingredient_ids?: number[] }): CocktailIndex {
  return {
    id: over.id,
    name_zh: over.name_zh ?? `C${over.id}`,
    name_en: '',
    name_alias: [],
    cover: null,
    views: over.views ?? 0,
    ingredient_ids: over.ingredient_ids,
    main_ingredient_ids: over.main_ingredient_ids ?? over.ingredient_ids,
    recipe_zh: '',
    method_zh: '',
    aroma: '',
    taste: '',
    style: '',
    scene: '',
    history: '',
  }
}

describe('recommend()', () => {
  it('strict mode: requires all ingredients', () => {
    const gin_tonic = mk({ id: 1, ingredient_ids: [10, 20] })
    const negroni = mk({ id: 2, ingredient_ids: [10, 20, 30] })
    const results = recommend([gin_tonic, negroni], new Set([10, 20]), 'strict')

    expect(results).toHaveLength(2)
    expect(results[0].cocktail.id).toBe(1)
    expect(results[0].tier).toBe('full')
    expect(results[0].missing).toEqual([])
    expect(results[1].cocktail.id).toBe(2)
    expect(results[1].tier).toBe('miss-1')
    expect(results[1].missing).toEqual([30])
  })

  it('main mode: ignores non-main ingredients', () => {
    const martini = mk({
      id: 1,
      ingredient_ids: [10, 20, 30],   // 全部
      main_ingredient_ids: [10, 20],  // 主料(不含装饰)
    })
    const results = recommend([martini], new Set([10, 20]), 'main')

    expect(results).toHaveLength(1)
    expect(results[0].tier).toBe('full')
    expect(results[0].ratio).toBe(1)
  })

  it('main mode: skips cocktails with empty main_ingredient_ids', () => {
    const incomplete = mk({ id: 1, ingredient_ids: [10, 20], main_ingredient_ids: [] })
    const results = recommend([incomplete], new Set([10, 20]), 'main')
    expect(results).toEqual([])
  })

  it('filters out 0-match results in both modes', () => {
    const a = mk({ id: 1, ingredient_ids: [99, 100] })
    const b = mk({ id: 2, ingredient_ids: [10, 20] })
    const results = recommend([a, b], new Set([10, 20]), 'strict')
    expect(results.every((r) => r.cocktail.id === 2)).toBe(true)
  })

  it('tiers are assigned by missing count', () => {
    const a = mk({ id: 1, ingredient_ids: [1, 2, 3] })
    const b = mk({ id: 2, ingredient_ids: [1, 2, 3, 4] })
    const c = mk({ id: 3, ingredient_ids: [1, 2, 3, 4, 5] })

    const r = recommend([a, b, c], new Set([1]), 'strict')
    const tiers = r.map((x) => x.tier).sort()
    expect(tiers).toEqual(['miss-2', 'miss-3+', 'miss-3+'])
  })

  it('sort: tier > ratio > views', () => {
    const full_low_views = mk({ id: 1, ingredient_ids: [10, 20], views: 1 })
    const full_high_views = mk({ id: 2, ingredient_ids: [10, 20], views: 999 })
    const miss1 = mk({ id: 3, ingredient_ids: [10, 30], views: 99999 })

    const r = recommend([full_low_views, full_high_views, miss1], new Set([10, 20]), 'strict')
    expect(r[0].tier).toBe('full')
    expect(r[0].cocktail.id).toBe(2)   // views 优先
    expect(r[1].tier).toBe('full')
    expect(r[2].tier).toBe('miss-1')
  })
})

describe('groupByTier()', () => {
  it('partitions results into 4 buckets', () => {
    const cocktails = [
      mk({ id: 1, ingredient_ids: [10] }),                          // full
      mk({ id: 2, ingredient_ids: [10, 20] }),                     // miss-1
      mk({ id: 3, ingredient_ids: [10, 20, 30] }),                 // miss-2
      mk({ id: 4, ingredient_ids: [10, 20, 30, 40, 50] }),         // miss-3+
    ]
    const r = recommend(cocktails, new Set([10]), 'strict')
    const g = groupByTier(r)

    expect(g.full).toHaveLength(1)
    expect(g['miss-1']).toHaveLength(1)
    expect(g['miss-2']).toHaveLength(1)
    expect(g['miss-3+']).toHaveLength(1)
  })
})
