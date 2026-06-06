<script setup lang="ts">
/**
 * 好友页 (uni-app 版)
 * 我的分组 + 待处理邀请 + 已建立关系
 * 新增分组弹窗 + 邀请好友弹窗
 */
import { ref, reactive, computed } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import {
  listMyGroups,
  createGroup,
  deleteGroup,
  inviteFriend,
  acceptRelationship,
  rejectRelationship,
  unbindRelationship,
  listMyRelationships,
  listIncomingRelationships,
} from '@/api/friend'
import { GROUP_TYPE_META } from '@/types/friend'
import type { FriendGroupVO, FriendRelationshipVO, GroupType } from '@/types/friend'

const groups = ref<FriendGroupVO[]>([])
const mine = ref<FriendRelationshipVO[]>([])
const incoming = ref<FriendRelationshipVO[]>([])
const loading = ref(false)
const loadError = ref<string | null>(null)
const busyId = ref<number | null>(null)

const groupTypeOptions = computed(() =>
  (['FRIEND', 'COUPLE', 'FAMILY', 'CUSTOM'] as GroupType[]).map((t) => ({
    value: t,
    label: GROUP_TYPE_META[t].emoji + ' ' + GROUP_TYPE_META[t].label,
  }))
)

const groupOptions = computed(() =>
  groups.value.map((g) => ({ value: g.id, label: g.name }))
)

// Create group modal
const showCreateGroup = ref(false)
const createGroupSubmitting = ref(false)
const createGroupForm = reactive({ name: '', type: 'FRIEND' as GroupType, color: '#667eea' })

// Invite modal
const showInvite = ref(false)
const inviteSubmitting = ref(false)
const inviteForm = reactive({ friendUserId: null as number | null, groupId: null as number | null })

