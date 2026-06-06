/**
 * 调酒台数据加载层
 * =================
 * 从 public/data/ 加载预构建的静态 JSON 索引。
 * 内存单例缓存,避免重复 fetch。
 *
 * 索引构建: scripts/build_workshop_index.mjs (直接调 enjoycocktail.com GraphQL API)
 */

import type { CocktailIndex, IngredientIndex } from '../types/workshop'

const COCKTAILS_URL = '/data/cocktails_index.json'
const INGREDIENTS_URL = '/data/ingredients_index.json'

let _cocktails: CocktailIndex[] | null = null
let _ingredients: IngredientIndex[] | null = null
let _ingredientsById: Map<number, IngredientIndex> | null = null

async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url, { cache: 'force-cache' })
  if (!res.ok) throw new Error(`Failed to load ${url}: HTTP ${res.status}`)
  return (await res.json()) as T
}

/** 加载 598 鸡尾酒索引(单例) */
export async function loadCocktails(): Promise<CocktailIndex[]> {
  if (_cocktails) return _cocktails
  _cocktails = await fetchJson<CocktailIndex[]>(COCKTAILS_URL)
  return _cocktails
}

/** 加载原料索引(单例) */
export async function loadIngredients(): Promise<IngredientIndex[]> {
  if (_ingredients) return _ingredients
  _ingredients = await fetchJson<IngredientIndex[]>(INGREDIENTS_URL)
  return _ingredients
}

/** 按 id 索引的原料字典(单例) */
export async function loadIngredientsById(): Promise<Map<number, IngredientIndex>> {
  if (_ingredientsById) return _ingredientsById
  const list = await loadIngredients()
  _ingredientsById = new Map(list.map((i) => [i.id, i]))
  return _ingredientsById
}

/** 清空缓存(开发/测试用) */
export function clearCache(): void {
  _cocktails = null
  _ingredients = null
  _ingredientsById = null
}
