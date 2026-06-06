/**
 * 调酒台匹配算法(纯函数)
 * =======================
 * 给定 鸡尾酒索引 + 用户已选原料 id 集合 + 模式,返回排序后的结果列表。
 *
 * 通用:此函数不依赖任何 Vue/Pinia,可被任何模块(详情页/酒单/采购)直接 import。
 *
 * 算法:
 *   1) 对每款酒取 required_ids:
 *      - strict 模式:全部 ingredient_ids
 *      - main 模式  :main_ingredient_ids(主料);若该酒 main=[] 则跳过
 *   2) 计算 matched / missing
 *   3) 计算 tier: missing 数量
 *   4) 排序: tier 优先 -> ratio 降序 -> 浏览量降序
 *   5) 过滤:严格模式下 matched=0 不显示(没有参考价值)
 *           主料模式  下 matched=0 也不显示
 */

import type {
  CocktailIndex,
  WorkshopMode,
  WorkshopResult,
  ResultTier,
} from '../types/workshop'
import { TIER_ORDER } from '../types/workshop'

export function recommend(
  cocktails: readonly CocktailIndex[],
  userIngredientIds: ReadonlySet<number>,
  mode: WorkshopMode,
): WorkshopResult[] {
  const out: WorkshopResult[] = []

  for (const cocktail of cocktails) {
    const required =
      mode === 'strict'
        ? cocktail.ingredient_ids
        : cocktail.main_ingredient_ids

    // 主料模式下,该酒无 main 字段 -> 残缺数据,跳过
    if (mode === 'main' && required.length === 0) continue

    const matched: number[] = []
    const missing: number[] = []
    for (const id of required) {
      ;(userIngredientIds.has(id) ? matched : missing).push(id)
    }

    // 0 命中过滤(两种模式都过滤,避免空结果噪声)
    if (matched.length === 0) continue

    const ratio = required.length > 0 ? matched.length / required.length : 0
    const tier: ResultTier =
      missing.length === 0 ? 'full' :
      missing.length === 1 ? 'miss-1' :
      missing.length === 2 ? 'miss-2' :
      'miss-3+'

    out.push({
      cocktail,
      required_ids: required,
      matched,
      missing,
      ratio,
      tier,
    })
  }

  return out.sort(
    (a, b) =>
      TIER_ORDER[a.tier] - TIER_ORDER[b.tier] ||
      b.ratio - a.ratio ||
      b.cocktail.views - a.cocktail.views,
  )
}

/** 按 tier 分组(用于 tabs) */
export function groupByTier(results: readonly WorkshopResult[]): Record<ResultTier, WorkshopResult[]> {
  const groups: Record<ResultTier, WorkshopResult[]> = {
    full: [],
    'miss-1': [],
    'miss-2': [],
    'miss-3+': [],
  }
  for (const r of results) groups[r.tier].push(r)
  return groups
}
