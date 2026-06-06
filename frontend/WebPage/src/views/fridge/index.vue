<template>
  <div class="fridge-page" :class="{ 'is-mobile': isMobile }">
    <!-- ============ 桌面端：顶部工具栏 ============ -->
    <n-card v-if="!isMobile" :bordered="true" size="small" class="desktop-toolbar">
      <n-space align="center" wrap style="width:100%">
        <n-radio-group v-model:value="viewMode">
          <n-radio-button value="visual">🧊 冰箱视图</n-radio-button>
          <n-radio-button value="card">📦 卡片视图</n-radio-button>
          <n-radio-button value="table">📋 列表</n-radio-button>
        </n-radio-group>
        <n-divider vertical />
        <n-button type="primary" strong @click="showQuickAdd = true">
          <template #icon><n-icon><add-outline /></n-icon></template>
          新增食材
        </n-button>
        <n-button type="info" strong @click="openRecognize">
          <template #icon><n-icon><camera-outline /></n-icon></template>
          拍照识别
        </n-button>
        <n-button @click="refreshAll">
          <template #icon><n-icon><refresh-outline /></n-icon></template>
          刷新
        </n-button>
        <n-divider vertical />
        <n-button @click="showSettings = true">
          <template #icon><n-icon><settings-outline /></n-icon></template>
          冰箱配置
        </n-button>
        <span style="flex:1" />
        <n-tag v-if="currentTemplate" :type="currentTemplate.default ? 'success' : 'default'" round>
          当前模板：{{ currentTemplate.name }}
        </n-tag>
      </n-space>
    </n-card>

    <!-- ============ 移动端：顶部精简条 ============ -->
    <div v-else class="mobile-toolbar">
      <n-space align="center" justify="space-between" :wrap="false" style="width:100%">
        <n-space align="center" :wrap="false" :size="8">
          <n-radio-group v-model:value="viewMode" size="small">
            <n-radio-button value="card">📦 卡片</n-radio-button>
            <n-radio-button value="visual">🧊 3D</n-radio-button>
            <n-radio-button value="table">📋 表</n-radio-button>
          </n-radio-group>
        </n-space>
        <n-dropdown trigger="click" :options="moreMenuOptions" @select="onMoreMenu">
          <n-button quaternary circle size="medium" aria-label="更多">
            <n-icon size="20"><ellipsis-horizontal /></n-icon>
          </n-button>
        </n-dropdown>
      </n-space>
      <div v-if="currentTemplate" class="mobile-template-line">
        模板：<b>{{ currentTemplate.name }}</b>
        <n-tag v-if="currentTemplate.default" size="tiny" type="success" style="margin-left:4px">默认</n-tag>
      </div>
    </div>

    <!-- ============ 主视图区 ============ -->
    <!-- iPad 横屏：紧凑 3-col 200/1fr/260 -->
    <FridgeIPadLandscapeView
      v-if="isIPadLandscape"
      :view-mode="viewMode"
      :basket-items="grouped.basket"
      :active-items="activeItems"
      :history="history"
      :categories="categories"
      :current-template="currentTemplate"
      :table-refresh-key="tableRefreshKey"
      @update:view-mode="(v) => (viewMode = v)"
      @add-item="showQuickAdd = true"
      @recognize="openRecognize"
      @refresh="refreshAll"
      @open-settings="showSettings = true"
      @drop-basket="onDropBasket"
      @drop-visual="onDropVisual"
      @item-click="onItemClick"
      @open-history-batch="onOpenHistoryBatch"
      @reuse-history-one="onReuseHistoryOne"
      @confirm-purchase="onConfirmPurchase"
    />

    <!-- 桌面：3 列（采购篮 / 视觉 / 历史） -->
    <div v-else-if="!isMobile && viewMode === 'visual'" class="visual-layout">
      <FridgeBasket
        :items="grouped.basket"
        @drop-basket="onDropBasket"
        @item-click="onItemClick"
      />
      <FridgeVisual
        :template="currentTemplate"
        :items="activeItems"
        :is-mobile="false"
        @drop="onDropVisual"
        @item-click="onItemClick"
      />
      <FridgeShoppingHistory
        :items="history"
        :categories="categories"
        @open-batch="onOpenHistoryBatch"
        @reuse-one="onReuseHistoryOne"
      />
    </div>

    <!-- 桌面：卡片视图（与移动端共用） -->
    <FridgeCardView
      v-else-if="viewMode === 'card'"
      :items="activeItems"
      :categories="categories"
      :basket-items="grouped.basket"
      :history="history"
      :is-mobile="isMobile"
      :template="currentTemplate"
      @item-click="onItemClick"
      @open-history-batch="onOpenHistoryBatch"
      @reuse-history-one="onReuseHistoryOne"
      @open-basket="onOpenBasketDrawer"
      @open-history="onOpenHistoryDrawer"
    />

    <!-- 桌面：列表 -->
    <FridgeTable
      v-else
      ref="tableRef"
      :categories="categories"
      :refresh-key="tableRefreshKey"
    />

    <!-- ============ 移动端：底部 ActionBar ============ -->
    <div v-if="isMobile" class="mobile-actionbar">
      <button class="action-btn" :class="{ active: viewMode === 'card' || viewMode === 'visual' }" @click="cycleViewMobile">
        <n-icon size="22"><snow-outline /></n-icon>
        <span class="lbl">{{ viewModeLabel }}</span>
      </button>
      <button class="action-btn primary" @click="showQuickAdd = true">
        <n-icon size="22"><add-circle /></n-icon>
        <span class="lbl">新增</span>
      </button>
      <button class="action-btn" @click="openRecognize">
        <n-icon size="22"><camera-outline /></n-icon>
        <span class="lbl">拍照</span>
      </button>
      <button class="action-btn" :class="{ active: showBasketDrawer }" @click="showBasketDrawer = true">
        <n-icon size="22"><basket-outline /></n-icon>
        <span class="lbl">篮({{ grouped.basket.length }})</span>
        <span v-if="grouped.basket.length > 0" class="dot" />
      </button>
    </div>

    <!-- ============ 移动端：采购篮底部 Sheet ============ -->
    <n-drawer
      v-model:show="showBasketDrawer"
      :height="'70vh'"
      placement="bottom"
      :native-scrollbar="false"
    >
      <n-drawer-content :show-icon="false" :title="`🛒 采购篮 (${grouped.basket.length})`" closable>
        <FridgeBasket
          :items="grouped.basket"
          :is-mobile="true"
          @drop-basket="onDropBasket"
          @item-click="onItemClick"
          @item-action="onItemAction"
        />
        <template #footer>
          <n-button
            type="primary"
            block
            size="large"
            strong
            :disabled="grouped.basket.length === 0"
            @click="onConfirmPurchase"
          >
            确认购买 ({{ grouped.basket.length }})
          </n-button>
        </template>
      </n-drawer-content>
    </n-drawer>

    <!-- ============ 移动端：历史侧抽屉 ============ -->
    <n-drawer
      v-model:show="showHistoryDrawer"
      :width="'88vw'"
      :max-width="360"
      placement="right"
    >
      <n-drawer-content :show-icon="false" title="🕘 采购历史" closable>
        <FridgeShoppingHistory
          :items="history"
          :categories="categories"
          :is-mobile="true"
          @open-batch="onOpenHistoryBatch"
          @reuse-one="onReuseHistoryOne"
        />
      </n-drawer-content>
    </n-drawer>

    <!-- ============ 移动端：ActionSheet（点击食材后） ============ -->
    <ItemActionSheet
      v-model:show="showActionSheet"
      :item="activeActionItem"
      :template="currentTemplate"
      @consume="onConsumeOne"
      @edit="onEditItem"
      @delete="onDeleteItem"
      @moved="onMovedBySheet"
    />

    <!-- ============ 桌面 FAB（手机端已用 ActionBar 替代） ============ -->
    <n-button
      v-if="!isMobile && viewMode === 'visual' && grouped.basket.length > 0"
      class="confirm-purchase-fab"
      type="primary"
      size="large"
      strong
      @click="onConfirmPurchase"
    >
      🛒 确认购买 ({{ grouped.basket.length }})
    </n-button>

    <!-- 通用弹窗 -->
    <QuickAddDialog
      v-model:show="showQuickAdd"
      :categories="categories"
      @saved="refreshAll"
    />

    <QuickAddDialog
      v-model:show="showEditDialog"
      :categories="categories"
      mode="edit"
      :initial="editingItem"
      @saved="refreshAll"
    />

    <MoveConfirmDialog
      v-model:show="showMoveConfirm"
      :pending="pendingMove"
      @confirm="onConfirmMove"
      @cancel="onCancelMove"
    />

    <FirstRunGuide
      v-model:show="showFirstRun"
      :templates="templates"
      :current-layout="currentTemplate?.layout ?? null"
      :activating-id="activatingTemplateId"
      @select="onFirstRunSelect"
    />

    <!-- 拍照识别 -->
    <n-modal
      v-model:show="showRecognizeModal"
      :title="isMobile ? '' : '拍照识别食材'"
      preset="card"
      :style="isMobile ? { width: '100vw', height: '100vh', maxWidth: '100vw', margin: 0, borderRadius: 0 } : { width: '720px' }"
      :mask-closable="false"
      :closable="!isMobile"
      :show-icon="!isMobile"
    >
      <n-space vertical :size="12">
        <n-upload
          v-if="!recognizedItems.length"
          :show-file-list="false"
          :custom-request="handleRecognizeUpload"
          accept="image/*"
        >
          <n-card hoverable style="text-align:center;cursor:pointer">
            <n-icon size="48"><cloud-upload-outline /></n-icon>
            <p>点击上传冰箱照片，自动识别食材</p>
            <p style="font-size:12px;color:#999">支持单张图片，建议光线充足</p>
          </n-card>
        </n-upload>
        <n-spin v-else :show="recognizing">
          <n-card title="识别结果（请确认或编辑后入库）" size="small">
            <n-data-table
              :columns="recognizedColumns"
              :data="recognizedItems"
              :bordered="true"
              size="small"
            />
          </n-card>
          <n-space justify="end" style="margin-top:12px">
            <n-button @click="resetRecognize">重新拍照</n-button>
            <n-button type="primary" :loading="batchSaving" @click="handleBatchSave">批量入库（采购篮）</n-button>
          </n-space>
        </n-spin>
      </n-space>
      <template v-if="isMobile" #header>
        <div class="mobile-modal-header">
          <span>📷 拍照识别食材</span>
          <n-button quaternary circle size="large" @click="showRecognizeModal = false" aria-label="关闭">
            <n-icon size="22"><close-outline /></n-icon>
          </n-button>
        </div>
      </template>
    </n-modal>

    <ConfirmPurchaseDialog
      v-model:show="showConfirmPurchase"
      :items="grouped.basket"
      @confirm="onConfirmPurchaseSubmit"
    />

    <HistoryBasketDialog
      v-model:show="showHistoryBasket"
      :batch="activeHistoryBatch"
      :categories="categories"
      @reuse-batch="onReuseHistoryBatchSubmit"
      @reuse-one="onReuseHistoryOne"
    />

    <FridgeSettingsDrawer
      v-model:show="showSettings"
      :template="currentTemplate"
      :templates="templates"
      :last-saved-at="lastSavedAt"
      :stats="stats"
      @update-template="onUpdateTemplate"
      @template-changed="onSwitchTemplate"
      @templates-refresh="loadTemplates"
      @open-first-run="showFirstRun = true"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  NSpace, NCard, NRadioGroup, NRadioButton, NButton, NIcon, NTag, NDivider,
  NModal, NUpload, NSpin, NDataTable, NDrawer, NDrawerContent, NDropdown,
  useMessage,
} from 'naive-ui'
import type { UploadCustomRequestOptions, DataTableColumns } from 'naive-ui'
import {
  AddOutline, CameraOutline, CloudUploadOutline, RefreshOutline, SettingsOutline,
  EllipsisHorizontal, SnowOutline, AddCircle, BasketOutline, CloseOutline,
} from '@vicons/ionicons5'
import {
  listItems,
  listCategories,
  listTemplates,
  getDefaultTemplate,
  activateTemplate,
  updateTemplate,
  moveItem,
  recognizeImage,
  consumeOneItem,
  confirmShopping,
  listShoppingHistory,
  reuseShoppingBatch,
  deleteItem,
} from '../../api/fridge'
import type {
  FridgeCategoryVO, FridgeItemVO, FridgeRecognizeVO, FridgeShoppingHistoryVO,
  FridgeTemplateVO, FridgeZone,
} from '../../types/fridge'
import { groupItems, resolvePosition, type FridgeLocation, isCrossZone } from '../../utils/subZoneMapping'
import { useBreakpoint } from '../../composables/useBreakpoint'
import FridgeVisual from './FridgeVisual.vue'
import FridgeBasket from './FridgeBasket.vue'
import FridgeShoppingHistory from './FridgeShoppingHistory.vue'
import FridgeCardView from './FridgeCardView.vue'
import FridgeTable from './FridgeTable.vue'
import FridgeIPadLandscapeView from './FridgeIPadLandscapeView.vue'
import QuickAddDialog from './QuickAddDialog.vue'
import MoveConfirmDialog from './MoveConfirmDialog.vue'
import FirstRunGuide from './FirstRunGuide.vue'
import FridgeSettingsDrawer from './FridgeSettingsDrawer.vue'
import ConfirmPurchaseDialog from './ConfirmPurchaseDialog.vue'
import HistoryBasketDialog from './HistoryBasketDialog.vue'
import ItemActionSheet from './ItemActionSheet.vue'

