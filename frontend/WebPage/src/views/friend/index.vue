<template>
  <n-space vertical :size="16">
    <n-h2 style="margin:0">好友与分组</n-h2>

    <n-alert v-if="loadError" type="error" :show-icon="true" :title="loadError" />

    <n-card title="我的分组" :bordered="true">
      <template #header-extra>
        <n-button type="primary" size="small" @click="openCreateGroup">
          + 新建分组
        </n-button>
      </template>

      <n-spin :show="loadingGroups">
        <n-empty v-if="!loadingGroups && groups.length === 0" description="还没有分组" />
        <n-grid v-else :cols="3" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
          <n-grid-item v-for="g in groups" :key="g.id" span="3 s:3 m:1">
            <div class="group-card" :style="{ borderLeftColor: g.color }">
              <n-space align="center" justify="space-between" :wrap-item="false">
                <n-space align="center" :wrap-item="false">
                  <span class="group-emoji">{{ meta(g.type).emoji }}</span>
                  <span class="group-name">{{ g.name }}</span>
                  <n-tag size="small" :type="groupTagType(g.type)">{{ meta(g.type).label }}</n-tag>
                </n-space>
                <n-dropdown trigger="click" :options="groupMenu(g.id)" @select="(k: string) => handleGroupMenu(k, g.id)">
                  <n-button text size="small">⋯</n-button>
                </n-dropdown>
              </n-space>
              <div class="group-meta">成员：{{ g.memberCount ?? 0 }} 人</div>
            </div>
          </n-grid-item>
        </n-grid>
      </n-spin>
    </n-card>

    <n-card title="待我处理的邀请" :bordered="true">
      <n-spin :show="loadingIncoming">
        <n-empty v-if="!loadingIncoming && incoming.length === 0" description="暂无待处理邀请" />
        <n-list v-else bordered>
          <n-list-item v-for="r in incoming" :key="r.id">
            <template #prefix>
              <n-tag :type="statusMeta(r.status).type">{{ statusMeta(r.status).label }}</n-tag>
            </template>
            <n-thing>
              <template #header>
                用户 <strong>#{{ r.ownerId }}</strong> 邀请你加入分组
                <span v-if="r.groupName" :style="{ color: r.groupColor || '#666' }">
                  「{{ r.groupName }}」
                </span>
              </template>
              <template #description>
                发起时间：{{ formatTime(r.createdAt) }}
                <span v-if="r.inviteEmailSent" style="margin-left:8px">
                  <n-tag size="tiny" type="info">已发邮件</n-tag>
                </span>
              </template>
            </n-thing>
            <template #suffix>
              <n-space>
                <n-button size="small" type="primary" :loading="busyId === r.id" @click="handleAccept(r.id)">接受</n-button>
                <n-button size="small" :loading="busyId === r.id" @click="handleReject(r.id)">拒绝</n-button>
              </n-space>
            </template>
          </n-list-item>
        </n-list>
      </n-spin>
    </n-card>

    <n-card title="我发起的 / 已建立的关系" :bordered="true">
      <template #header-extra>
        <n-button type="primary" size="small" :disabled="groups.length === 0" @click="openInvite">
          + 邀请好友
        </n-button>
      </template>
      <n-spin :show="loadingMine">
        <n-empty v-if="!loadingMine && mine.length === 0" description="还没有任何关系" />
        <n-list v-else bordered>
          <n-list-item v-for="r in mine" :key="r.id">
            <template #prefix>
              <n-tag :type="statusMeta(r.status).type">{{ statusMeta(r.status).label }}</n-tag>
            </template>
            <n-thing>
              <template #header>
                好友 <strong>#{{ r.friendUserId }}</strong>
                <span v-if="r.groupName" :style="{ color: r.groupColor || '#666' }">
                  ·「{{ r.groupName }}」
                </span>
              </template>
              <template #description>
                发起：{{ formatTime(r.createdAt) }}
                <span v-if="r.respondedAt"> · 回应：{{ formatTime(r.respondedAt) }}</span>
              </template>
            </n-thing>
            <template #suffix>
              <n-button
                size="small"
                type="error"
                tertiary
                :loading="busyId === r.id"
                @click="handleUnbind(r)"
              >
                {{ r.status === 'ACCEPTED' ? '解除' : '撤销' }}
              </n-button>
            </template>
          </n-list-item>
        </n-list>
      </n-spin>
    </n-card>

    <n-modal v-model:show="createModal.show" preset="card" title="新建分组" :style="isMobile ? { width: '94vw', maxWidth: '420px' } : { width: '480px' }">
      <n-form ref="createFormRef" :model="createModal.form" :rules="groupRules" label-placement="top">
        <n-form-item label="分组名称" path="name">
          <n-input v-model:value="createModal.form.name" placeholder="如：周末饭搭子" maxlength="20" show-count />
        </n-form-item>
        <n-form-item label="分组类型" path="type">
          <n-select
            v-model:value="createModal.form.type"
            :options="groupTypeOptions"
            @update:value="onTypeChange"
          />
        </n-form-item>
        <n-form-item label="色标" path="color">
          <n-color-picker v-model:value="createModal.form.color" :modes="['hex']" :show-alpha="false" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="createModal.show = false">取消</n-button>
          <n-button type="primary" :loading="createModal.busy" @click="submitCreate">创建</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="inviteModal.show" preset="card" title="邀请好友加入分组" :style="isMobile ? { width: '94vw', maxWidth: '420px' } : { width: '480px' }">
      <n-form ref="inviteFormRef" :model="inviteModal.form" :rules="inviteRules" label-placement="top">
        <n-form-item label="目标用户 ID" path="friendUserId">
          <n-input-number v-model:value="inviteModal.form.friendUserId" placeholder="对方用户 ID" :min="1" style="width:100%" />
        </n-form-item>
        <n-form-item label="目标分组" path="groupId">
          <n-select
            v-model:value="inviteModal.form.groupId"
            :options="groupOptions"
            placeholder="选择分组"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="inviteModal.show = false">取消</n-button>
          <n-button type="primary" :loading="inviteModal.busy" @click="submitInvite">发送邀请</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { NButton, NSpace, NTag, useMessage, type FormInst, type FormRules } from 'naive-ui'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
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
} from '../../api/friend'
import {
  GROUP_TYPE_META,
  RELATIONSHIP_STATUS_META,
  type FriendGroupVO,
  type FriendRelationshipVO,
  type GroupType,
  type FriendGroupRequest,
} from '../../types/friend'

