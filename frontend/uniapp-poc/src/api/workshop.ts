/**
 * 调酒台 api
 * ==========
 * 端点:
 *   GET  /api/workshop/ingredients  → 全量原料 (后端 ApiResult<IngredientVO[]>)
 *   POST /api/workshop/recommend    → 推荐结果 (后端 ApiResult<RecommendResultVO[]>)
 *
 * FE 内部约定 (types/workshop) 是 snake_case,运行时有一层 toX 转换。
 */

import request from './request'
import type { ApiResult } from '../types/api'
import type { IngredientIndex } from '../types/workshop'

// 后端 VO (camelCase)
export interface BackendIngredient {
  id: number
  nameZh: string
  nameEn: string
  aliases: string[]
  defaultBottleMl?: number | null
  cocktailCount?: number
  plannedAmountMl?: number | null
  isMain?: boolean
}

export interface BackendCocktailIngredient {
  id: number
  nameZh: string
  nameEn: string
  isMain: boolean
  plannedAmountMl?: number | null
}

export interface BackendCocktail {
  id: number
  nameZh: string
  nameEn: string
  nameAlias: string[]
  cover: string | null
  views: number
  recipeZh: string
  methodZh: string
  aroma: string
  taste: string
  style: string
  scene: string
  history: string
  sourceUrl?: string
  ingredients?: BackendCocktailIngredient[]
}

export interface BackendRecommendResult {
  tier: 'full' | 'miss-1' | 'miss-2' | 'miss-3+'
  cocktail: BackendCocktail
  missingIngredients: BackendIngredient[]
  matchedIngredients: BackendIngredient[]
  missingCount: number
}

export interface RecommendPayload {
  ingredientIds: number[]
  mode: 'STRICT' | 'MAIN'
}

// 转换:后端 camelCase → FE snake_case IngredientIndex
function toIngredientIndex(b: BackendIngredient): IngredientIndex {
  return {
    id: b.id,
    name_zh: b.nameZh,
    name_en: b.nameEn,
    aliases: b.aliases ?? [],
    cocktail_count: b.cocktailCount ?? 0,
  }
}

let _ingredients: IngredientIndex[] = []
let _byId: Map<number, IngredientIndex> = new Map()

export async function loadIngredients(): Promise<IngredientIndex[]> {
  if (_ingredients.length > 0) return _ingredients
  const res = await request.get<ApiResult<BackendIngredient[]>>('/workshop/ingredients')
  _ingredients = (res.data.data ?? []).map(toIngredientIndex)
  return _ingredients
}

export async function loadIngredientsById(): Promise<Map<number, IngredientIndex>> {
  if (_byId.size > 0) return _byId
  const list = await loadIngredients()
  _byId = new Map(list.map((i) => [i.id, i]))
  return _byId
}

export async function recommendCocktails(
  ingredientIds: number[],
  mode: 'STRICT' | 'MAIN',
): Promise<BackendRecommendResult[]> {
  const res = await request.post<ApiResult<BackendRecommendResult[]>>(
    '/workshop/recommend',
    { ingredientIds, mode },
  )
  return res.data.data ?? []
}

export function clearCache(): void {
  _ingredients = []
  _byId = new Map()
}
