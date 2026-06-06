<template>
  <n-modal
    :show="show"
    preset="card"
    :style="isMobile ? { width: '94vw', maxWidth: '480px' } : { width: '520px' }"
    title="🛒 确认购买"
    @update:show="$emit('update:show', $event)"
  >
    <n-space vertical :size="14">
      <div class="summary">
        <n-icon size="20" color="#f97316"><basket-outline /></n-icon>
        <span>本次共 <b>{{ uniqueCount }}</b> 种食材，{{ totalQuantity }} 件</span>
      </div>

      <n-card size="small" :bordered="true" style="max-height:240px;overflow-y:auto">
        <n-space vertical :size="6">
          <div v-for="it in items" :key="it.id" class="row">
            <span class="emoji">{{ it.categoryIcon || '🍽️' }}</span>
            <span class="name">{{ it.name }}</span>
            <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
          </div>
        </n-space>
      </n-card>

      <n-form-item label="通知对方 email" :show-feedback="false">
        <n-input
          v-model:value="partnerEmail"
          placeholder="可选；填了会发送邮件通知（组外）"
          clearable
        />
      </n-form-item>

      <n-form-item label="同步到组内成员" :show-feedback="false">
        <n-select
          v-model:value="groupId"
          :options="groupOptions"
          placeholder="可选；选择后把清单同步给该组所有 ACCEPTED 成员"
          clearable
          :loading="loadingGroups"
        />
        <div v-if="groupOptions.length === 0 && !loadingGroups" class="hint">
          还没有分组，请到「好友与分组」页创建
        </div>
      </n-form-item>
    </n-space>
    <template #footer>
      <n-space justify="end">
        <n-button @click="$emit('update:show', false)">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="onSubmit">
          确认购买
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { NModal, NSpace, NCard, NFormItem, NInput, NButton, NIcon, NSelect } from 'naive-ui'
import { BasketOutline } from '@vicons/ionicons5'
import type { FridgeItemVO } from '../../types/fridge'
import { listMyGroups } from '../../api/friend'
import { GROUP_TYPE_META, type FriendGroupVO, type GroupType } from '../../types/friend'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

const props = defineProps<{
  show: boolean
  items: FridgeItemVO[]
}>()

const emit = defineEmits<{
  'update:show': [v: boolean]
  confirm: [payload: { partnerEmail?: string; groupId?: number }]
}>()

const partnerEmail = ref('')
const groupId = ref<number | null>(null)
const submitting = ref(false)
const groups = ref<FriendGroupVO[]>([])
const loadingGroups = ref(false)

const uniqueCount = computed(() => props.items.length)
const totalQuantity = computed(() =>
  props.items.reduce((sum, it) => sum + (Number(it.quantity) || 1), 0),
)

const groupOptions = computed(() =>
  groups.value.map((g) => ({
    label: `${GROUP_TYPE_META[g.type as GroupType]?.emoji || '⭐'} ${g.name}（${g.memberCount ?? 0} 人）`,
    value: g.id,
  })),
)

async function fetchGroups() {
  loadingGroups.value = true
  try {
    const res = await listMyGroups()
    groups.value = res.data.data ?? []
  } catch {
    groups.value = []
  } finally {
    loadingGroups.value = false
  }
}

watch(
  () => props.show,
  (v) => {
    if (v) {
      partnerEmail.value = ''
      groupId.value = null
      submitting.value = false
      fetchGroups()
    }
  },
)

onMounted(fetchGroups)

async function onSubmit() {
  submitting.value = true
  try {
    emit('confirm', {
      partnerEmail: partnerEmail.value || undefined,
      groupId: groupId.value ?? undefined,
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1e293b;
}
.row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
  color: #1e293b;
}
.emoji {
  font-size: 18px;
}
.name {
  flex: 1;
  font-weight: 500;
}
.qty {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 6px;
  font-weight: 700;
  background: #fef3c7;
  color: #b45309;
}
.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}
</style>