const message = useMessage()
const { isMobile, isIPadLandscape } = useBreakpoint()

// 视图模式：mobile 默认 card，桌面默认 visual
const viewMode = ref<'visual' | 'card' | 'table'>(isMobile.value ? 'card' : 'visual')

// 切回桌面/移动端时，按"用户原选择"还是"平台默认值"？—— 保留用户选择不重置（按 §G 决策 1）
// 仅当首次进入或 viewMode 仍是无效组合时才回退
watch(isMobile, (v) => {
  if (v && viewMode.value === 'visual') {
    // 移动端拒绝 3D，回退到卡片
    viewMode.value = 'card'
  }
})

const viewModeLabel = computed(() => {
  if (viewMode.value === 'card') return '卡片'
  if (viewMode.value === 'table') return '列表'
  return '3D'
})

function cycleViewMobile() {
  if (viewMode.value === 'card') viewMode.value = 'visual'
  else if (viewMode.value === 'visual') viewMode.value = 'table'
  else viewMode.value = 'card'
}

// 数据
const items = ref<FridgeItemVO[]>([])
const activeItems = computed(() => items.value.filter((it) => it.status === 'ACTIVE' || it.status === 'PENDING'))
const categories = ref<FridgeCategoryVO[]>([])
const templates = ref<FridgeTemplateVO[]>([])
const currentTemplate = ref<FridgeTemplateVO | null>(null)
const lastSavedAt = ref<string | null>(null)
const tableRefreshKey = ref(0)

