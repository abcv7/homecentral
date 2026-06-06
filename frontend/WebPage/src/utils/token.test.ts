import { describe, expect, it, beforeEach } from 'vitest'
import {
  getAccessToken, setAccessToken,
  getRefreshToken, setRefreshToken,
  getUsername, setUsername,
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

  it('username: get/set roundtrip', () => {
    expect(getUsername()).toBeNull()
    setUsername('alice')
    expect(getUsername()).toBe('alice')
  })

  it('clearTokens removes all three keys atomically', () => {
    setAccessToken('a')
    setRefreshToken('b')
    setUsername('c')
    clearTokens()
    expect(getAccessToken()).toBeNull()
    expect(getRefreshToken()).toBeNull()
    expect(getUsername()).toBeNull()
  })

  it('overwrites existing values on re-set', () => {
    setAccessToken('first')
    setAccessToken('second')
    expect(getAccessToken()).toBe('second')
  })
})