async function fetchAll() {
  loading.value = true
  loadError.value = null
  try {
    await Promise.all([fetchGroups(), fetchMine(), fetchIncoming()])
  } catch (e) {
    loadError.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function fetchGroups() {
  const res = await listMyGroups()
  groups.value = res.data.data ?? []
}

async function fetchMine() {
  const res = await listMyRelationships()
  mine.value = res.data.data ?? []
}

async function fetchIncoming() {
  const res = await listIncomingRelationships()
  incoming.value = res.data.data ?? []
}

uniOnShow(() => fetchAll())

function openCreateGroup() {
  createGroupForm.name = ''
  createGroupForm.type = 'FRIEND'
  createGroupForm.color = '#667eea'
  showCreateGroup.value = true
}

function onTypeChange(e: { detail: { value: number } }) {
  const idx = e.detail.value
  const types: GroupType[] = ['FRIEND', 'COUPLE', 'FAMILY', 'CUSTOM']
  createGroupForm.type = types[idx] ?? 'FRIEND'
}

async function submitCreateGroup() {
  if (!createGroupForm.name.trim()) {
    uni.showToast({ title: '请输入分组名', icon: 'none' })
    return
  }
  createGroupSubmitting.value = true
  try {
    await createGroup(createGroupForm)
    showCreateGroup.value = false
    await fetchGroups()
    uni.showToast({ title: '分组已创建', icon: 'success' })
  } catch {
    uni.showToast({ title: '创建失败', icon: 'none' })
  } finally {
    createGroupSubmitting.value = false
  }
}

async function handleDeleteGroup(id: number) {
  busyId.value = id
  try {
    await deleteGroup(id)
    await fetchGroups()
    uni.showToast({ title: '已删除', icon: 'success' })
  } catch {
    uni.showToast({ title: '删除失败', icon: 'none' })
  } finally {
    busyId.value = null
  }
}

function openInvite() {
  inviteForm.friendUserId = null
  inviteForm.groupId = null
  showInvite.value = true
}

function onGroupChange(e: { detail: { value: number } }) {
  const idx = e.detail.value
  inviteForm.groupId = groupOptions.value[idx as number]?.value ?? null
}

async function submitInvite() {
  if (!inviteForm.friendUserId) {
    uni.showToast({ title: '请输入好友 ID', icon: 'none' })
    return
  }
  if (!inviteForm.groupId) {
    uni.showToast({ title: '请选择分组', icon: 'none' })
    return
  }
  inviteSubmitting.value = true
  try {
    await inviteFriend({ friendUserId: inviteForm.friendUserId, groupId: inviteForm.groupId })
    showInvite.value = false
    await fetchAll()
    uni.showToast({ title: '邀请已发送', icon: 'success' })
  } catch {
    uni.showToast({ title: '邀请失败', icon: 'none' })
  } finally {
    inviteSubmitting.value = false
  }
}

async function handleAccept(rel: FriendRelationshipVO) {
  busyId.value = rel.id
  try {
    await acceptRelationship({ friendUserId: rel.friendUserId ?? rel.ownerId ?? 0, groupId: rel.groupId ?? 0 })
    await fetchAll()
    uni.showToast({ title: '已接受', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    busyId.value = null
  }
}

async function handleReject(rel: FriendRelationshipVO) {
  busyId.value = rel.id
  try {
    await rejectRelationship({ friendUserId: rel.friendUserId ?? rel.ownerId ?? 0, groupId: rel.groupId ?? 0 })
    await fetchIncoming()
    uni.showToast({ title: '已拒绝', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    busyId.value = null
  }
}

async function handleUnbind(id: number) {
  busyId.value = id
  try {
    await unbindRelationship(id)
    await fetchMine()
    uni.showToast({ title: '已解除', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    busyId.value = null
  }
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">👥 好友与分组</text>
    </view>

    <view v-if="loadError" class="error">{{ loadError }}</view>
    <view v-if="loading" class="loading">加载中…</view>

    <!-- My Groups -->
    <view v-if="!loading" class="section">
      <view class="section-header">
        <text class="section-title">我的分组</text>
        <button class="section-btn" @click="openCreateGroup">+ 新建分组</button>
      </view>
      <view v-if="groups.length === 0" class="empty">暂无分组</view>
      <view v-else class="group-grid">
        <view v-for="g in groups" :key="g.id" class="group-card" :style="{ borderLeftColor: g.color || '#f97316' }">
          <text class="group-emoji">{{ GROUP_TYPE_META[g.type]?.emoji ?? '👥' }}</text>
          <text class="group-name">{{ g.name }}</text>
          <view class="group-meta-row">
            <text class="group-type">{{ GROUP_TYPE_META[g.type]?.label ?? g.type }}</text>
            <text class="group-members">{{ g.memberCount ?? 0 }} 人</text>
          </view>
          <button v-if="busyId !== g.id" class="btn-sm btn-danger" @click="handleDeleteGroup(g.id)">删除</button>
        </view>
      </view>
    </view>

    <!-- Incoming Invites -->
    <view v-if="!loading" class="section">
      <text class="section-title standalone">待我处理的邀请</text>
      <view v-if="incoming.length === 0" class="empty">暂无邀请</view>
      <view v-for="rel in incoming" :key="rel.id" class="rel-item">
        <view class="rel-row">
          <text class="rel-name">用户 {{ rel.ownerId }}</text>
          <view class="badge badge-pending">
            <text class="badge-text">待处理</text>
          </view>
        </view>
        <view class="rel-actions">
          <button class="btn-sm btn-warm" @click="handleAccept(rel)">接受</button>
          <button class="btn-sm btn-danger" @click="handleReject(rel)">拒绝</button>
        </view>
      </view>
    </view>

    <!-- My Relationships -->
    <view v-if="!loading" class="section">
      <view class="section-header">
        <text class="section-title">我的关系</text>
        <button class="section-btn" @click="openInvite">+ 邀请好友</button>
      </view>
      <view v-if="mine.length === 0" class="empty">暂无关系</view>
      <view v-for="rel in mine" :key="rel.id" class="rel-item">
        <view class="rel-row">
          <text class="rel-name">{{ rel.status === 'ACCEPTED' ? '好友 ' + (rel.friendUserId ?? '') : '用户 ' + (rel.friendUserId ?? '') }}</text>
          <view :class="['badge', rel.status === 'ACCEPTED' ? 'badge-done' : rel.status === 'PENDING' ? 'badge-pending' : 'badge-disabled']">
            <text class="badge-text">{{ rel.status }}</text>
          </view>
        </view>
        <view v-if="rel.status === 'ACCEPTED'" class="rel-actions">
          <button class="btn-sm btn-danger" @click="handleUnbind(rel.id)">解除</button>
        </view>
      </view>
    </view>

    <!-- Create Group Modal -->
    <view v-if="showCreateGroup" class="modal-mask" @click="showCreateGroup = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">新建分组</text>
        <view class="form-item">
          <text class="label">分组名</text>
          <input v-model="createGroupForm.name" class="input" placeholder="分组名称" />
        </view>
        <view class="form-item">
          <text class="label">类型</text>
          <picker :range="groupTypeOptions" range-key="label" @change="onTypeChange">
            <view class="picker-display">
              <text>{{ GROUP_TYPE_META[createGroupForm.type]?.emoji }} {{ GROUP_TYPE_META[createGroupForm.type]?.label }}</text>
            </view>
          </picker>
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showCreateGroup = false">取消</button>
          <button class="modal-btn primary" :disabled="createGroupSubmitting" @click="submitCreateGroup">
            {{ createGroupSubmitting ? '创建中…' : '创建' }}
          </button>
        </view>
      </view>
    </view>

    <!-- Invite Modal -->
    <view v-if="showInvite" class="modal-mask" @click="showInvite = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">邀请好友</text>
        <view class="form-item">
          <text class="label">好友用户 ID</text>
          <input v-model="inviteForm.friendUserId" class="input" type="number" placeholder="输入对方的用户 ID" />
        </view>
        <view class="form-item">
          <text class="label">分组</text>
          <picker :range="groupOptions" range-key="label" @change="onGroupChange">
            <view class="picker-display">
              <text>{{ inviteForm.groupId ? groupOptions.find(g => g.value === inviteForm.groupId)?.label ?? '请选择' : '请选择分组' }}</text>
            </view>
          </picker>
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showInvite = false">取消</button>
          <button class="modal-btn primary" :disabled="inviteSubmitting" @click="submitInvite">
            {{ inviteSubmitting ? '发送中…' : '邀请' }}
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
.error {
  color: var(--qwu-danger, #ef4444);
  font-size: 13px;
  margin-bottom: 10px;
  background: var(--qwu-danger-light, #fef2f2);
  padding: 8px 12px;
  border-radius: var(--qwu-radius-xs, 6px);
}
.loading {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 40px 0;
  font-size: 14px;
}
.section {
  margin-bottom: 28px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--qwu-text, #1c1917);
  padding-left: 10px;
  border-left: 3px solid var(--qwu-primary, #f97316);
}
.section-title.standalone {
  margin-bottom: 14px;
}
.section-btn {
  font-size: 12px;
  padding: 5px 14px;
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-xs, 6px);
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.empty {
  text-align: center;
  color: var(--qwu-text-muted, #a8a29e);
  padding: 24px 0;
  font-size: 13px;
}
.group-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.group-card {
  width: calc(50% - 5px);
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 14px;
  border-left: 4px solid #f97316;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
}
.group-emoji {
  font-size: 24px;
  display: block;
}
.group-name {
  font-size: 15px;
  font-weight: 600;
  margin-top: 6px;
  display: block;
  color: var(--qwu-text, #1c1917);
}
.group-meta-row {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}
.group-type {
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
}
.group-members {
  font-size: 12px;
  color: var(--qwu-text-muted, #a8a29e);
}
.rel-item {
  background: var(--qwu-card, #ffffff);
  border-radius: var(--qwu-radius, 14px);
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
}
.rel-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rel-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--qwu-text, #1c1917);
}
.rel-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.badge {
  padding: 3px 10px;
  border-radius: 10px;
}
.badge-done {
  background: var(--qwu-success, #10b981);
}
.badge-pending {
  background: var(--qwu-warning, #f59e0b);
}
.badge-disabled {
  background: var(--qwu-text-muted, #a8a29e);
}
.badge-text {
  font-size: 11px;
  color: #fff;
  font-weight: 500;
}
.btn-sm {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: var(--qwu-radius-xs, 6px);
  font-weight: 500;
  border: none;
}
.btn-warm {
  background: var(--qwu-primary-light, #fff7ed);
  color: var(--qwu-primary, #f97316);
  border: 1px solid rgba(249, 115, 22, 0.2);
}
.btn-danger {
  background: var(--qwu-danger-light, #fef2f2);
  color: var(--qwu-danger, #ef4444);
  border: 1px solid rgba(239, 68, 68, 0.2);
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
.form-item {
  margin-bottom: 14px;
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
.picker-display {
  padding: 10px 12px;
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: var(--qwu-radius-xs, 6px);
  background: var(--qwu-bg, #faf8f5);
  font-size: 14px;
  color: var(--qwu-text-secondary, #78716c);
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
