import axios, { AxiosError } from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, setAccessToken, getRefreshToken, setRefreshToken, clearTokens } from '../utils/token'
import type { ApiResult, LoginResponse } from '../types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
    const userId = parseUserIdFromToken(token)
    if (userId != null) {
      config.headers['X-User-Id'] = String(userId)
    }
  }
  return config
})

function parseUserIdFromToken(token: string): number | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4)
    const json = atob(padded)
    const obj = JSON.parse(json) as { userId?: number }
    return obj.userId ?? null
  } catch {
    return null
  }
}

let refreshing = false
let pendingQueue: Array<{
  resolve: (token: string) => void
  reject: (err: unknown) => void
}> = []

request.interceptors.response.use(
  (res) => res,
  async (err: AxiosError<ApiResult<unknown>>) => {
    const status = err.response?.status
    const original = err.config

    if (status === 401 && original && !original.headers._retry) {
      const refreshToken = getRefreshToken()
      if (!refreshToken) {
        clearTokens()
        window.location.href = '/login'
        return Promise.reject(err)
      }

      if (refreshing) {
        return new Promise<string>((resolve, reject) => {
          pendingQueue.push({ resolve, reject })
        }).then((token) => {
          original.headers.Authorization = `Bearer ${token}`
          return request(original)
        })
      }

      refreshing = true
      original.headers._retry = true

      try {
        const res = await axios.post<ApiResult<LoginResponse>>('/api/auth/refresh', { refreshToken })
        const data = res.data.data
        setAccessToken(data.accessToken)
        setRefreshToken(data.refreshToken)
        pendingQueue.forEach((p) => p.resolve(data.accessToken))
        pendingQueue = []
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return request(original)
      } catch {
        clearTokens()
        window.location.href = '/login'
        return Promise.reject(err)
      } finally {
        refreshing = false
      }
    }

    return Promise.reject(err)
  },
)

export default request