// 统计
const stats = ref({ total: 0, expired: 0, expiringSoon: 0, fresh: 0 })

// 跨区确认
interface PendingMove {
  item: FridgeItemVO
  fromZone: FridgeZone | null
  fromSubZone: string | null
  toZone: FridgeZone | null
  toSubZone: string | null
  toLabel: string
  fromLabel: string
}
const showMoveConfirm = ref(false)
const pendingMove = ref<PendingMove | null>(null)

// 新增 / 识别
const showQuickAdd = ref(false)
const showEditDialog = ref(false)
const editingItem = ref<FridgeItemVO | null>(null)
const showRecognizeModal = ref(false)
const recognizing = ref(false)
const recognizedItems = ref<FridgeRecognizeVO[]>([])
const batchSaving = ref(false)

// 重新选择门型
const showFirstRun = ref(false)
const activatingTemplateId = ref<number | null>(null)

// 采购历史
const history = ref<FridgeShoppingHistoryVO[]>([])
const showConfirmPurchase = ref(false)
const showHistoryBasket = ref(false)
const activeHistoryBatch = ref<FridgeShoppingHistoryVO | null>(null)

// 冰箱配置抽屉
const showSettings = ref(false)

// 移动端抽屉
const showBasketDrawer = ref(false)
const showHistoryDrawer = ref(false)

