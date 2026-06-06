import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getMe as getMeApi } from '../api/auth'
import {
  setAccessToken,
  setRefreshToken,
  setUsername,
  clearTokens,
  getAccessToken,
  getUsername,
} from '../utils/token'
import type { LoginRequest, RegisterRequest, MemberProfileVO } from '../types/api'

export const useAuthStore = defineStore('auth', () => {
  const isLogin = ref(!!getAccessToken())
  const username = ref(getUsername() ?? '')
  const profile = ref<MemberProfileVO | null>(null)

  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    const { accessToken, refreshToken, username: uname } = res.data.data
    setAccessToken(accessToken)
    setRefreshToken(refreshToken)
    setUsername(uname)
    username.value = uname
    isLogin.value = true
    await fetchProfile()
  }

  async function register(data: RegisterRequest) {
    const res = await registerApi(data)
    const { accessToken, refreshToken, username: uname } = res.data.data
    setAccessToken(accessToken)
    setRefreshToken(refreshToken)
    setUsername(uname)
    username.value = uname
    isLogin.value = true
    await fetchProfile()
  }

  async function fetchProfile() {
    try {
      const res = await getMeApi()
      profile.value = res.data.data
      if (profile.value?.username) {
        username.value = profile.value.username
        setUsername(profile.value.username)
      }
    } catch (e) {
      profile.value = null
    }
  }

  function logout() {
    clearTokens()
    isLogin.value = false
    username.value = ''
    profile.value = null
  }

  return { isLogin, username, profile, login, register, logout, fetchProfile }
})
