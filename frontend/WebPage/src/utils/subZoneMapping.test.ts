import { describe, expect, it } from 'vitest'
import { resolvePosition, buildSubZone, buildDoorSubZone, isCrossZone, groupItems } from './subZoneMapping'
import type { FridgeItemVO } from '../types/fridge'

function mk(over: Partial<FridgeItemVO> & { id: number; name: string }): FridgeItemVO {
  return {
    ...over,
    id: over.id,
    name: over.name,
    zone: over.zone ?? 'REFRIGERATED',
    source: over.source ?? 'MANUAL',
    status: over.status ?? 'ACTIVE',
    subZone: over.subZone,
  } as FridgeItemVO
}

describe('resolvePosition()', () => {
  it('PENDING status → basket (采购篮)', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', status: 'PENDING' }))
    expect(pos.location).toBe('basket')
    expect(pos.matched).toBe(false)
  })

  it('规范 subZone "REFRIGERATED-L1" → fridge, layer 0', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'REFRIGERATED-L1' }))
    expect(pos.location).toBe('fridge')
    expect(pos.layer).toBe(0)
    expect(pos.matched).toBe(true)
  })

  it('规范 subZone "FROZEN-L2" → freezer, layer 1', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'FROZEN-L2' }))
    expect(pos.location).toBe('freezer')
    expect(pos.layer).toBe(1)
  })

  it('规范 subZone "CHILLER-L1" → chiller', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'CHILLER-L1' }))
    expect(pos.location).toBe('chiller')
  })

  it('规范 door subZone "DOOR-LEFT-L1" → doorLeft, layer 0', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'DOOR-LEFT-L1' }))
    expect(pos.location).toBe('doorLeft')
    expect(pos.layer).toBe(0)
  })

  it('规范 door subZone "DOOR-RIGHT-L2" → doorRight, layer 1', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'DOOR-RIGHT-L2' }))
    expect(pos.location).toBe('doorRight')
    expect(pos.layer).toBe(1)
  })

  it('大小写不敏感：toUpperCase()', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: 'refrigerated-l2' }))
    expect(pos.location).toBe('fridge')
    expect(pos.layer).toBe(1)
  })

  it('中文 subZone "冷藏-中层" → fridge, layer 1 (fuzzy, matched=false)', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: '冷藏-中层' }))
    expect(pos.location).toBe('fridge')
    expect(pos.layer).toBe(1)
    expect(pos.matched).toBe(false)
  })

  it('中文 subZone "冷冻-下层" → freezer, layer 2', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: '冷冻-下层' }))
    expect(pos.location).toBe('freezer')
    expect(pos.layer).toBe(2)
  })

  it('无 subZone + 有 zone → 落到 zone 默认层 0', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', zone: 'FROZEN' }))
    expect(pos.location).toBe('freezer')
    expect(pos.layer).toBe(0)
    expect(pos.matched).toBe(false)
  })

  it('无 subZone + 无 zone → 兜底 basket', () => {
    const pos = resolvePosition({ id: 1, name: 'a', zone: null, source: 'MANUAL', status: 'ACTIVE' } as FridgeItemVO)
    expect(pos.location).toBe('basket')
  })

  it('无法识别的 subZone + 有 zone → 兜底 zone 0 层', () => {
    const pos = resolvePosition(mk({ id: 1, name: 'a', subZone: '???', zone: 'REFRIGERATED' }))
    expect(pos.location).toBe('fridge')
    expect(pos.layer).toBe(0)
  })
})

describe('buildSubZone() / buildDoorSubZone()', () => {
  it('冷藏 layer 0 → REFRIGERATED-L1', () => {
    expect(buildSubZone('REFRIGERATED', 0)).toBe('REFRIGERATED-L1')
  })

  it('冷冻 layer 1 → FROZEN-L2', () => {
    expect(buildSubZone('FROZEN', 1)).toBe('FROZEN-L2')
  })

  it('门-左 layer 0 → DOOR-LEFT-L1', () => {
    expect(buildDoorSubZone('LEFT', 0)).toBe('DOOR-LEFT-L1')
  })

  it('门-右 layer 2 → DOOR-RIGHT-L3', () => {
    expect(buildDoorSubZone('RIGHT', 2)).toBe('DOOR-RIGHT-L3')
  })
})

describe('isCrossZone()', () => {
  it('冷藏 → 冷冻 = 跨区', () => {
    expect(isCrossZone(
      { zone: 'REFRIGERATED', location: 'fridge' },
      { zone: 'FROZEN', location: 'freezer' },
    )).toBe(true)
  })

  it('冷藏 → 冷藏 = 同区', () => {
    expect(isCrossZone(
      { zone: 'REFRIGERATED', location: 'fridge' },
      { zone: 'REFRIGERATED', location: 'fridge' },
    )).toBe(false)
  })

  it('任一端 basket = 不算跨区', () => {
    expect(isCrossZone(
      { zone: 'REFRIGERATED', location: 'basket' },
      { zone: 'FROZEN', location: 'freezer' },
    )).toBe(false)
  })

  it('chiller 视为 CHILLER 区，与冷藏/冷冻跨区', () => {
    expect(isCrossZone(
      { zone: 'REFRIGERATED', location: 'fridge' },
      { zone: null, location: 'chiller' },
    )).toBe(true)
  })
})

describe('groupItems()', () => {
  it('按 location+layer 正确分桶', () => {
    const items = [
      mk({ id: 1, name: 'a', subZone: 'REFRIGERATED-L1' }),
      mk({ id: 2, name: 'b', subZone: 'REFRIGERATED-L1' }),
      mk({ id: 3, name: 'c', subZone: 'REFRIGERATED-L2' }),
      mk({ id: 4, name: 'd', subZone: 'FROZEN-L1' }),
      mk({ id: 5, name: 'e', status: 'PENDING' }),
    ]
    const g = groupItems(items)
    expect(g.fridge[0]).toHaveLength(2)
    expect(g.fridge[1]).toHaveLength(1)
    expect(g.freezer[0]).toHaveLength(1)
    expect(g.basket).toHaveLength(1)
  })

  it('空数组 → 全空桶', () => {
    const g = groupItems([])
    expect(g.basket).toEqual([])
    expect(Object.keys(g.fridge)).toHaveLength(0)
  })
})
