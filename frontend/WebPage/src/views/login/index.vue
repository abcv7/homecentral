<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h2 class="login-title">HomeCentral</h2>
      <p class="login-subtitle">家庭中心控制系统</p>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top" @submit.prevent="handleLogin">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" attr-type="submit">登 录</n-button>
      </n-form>
      <p class="register-link">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await authStore.login(form)
    message.success('登录成功')
    router.push('/')
  } catch {
    message.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 380px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.login-title {
  margin: 0 0 4px;
  font-size: 24px;
  text-align: center;
  color: #333;
}
.login-subtitle {
  margin: 0 0 28px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
.register-link {
  margin: 16px 0 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}
.register-link a {
  color: #667eea;
  text-decoration: none;
}
</style>
