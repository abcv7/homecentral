<template>
  <div class="profile-page">
    <n-card title="个人资料" :bordered="false" class="profile-card">
      <n-spin :show="loading">
        <n-form
          v-if="profile"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '100'"
          :show-feedback="false"
          :model="form"
          style="max-width: 520px"
        >
          <n-form-item label="用户名">
            <n-input :value="profile.username" disabled />
          </n-form-item>
          <n-form-item label="角色">
            <n-tag :type="profile.role === 'ADMIN' ? 'warning' : 'default'" size="small">
              {{ profile.role ?? 'USER' }}
            </n-tag>
          </n-form-item>
          <n-form-item label="昵称">
            <n-space style="width: 100%">
              <n-input v-model:value="form.nickname" placeholder="请输入昵称" maxlength="32" show-count />
              <n-button type="primary" :loading="savingProfile" @click="onSaveProfile">保存</n-button>
            </n-space>
          </n-form-item>
          <n-form-item label="手机号">
            <n-space style="width: 100%">
              <n-input v-model:value="form.phone" placeholder="请输入手机号" maxlength="20" />
              <n-button type="primary" :loading="savingProfile" @click="onSaveProfile">保存</n-button>
            </n-space>
          </n-form-item>
          <n-form-item label="邮箱">
            <n-space align="center">
              <n-tag :type="profile.email ? 'success' : 'default'">
                {{ profile.email ?? '未设置' }}
              </n-tag>
              <n-button size="small" @click="openEmailModal">修改邮箱</n-button>
            </n-space>
          </n-form-item>
          <n-form-item label="密码">
            <n-button size="small" @click="openPasswordModal">修改密码</n-button>
          </n-form-item>
        </n-form>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="emailModalVisible"
      preset="card"
      title="修改邮箱"
      :style="isMobile ? { width: '94vw', maxWidth: '420px' } : { width: '480px' }"
      :mask-closable="false"
    >
      <n-form label-placement="left" label-width="80" :model="emailForm">
        <n-form-item label="新邮箱">
          <n-input v-model:value="emailForm.newEmail" placeholder="example@qq.com" />
        </n-form-item>
        <n-form-item label="验证码">
          <n-space style="width: 100%">
            <n-input v-model:value="emailForm.code" placeholder="6 位验证码" maxlength="6" />
            <n-button
              :disabled="emailCooldown > 0"
              :loading="sendingCode"
              @click="onSendEmailCode"
            >
              {{ emailCooldown > 0 ? `${emailCooldown}s 后重发` : '发送验证码' }}
            </n-button>
          </n-space>
        </n-form-item>
        <n-text depth="3" style="font-size: 12px">
          验证码将发送至新邮箱，5 分钟内有效
        </n-text>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="emailModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="confirmingEmail" @click="onConfirmEmailChange">
            确认
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="passwordModalVisible"
      preset="card"
      title="修改密码"
      :style="isMobile ? { width: '94vw', maxWidth: '420px' } : { width: '480px' }"
      :mask-closable="false"
    >
      <n-form label-placement="left" label-width="100" :model="passwordForm">
        <n-form-item label="验证码">
          <n-space style="width: 100%">
            <n-input v-model:value="passwordForm.code" placeholder="6 位验证码" maxlength="6" />
            <n-button
              :disabled="passwordCooldown > 0"
              :loading="sendingCode"
              @click="onSendPasswordCode"
            >
              {{ passwordCooldown > 0 ? `${passwordCooldown}s 后重发` : '发送验证码' }}
            </n-button>
          </n-space>
        </n-form-item>
        <n-form-item label="新密码">
          <n-input
            v-model:value="passwordForm.newPassword"
            type="password"
            show-password-on="click"
            placeholder="6-20 位"
            maxlength="20"
          />
        </n-form-item>
        <n-text depth="3" style="font-size: 12px">
          验证码将发送至当前邮箱 {{ profile?.email ?? '' }}，5 分钟内有效
        </n-text>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="passwordModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="confirmingPassword" @click="onChangePassword">
            确认
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import {
  NCard,
  NSpin,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace,
  NTag,
  NModal,
  NText,
  useMessage,
} from 'naive-ui'
import { useAuthStore } from '../../stores/auth'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
import {
  updateProfile,
  sendEmailCode as sendEmailCodeApi,
  confirmEmailChange as confirmEmailChangeApi,
  sendPasswordCode as sendPasswordCodeApi,
  changePassword as changePasswordApi,
} from '../../api/auth'

const authStore = useAuthStore()
const message = useMessage()

