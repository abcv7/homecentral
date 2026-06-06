import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getMe as getMeApi } from '../api/auth'
import {
  setAccessToken,
  setRefreshToken,
  clearTokens,
  getAccessToken,
} from '../utils/token'
import type { LoginRequest, RegisterRequest, MemberProfileVO } from '../types/api'

export const useAuthStore = defineStore('auth', () => {
  const isLogin = ref(!!getAccessToken())
  const profile = ref<MemberProfileVO | null>(null)

  // username 不再本地存储，恒从后端 /me 拉取
  const username = computed(() => profile.value?.username ?? '')

  /**
   * 应用启动时调用：若本地有 token 则调 /me 拉取用户信息。
   * 失败（401 等）则清空登录态。
   */
  async function hydrate() {
    if (!getAccessToken()) {
      isLogin.value = false
      profile.value = null
      return
    }
    isLogin.value = true
    try {
      const res = await getMeApi()
      profile.value = res.data.data
    } catch {
      // 401 由 request 拦截器处理；此处只清本地态
      profile.value = null
    }
  }

  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    const { accessToken, refreshToken } = res.data.data
    setAccessToken(accessToken)
    setRefreshToken(refreshToken)
    isLogin.value = true
    await hydrate()
  }

  async function register(data: RegisterRequest) {
    const res = await registerApi(data)
    const { accessToken, refreshToken } = res.data.data
    setAccessToken(accessToken)
    setRefreshToken(refreshToken)
    isLogin.value = true
    await hydrate()
  }

  async function fetchProfile() {
    await hydrate()
  }

  function logout() {
    clearTokens()
    isLogin.value = false
    profile.value = null
  }

  return { isLogin, username, profile, login, register, logout, hydrate, fetchProfile }
})