// 移动端 ActionSheet
const showActionSheet = ref(false)
const activeActionItem = ref<FridgeItemVO | null>(null)

// …菜单（手机端）
const moreMenuOptions = computed(() => [
  { label: '刷新数据', key: 'refresh' },
  { label: '冰箱配置', key: 'settings' },
  { label: '历史记录', key: 'history' },
  { label: '重新选择门型', key: 'first-run' },
])
function onMoreMenu(key: string) {
  if (key === 'refresh') refreshAll()
  else if (key === 'settings') showSettings.value = true
  else if (key === 'history') showHistoryDrawer.value = true
  else if (key === 'first-run') showFirstRun.value = true
}

function onOpenBasketDrawer() { showBasketDrawer.value = true }
function onOpenHistoryDrawer() { showHistoryDrawer.value = true }

const recognizedColumns: DataTableColumns<FridgeRecognizeVO> = [
  { title: '名称', key: 'name', width: 120 },
  { title: '建议分类', key: 'suggestedCategoryName', width: 100 },
  { title: '建议区域', key: 'suggestedZone', width: 80, render: (r) => zoneLabel(r.suggestedZone) },
  { title: '数量', key: 'estimatedQuantity', width: 80, render: (r) => (r.estimatedQuantity != null ? `${r.estimatedQuantity}${r.estimatedUnit ?? ''}` : '-') },
  { title: '保质期(天)', key: 'estimatedExpiryDays', width: 100 },
]

