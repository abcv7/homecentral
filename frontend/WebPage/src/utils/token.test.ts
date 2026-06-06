import { describe, expect, it, beforeEach } from 'vitest'
import {
  getAccessToken, setAccessToken,
  getRefreshToken, setRefreshToken,
  clearTokens,
} from './token'

beforeEach(() => {
  localStorage.clear()
})

describe('token', () => {
  it('access token: get/set roundtrip', () => {
    expect(getAccessToken()).toBeNull()
    setAccessToken('abc.def.ghi')
    expect(getAccessToken()).toBe('abc.def.ghi')
  })

  it('refresh token: get/set roundtrip', () => {
    expect(getRefreshToken()).toBeNull()
    setRefreshToken('refresh-token-xyz')
    expect(getRefreshToken()).toBe('refresh-token-xyz')
  })

  it('clearTokens removes both token keys atomically', () => {
    setAccessToken('a')
    setRefreshToken('b')
    clearTokens()
    expect(getAccessToken()).toBeNull()
    expect(getRefreshToken()).toBeNull()
  })

  it('overwrites existing values on re-set', () => {
    setAccessToken('first')
    setAccessToken('second')
    expect(getAccessToken()).toBe('second')
  })
})
