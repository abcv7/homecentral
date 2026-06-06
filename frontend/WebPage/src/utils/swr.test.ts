import { describe, expect, it, beforeEach, vi } from 'vitest'
import { swr, invalidate, invalidatePrefix, clearAll, size } from './swr'

beforeEach(() => {
  clearAll()
  vi.useRealTimers()
})

describe('swr()', () => {
  it('first call: invokes fetcher and caches result', async () => {
    const fetcher = vi.fn().mockResolvedValue({ id: 1 })
    const result = await swr('k1', fetcher, 5000)
    expect(result).toEqual({ id: 1 })
    expect(fetcher).toHaveBeenCalledTimes(1)
    expect(size()).toBe(1)
  })

  it('within TTL: returns cached value, fetcher NOT called again', async () => {
    const fetcher = vi.fn().mockResolvedValue('v1')
    await swr('k1', fetcher, 5000)
    const r2 = await swr('k1', fetcher, 5000)
    expect(r2).toBe('v1')
    expect(fetcher).toHaveBeenCalledTimes(1)
  })

  it('after TTL: revals and updates cache', async () => {
    vi.useFakeTimers()
    const fetcher = vi.fn()
      .mockResolvedValueOnce('v1')
      .mockResolvedValueOnce('v2')
    await swr('k1', fetcher, 1000)
    vi.advanceTimersByTime(2000)
    const r = await swr('k1', fetcher, 1000)
    expect(r).toBe('v2')
    expect(fetcher).toHaveBeenCalledTimes(2)
    vi.useRealTimers()
  })

  it('force=true: revals even within TTL', async () => {
    const fetcher = vi.fn()
      .mockResolvedValueOnce('v1')
      .mockResolvedValueOnce('v2')
    await swr('k1', fetcher, 5000)
    const r = await swr('k1', fetcher, { ttl: 5000, force: true })
    expect(r).toBe('v2')
    expect(fetcher).toHaveBeenCalledTimes(2)
  })

  it('concurrent calls within TTL: fetcher called once', async () => {
    const fetcher = vi.fn().mockResolvedValue('shared')
    const [a, b, c] = await Promise.all([
      swr('k1', fetcher, 5000),
      swr('k1', fetcher, 5000),
      swr('k1', fetcher, 5000),
    ])
    expect(a).toBe('shared')
    expect(b).toBe('shared')
    expect(c).toBe('shared')
    expect(fetcher).toHaveBeenCalledTimes(1)
  })

  it('failure: keeps prior cache, throws error', async () => {
    vi.useFakeTimers()
    const fetcher = vi.fn()
      .mockResolvedValueOnce('v1')
      .mockRejectedValueOnce(new Error('boom'))
    await swr('k1', fetcher, 1000)
    vi.advanceTimersByTime(2000)
    await expect(swr('k1', fetcher, 1000)).rejects.toThrow('boom')
    vi.useRealTimers()
  })

  it('different keys are independent', async () => {
    const f1 = vi.fn().mockResolvedValue('a')
    const f2 = vi.fn().mockResolvedValue('b')
    const r1 = await swr('k1', f1, 5000)
    const r2 = await swr('k2', f2, 5000)
    expect(r1).toBe('a')
    expect(r2).toBe('b')
    expect(size()).toBe(2)
  })
})

describe('invalidate() / invalidatePrefix() / clearAll()', () => {
  it('invalidate(key) removes single entry', async () => {
    await swr('k1', async () => 'a', 5000)
    await swr('k2', async () => 'b', 5000)
    invalidate('k1')
    expect(size()).toBe(1)
  })

  it('invalidatePrefix removes matching entries', async () => {
    await swr('parcels:list:1', async () => 'a', 5000)
    await swr('parcels:list:2', async () => 'b', 5000)
    await swr('fridge:list', async () => 'c', 5000)
    invalidatePrefix('parcels:')
    expect(size()).toBe(1)
  })

  it('clearAll removes everything', async () => {
    await swr('k1', async () => 'a', 5000)
    await swr('k2', async () => 'b', 5000)
    clearAll()
    expect(size()).toBe(0)
  })
})
