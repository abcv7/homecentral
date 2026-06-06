<template>
  <n-space vertical :size="16">
    <n-h2>生活备忘</n-h2>

    <n-tabs type="line" default-value="shopping">
      <!-- ============ Shopping Memos ============ -->
      <n-tab-pane name="shopping" tab="购物清单">
        <n-space vertical :size="12">
          <n-space justify="space-between">
            <span />
            <n-button type="primary" size="small" @click="openShoppingCreate">新增</n-button>
          </n-space>
          <n-data-table
            :columns="shoppingColumns"
            :data="shoppingList"
            :loading="shoppingLoading"
            :bordered="true"
            :pagination="{ pageSize: 20 }"
            :scroll-x="600"
          />
        </n-space>
      </n-tab-pane>

      <!-- ============ Anniversaries ============ -->
      <n-tab-pane name="anniversary" tab="纪念日">
        <n-space vertical :size="12">
          <n-space justify="space-between">
            <span />
            <n-button type="primary" size="small" @click="openAnniversaryCreate">新增</n-button>
          </n-space>
          <n-data-table
            :columns="anniversaryColumns"
            :data="anniversaryList"
            :loading="anniversaryLoading"
            :bordered="true"
            :pagination="{ pageSize: 20 }"
            :scroll-x="600"
          />
        </n-space>
      </n-tab-pane>

      <!-- ============ Reminder Rules ============ -->
      <n-tab-pane name="reminder" tab="提醒规则">
        <n-space vertical :size="12">
          <n-space justify="space-between">
            <span />
            <n-button type="primary" size="small" @click="openReminderCreate">新增</n-button>
          </n-space>
          <n-data-table
            :columns="reminderColumns"
            :data="reminderList"
            :loading="reminderLoading"
            :bordered="true"
            :pagination="{ pageSize: 20 }"
            :scroll-x="700"
          />
        </n-space>
      </n-tab-pane>
    </n-tabs>

    <!-- Shopping Create Modal -->
    <n-modal v-model:show="showShopping" title="新增购物项" preset="card" style="width:420px" :mask-closable="false">
      <n-form ref="shoppingFormRef" :model="shoppingForm" :rules="shoppingRules" label-placement="top"
        @submit.prevent="handleShoppingCreate">
        <n-form-item label="物品名称" path="itemName">
          <n-input v-model:value="shoppingForm.itemName" placeholder="如：牛奶" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="shoppingForm.note" type="textarea" :rows="3" placeholder="备注（可选）" />
        </n-form-item>
        <n-space justify="end">
          <n-button @click="showShopping = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="shoppingSubmitting">确定</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <!-- Anniversary Create Modal -->
    <n-modal v-model:show="showAnniversary" title="新增纪念日" preset="card" style="width:420px" :mask-closable="false">
      <n-form ref="anniversaryFormRef" :model="anniversaryForm" :rules="anniversaryRules" label-placement="top"
        @submit.prevent="handleAnniversaryCreate">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="anniversaryForm.title" placeholder="如：生日" />
        </n-form-item>
        <n-form-item label="日期" path="eventDate">
          <n-date-picker v-model:value="anniversaryDatePicker" type="date" placeholder="选择日期" clearable />
        </n-form-item>
        <n-form-item label="提前提醒天数">
          <n-input-number v-model:value="remindBeforeDaysVal" :min="0" :max="30" placeholder="0" />
        </n-form-item>
        <n-space justify="end">
          <n-button @click="showAnniversary = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="anniversarySubmitting">确定</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <!-- Reminder Create Modal -->
    <n-modal v-model:show="showReminder" title="新增提醒规则" preset="card" style="width:480px" :mask-closable="false">
      <n-form ref="reminderFormRef" :model="reminderForm" :rules="reminderRules" label-placement="top"
        @submit.prevent="handleReminderCreate">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="reminderForm.title" placeholder="如：每日喝水提醒" />
        </n-form-item>
        <n-form-item label="内容">
          <n-input v-model:value="reminderForm.content" type="textarea" :rows="2" placeholder="提醒内容（可选）" />
        </n-form-item>
        <n-form-item label="Cron 表达式" path="cronExpression">
          <n-input v-model:value="reminderForm.cronExpression" placeholder="如：0 0 9 * * ?（每天早上9点）" />
        </n-form-item>
        <n-form-item label="是否启用">
          <n-switch v-model:value="reminderForm.enabled" />
        </n-form-item>
        <n-space justify="end">
          <n-button @click="showReminder = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="reminderSubmitting">确定</n-button>
        </n-space>
      </n-form>
    </n-modal>
  </n-space>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSwitch } from 'naive-ui'