const grouped = computed(() => groupItems(items.value))

function zoneLabel(zone: string | null) {
  if (zone === 'REFRIGERATED') return '冷藏'
  if (zone === 'FROZEN') return '冷冻'
  return '-'
}

function locationLabel(loc: FridgeLocation, layer: number): string {
  const labels: Record<FridgeLocation, string> = {
    basket: '采购篮',
    fridge: '冷藏',
    freezer: '冷冻',
    chiller: '解冻',
    doorLeft: '左门',
    doorRight: '右门',
  }
  return `${labels[loc]} L${layer + 1}`
}

function pad(n: number) { return n < 10 ? `0${n}` : `${n}` }
function nowStr() {
  const d = new Date()
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function recomputeStats() {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const threshold = new Date(today)
  threshold.setDate(threshold.getDate() + 3)
  let total = 0
  let expired = 0
  let expiringSoon = 0
  for (const it of items.value) {
    if (it.status !== 'ACTIVE') continue
    total++
    if (!it.expiryDate) continue
    const exp = new Date(it.expiryDate)
    exp.setHours(0, 0, 0, 0)
    if (exp.getTime() < today.getTime()) expired++
    else if (exp.getTime() <= threshold.getTime()) expiringSoon++
  }
  stats.value = { total, expired, expiringSoon, fresh: total - expired - expiringSoon }
}

async function loadCategories() {
  const res = await listCategories()
  if (res.data?.success) {
    categories.value = res.data.data ?? []
  }
}

async function loadItems() {
  const res = await listItems({ includePending: true })
  if (res.data?.success) {
    items.value = res.data.data ?? []
    recomputeStats()
  } else {
    message.error(res.data?.message ?? '加载失败')
  }
}

async function loadTemplates() {
  const [listRes, defRes] = await Promise.all([listTemplates(), getDefaultTemplate()])
  if (listRes.data?.success) templates.value = listRes.data.data ?? []
  if (defRes.data?.success) currentTemplate.value = defRes.data.data
}

async function loadHistory() {
  const res = await listShoppingHistory(50)
  if (res.data?.success) history.value = res.data.data ?? []
}

function refreshAll() {
  loadItems()
  loadTemplates()
  loadHistory()
  tableRefreshKey.value++
}

async function onSwitchTemplate(id: number) {
  try {
    const res = await activateTemplate(id)
    if (res.data?.success) {
      currentTemplate.value = res.data.data
      await loadTemplates()
      message.success('已切换模板')
    } else {
      message.error(res.data?.message ?? '切换失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '切换失败')
  }
}

async function onFirstRunSelect(t: FridgeTemplateVO) {
  activatingTemplateId.value = t.id
  try {
    const res = await activateTemplate(t.id)
    if (res.data?.success) {
      currentTemplate.value = res.data.data
      await loadTemplates()
      showFirstRun.value = false
      message.success(`已切换为 ${t.name}`)
    } else {
      message.error(res.data?.message ?? '切换失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '切换失败')
  } finally {
    activatingTemplateId.value = null
  }
}

async function onUpdateTemplate(t: FridgeTemplateVO) {
  if (!t.id) return
  try {
    const res = await updateTemplate(t.id, {
      name: t.name,
      layout: t.layout,
      fridgeLayers: t.fridgeLayers,
      freezerLayers: t.freezerLayers,
      chillerLayers: t.chillerLayers,
      doorShelfCount: t.doorShelfCount,
    })
    if (res.data?.success) {
      lastSavedAt.value = nowStr()
      currentTemplate.value = res.data.data
      message.success('已自动保存')
    } else {
      message.error(res.data?.message ?? '保存失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '保存失败')
  }
}

function onDropVisual(payload: { itemId: number; zone: FridgeZone | null; subZone: string | null }) {
  const item = items.value.find((it) => it.id === payload.itemId)
  if (!item) return
  requestMove(item, payload.zone, payload.subZone)
}

function requestMove(item: FridgeItemVO, zone: FridgeZone | null, subZone: string | null) {
  const from = resolvePosition(item)
  const to: { zone: FridgeZone | null; location: FridgeLocation; layer: number } = {
    zone,
    location: zone === 'REFRIGERATED' ? 'fridge' : zone === 'FROZEN' ? 'freezer' : 'chiller',
    layer: 0,
  }
  if (subZone) {
    const m = /-L(\d+)$/.exec(subZone)
    if (m) to.layer = parseInt(m[1], 10) - 1
    if (/DOOR-LEFT-L/.test(subZone)) to.location = 'doorLeft'
    else if (/DOOR-RIGHT-L/.test(subZone)) to.location = 'doorRight'
    else if (/CHILLER-L/.test(subZone)) to.location = 'chiller'
  }
  if (isCrossZone({ zone: item.zone, location: from.location }, to)) {
    pendingMove.value = {
      item,
      fromZone: item.zone,
      fromSubZone: item.subZone ?? null,
      toZone: zone,
      toSubZone: subZone,
      fromLabel: locationLabel(from.location, from.layer),
      toLabel: locationLabel(to.location, to.layer),
    }
    showMoveConfirm.value = true
  } else {
    doMove(item.id, zone, subZone)
  }
}

async function doMove(id: number, zone: FridgeZone | null, subZone: string | null) {
  try {
    const res = await moveItem(id, { zone, subZone })
    if (res.data?.success) {
      message.success('已移动')
      await loadItems()
    } else {
      message.error(res.data?.message ?? '移动失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '移动失败')
  }
}

function onConfirmMove() {
  if (pendingMove.value) {
    doMove(pendingMove.value.item.id, pendingMove.value.toZone, pendingMove.value.toSubZone)
  }
  showMoveConfirm.value = false
  pendingMove.value = null
}

function onCancelMove() {
  pendingMove.value = null
}

function onDropBasket() {
  // 占位：3D 视图退回采购篮
}

// Item 点击：手机端 → 弹 ActionSheet；桌面端 → 直接消耗 1 份
async function onItemClick(item: FridgeItemVO) {
  if (isMobile.value) {
    if (item.status === 'PENDING') {
      // 采购篮：直接删除（PENDING 点击即从篮子移除）
      await onDeleteItem(item)
      return
    }
    activeActionItem.value = item
    showActionSheet.value = true
    return
  }
  await consumeOrDeleteOnDesktop(item)
}

async function consumeOrDeleteOnDesktop(item: FridgeItemVO) {
  if (item.status === 'PENDING') {
    try {
      const res = await deleteItem(item.id)
      if (res.data?.success) {
        message.success(`已从采购篮移除 ${item.name}`)
        await loadItems()
      } else {
        message.error(res.data?.message ?? '操作失败')
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '操作失败')
    }
    return
  }
  await onConsumeOne(item)
}

// 给 FridgeBasket 转发（手机端抽屉里的卡片）
async function onItemAction(item: FridgeItemVO) {
  if (isMobile.value) {
    activeActionItem.value = item
    showActionSheet.value = true
  } else {
    await consumeOrDeleteOnDesktop(item)
  }
}

async function onConsumeOne(item: FridgeItemVO) {
  try {
    const res = await consumeOneItem(item.id)
    if (res.data?.success) {
      const after = res.data.data
      if (after?.status === 'CONSUMED') {
        message.success(`${item.name} 已消耗完毕`)
      } else {
        message.success(`${item.name} -1，剩 ${after?.quantity ?? 1} 份`)
      }
      await loadItems()
    } else {
      message.error(res.data?.message ?? '消耗失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '消耗失败')
  }
}

async function onDeleteItem(item: FridgeItemVO) {
  try {
    const res = await deleteItem(item.id)
    if (res.data?.success) {
      message.success(`已移除 ${item.name}`)
      await loadItems()
    } else {
      message.error(res.data?.message ?? '删除失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '删除失败')
  }
}

function onMovedBySheet(payload: { item: FridgeItemVO; zone: FridgeZone | null; subZone: string | null }) {
  requestMove(payload.item, payload.zone, payload.subZone)
}

function onEditItem(item: FridgeItemVO) {
  editingItem.value = item
  showEditDialog.value = true
}

function onConfirmPurchase() {
  if (grouped.value.basket.length === 0) {
    message.warning('采购篮是空的')
    return
  }
  showConfirmPurchase.value = true
}

async function onConfirmPurchaseSubmit(payload: { partnerEmail?: string; groupId?: number }) {
  const basketSnapshot = grouped.value.basket.slice()
  const itemsPayload = basketSnapshot.map((it) => ({
    name: it.name,
    categoryId: it.categoryId,
    quantity: it.quantity ?? 1,
    unit: it.unit,
    source: it.source || 'MANUAL',
  }))
  const basketItemIds = basketSnapshot.map((it) => it.id)
  try {
    const res = await confirmShopping({
      items: itemsPayload,
      partnerEmail: payload.partnerEmail,
      groupId: payload.groupId,
      basketItemIds,
    })
    if (res.data?.success) {
      message.success(payload.partnerEmail
        ? `已确认购买 ${itemsPayload.length} 项，已通知 ${payload.partnerEmail}`
        : payload.groupId
          ? `已确认购买 ${itemsPayload.length} 项，已同步到组内成员`
          : `已确认购买 ${itemsPayload.length} 项`)
      showConfirmPurchase.value = false
      showBasketDrawer.value = false
      await Promise.all([loadItems(), loadHistory()])
    } else {
      message.error(res.data?.message ?? '确认失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '确认失败')
  }
}

function onOpenHistoryBatch(batch: FridgeShoppingHistoryVO) {
  activeHistoryBatch.value = batch
  showHistoryBasket.value = true
}

async function onReuseHistoryOne(item: { name: string; categoryId?: number; quantity?: number; unit?: string; source?: string }) {
  const { quickCreateItem } = await import('../../api/fridge')
  try {
    const res = await quickCreateItem({
      name: item.name,
      categoryId: item.categoryId,
      quantity: item.quantity,
      unit: item.unit,
      source: (item.source as any) ?? 'MANUAL',
      zone: null,
      subZone: null,
    })
    if (res.data?.success) {
      message.success(`已加入采购篮：${item.name}`)
      await loadItems()
    } else {
      message.error(res.data?.message ?? '加入失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '加入失败')
  }
}

async function onReuseHistoryBatchSubmit(batch: FridgeShoppingHistoryVO) {
  const res = await reuseShoppingBatch(batch.batchId)
  if (!res.data?.success) {
    message.error(res.data?.message ?? '复用失败')
    return
  }
  const reusedItems = res.data.data ?? []
  const { quickCreateItem } = await import('../../api/fridge')
  let success = 0
  for (const it of reusedItems) {
    try {
      const r = await quickCreateItem({
        name: it.name,
        categoryId: it.categoryId,
        quantity: it.quantity,
        unit: it.unit,
        source: (it.source as any) ?? 'MANUAL',
        zone: null,
        subZone: null,
      })
      if (r.data?.success) success++
    } catch {
      // ignore single failure
    }
  }
  message.success(`已加入采购篮 ${success} 项`)
  showHistoryBasket.value = false
  await loadItems()
}

function openRecognize() {
  recognizedItems.value = []
  showRecognizeModal.value = true
}
function resetRecognize() {
  recognizedItems.value = []
}

async function handleRecognizeUpload({ file, onFinish, onError }: UploadCustomRequestOptions) {
  recognizing.value = true
  try {
    const raw = file.file as File
    const dataUrl = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = () => reject(reader.error)
      reader.readAsDataURL(raw)
    })
    const res = await recognizeImage({ imageBase64: dataUrl })
    if (res.data.success) {
      recognizedItems.value = res.data.data?.items ?? []
      if (!recognizedItems.value.length) {
        message.warning('未能识别出食材，请手动添加')
      } else {
        message.success(`识别出 ${recognizedItems.value.length} 项`)
      }
      onFinish()
    } else {
      message.error(res.data.message ?? '识别失败')
      onError()
    }
  } catch (e) {
    message.error('识别失败')
    onError()
  } finally {
    recognizing.value = false
  }
}

async function handleBatchSave() {
  if (!recognizedItems.value.length) return
  batchSaving.value = true
  try {
    const { quickCreateItem } = await import('../../api/fridge')
    const purchaseBase = new Date()
    let success = 0
    for (const r of recognizedItems.value) {
      const purchase = r.purchaseDate ? new Date(r.purchaseDate) : purchaseBase
      const expiry = r.estimatedExpiryDays != null
        ? new Date(purchase.getTime() + r.estimatedExpiryDays * 24 * 3600 * 1000).toISOString().slice(0, 10)
        : undefined
      try {
        const res = await quickCreateItem({
          name: r.name,
          categoryId: r.suggestedCategoryId,
          purchaseDate: purchase.toISOString().slice(0, 10),
          expiryDate: expiry,
          zone: null,
          subZone: null,
        })
        if (res.data?.success) success++
      } catch {
        // ignore
      }
    }
    message.success(`已加入采购篮 ${success} 项`)
    showRecognizeModal.value = false
    recognizedItems.value = []
    refreshAll()
  } finally {
    batchSaving.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadItems()
  loadTemplates()
  loadHistory()
})
</script>

<style scoped>
.fridge-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.fridge-page.is-mobile {
  gap: 8px;
}

.desktop-toolbar {
  margin-bottom: 4px;
}

.visual-layout {
  display: grid;
  grid-template-columns: 260px 1fr 280px;
  gap: 16px;
  align-items: start;
}
@media (max-width: 1280px) {
  .visual-layout {
    grid-template-columns: 240px 1fr 240px;
  }
}
@media (max-width: 1100px) {
  .visual-layout {
    grid-template-columns: 240px 1fr;
  }
  .visual-layout > :nth-child(3) {
    grid-column: 1 / -1;
  }
}

.confirm-purchase-fab {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 100;
  box-shadow: 0 12px 32px rgba(249, 115, 22, 0.35);
}

/* ============ 移动端：顶部精简条 ============ */
.mobile-toolbar {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f1f5f9;
  padding: 10px 12px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mobile-template-line {
  font-size: 12px;
  color: #64748b;
}

/* ============ 移动端：底部 ActionBar ============ */
.mobile-actionbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 80;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid #e2e8f0;
  padding: 6px 8px calc(6px + env(safe-area-inset-bottom, 0px));
}
.action-btn {
  appearance: none;
  background: transparent;
  border: 0;
  padding: 6px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #64748b;
  border-radius: 10px;
  position: relative;
  transition: background 0.15s;
}
.action-btn:active {
  background: #f1f5f9;
}
.action-btn .lbl {
  font-weight: 600;
}
.action-btn.primary {
  color: #f97316;
}
.action-btn.active {
  color: #f97316;
  background: #fff7ed;
}
.action-btn .dot {
  position: absolute;
  top: 4px;
  right: 22%;
  min-width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 999px;
}

/* ============ 移动端 Modal 全屏头部 ============ */
.mobile-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
}
</style>
