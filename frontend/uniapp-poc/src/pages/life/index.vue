<script setup lang="ts">
/**
 * 生活备忘页 (uni-app 版)
 * 3 tab: 购物清单 / 纪念日 / 提醒规则
 * 用顶部 tab 切换，每个 tab 列表 + 新增弹窗
 */
import { ref, reactive } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import {
  listShoppingMemos,
  createShoppingMemo,
  toggleShoppingMemo,
  listAnniversaries,
  createAnniversary,
  listReminderRules,
  createReminderRule,
  toggleReminderRule,
} from '@/api/life'
import type { ShoppingMemoVO, AnniversaryVO, ReminderRuleVO } from '@/types/api'

const activeTab = ref<'shopping' | 'anniversary' | 'reminder'>('shopping')
const loading = ref(false)

// Shopping
const shoppingList = ref<ShoppingMemoVO[]>([])
const showShoppingCreate = ref(false)
const shoppingSubmitting = ref(false)
const shoppingForm = reactive({ itemName: '', note: '' })

// Anniversary
const anniversaryList = ref<AnniversaryVO[]>([])
const showAnniversaryCreate = ref(false)
const anniversarySubmitting = ref(false)
const anniversaryForm = reactive({ title: '', eventDate: '', remindBeforeDays: 3 })
let anniversaryDateTemp = ''

// Reminder
const reminderList = ref<ReminderRuleVO[]>([])
const showReminderCreate = ref(false)
const reminderSubmitting = ref(false)
const reminderForm = reactive({ title: '', content: '', cronExpression: '', enabled: true })

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([fetchShopping(), fetchAnniversaries(), fetchReminders()])
  } finally {
    loading.value = false
  }
}

async function fetchShopping() {
  const res = await listShoppingMemos()
  shoppingList.value = res.data.data ?? []
}

async function fetchAnniversaries() {
  const res = await listAnniversaries()
  anniversaryList.value = res.data.data ?? []
}

async function fetchReminders() {
  const res = await listReminderRules()
  reminderList.value = res.data.data ?? []
}

uniOnShow(() => loadAll())

function switchTab(tab: 'shopping' | 'anniversary' | 'reminder') {
  activeTab.value = tab
}

// Shopping actions
function openShoppingCreate() {
  shoppingForm.itemName = ''
  shoppingForm.note = ''
  showShoppingCreate.value = true
}

