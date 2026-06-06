import type { FridgeItemVO, FridgeZone } from '../types/fridge'

/**
 * 3D 视图中食材的位置
 * - location: 抽屉/门的逻辑分组
 * - layer: 该分组下的层号（0-based；前端显示用 +1）
 * - matched: true 表示按规范 subZone 匹配；false 表示兼容匹配或兜底（用户无需重排）
 */
export type FridgeLocation =
  | 'basket'
  | 'fridge'
  | 'freezer'
  | 'chiller'
  | 'doorLeft'
  | 'doorRight'

export interface ResolvedPosition {
  location: FridgeLocation
  layer: number
  matched: boolean
  rawSubZone: string | null
  rawZone: FridgeZone | null
}

const ZONE_TO_LOCATION: Record<FridgeZone, FridgeLocation> = {
  REFRIGERATED: 'fridge',
  FROZEN: 'freezer',
}

// 规范 subZone 编码：<ZONE>-L<N> 或 DOOR-(LEFT|RIGHT)-L<N>
const STRICT_RE = /^(REFRIGERATED|FROZEN|CHILLER)-L(\d+)$/
const STRICT_DOOR_RE = /^DOOR-(LEFT|RIGHT)-L(\d+)$/

// 中文 subZone 兜底匹配
const FUZZY_RE = {
  fridge: [
    { re: /(冷藏|REFRIGERATED).*(上|顶)/, layer: 0 },
    { re: /(冷藏|REFRIGERATED).*(中)/, layer: 1 },
    { re: /(冷藏|REFRIGERATED).*(下|底)/, layer: 2 },
  ],
  freezer: [
    { re: /(冷冻|FROZEN).*(上|顶)/, layer: 0 },
    { re: /(冷冻|FROZEN).*(中)/, layer: 1 },
    { re: /(冷冻|FROZEN).*(下|底)/, layer: 2 },
  ],
  chiller: [{ re: /(解冻|CHILLER)/, layer: 0 }],
  doorLeft: [
    { re: /门.*左|left.*门/i, layer: 0 },
    { re: /(左门|left).*(上|顶)/, layer: 0 },
    { re: /(左门|left).*(中)/, layer: 1 },
    { re: /(左门|left).*(下|底)/, layer: 2 },
  ],
  doorRight: [
    { re: /门.*右|right.*门/i, layer: 0 },
    { re: /(右门|right).*(上|顶)/, layer: 0 },
    { re: /(右门|right).*(中)/, layer: 1 },
    { re: /(右门|right).*(下|底)/, layer: 2 },
  ],
}

/**
 * 解析食材的位置。
 * 优先级：status PENDING -> 采购篮 -> 规范 subZone 匹配 -> 中文模糊匹配 -> 兜底
 */
export function resolvePosition(item: FridgeItemVO): ResolvedPosition {
  const rawSub = item.subZone ?? null
  const rawZone = item.zone ?? null

  if (item.status === 'PENDING') {
    return { location: 'basket', layer: 0, matched: false, rawSubZone: rawSub, rawZone: rawZone }
  }

  if (rawSub) {
    const s = rawSub.trim().toUpperCase()
    const m = STRICT_RE.exec(s)
    if (m) {
      const zoneToken = m[1] as 'REFRIGERATED' | 'FROZEN' | 'CHILLER'
      const layer = Math.max(0, parseInt(m[2], 10) - 1)
      const location: FridgeLocation =
        zoneToken === 'CHILLER' ? 'chiller' : ZONE_TO_LOCATION[zoneToken]
      return { location, layer, matched: true, rawSubZone: rawSub, rawZone: rawZone }
    }
    const dm = STRICT_DOOR_RE.exec(s)
    if (dm) {
      const side = dm[1] === 'LEFT' ? 'doorLeft' : 'doorRight'
      const layer = Math.max(0, parseInt(dm[2], 10) - 1)
      return { location: side, layer, matched: true, rawSubZone: rawSub, rawZone: rawZone }
    }

    // 模糊匹配（中文 subZone 兼容）
    for (const [loc, list] of Object.entries(FUZZY_RE) as [FridgeLocation, { re: RegExp; layer: number }[]][]) {
      for (const { re, layer } of list) {
        if (re.test(rawSub)) {
          return { location: loc, layer, matched: false, rawSubZone: rawSub, rawZone: rawZone }
        }
      }
    }
  }

  // 兜底：没有 subZone 或无法匹配时，按 zone 落到默认层（最上层 = 0）
  if (rawZone) {
    const loc = ZONE_TO_LOCATION[rawZone]
    return { location: loc, layer: 0, matched: false, rawSubZone: rawSub, rawZone: rawZone }
  }
  return { location: 'basket', layer: 0, matched: false, rawSubZone: rawSub, rawZone: rawZone }
}

/**
 * 把 1-based 层号生成为规范 subZone 字符串。
 */
export function buildSubZone(zone: FridgeZone, layerIndex: number): string {
  const n = layerIndex + 1
  const prefix = zone === 'REFRIGERATED' ? 'REFRIGERATED' : zone === 'FROZEN' ? 'FROZEN' : 'CHILLER'
  return `${prefix}-L${n}`
}

export function buildDoorSubZone(side: 'LEFT' | 'RIGHT', layerIndex: number): string {
  return `DOOR-${side}-L${layerIndex + 1}`
}

/**
 * 判断两个位置是否属于同一温区（用于跨区确认弹窗判定）
 * - 同一温区内移动：静默
 * - 跨温区：弹确认
 *
 * 注意：本函数假设 layout 已确定，可以从 layout 推门-温区映射。
 * 这里我们用 zone + 简易规则：冷藏 <-> 冷冻 跨温区；冷藏/冷冻 <-> 解冻 跨温区；
 * 左右门视 layout 而定。
 */
export function isCrossZone(
  from: { zone: FridgeZone | null; location: FridgeLocation },
  to: { zone: FridgeZone | null; location: FridgeLocation },
): boolean {
  const fromZone = from.location === 'chiller' ? 'CHILLER' : from.zone
  const toZone = to.location === 'chiller' ? 'CHILLER' : to.zone
  // 任一端是采购篮，不算跨区
  if (from.location === 'basket' || to.location === 'basket') return false
  if (!fromZone || !toZone) return false
  return fromZone !== toZone
}

/**
 * 对一组食材按 location+layer 分组，便于视图渲染。
 * 返回结构：
 * {
 *   basket: FridgeItemVO[],
 *   fridge: { 0: [...], 1: [...] },
 *   freezer: { 0: [...] },
 *   chiller: { 0: [...] },
 *   doorLeft: { 0: [...] },
 *   doorRight: { 0: [...] }
 * }
 */
export function groupItems(items: FridgeItemVO[]): GroupedItems {
  const grouped: GroupedItems = {
    basket: [],
    fridge: {},
    freezer: {},
    chiller: {},
    doorLeft: {},
    doorRight: {},
  }
  for (const it of items) {
    const pos = resolvePosition(it)
    if (pos.location === 'basket') {
      grouped.basket.push(it)
    } else {
      const target = grouped[pos.location]
      ;(target[pos.layer] ||= []).push(it)
    }
  }
  return grouped
}

export interface GroupedItems {
  basket: FridgeItemVO[]
  fridge: Record<number, FridgeItemVO[]>
  freezer: Record<number, FridgeItemVO[]>
  chiller: Record<number, FridgeItemVO[]>
  doorLeft: Record<number, FridgeItemVO[]>
  doorRight: Record<number, FridgeItemVO[]>
}
