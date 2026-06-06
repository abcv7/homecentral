import { describe, expect, it } from 'vitest'
import { expandByQuantity } from './fridgeExpand'
import type { FridgeItemVO } from '../types/fridge'

function mk(over: Partial<FridgeItemVO> & { id: number; name: string }): FridgeItemVO {
  return {
    ...over,
    id: over.id,
    name: over.name,
    zone: over.zone ?? 'REFRIGERATED',
    source: over.source ?? 'MANUAL',
    status: over.status ?? 'ACTIVE',
    quantity: over.quantity,
  } as FridgeItemVO
}

describe('expandByQuantity()', () => {
  it('quantity=3 → 3 cards with descending count 3/2/1', () => {
    const out = expandByQuantity([mk({ id: 1, name: '牛奶', quantity: 3 })])
    expect(out).toHaveLength(3)
    expect(out.map((x) => x.quantity)).toEqual([3, 2, 1])
    expect(out.every((x) => x.id === 1 && x.name === '牛奶')).toBe(true)
  })

  it('quantity=1 → 1 card with count 1', () => {
    const out = expandByQuantity([mk({ id: 1, name: '苹果', quantity: 1 })])
    expect(out).toHaveLength(1)
    expect(out[0].quantity).toBe(1)
  })

  it('quantity=0/空/null → fallback to 1', () => {
    expect(expandByQuantity([mk({ id: 1, name: 'a' })])[0].quantity).toBe(1)
    expect(expandByQuantity([mk({ id: 2, name: 'b', quantity: 0 })])[0].quantity).toBe(1)
    expect(expandByQuantity([mk({ id: 3, name: 'c', quantity: null as any })])[0].quantity).toBe(1)
  })

  it('quantity 字符串 "2" → 解析为 2', () => {
    const out = expandByQuantity([mk({ id: 1, name: '鸡蛋', quantity: '2' as any })])
    expect(out).toHaveLength(2)
    expect(out.map((x) => x.quantity)).toEqual([2, 1])
  })

  it('小数 quantity=2.7 → 取整 2', () => {
    const out = expandByQuantity([mk({ id: 1, name: 'x', quantity: 2.7 })])
    expect(out).toHaveLength(2)
  })

  it('负数 quantity → 兜底为 1', () => {
    const out = expandByQuantity([mk({ id: 1, name: 'x', quantity: -1 })])
    expect(out).toHaveLength(1)
    expect(out[0].quantity).toBe(1)
  })

  it('null/空数组 → 返回空数组', () => {
    expect(expandByQuantity(null)).toEqual([])
    expect(expandByQuantity(undefined)).toEqual([])
    expect(expandByQuantity([])).toEqual([])
  })

  it('多 item 按顺序拼接', () => {
    const out = expandByQuantity([
      mk({ id: 1, name: 'a', quantity: 2 }),
      mk({ id: 2, name: 'b', quantity: 3 }),
    ])
    expect(out).toHaveLength(5)
    expect(out.filter((x) => x.id === 1)).toHaveLength(2)
    expect(out.filter((x) => x.id === 2)).toHaveLength(3)
  })

  it('展开后原对象的非 quantity 字段不变', () => {
    const original = mk({ id: 1, name: '酸奶', quantity: 2, unit: '盒' })
    const out = expandByQuantity([original])
    expect(out[0].name).toBe('酸奶')
    expect(out[0].unit).toBe('盒')
  })
})
