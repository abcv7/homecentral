/**
 * uni-app 版 HTTP client
 * ======================
 * 替代 web 的 axios + XHR,基于 uni.request 同时支持 H5 / 微信小程序 / App。
 *
 * API 形态尽量贴近 web 版 (返回 { data: ApiResult<T> } 结构)
 * 让共享的 types 直接可用,不需要 wrap 一层。
 */

import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken, clearTokens } from '../utils/token'
import type { LoginResponse } from '../types/api'

interface RequestConfig {
  url: string
  method: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
  params?: Record<string, unknown>
  headers?: Record<string, string>
}

interface ApiResponse<T> {
  data: T
  status: number
}

const BASE_URL = '/api'

function buildUrl(path: string, params?: Record<string, unknown>): string {
  const fullPath = path.startsWith('/') ? `${BASE_URL}${path}` : `${BASE_URL}/${path}`
  if (!params) return fullPath
  const qs = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')
  return qs ? `${fullPath}?${qs}` : fullPath
}

function decodeBase64(s: string): string {
  // H5 环境原生 atob;小程序/APP 用 Buffer
  // @ts-ignore - 跨平台兼容
  if (typeof atob !== 'undefined') return atob(s)
  // @ts-ignore - 微信小程序/APP 走 Buffer
  return Buffer.from(s, 'base64').toString('utf-8')
}

function parseUserIdFromToken(token: string): number | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4)
    const json = decodeBase64(padded)
    const obj = JSON.parse(json) as { userId?: number }
    return obj.userId ?? null
  } catch {
    return null
  }
}

function buildHeaders(): Record<string, string> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  const token = getAccessToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
    const userId = parseUserIdFromToken(token)
    if (userId != null) headers['X-User-Id'] = String(userId)
  }
  return headers
}

function request<T>(config: RequestConfig): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const doRequest = (): void => {
      uni.request({
        url: buildUrl(config.url, config.params),
        method: config.method,
        data: config.data as string | object | ArrayBuffer | undefined,
        header: { ...buildHeaders(), ...(config.headers ?? {}) },
        timeout: 15000,
        success: (res) => {
          const body = res.data as T
          if (res.statusCode === 401) {
            handleRefresh().then((newToken) => {
              if (!newToken) {
                reject(new Error('Unauthorized'))
                return
              }
              doRequest()
            }).catch(() => reject(new Error('Unauthorized')))
            return
          }
          resolve({ data: body, status: res.statusCode })
        },
        fail: (err: unknown) => reject(err),
      })
    }
    doRequest()
  })
}

let refreshing = false
let pendingQueue: Array<{ resolve: (t: string) => void; reject: (e: unknown) => void }> = []

async function handleRefresh(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    clearTokens()
    uni.reLaunch({ url: '/pages/login/index' })
    return null
  }
  if (refreshing) {
    return new Promise<string>((resolve, reject) => {
      pendingQueue.push({ resolve, reject })
    })
  }
  refreshing = true
  try {
    const res = await new Promise<ApiResponse<LoginResponse>>((resolve, reject) => {
      uni.request({
        url: '/api/auth/refresh',
        method: 'POST',
        data: { refreshToken } as object,
        header: { 'Content-Type': 'application/json' },
        success: (r) => resolve({ data: r.data as LoginResponse, status: r.statusCode }),
        fail: (e: unknown) => reject(e),
      })
    })
    const data = res.data
    if (!data) {
      clearTokens()
      uni.reLaunch({ url: '/pages/login/index' })
      return null
    }
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    pendingQueue.forEach((p) => p.resolve(data.accessToken))
    pendingQueue = []
    return data.accessToken
  } catch {
    clearTokens()
    uni.reLaunch({ url: '/pages/login/index' })
    return null
  } finally {
    refreshing = false
  }
}

const requestDefault = {
  get<T>(url: string, opts?: { params?: Record<string, unknown> }): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'GET', params: opts?.params })
  },
  post<T>(url: string, data?: unknown, opts?: { params?: Record<string, unknown> }): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'POST', data, params: opts?.params })
  },
  put<T>(url: string, data?: unknown, opts?: { params?: Record<string, unknown> }): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'PUT', data, params: opts?.params })
  },
  delete<T>(url: string, opts?: { params?: Record<string, unknown> }): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'DELETE', params: opts?.params })
  },
}

export default requestDefault