const message = useMessage()

const groups = ref<FriendGroupVO[]>([])
const mine = ref<FriendRelationshipVO[]>([])
const incoming = ref<FriendRelationshipVO[]>([])

const loadingGroups = ref(false)
const loadingMine = ref(false)
const loadingIncoming = ref(false)
const loadError = ref<string | null>(null)
const busyId = ref<number | null>(null)

const meta = (t: GroupType) => GROUP_TYPE_META[t] || GROUP_TYPE_META.CUSTOM
const statusMeta = (s: FriendRelationshipVO['status']) => RELATIONSHIP_STATUS_META[s]

const groupTypeOptions = (Object.keys(GROUP_TYPE_META) as GroupType[]).map((k) => ({
  label: `${GROUP_TYPE_META[k].emoji} ${GROUP_TYPE_META[k].label}`,
  value: k,
}))

const groupOptions = computed(() =>
  groups.value.map((g) => ({
    label: `${meta(g.type).emoji} ${g.name}（${meta(g.type).label}）`,
    value: g.id,
  })),
)

const groupTagType = (t: GroupType): 'default' | 'info' | 'success' | 'warning' => {
  switch (t) {
    case 'COUPLE': return 'warning'
    case 'FAMILY': return 'info'
    case 'FRIEND': return 'default'
    default:       return 'success'
  }
}

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN', { hour12: false })
}

async function fetchAll() {
  loadError.value = null
  await Promise.all([fetchGroups(), fetchMine(), fetchIncoming()])
}

async function fetchGroups() {
  loadingGroups.value = true
  try {
    const res = await listMyGroups()
    groups.value = res.data.data ?? []
  } catch (e: any) {
    loadError.value = e?.response?.data?.message || e?.message || '加载分组失败'
  } finally {
    loadingGroups.value = false
  }
}

async function fetchMine() {
  loadingMine.value = true
  try {
    const res = await listMyRelationships()
    mine.value = res.data.data ?? []
  } catch (e: any) {
    loadError.value = e?.response?.data?.message || e?.message || '加载关系失败'
  } finally {
    loadingMine.value = false
  }
}

