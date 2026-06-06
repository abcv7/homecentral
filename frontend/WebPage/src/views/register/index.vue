<template>
  <div class="register-wrapper">
    <div class="register-card">
      <h2 class="register-title">创建账号</h2>
      <p class="register-subtitle">注册 HomeCentral 账户</p>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top" @submit.prevent="handleRegister">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="2-50个字符" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="至少6位" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="form.nickname" placeholder="可选，默认为用户名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="form.phone" placeholder="用于快递物流查询（可选）" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" attr-type="submit">注 册</n-button>
      </n-form>
      <p class="login-link">
        已有账号？
        <router-link to="/login">去登录</router-link>
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
  nickname: '',
  phone: '',
})

const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await authStore.register(form)
    message.success('注册成功')
    router.push('/')
  } catch {
    message.error('注册失败，请检查用户名是否已被使用')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 380px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.register-title {
  margin: 0 0 4px;
  font-size: 24px;
  text-align: center;
  color: #333;
}
.register-subtitle {
  margin: 0 0 28px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
.login-link {
  margin: 16px 0 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}
.login-link a {
  color: #667eea;
  text-decoration: none;
}
</style>
