<script setup lang="ts">
/**
 * 注册页
 * ======
 * 用户名 + 密码 + 确认密码 → authStore.register → 跳转首页
 */
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const error = ref<string | null>(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

async function handleRegister() {
  if (!form.username.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!form.password) {
    error.value = '请输入密码'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  if (form.password !== form.confirmPassword) {
    error.value = '两次密码不一致'
    return
  }
  loading.value = true
  error.value = null
  try {
    await authStore.register({ username: form.username, password: form.password })
    uni.reLaunch({ url: '/pages/fridge/index' })
  } catch {
    error.value = '注册失败，用户名可能已存在'
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  uni.navigateBack()
}
</script>

<template>
  <view class="login-page">
    <view class="login-card">
      <text class="title">注册账号</text>
      <text class="subtitle">加入 HomeCentral</text>

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
            placeholder="至少 6 位"
            password
            :maxlength="64"
          />
        </view>
        <view class="form-item">
          <text class="label">确认密码</text>
          <input
            v-model="form.confirmPassword"
            class="input"
            placeholder="再次输入密码"
            password
            :maxlength="64"
          />
        </view>

        <text v-if="error" class="error">{{ error }}</text>

        <button class="login-btn" :disabled="loading" @click="handleRegister">
          {{ loading ? '注册中…' : '注 册' }}
        </button>
      </view>

      <view class="footer">
        <text class="footer-text">已有账号？</text>
        <text class="link" @click="goToLogin">去登录</text>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 28px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.title {
  display: block;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  color: #333;
}
.subtitle {
  display: block;
  font-size: 14px;
  text-align: center;
  color: #999;
  margin-top: 4px;
  margin-bottom: 28px;
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
  color: #666;
  margin-bottom: 6px;
}
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: #fafafa;
  box-sizing: border-box;
}
.error {
  display: block;
  color: #f56c6c;
  font-size: 13px;
  margin-bottom: 8px;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
}
.login-btn[disabled] {
  background: #b0b8d6;
}
.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}
.footer-text {
  font-size: 13px;
  color: #999;
}
.link {
  font-size: 13px;
  color: #667eea;
  margin-left: 4px;
}
</style>