async function fetchIncoming() {
  loadingIncoming.value = true
  try {
    const res = await listIncomingRelationships()
    incoming.value = res.data.data ?? []
  } catch {
    incoming.value = []
  } finally {
    loadingIncoming.value = false
  }
}

function openCreateGroup() {
  createModal.form.name = ''
  createModal.form.type = 'FRIEND'
  createModal.form.color = GROUP_TYPE_META.FRIEND.defaultColor
  createModal.show = true
}

function onTypeChange(t: GroupType) {
  if (createModal.form.color === '#FFFFFF' || !createModal.form.color) {
    createModal.form.color = meta(t).defaultColor
  } else {
    const preset = (Object.values(GROUP_TYPE_META) as Array<{ defaultColor: string }>).some(
      (m) => m.defaultColor.toLowerCase() === (createModal.form.color || '').toLowerCase(),
    )
    if (preset) {
      createModal.form.color = meta(t).defaultColor
    }
  }
}

const createFormRef = ref<FormInst | null>(null)
const groupRules: FormRules = {
  name: { required: true, message: '请输入分组名称', trigger: 'blur' },
  type: { required: true, message: '请选择类型', trigger: 'change' },
}
const createModal = reactive({
  show: false,
  busy: false,
  form: { name: '', type: 'FRIEND' as GroupType, color: GROUP_TYPE_META.FRIEND.defaultColor },
})

async function submitCreate() {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }
  createModal.busy = true
  try {
    await createGroup(createModal.form as FriendGroupRequest)
    message.success('分组已创建')
    createModal.show = false
    await fetchGroups()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '创建失败')
  } finally {
    createModal.busy = false
  }
}

function groupMenu(_id: number) {
  return [
    { label: '删除', key: 'delete' },
  ]
}

async function handleGroupMenu(key: string, id: number) {
  if (key === 'delete') {
    try {
      await deleteGroup(id)
      message.success('已删除')
      await fetchGroups()
      await fetchMine()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除失败')
    }
  }
}

const inviteFormRef = ref<FormInst | null>(null)
const inviteRules: FormRules = {
  friendUserId: {
    type: 'number',
    required: true,
    message: '请输入对方用户 ID',
    trigger: 'change',
  },
  groupId: {
    type: 'number',
    required: true,
    message: '请选择分组',
    trigger: 'change',
  },
}
const inviteModal = reactive({
  show: false,
  busy: false,
  form: { friendUserId: null as number | null, groupId: null as number | null },
})

function openInvite() {
  inviteModal.form.friendUserId = null
  inviteModal.form.groupId = groups.value[0]?.id ?? null
  inviteModal.show = true
}

async function submitInvite() {
  try {
    await inviteFormRef.value?.validate()
  } catch {
    return
  }
  inviteModal.busy = true
  try {
    await inviteFriend({
      friendUserId: inviteModal.form.friendUserId!,
      groupId: inviteModal.form.groupId!,
    })
    message.success('邀请已发送，等待对方接受')
    inviteModal.show = false
    await fetchMine()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '发送失败')
  } finally {
    inviteModal.busy = false
  }
}

async function handleAccept(id: number) {
  busyId.value = id
  try {
    await acceptRelationship({ relationshipId: id })
    message.success('已接受（已通知发起方）')
    await fetchAll()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

async function handleReject(id: number) {
  busyId.value = id
  try {
    await rejectRelationship({ relationshipId: id })
    message.success('已拒绝')
    await fetchIncoming()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

async function handleUnbind(r: FriendRelationshipVO) {
  busyId.value = r.id
  try {
    await unbindRelationship(r.id)
    message.success(r.status === 'ACCEPTED' ? '已解除关系' : '已撤销邀请')
    await fetchMine()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

onMounted(fetchAll)
</script>

<style scoped>
.group-card {
  border: 1px solid var(--n-border-color);
  border-left: 4px solid #909399;
  border-radius: 6px;
  padding: 12px;
  background: #fff;
  transition: transform 0.15s, box-shadow 0.15s;
}
.group-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.group-emoji {
  font-size: 22px;
}
.group-name {
  font-weight: 600;
  font-size: 15px;
}
.group-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}
</style>