import type { DataTableColumn, FormInst, FormRules } from 'naive-ui'
import type {
  ShoppingMemoVO, AnniversaryVO, ReminderRuleVO,
  ShoppingMemoCreateRequest, AnniversaryCreateRequest, ReminderRuleCreateRequest,
} from '../../types/api'
import {
  listShoppingMemos, createShoppingMemo, toggleShoppingMemo,
  listAnniversaries, createAnniversary,
  listReminderRules, createReminderRule, toggleReminderRule,
} from '../../api/life'

const message = useMessage()

// ============ Shopping ============
const shoppingList = ref<ShoppingMemoVO[]>([])
const shoppingLoading = ref(false)
const showShopping = ref(false)
const shoppingSubmitting = ref(false)
const shoppingFormRef = ref<FormInst | null>(null)

const shoppingForm = reactive<ShoppingMemoCreateRequest>({ itemName: '', note: '' })
const shoppingRules: FormRules = { itemName: { required: true, message: '请输入物品名称', trigger: 'blur' } }

function resetShoppingForm() { shoppingForm.itemName = ''; shoppingForm.note = '' }

async function fetchShopping() {
  shoppingLoading.value = true
  try { shoppingList.value = (await listShoppingMemos()).data.data }
  catch { message.error('加载购物清单失败') }
  finally { shoppingLoading.value = false }
}

function openShoppingCreate() { resetShoppingForm(); showShopping.value = true }

async function handleShoppingCreate() {
  try { await shoppingFormRef.value?.validate() } catch { return }
  shoppingSubmitting.value = true
  try {
    const payload: ShoppingMemoCreateRequest = { itemName: shoppingForm.itemName }
    if (shoppingForm.note) payload.note = shoppingForm.note
    await createShoppingMemo(payload)
    message.success('添加成功'); showShopping.value = false; fetchShopping()
  } catch { message.error('添加失败') }
  finally { shoppingSubmitting.value = false }
}

async function handleToggleShopping(row: ShoppingMemoVO) {
  try { await toggleShoppingMemo(row.id); fetchShopping() }
  catch { message.error('操作失败') }
}

const shoppingColumns: DataTableColumn<ShoppingMemoVO>[] = [
  { title: '物品', key: 'itemName' },
  { title: '备注', key: 'note', ellipsis: { tooltip: true } },
  {
    title: '已购买', key: 'purchased', width: 90,
    render(row) {
      return h(NTag, { type: row.purchased ? 'success' : 'default', size: 'small' },
        { default: () => row.purchased ? '是' : '否' })
    },
  },
  {
    title: '操作', key: 'actions', width: 100,
    render(row) {
      return h(NButton, {
        size: 'tiny', type: row.purchased ? 'default' : 'primary', secondary: true,
        onClick: () => handleToggleShopping(row),
      }, { default: () => row.purchased ? '取消购买' : '标记已购' })
    },
  },
]

// ============ Anniversary ============
const anniversaryList = ref<AnniversaryVO[]>([])
const anniversaryLoading = ref(false)
const showAnniversary = ref(false)
const anniversarySubmitting = ref(false)
const anniversaryFormRef = ref<FormInst | null>(null)

const remindBeforeDaysVal = ref<number | null>(null)
const anniversaryForm = reactive<AnniversaryCreateRequest>({
  title: '', eventDate: '',
})
const anniversaryDatePicker = ref<number | null>(null)
const anniversaryRules: FormRules = { title: { required: true, message: '请输入标题', trigger: 'blur' } }

