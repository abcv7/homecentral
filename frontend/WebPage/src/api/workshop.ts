/**
 * 调酒台数据加载层
 * =================
 * 后端化 (Phase N.1)：所有数据来自 /api/workshop/* 端点。
 * 内存单例缓存,避免重复 fetch。
 */

import request from './request'
import type { ApiResult } from '../types/api'
import type { IngredientIndex } from '../types/workshop'

// ----- 原料索引 (替代原 public/data/ingredients_index.json) -----

let _ingredients: IngredientIndex[] | null = null
let _ingredientsById: Map<number, IngredientIndex> | null = null

/** 拉所有原料 (~1500 条, 给搜索框用) */
export async function loadIngredients(): Promise<IngredientIndex[]> {
  if (_ingredients) return _ingredients
  const res = await request.get<ApiResult<IngredientIndex[]>>('/workshop/ingredients')
  _ingredients = res.data.data ?? []
  return _ingredients
}

/** 按 id 索引的原料字典 (单例) */
export async function loadIngredientsById(): Promise<Map<number, IngredientIndex>> {
  if (_ingredientsById) return _ingredientsById
  const list = await loadIngredients()
  _ingredientsById = new Map(list.map((i) => [i.id, i]))
  return _ingredientsById
}

// ----- 推荐 (替代前端 recommend 纯函数) -----

export interface BackendRecommendResult {
  tier: 'full' | 'miss-1' | 'miss-2' | 'miss-3+'
  cocktail: BackendCocktail
  missingIngredients: BackendIngredient[]
  matchedIngredients: BackendIngredient[]
  missingCount: number
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
  ingredients?: BackendIngredient[]
}

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

export interface RecommendPayload {
  ingredientIds: number[]
  mode: 'STRICT' | 'MAIN'
}

/** 调后端 recommend 端点 */
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

// ----- 缓存清理 -----

/** 清空缓存(开发/测试用) */
export function clearCache(): void {
  _ingredients = null
  _ingredientsById = null
}
