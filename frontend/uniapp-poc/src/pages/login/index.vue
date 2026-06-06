<script setup lang="ts">
/**
 * 登录页
 * ======
 * 用户名 + 密码 → authStore.login → 跳转首页
 * 底部链接去注册页
 */
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const error = ref<string | null>(null)

const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  loading.value = true
  error.value = null
  try {
    await authStore.login(form)
    uni.reLaunch({ url: '/pages/fridge/index' })
  } catch {
    error.value = '用户名或密码错误'
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  uni.navigateTo({ url: '/pages/register/index' })
}
</script>

<template>
  <view class="login-page">
    <view class="login-card">
      <view class="brand-icon">
        <text class="brand-emoji">🏡</text>
      </view>
      <text class="title">栖物集</text>
      <text class="subtitle">HOME CENTRAL · 家的中心</text>

      <view class="form">
        <view class="form-item">
          <text class="label">用户名</text>
          <input
            v-model="form.username"
            class="input"
            placeholder="请输入用户名"
            :maxlength="32"
          />
        </view>
        <view class="form-item">
          <text class="label">密码</text>
          <input
            v-model="form.password"
            class="input"
            placeholder="请输入密码"
            password
            :maxlength="64"
          />
        </view>

        <text v-if="error" class="error">{{ error }}</text>

        <button class="login-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </view>

      <view class="footer">
        <text class="footer-text">没有账号？</text>
        <text class="link" @click="goToRegister">去注册</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f97316 0%, #fbbf24 100%);
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--qwu-radius, 14px);
  padding: 40px 28px;
  box-shadow: var(--qwu-shadow-md, 0 4px 6px -1px rgba(28,25,23,0.07), 0 2px 4px -2px rgba(28,25,23,0.05));
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.brand-icon {
  text-align: center;
  margin-bottom: 8px;
}
.brand-emoji {
  font-size: 40px;
}
.title {
  display: block;
  font-size: 26px;
  font-weight: 800;
  text-align: center;
  color: var(--qwu-text, #1c1917);
  letter-spacing: -0.5px;
}
.subtitle {
  display: block;
  font-size: 13px;
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  margin-top: 4px;
  margin-bottom: 32px;
  letter-spacing: 1px;
}
.form {
  width: 100%;
}
.form-item {
  margin-bottom: 16px;
}
.label {
  display: block;
  font-size: 13px;
  color: var(--qwu-text-secondary, #78716c);
  margin-bottom: 6px;
  font-weight: 500;
}
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: var(--qwu-radius-xs, 6px);
  font-size: 14px;
  background: var(--qwu-bg, #faf8f5);
  box-sizing: border-box;
  color: var(--qwu-text, #1c1917);
}
.input:focus {
  border-color: var(--qwu-primary, #f97316);
  outline: none;
}
.error {
  display: block;
  color: var(--qwu-danger, #ef4444);
  font-size: 13px;
  margin-bottom: 8px;
  background: var(--qwu-danger-light, #fef2f2);
  padding: 6px 10px;
  border-radius: var(--qwu-radius-xs, 6px);
}
.login-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #f97316, #f59e0b);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-sm, 10px);
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.3);
}
.login-btn[disabled] {
  background: var(--qwu-border, #e7e5e4);
  box-shadow: none;
}
.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
}
.footer-text {
  font-size: 13px;
  color: var(--qwu-text-muted, #a8a29e);
}
.link {
  font-size: 13px;
  color: var(--qwu-primary, #f97316);
  margin-left: 4px;
  font-weight: 600;
}
</style>