function resetAnniversaryForm() { anniversaryForm.title = ''; anniversaryForm.eventDate = ''; remindBeforeDaysVal.value = null; anniversaryDatePicker.value = null }

async function fetchAnniversaries() {
  anniversaryLoading.value = true
  try { anniversaryList.value = (await listAnniversaries()).data.data }
  catch { message.error('加载纪念日失败') }
  finally { anniversaryLoading.value = false }
}

function openAnniversaryCreate() { resetAnniversaryForm(); showAnniversary.value = true }

async function handleAnniversaryCreate() {
  try { await anniversaryFormRef.value?.validate() } catch { return }
  if (!anniversaryDatePicker.value) { message.warning('请选择日期'); return }
  anniversarySubmitting.value = true
  try {
    const d = new Date(anniversaryDatePicker.value)
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    const payload: AnniversaryCreateRequest = { title: anniversaryForm.title, eventDate: dateStr }
    if (remindBeforeDaysVal.value != null) payload.remindBeforeDays = remindBeforeDaysVal.value
    await createAnniversary(payload)
    message.success('创建成功'); showAnniversary.value = false; fetchAnniversaries()
  } catch { message.error('创建失败') }
  finally { anniversarySubmitting.value = false }
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const anniversaryColumns: DataTableColumn<AnniversaryVO>[] = [
  { title: '标题', key: 'title' },
  { title: '日期', key: 'eventDate', render: row => formatDate(row.eventDate) },
  { title: '提前提醒(天)', key: 'remindBeforeDays', width: 120 },
]

// ============ Reminder Rule ============
const reminderList = ref<ReminderRuleVO[]>([])
const reminderLoading = ref(false)
const showReminder = ref(false)
const reminderSubmitting = ref(false)
const reminderFormRef = ref<FormInst | null>(null)

const reminderForm = reactive<ReminderRuleCreateRequest>({ title: '', content: '', cronExpression: '', enabled: true })
const reminderRules: FormRules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  cronExpression: { required: true, message: '请输入 Cron 表达式', trigger: 'blur' },
}

function resetReminderForm() { reminderForm.title = ''; reminderForm.content = ''; reminderForm.cronExpression = ''; reminderForm.enabled = true }

async function fetchReminders() {
  reminderLoading.value = true
  try { reminderList.value = (await listReminderRules()).data.data }
  catch { message.error('加载提醒规则失败') }
  finally { reminderLoading.value = false }
}

function openReminderCreate() { resetReminderForm(); showReminder.value = true }

async function handleReminderCreate() {
  try { await reminderFormRef.value?.validate() } catch { return }
  reminderSubmitting.value = true
  try {
    const payload: ReminderRuleCreateRequest = { title: reminderForm.title, cronExpression: reminderForm.cronExpression, enabled: reminderForm.enabled }
    if (reminderForm.content) payload.content = reminderForm.content
    await createReminderRule(payload)
    message.success('创建成功'); showReminder.value = false; fetchReminders()
  } catch { message.error('创建失败') }
  finally { reminderSubmitting.value = false }
}

async function handleToggleReminder(row: ReminderRuleVO) {
  try { await toggleReminderRule(row.id); fetchReminders() }
  catch { message.error('操作失败') }
}

const reminderColumns: DataTableColumn<ReminderRuleVO>[] = [
  { title: '标题', key: 'title' },
  { title: '内容', key: 'content', ellipsis: { tooltip: true } },
  { title: 'Cron 表达式', key: 'cronExpression', width: 150 },
  {
    title: '启用', key: 'enabled', width: 80,
    render(row) {
      return h(NSwitch, { value: row.enabled, disabled: true })
    },
  },
  {
    title: '操作', key: 'actions', width: 100,
    render(row) {
      return h(NButton, {
        size: 'tiny', type: row.enabled ? 'warning' : 'primary', secondary: true,
        onClick: () => handleToggleReminder(row),
      }, { default: () => row.enabled ? '停用' : '启用' })
    },
  },
]

onMounted(() => { fetchShopping(); fetchAnniversaries(); fetchReminders() })
</script>
