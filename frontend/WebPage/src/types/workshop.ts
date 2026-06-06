/**
 * 调酒台类型契约
 * ==============
 * 调酒台是完全独立的功能模块(不依赖冰箱/其它业务),
 * 类型和数据契约开放给未来其他模块复用:
 *   - utils/workshop-recommend.ts
 *   - stores/workshop.ts
 *   - views/workshop/**
 */

/** 匹配模式:严格 = 全部原料;主料 = 只看 main 原料(装饰/果汁等可缺) */
export type WorkshopMode = 'strict' | 'main'

/** 结果分层 */
export type ResultTier = 'full' | 'miss-1' | 'miss-2' | 'miss-3+'

/** 鸡尾酒索引条目(由 scripts/build_workshop_index.mjs 生成) */
export interface CocktailIndex {
  id: number
  name_zh: string
  name_en: string
  name_alias: string[]
  cover: string | null
  views: number
  /** 该款酒的全部原料 id 列表(用于 strict 模式) */
  ingredient_ids: number[]
  /** 该款酒的主料 id 子集(用于 main 模式;装饰/果汁等不在内) */
  main_ingredient_ids: number[]
  recipe_zh: string
  method_zh: string
  aroma: string
  taste: string
  style: string
  scene: string
  history: string
}

/** 原料索引条目 */
export interface IngredientIndex {
  id: number
  name_zh: string
  name_en: string
  /** 归一化后的搜索键(去空格 + 小写),用于客户端匹配 */
  aliases: string[]
  /** 出现于多少款酒(用于搜索结果排序) */
  cocktail_count: number
}

/** 单条匹配结果 */
export interface WorkshopResult {
  cocktail: CocktailIndex
  /** 当前模式下的"必需"原料(严格 = 全部,主料 = main 集) */
  required_ids: number[]
  /** 已命中 id 列表 */
  matched: number[]
  /** 缺失 id 列表 */
  missing: number[]
  /** 匹配率:matched.length / required.length (0~1) */
  ratio: number
  tier: ResultTier
}

/** TIER 优先级(数字越小越靠前) */
export const TIER_ORDER: Record<ResultTier, number> = {
  full: 0,
  'miss-1': 1,
  'miss-2': 2,
  'miss-3+': 3,
}
