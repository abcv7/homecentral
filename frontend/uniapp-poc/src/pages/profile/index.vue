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
        <text :class="['readonly', profile.email ? 'success' : '']">{{ profile.email ?? '未设置' }}</text>
        <button class="change-btn" @click="openEmailModal">修改邮箱</button>
      </view>
      <view class="form-item">
        <text class="label">密码</text>
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
.page { padding: 16px; }
.header { margin-bottom: 16px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.form-section { background: #fff; border-radius: 8px; padding: 16px; }
.form-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.form-item:last-child { border-bottom: none; }
.label { font-size: 14px; color: #666; width: 70px; flex-shrink: 0; }
.readonly { font-size: 14px; color: #333; flex: 1; }
.readonly.success { color: #10b981; }
.input { flex: 1; padding: 6px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; background: #fafafa; }
.save-btn { font-size: 12px; padding: 4px 8px; background: #667eea; color: #fff; border: none; border-radius: 4px; margin-left: 8px; }
.change-btn { font-size: 12px; padding: 4px 8px; background: transparent; color: #667eea; border: 1px solid #667eea; border-radius: 4px; margin-left: 8px; }
.logout-btn { margin-top: 24px; padding: 12px; background: #f5f5f5; color: #f56c6c; border: 1px solid #fecaca; border-radius: 8px; font-size: 14px; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 85%; max-width: 380px; background: #fff; border-radius: 12px; padding: 24px; }
.modal-title { display: block; font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.code-row { display: flex; gap: 8px; }
.code-input { flex: 1; }
.code-btn { font-size: 12px; padding: 8px 12px; background: #667eea; color: #fff; border: none; border-radius: 4px; flex-shrink: 0; }
.code-btn[disabled] { background: #b0b8d6; }
.hint { display: block; font-size: 12px; color: #999; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.modal-btn { font-size: 14px; padding: 8px 16px; border-radius: 6px; }
.modal-btn.cancel { background: #f5f5f5; color: #666; border: 1px solid #e5e5e5; }
.modal-btn.primary { background: #667eea; color: #fff; border: none; }
.modal-btn[disabled] { background: #b0b8d6; color: #fff; }
</style>