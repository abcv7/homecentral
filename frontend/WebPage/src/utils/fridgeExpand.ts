import type { FridgeItemVO } from '../types/fridge'

/**
 * 按 quantity 数量展开食材为 N 张卡片。
 * 同一 itemId 复制 N 次（quantity=2 → 2 张），点任一卡消耗 1 → 剩 1 张。
 * quantity 为 0/空/null 时按 1 处理。
 */
export function expandByQuantity(items: FridgeItemVO[] | null | undefined): FridgeItemVO[] {
  if (!items || items.length === 0) return []
  const result: FridgeItemVO[] = []
  for (const item of items) {
    const qty = Number(item.quantity) || 1
    const safeQty = Math.max(1, Math.floor(qty))
    for (let i = 0; i < safeQty; i++) {
      result.push({ ...item, quantity: safeQty - i })
    }
  }
  return result
}
