<script setup lang="ts">
/**
 * 个人资料页 (uni-app 版)
 * 昵称/手机编辑 + 邮箱变更(验证码) + 密码变更(验证码) + 登出
 */
import { ref, reactive } from 'vue'
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  updateProfile,
  sendEmailCode,
  confirmEmailChange,
  sendPasswordCode,
  changePassword,
} from '@/api/auth'

const authStore = useAuthStore()
const loading = ref(false)
const savingProfile = ref(false)

const profile = ref(authStore.profile)
const form = reactive({ nickname: '', phone: '' })

// Email change
const showEmailModal = ref(false)
const emailSubmitting = ref(false)
const sendingCode = ref(false)
const emailCooldown = ref(0)
const emailForm = reactive({ newEmail: '', code: '' })
let emailTimer: ReturnType<typeof setInterval> | null = null

// Password change
const showPasswordModal = ref(false)
const passwordSubmitting = ref(false)
const passwordCooldown = ref(0)
const passwordForm = reactive({ code: '', newPassword: '' })
let passwordTimer: ReturnType<typeof setInterval> | null = null

async function loadProfile() {
  loading.value = true
  try {
    await authStore.fetchProfile()
    profile.value = authStore.profile
    if (profile.value) {
      form.nickname = profile.value.nickname ?? ''
      form.phone = profile.value.phone ?? ''
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => loadProfile())

async function onSaveProfile() {
  savingProfile.value = true
  try {
    const payload: Record<string, string> = {}
    if (form.nickname !== (profile.value?.nickname ?? '')) payload.nickname = form.nickname
    if (form.phone !== (profile.value?.phone ?? '')) payload.phone = form.phone
    if (Object.keys(payload).length === 0) {
      uni.showToast({ title: '未修改', icon: 'none' })
      return
    }
    const res = await updateProfile(payload)
    authStore.profile = res.data.data
    profile.value = res.data.data
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    savingProfile.value = false
  }
}

// Email modal
function openEmailModal() {
  emailForm.newEmail = ''
  emailForm.code = ''
  showEmailModal.value = true
}

function startEmailCooldown() {
  emailCooldown.value = 60
  if (emailTimer) clearInterval(emailTimer)
  emailTimer = setInterval(() => {
    if (emailCooldown.value > 0) emailCooldown.value -= 1
    else if (emailTimer) { clearInterval(emailTimer); emailTimer = null }
  }, 1000)
}

async function onSendEmailCode() {
  if (!emailForm.newEmail.trim()) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  sendingCode.value = true
  try {
    await sendEmailCode(emailForm.newEmail)
    startEmailCooldown()
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    sendingCode.value = false
  }
}

async function onConfirmEmailChange() {
  if (!emailForm.code || emailForm.code.length !== 6) {
    uni.showToast({ title: '请输入6位验证码', icon: 'none' })
    return
  }
  emailSubmitting.value = true
  try {
    const res = await confirmEmailChange({ purpose: 'EMAIL_CHANGE', newEmail: emailForm.newEmail, code: emailForm.code })
    authStore.profile = res.data.data
    profile.value = res.data.data
    showEmailModal.value = false
    uni.showToast({ title: '邮箱已更新', icon: 'success' })
  } catch {
    uni.showToast({ title: '更新失败', icon: 'none' })
  } finally {
    emailSubmitting.value = false
  }
}

// Password modal
function openPasswordModal() {
  passwordForm.code = ''
  passwordForm.newPassword = ''
  showPasswordModal.value = true
}

function startPasswordCooldown() {
  passwordCooldown.value = 60
  if (passwordTimer) clearInterval(passwordTimer)
  passwordTimer = setInterval(() => {
    if (passwordCooldown.value > 0) passwordCooldown.value -= 1
    else if (passwordTimer) { clearInterval(passwordTimer); passwordTimer = null }
  }, 1000)
}

async function onSendPasswordCode() {
  if (!profile.value?.email) {
    uni.showToast({ title: '未设置邮箱', icon: 'none' })
    return
  }
  sendingCode.value = true
  try {
    await sendPasswordCode()
    startPasswordCooldown()
    uni.showToast({ title: '验证码已发送至当前邮箱', icon: 'success' })
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    sendingCode.value = false
  }
}

async function onChangePassword() {
  if (!passwordForm.code || passwordForm.code.length !== 6) {
    uni.showToast({ title: '请输入6位验证码', icon: 'none' })
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  passwordSubmitting.value = true
  try {
    await changePassword({ code: passwordForm.code, newPassword: passwordForm.newPassword })
    showPasswordModal.value = false
    uni.showToast({ title: '密码已修改，请重新登录', icon: 'success' })
    setTimeout(() => authStore.logout(), 1500)
  } catch {
    uni.showToast({ title: '修改失败', icon: 'none' })
  } finally {
    passwordSubmitting.value = false
  }
}

function handleLogout() {
  authStore.logout()
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">👤 个人资料</text>
    </view>

    <view v-if="loading" class="loading">加载中…</view>

    <view v-if="!loading && profile" class="form-section">
      <view class="form-item">
        <text class="label">用户名</text>
        <text class="readonly">{{ profile.username }}</text>
      </view>
      <view class="form-item">
        <text class="label">角色</text>
        <text class="readonly">{{ profile.role ?? 'USER' }}</text>
      </view>
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" class="input" placeholder="请输入昵称" />
        <button class="save-btn" :disabled="savingProfile" @click="onSaveProfile">
          {{ savingProfile ? '…' : '保存' }}
        </button>
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.phone" class="input" placeholder="请输入手机号" />
        <button class="save-btn" :disabled="savingProfile" @click="onSaveProfile">
          {{ savingProfile ? '…' : '保存' }}
        </button>
      </view>
      <view class="form-item">
        <text class="label">邮箱</text>
        <text :class="['readonly', profile.email ? 'has-email' : '']">{{ profile.email ?? '未设置' }}</text>
        <button class="change-btn" @click="openEmailModal">修改邮箱</button>
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <text class="readonly muted">••••••</text>
        <button class="change-btn" @click="openPasswordModal">修改密码</button>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <!-- Email Change Modal -->
    <view v-if="showEmailModal" class="modal-mask" @click="showEmailModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">修改邮箱</text>
        <view class="form-item">
          <text class="label">新邮箱</text>
          <input v-model="emailForm.newEmail" class="input" placeholder="example@qq.com" />
        </view>
        <view class="form-item">
          <text class="label">验证码</text>
          <view class="code-row">
            <input v-model="emailForm.code" class="input code-input" placeholder="6位验证码" :maxlength="6" />
            <button class="code-btn" :disabled="emailCooldown > 0 || sendingCode" @click="onSendEmailCode">
              {{ emailCooldown > 0 ? `${emailCooldown}s` : '发送' }}
            </button>
          </view>
        </view>
        <text class="hint">验证码发送至新邮箱，5分钟内有效</text>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showEmailModal = false">取消</button>
          <button class="modal-btn primary" :disabled="emailSubmitting" @click="onConfirmEmailChange">
            {{ emailSubmitting ? '…' : '确认' }}
          </button>
        </view>
      </view>
    </view>

    <!-- Password Change Modal -->
    <view v-if="showPasswordModal" class="modal-mask" @click="showPasswordModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">修改密码</text>
        <view class="form-item">
          <text class="label">验证码</text>
          <view class="code-row">
            <input v-model="passwordForm.code" class="input code-input" placeholder="6位验证码" :maxlength="6" />
            <button class="code-btn" :disabled="passwordCooldown > 0 || sendingCode" @click="onSendPasswordCode">
              {{ passwordCooldown > 0 ? `${passwordCooldown}s` : '发送' }}
            </button>
          </view>
        </view>
        <view class="form-item">
          <text class="label">新密码</text>
          <input v-model="passwordForm.newPassword" class="input" password placeholder="至少6位" />
        </view>
        <text class="hint">验证码发送至当前邮箱 {{ profile?.email ?? '' }}</text>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showPasswordModal = false">取消</button>
          <button class="modal-btn primary" :disabled="passwordSubmitting" @click="onChangePassword">
            {{ passwordSubmitting ? '…' : '确认' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 16px 20px;
  padding-bottom: 80px;
  background: var(--qwu-bg, #faf8f5);
  min-height: 100vh;
}
.header {
  margin-bottom: 20px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  display: block;
  color: var(--qwu-text, #1c1917);
  letter-spacing: -0.5px;
}
.loading {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
}
.form-section {
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 6px 16px;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
}
.form-item {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--qwu-border-light, #f5f5f4);
}
.form-item:last-child {
  border-bottom: none;
}
.label {
  font-size: 14px;
  color: var(--qwu-text-secondary, #78716c);
  width: 70px;
  flex-shrink: 0;
  font-weight: 500;
}
.readonly {
  font-size: 14px;
  color: var(--qwu-text, #1c1917);
  flex: 1;
}
.readonly.has-email {
  color: var(--qwu-success, #10b981);
}
.readonly.muted {
  color: var(--qwu-text-muted, #a8a29e);
}
.input {
  flex: 1;
  padding: 8px 10px;
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: var(--qwu-radius-xs, 6px);
  font-size: 14px;
  background: var(--qwu-bg, #faf8f5);
  color: var(--qwu-text, #1c1917);
}
.input:focus {
  border-color: var(--qwu-primary, #f97316);
  outline: none;
}
.save-btn {
  font-size: 12px;
  padding: 5px 12px;
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-xs, 6px);
  margin-left: 8px;
  font-weight: 600;
}
.save-btn[disabled] {
  background: var(--qwu-border, #e7e5e4);
}
.change-btn {
  font-size: 12px;
  padding: 5px 12px;
  background: transparent;
  color: var(--qwu-primary, #f97316);
  border: 1.5px solid var(--qwu-primary, #f97316);
  border-radius: var(--qwu-radius-xs, 6px);
  margin-left: 8px;
  font-weight: 500;
}
.logout-btn {
  margin-top: 28px;
  padding: 13px;
  background: var(--qwu-danger-light, #fef2f2);
  color: var(--qwu-danger, #ef4444);
  border: 1.5px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--qwu-radius-sm, 10px);
  font-size: 14px;
  font-weight: 600;
}
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(28, 25, 23, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  width: 85%;
  max-width: 380px;
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 24px;
  box-shadow: var(--qwu-shadow-md, 0 4px 6px -1px rgba(28,25,23,0.07), 0 2px 4px -2px rgba(28,25,23,0.05));
}
.modal-title {
  display: block;
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 18px;
  color: var(--qwu-text, #1c1917);
}
.code-row {
  display: flex;
  gap: 8px;
}
.code-input {
  flex: 1;
}
.code-btn {
  font-size: 12px;
  padding: 9px 14px;
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-xs, 6px);
  flex-shrink: 0;
  font-weight: 600;
}
.code-btn[disabled] {
  background: var(--qwu-border, #e7e5e4);
  color: var(--qwu-text-muted, #a8a29e);
}
.hint {
  display: block;
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
  margin-top: 10px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
.modal-btn {
  font-size: 14px;
  padding: 9px 18px;
  border-radius: var(--qwu-radius-sm, 10px);
  font-weight: 500;
}
.modal-btn.cancel {
  background: var(--qwu-bg, #faf8f5);
  color: var(--qwu-text-secondary, #78716c);
  border: 1.5px solid var(--qwu-border, #e7e5e4);
}
.modal-btn.primary {
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.modal-btn[disabled] {
  background: var(--qwu-border, #e7e5e4);
  color: var(--qwu-text-muted, #a8a29e);
  box-shadow: none;
}
</style>
