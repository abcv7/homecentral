import { describe, expect, it } from 'vitest'
import { nowHHmmss } from './useFridgeData'

describe('nowHHmmss()', () => {
  it('returns HH:MM:SS format', () => {
    const out = nowHHmmss()
    expect(out).toMatch(/^\d{2}:\d{2}:\d{2}$/)
  })

  it('pads single-digit hours/minutes/seconds with 0', () => {
    const RealDate = globalThis.Date
    const FakeDate = function (this: Date, ...args: unknown[]) {
      if (args.length === 0) {
        return new RealDate('2024-01-01T03:05:09') as unknown as Date
      }
      return new (RealDate as unknown as new (...a: unknown[]) => Date)(...args) as unknown as Date
    } as unknown as typeof Date
    Object.setPrototypeOf(FakeDate, RealDate)
    globalThis.Date = FakeDate
    try {
      expect(nowHHmmss()).toBe('03:05:09')
    } finally {
      globalThis.Date = RealDate
    }
  })
})