const loading = ref(false)
const savingProfile = ref(false)
const sendingCode = ref(false)
const confirmingEmail = ref(false)
const confirmingPassword = ref(false)

const profile = ref(authStore.profile)
const form = reactive({ nickname: '', phone: '' })

const emailModalVisible = ref(false)
const emailCooldown = ref(0)
const emailForm = reactive({ newEmail: '', code: '' })

const passwordModalVisible = ref(false)
const passwordCooldown = ref(0)
const passwordForm = reactive({ code: '', newPassword: '' })

let cooldownTimer: number | null = null

function startCooldown(target: 'email' | 'password') {
  const setter = target === 'email' ? (v: number) => (emailCooldown.value = v) : (v: number) => (passwordCooldown.value = v)
  setter(60)
  if (cooldownTimer) window.clearInterval(cooldownTimer)
  cooldownTimer = window.setInterval(() => {
    if (emailCooldown.value > 0) emailCooldown.value -= 1
    if (passwordCooldown.value > 0) passwordCooldown.value -= 1
    if (emailCooldown.value <= 0 && passwordCooldown.value <= 0 && cooldownTimer) {
      window.clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

async function loadProfile() {
  loading.value = true
  try {
    await authStore.fetchProfile()
    profile.value = authStore.profile
    if (profile.value) {
      form.nickname = profile.value.nickname ?? ''
      form.phone = profile.value.phone ?? ''
    }
  } catch (e: any) {
    message.error(e?.message ?? '加载个人资料失败')
  } finally {
    loading.value = false
  }
}

async function onSaveProfile() {
  savingProfile.value = true
  try {
    const payload: { nickname?: string; phone?: string } = {}
    if (form.nickname !== (profile.value?.nickname ?? '')) payload.nickname = form.nickname
    if (form.phone !== (profile.value?.phone ?? '')) payload.phone = form.phone
    if (Object.keys(payload).length === 0) {
      message.info('未修改')
      return
    }
    const res = await updateProfile(payload)
    message.success('保存成功')
    authStore.profile = res.data.data
    profile.value = res.data.data
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '保存失败')
  } finally {
    savingProfile.value = false
  }
}

function openEmailModal() {
  emailForm.newEmail = ''
  emailForm.code = ''
  emailModalVisible.value = true
}

async function onSendEmailCode() {
  if (!emailForm.newEmail || !/^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(emailForm.newEmail)) {
    message.warning('请输入有效邮箱')
    return
  }
  sendingCode.value = true
  try {
    await sendEmailCodeApi(emailForm.newEmail)
    message.success('验证码已发送至新邮箱')
    startCooldown('email')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function onConfirmEmailChange() {
  if (!emailForm.code || emailForm.code.length !== 6) {
    message.warning('请输入 6 位验证码')
    return
  }
  confirmingEmail.value = true
  try {
    const res = await confirmEmailChangeApi({ purpose: 'EMAIL_CHANGE', newEmail: emailForm.newEmail, code: emailForm.code })
    message.success('邮箱已更新')
    authStore.profile = res.data.data
    profile.value = res.data.data
    emailModalVisible.value = false
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '邮箱更新失败')
  } finally {
    confirmingEmail.value = false
  }
}

function openPasswordModal() {
  passwordForm.code = ''
  passwordForm.newPassword = ''
  passwordModalVisible.value = true
}

async function onSendPasswordCode() {
  if (!profile.value?.email) {
    message.warning('当前账号未设置邮箱，无法修改密码')
    return
  }
  sendingCode.value = true
  try {
    await sendPasswordCodeApi()
    message.success(`验证码已发送至 ${profile.value.email}`)
    startCooldown('password')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function onChangePassword() {
  if (!passwordForm.code || passwordForm.code.length !== 6) {
    message.warning('请输入 6 位验证码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    message.warning('新密码至少 6 位')
    return
  }
  confirmingPassword.value = true
  try {
    await changePasswordApi({ code: passwordForm.code, newPassword: passwordForm.newPassword })
    message.success('密码已修改，请使用新密码重新登录')
    passwordModalVisible.value = false
    setTimeout(() => {
      authStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '修改失败')
  } finally {
    confirmingPassword.value = false
  }
}

onMounted(() => {
  loadProfile()
})

onBeforeUnmount(() => {
  if (cooldownTimer) window.clearInterval(cooldownTimer)
})
</script>

<style scoped>
.profile-page {
  max-width: 720px;
}
.profile-card {
  border-radius: 8px;
}
</style>
