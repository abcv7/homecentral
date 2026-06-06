/**
 * uni-app 版 token 存储
 * 用 uni.getStorageSync/setStorageSync (H5/小程序/App 全平台支持)
 * 替代 web 的 localStorage (在小程序环境不可用)
 */

const ACCESS_KEY = 'auth:access'
const REFRESH_KEY = 'auth:refresh'
const USERNAME_KEY = 'auth:username'

export function getAccessToken(): string | null {
  try {
    return uni.getStorageSync(ACCESS_KEY) || null
  } catch {
    return null
  }
}

export function setAccessToken(token: string): void {
  try {
    uni.setStorageSync(ACCESS_KEY, token)
  } catch {
    /* ignore */
  }
}

export function getRefreshToken(): string | null {
  try {
    return uni.getStorageSync(REFRESH_KEY) || null
  } catch {
    return null
  }
}

export function setRefreshToken(token: string): void {
  try {
    uni.setStorageSync(REFRESH_KEY, token)
  } catch {
    /* ignore */
  }
}

export function getUsername(): string | null {
  try {
    return uni.getStorageSync(USERNAME_KEY) || null
  } catch {
    return null
  }
}

export function setUsername(name: string): void {
  try {
    uni.setStorageSync(USERNAME_KEY, name)
  } catch {
    /* ignore */
  }
}

export function clearTokens(): void {
  try {
    uni.removeStorageSync(ACCESS_KEY)
    uni.removeStorageSync(REFRESH_KEY)
    uni.removeStorageSync(USERNAME_KEY)
  } catch {
    /* ignore */
  }
}
