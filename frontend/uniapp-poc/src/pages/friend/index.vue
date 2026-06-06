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
        <view v-for="g in groups" :key="g.id" class="group-card" :style="{ borderLeftColor: g.color || '#ccc' }">
          <text class="group-emoji">{{ GROUP_TYPE_META[g.type]?.emoji ?? '👥' }}</text>
          <text class="group-name">{{ g.name }}</text>
          <view class="group-meta-row">
            <text class="group-type">{{ GROUP_TYPE_META[g.type]?.label ?? g.type }}</text>
            <text class="group-members">{{ g.memberCount ?? 0 }} 人</text>
          </view>
          <button v-if="busyId !== g.id" class="btn-sm danger" @click="handleDeleteGroup(g.id)">删除</button>
        </view>
      </view>
    </view>

    <!-- Incoming Invites -->
    <view v-if="!loading" class="section">
      <text class="section-title">待我处理的邀请</text>
      <view v-if="incoming.length === 0" class="empty">暂无邀请</view>
      <view v-for="rel in incoming" :key="rel.id" class="rel-item">
        <view class="rel-row">
          <text class="rel-name">用户 {{ rel.ownerId }}</text>
          <view class="badge pending">
            <text class="badge-text">待处理</text>
          </view>
        </view>
        <view class="rel-actions">
          <button class="btn-sm" @click="handleAccept(rel)">接受</button>
          <button class="btn-sm danger" @click="handleReject(rel)">拒绝</button>
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
          <view :class="['badge', rel.status === 'ACCEPTED' ? 'done' : rel.status === 'PENDING' ? 'pending' : 'disabled']">
            <text class="badge-text">{{ rel.status }}</text>
          </view>
        </view>
        <view v-if="rel.status === 'ACCEPTED'" class="rel-actions">
          <button class="btn-sm danger" @click="handleUnbind(rel.id)">解除</button>
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
.page { padding: 16px; padding-bottom: 80px; }
.header { margin-bottom: 12px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.error { color: #f56c6c; font-size: 13px; margin-bottom: 8px; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.section { margin-bottom: 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 600; }
.section-btn { font-size: 12px; padding: 4px 12px; background: #667eea; color: #fff; border: none; border-radius: 6px; }
.empty { text-align: center; color: #888; padding: 24px 0; font-size: 13px; }
.group-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.group-card { width: calc(50% - 4px); background: #fff; border-radius: 8px; padding: 12px; border-left: 4px solid #ccc; }
.group-emoji { font-size: 22px; display: block; }
.group-name { font-size: 15px; font-weight: 600; margin-top: 4px; display: block; }
.group-meta-row { display: flex; justify-content: space-between; margin-top: 6px; }
.group-type { font-size: 12px; color: #888; }
.group-members { font-size: 12px; color: #888; }
.rel-item { background: #fff; border-radius: 8px; padding: 12px; margin-bottom: 8px; }
.rel-row { display: flex; justify-content: space-between; align-items: center; }
.rel-name { font-size: 14px; font-weight: 600; }
.rel-actions { display: flex; gap: 8px; margin-top: 8px; }
.badge { padding: 2px 8px; border-radius: 10px; }
.badge.done { background: #10b981; }
.badge.pending { background: #f59e0b; }
.badge.disabled { background: #d1d5db; }
.badge-text { font-size: 11px; color: #fff; }
.btn-sm { font-size: 12px; padding: 4px 10px; background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; border-radius: 4px; }
.btn-sm.danger { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 85%; max-width: 380px; background: #fff; border-radius: 12px; padding: 24px; }
.modal-title { display: block; font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.form-item { margin-bottom: 12px; }
.label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.input { width: 100%; padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; background: #fafafa; box-sizing: border-box; }
.picker-display { padding: 10px 12px; border: 1px solid #ddd; border-radius: 4px; background: #fafafa; font-size: 14px; color: #666; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.modal-btn { font-size: 14px; padding: 8px 16px; border-radius: 6px; }
.modal-btn.cancel { background: #f5f5f5; color: #666; border: 1px solid #e5e5e5; }
.modal-btn.primary { background: #667eea; color: #fff; border: none; }
.modal-btn[disabled] { background: #b0b8d6; color: #fff; }
</style>