async function handleShoppingCreate() {
  if (!shoppingForm.itemName.trim()) {
    uni.showToast({ title: '请输入物品名', icon: 'none' })
    return
  }
  shoppingSubmitting.value = true
  try {
    await createShoppingMemo(shoppingForm)
    showShoppingCreate.value = false
    await fetchShopping()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch {
    uni.showToast({ title: '添加失败', icon: 'none' })
  } finally {
    shoppingSubmitting.value = false
  }
}

async function handleToggleShopping(item: ShoppingMemoVO) {
  try {
    await toggleShoppingMemo(item.id)
    await fetchShopping()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// Anniversary actions
function openAnniversaryCreate() {
  anniversaryForm.title = ''
  anniversaryForm.eventDate = ''
  anniversaryDateTemp = ''
  anniversaryForm.remindBeforeDays = 3
  showAnniversaryCreate.value = true
}

function onAnniversaryDateChange(e: { detail: { value: string } }) {
  anniversaryDateTemp = e.detail.value
  anniversaryForm.eventDate = e.detail.value
}

async function handleAnniversaryCreate() {
  if (!anniversaryForm.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!anniversaryForm.eventDate) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }
  anniversarySubmitting.value = true
  try {
    await createAnniversary(anniversaryForm)
    showAnniversaryCreate.value = false
    await fetchAnniversaries()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch {
    uni.showToast({ title: '添加失败', icon: 'none' })
  } finally {
    anniversarySubmitting.value = false
  }
}

// Reminder actions
function openReminderCreate() {
  reminderForm.title = ''
  reminderForm.content = ''
  reminderForm.cronExpression = ''
  reminderForm.enabled = true
  showReminderCreate.value = true
}

async function handleReminderCreate() {
  if (!reminderForm.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  reminderSubmitting.value = true
  try {
    await createReminderRule(reminderForm)
    showReminderCreate.value = false
    await fetchReminders()
    uni.showToast({ title: '已添加', icon: 'success' })
  } catch {
    uni.showToast({ title: '添加失败', icon: 'none' })
  } finally {
    reminderSubmitting.value = false
  }
}

async function handleToggleReminder(item: ReminderRuleVO) {
  try {
    await toggleReminderRule(item.id)
    await fetchReminders()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">📝 生活备忘</text>
    </view>

    <view class="tab-bar">
      <button :class="['tab-btn', activeTab === 'shopping' ? 'active' : '']" @click="switchTab('shopping')">购物清单</button>
      <button :class="['tab-btn', activeTab === 'anniversary' ? 'active' : '']" @click="switchTab('anniversary')">纪念日</button>
      <button :class="['tab-btn', activeTab === 'reminder' ? 'active' : '']" @click="switchTab('reminder')">提醒</button>
    </view>

    <view v-if="loading" class="loading">加载中…</view>

    <!-- Shopping Tab -->
    <view v-if="!loading && activeTab === 'shopping'" class="tab-content">
      <button class="add-btn" @click="openShoppingCreate">+ 新增购物项</button>
      <view v-if="shoppingList.length === 0" class="empty">暂无购物项</view>
      <view v-for="item in shoppingList" :key="item.id" class="list-item">
        <view class="item-row">
          <text class="item-name">{{ item.itemName }}</text>
          <view :class="['badge', item.purchased ? 'done' : 'pending']">
            <text class="badge-text">{{ item.purchased ? '已购' : '待购' }}</text>
          </view>
        </view>
        <text v-if="item.note" class="item-note">{{ item.note }}</text>
        <button class="btn-sm" @click="handleToggleShopping(item)">
          {{ item.purchased ? '标记待购' : '标记已购' }}
        </button>
      </view>
    </view>

    <!-- Anniversary Tab -->
    <view v-if="!loading && activeTab === 'anniversary'" class="tab-content">
      <button class="add-btn" @click="openAnniversaryCreate">+ 新增纪念日</button>
      <view v-if="anniversaryList.length === 0" class="empty">暂无纪念日</view>
      <view v-for="item in anniversaryList" :key="item.id" class="list-item">
        <view class="item-row">
          <text class="item-name">{{ item.title }}</text>
          <text class="item-date">{{ item.eventDate }}</text>
        </view>
        <text v-if="item.remindBeforeDays" class="item-meta">提前 {{ item.remindBeforeDays }} 天提醒</text>
      </view>
    </view>

    <!-- Reminder Tab -->
    <view v-if="!loading && activeTab === 'reminder'" class="tab-content">
      <button class="add-btn" @click="openReminderCreate">+ 新增提醒</button>
      <view v-if="reminderList.length === 0" class="empty">暂无提醒规则</view>
      <view v-for="item in reminderList" :key="item.id" class="list-item">
        <view class="item-row">
          <text class="item-name">{{ item.title }}</text>
          <view :class="['badge', item.enabled ? 'done' : 'disabled']">
            <text class="badge-text">{{ item.enabled ? '启用' : '禁用' }}</text>
          </view>
        </view>
        <text v-if="item.content" class="item-note">{{ item.content }}</text>
        <text v-if="item.cronExpression" class="item-meta">{{ item.cronExpression }}</text>
        <button class="btn-sm" @click="handleToggleReminder(item)">
          {{ item.enabled ? '禁用' : '启用' }}
        </button>
      </view>
    </view>

    <!-- Shopping Create Modal -->
    <view v-if="showShoppingCreate" class="modal-mask" @click="showShoppingCreate = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">新增购物项</text>
        <view class="form-item">
          <text class="label">物品名</text>
          <input v-model="shoppingForm.itemName" class="input" placeholder="请输入物品名" />
        </view>
        <view class="form-item">
          <text class="label">备注</text>
          <input v-model="shoppingForm.note" class="input" placeholder="可选备注" />
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showShoppingCreate = false">取消</button>
          <button class="modal-btn primary" :disabled="shoppingSubmitting" @click="handleShoppingCreate">
            {{ shoppingSubmitting ? '提交中…' : '确认' }}
          </button>
        </view>
      </view>
    </view>

    <!-- Anniversary Create Modal -->
    <view v-if="showAnniversaryCreate" class="modal-mask" @click="showAnniversaryCreate = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">新增纪念日</text>
        <view class="form-item">
          <text class="label">标题</text>
          <input v-model="anniversaryForm.title" class="input" placeholder="纪念日名称" />
        </view>
        <view class="form-item">
          <text class="label">日期</text>
          <picker mode="date" :value="anniversaryDateTemp" @change="onAnniversaryDateChange">
            <view class="picker-display">
              <text>{{ anniversaryDateTemp || '请选择日期' }}</text>
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">提前提醒天数</text>
          <input v-model="anniversaryForm.remindBeforeDays" class="input" type="number" placeholder="3" />
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showAnniversaryCreate = false">取消</button>
          <button class="modal-btn primary" :disabled="anniversarySubmitting" @click="handleAnniversaryCreate">
            {{ anniversarySubmitting ? '提交中…' : '确认' }}
          </button>
        </view>
      </view>
    </view>

    <!-- Reminder Create Modal -->
    <view v-if="showReminderCreate" class="modal-mask" @click="showReminderCreate = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">新增提醒规则</text>
        <view class="form-item">
          <text class="label">标题</text>
          <input v-model="reminderForm.title" class="input" placeholder="提醒标题" />
        </view>
        <view class="form-item">
          <text class="label">内容</text>
          <input v-model="reminderForm.content" class="input" placeholder="提醒内容" />
        </view>
        <view class="form-item">
          <text class="label">Cron 表达式</text>
          <input v-model="reminderForm.cronExpression" class="input" placeholder="如 0 0 8 * * ?" />
        </view>
        <view class="modal-actions">
          <button class="modal-btn cancel" @click="showReminderCreate = false">取消</button>
          <button class="modal-btn primary" :disabled="reminderSubmitting" @click="handleReminderCreate">
            {{ reminderSubmitting ? '提交中…' : '确认' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 16px; }
.header { margin-bottom: 12px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.tab-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.tab-btn { font-size: 13px; padding: 6px 14px; background: #f5f5f5; color: #333; border: 1px solid #e5e5e5; border-radius: 16px; }
.tab-btn.active { background: #1e293b; color: #fff; border-color: #1e293b; }
.loading { text-align: center; color: #888; padding: 40px 0; }
.add-btn { font-size: 13px; padding: 8px 16px; background: #667eea; color: #fff; border: none; border-radius: 6px; margin-bottom: 12px; }
.empty { text-align: center; color: #888; padding: 40px 0; }
.tab-content { display: flex; flex-direction: column; gap: 8px; }
.list-item { background: #fff; border-radius: 8px; padding: 12px; }
.item-row { display: flex; justify-content: space-between; align-items: center; }
.item-name { font-size: 15px; font-weight: 600; }
.item-date { font-size: 13px; color: #666; }
.item-note { font-size: 13px; color: #888; margin-top: 2px; display: block; }
.item-meta { font-size: 12px; color: #888; margin-top: 2px; display: block; }
.badge { padding: 2px 8px; border-radius: 10px; }
.badge.done { background: #10b981; }
.badge.pending { background: #f59e0b; }
.badge.disabled { background: #d1d5db; }
.badge-text { font-size: 11px; color: #fff; }
.btn-sm { font-size: 12px; padding: 4px 10px; background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; border-radius: 4px; margin-top: 8px; }
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