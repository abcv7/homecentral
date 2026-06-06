<template>
  <n-space vertical :size="16">
    <n-h2>驿站管理</n-h2>

    <!-- Filters -->
    <n-card :bordered="true" size="small">
      <n-space align="center" wrap>
        <n-select
          v-model:value="filter.status"
          :options="statusOptions"
          placeholder="全部状态"
          clearable
          style="width:140px"
        />
        <n-input
          v-model:value="filter.trackingNumber"
          placeholder="搜索运单号..."
          clearable
          style="width:200px"
          @keyup.enter="handleSearch"
        />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="primary" strong @click="openCreate">
          <template #icon><n-icon><add-outline /></n-icon></template>
          新增包裹
        </n-button>
        <n-badge
          :value="recognitionRunningCount"
          :max="9"
          :show="recognitionRunningCount > 0"
          :offset="[-6, 4]"
        >
          <n-button
            type="info"
            strong
            :disabled="atRecognitionLimit"
            @click="openRecognitionInput"
          >
            <template #icon><n-icon><camera-outline /></n-icon></template>
            拍照识别
          </n-button>
        </n-badge>
      </n-space>
    </n-card>

    <!-- Table -->
    <n-data-table
      :columns="columns"
      :data="list"
      :loading="loading"
      :pagination="pagination"
      :bordered="true"
      :scroll-x="900"
    />

    <!-- Create / Edit Modal -->
    <n-modal
      v-model:show="showModal"
      :title="isEdit ? '编辑包裹' : '新增包裹'"
      preset="card"
      :style="isMobile ? { width: '94vw', maxWidth: '480px' } : { width: '560px' }"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-placement="top"
        @submit.prevent="handleSubmit"
      >
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="快递公司" path="courierCompany">
              <n-input v-model:value="form.courierCompany" placeholder="如：顺丰速运" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="运单号" path="trackingNumber">
              <n-input v-model:value="form.trackingNumber" placeholder="运单号" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="取件码">
              <n-input v-model:value="form.pickupCode" placeholder="取件码（可选）" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="归属人">
              <n-input v-model:value="form.ownerName" placeholder="默认当前用户" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="到站日期">
              <n-date-picker v-model:value="form.arrivedDateTs" type="date" placeholder="选择到站日期" clearable />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="手机尾号（顺丰查询用）">
              <n-input :value="phoneTail ?? ''" placeholder="未设置手机号" disabled />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="附件">
              <n-upload :default-upload="false" @change="handleFileChange" accept="image/*,.pdf">
                <n-button size="small">选择文件</n-button>
              </n-upload>
              <span v-if="form.attachmentUrl" style="margin-left:8px;font-size:12px;color:#999;">已上传</span>
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :rows="3" placeholder="备注（可选）" />
        </n-form-item>
        <n-space justify="end" style="margin-top:16px;">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="submitting">确定</n-button>
        </n-space>
      </n-form>
    </n-modal>

    <!-- Detail Drawer -->
    <n-drawer v-model:show="showDetail" :width="420" placement="right">
      <n-drawer-content title="包裹详情" closable>
        <n-descriptions v-if="detail" label-placement="left" :column="1">
          <n-descriptions-item label="ID">{{ detail.id }}</n-descriptions-item>
          <n-descriptions-item label="快递公司">{{ detail.courierCompany }}</n-descriptions-item>
          <n-descriptions-item label="运单号">{{ detail.trackingNumber }}</n-descriptions-item>
          <n-descriptions-item label="取件码">{{ detail.pickupCode || '-' }}</n-descriptions-item>
          <n-descriptions-item label="归属人">{{ detail.ownerName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="detail.status === 'PENDING_PICKUP' ? 'warning' : detail.status === 'RECEIVED' ? 'info' : 'success'" size="small">
              {{ statusMap[detail.status] }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="到站天数">
            <n-tag :type="(detail.daysAtStation ?? 0) >= 3 ? 'error' : (detail.daysAtStation ?? 0) >= 2 ? 'warning' : 'default'" size="small">
              {{ detail.daysAtStation ?? 0 }} 天
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="来源">{{ detail.source === 'MANUAL' ? '手动录入' : detail.source === 'OCR' ? 'AI识别' : 'API导入' }}</n-descriptions-item>
          <n-descriptions-item label="商品">{{ detail.productName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detail.remark || '-' }}</n-descriptions-item>
          <n-descriptions-item label="附件">
            <template v-if="detail.attachmentUrl">
              <n-button text tag="a" :href="detail.attachmentUrl" target="_blank">查看</n-button>
            </template>
            <template v-else>-</template>
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ detail.createdAt }}</n-descriptions-item>
        </n-descriptions>
      </n-drawer-content>
    </n-drawer>

    <!-- Delete Confirm -->
    <n-modal v-model:show="showDeleteConfirm" preset="dialog" type="warning" title="确认删除"
      content="确定要删除该包裹吗？已收货的包裹删除后不可恢复。"
      positive-text="确定删除"
      negative-text="取消"
      @positive-click="handleDelete"
      @negative-click="showDeleteConfirm = false"
    />

    <!-- Tracking Drawer -->
    <n-drawer v-model:show="showTracking" :width="480" placement="right">
      <n-drawer-content title="物流轨迹" closable>
        <template v-if="trackingData">
          <n-descriptions label-placement="left" :column="1" size="small">
            <n-descriptions-item label="运单号">{{ trackingData.trackingNumber }}</n-descriptions-item>
            <n-descriptions-item label="快递公司">{{ trackingData.courierName || trackingData.courierCode }}</n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="trackingData.state === '3' ? 'success' : trackingData.state === 'error' || trackingData.state === 'unknown' ? 'error' : 'info'" size="small">
                {{ trackingData.stateLabel }}
              </n-tag>
            </n-descriptions-item>
          </n-descriptions>
          <n-divider />
          <n-h5 v-if="trackingData.tracks.length === 0">暂无物流信息</n-h5>
          <n-timeline v-else>
            <n-timeline-item
              v-for="(item, index) in trackingData.tracks"
              :key="index"
              :type="index === 0 ? 'success' : 'default'"
              :time="item.time"
              :content="item.context"
            />
          </n-timeline>
        </template>
        <n-spin v-else :show="trackingLoading">
          <n-empty v-if="!trackingLoading" description="点击物流查询查看轨迹" />
        </n-spin>
      </n-drawer-content>
    </n-drawer>

    <!-- Recognition Panel -->
    <RecognitionPanel
      ref="recognitionPanel"
      @imported="fetchList"
      @task-count-change="onTaskCountChange"
    />

    <!-- Recognition Task Input -->
    <RecognitionTaskInput
      v-model:show="showRecognitionInput"
      @submit="onRecognitionSubmit"
    />

    <!-- Recognition Floating Bubble -->
    <n-float-button
      v-if="showRecognitionBubble"
      :right="24"
      :bottom="24"
      type="primary"
      @click="expandRecognitionPanel"
    >
      识别 {{ recognitionRunningCount }}
    </n-float-button>
  </n-space>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useMessage, NButton, NTag, NSpace, NIcon, NTimeline, NTimelineItem, NDescriptions, NDescriptionsItem, NDivider, NH5, NSpin, NEmpty, NBadge, NFloatButton } from 'naive-ui'
import { AddOutline, CreateOutline, EyeOutline, LocateOutline, CameraOutline } from '@vicons/ionicons5'
import type { DataTableColumn, FormInst, FormRules, SelectOption } from 'naive-ui'
import type { ParcelVO, ParcelStatus, ParcelCreateRequest, ParcelUpdateRequest, TrackingVO } from '../../types/api'
import { listParcels, getParcel, createParcel, updateParcel, pickUpParcel, receiveParcel, deleteParcel, queryParcelTracking, getPhoneTail } from '../../api/parcel'
import { uploadFile } from '../../api/file'
import type { UploadFileInfo } from 'naive-ui'
import RecognitionPanel from '../../components/RecognitionPanel.vue'
import RecognitionTaskInput from '../../components/RecognitionTaskInput.vue'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

const message = useMessage()
const recognitionPanel = ref<InstanceType<typeof RecognitionPanel> | null>(null)
const showRecognitionInput = ref(false)
const recognitionRunningCount = ref(0)
const recognitionTotalCount = ref(0)
const RECOGNITION_TASK_LIMIT = 5
const atRecognitionLimit = computed(() => recognitionTotalCount.value >= RECOGNITION_TASK_LIMIT)
const showRecognitionBubble = computed(
  () => recognitionTotalCount.value > 0 && !recognitionPanel.value?.isVisible(),
)

const statusMap: Record<ParcelStatus, string> = {
  PENDING_PICKUP: '待取件',
  PICKED_UP: '已取件',
  RECEIVED: '已收货',
}

const statusOptions: SelectOption[] = [
  { label: '待取件', value: 'PENDING_PICKUP' },
  { label: '已取件', value: 'PICKED_UP' },
  { label: '已收货', value: 'RECEIVED' },
]

// --- Filter ---
const filter = reactive({ status: null as string | null, trackingNumber: '' })
const list = ref<ParcelVO[]>([])
const loading = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onChange: (page: number) => { pagination.page = page; fetchList() },
  onUpdatePageSize: (size: number) => { pagination.pageSize = size; pagination.page = 1; fetchList() },
})

async function fetchList() {
  loading.value = true
  try {
    const res = await listParcels({
      status: filter.status ?? undefined,
      trackingNumber: filter.trackingNumber || undefined,
      page: pagination.page,
      size: pagination.pageSize,
    })
    list.value = res.data.data.content
    pagination.itemCount = res.data.data.totalElements
  } catch {
    message.error('加载包裹列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; fetchList() }
function handleReset() { filter.status = null; filter.trackingNumber = ''; pagination.page = 1; fetchList() }

// --- Recognition Panel ---
function openRecognitionInput() {
  if (atRecognitionLimit.value) {
    message.warning(`最多同时进行 ${RECOGNITION_TASK_LIMIT} 个识别任务，请先关闭已完成的任务`)
    return
  }
  showRecognitionInput.value = true
}

async function onRecognitionSubmit(
  payload: { mode: 'image'; file: File } | { mode: 'sms'; smsText: string },
) {
  showRecognitionInput.value = false
  if (payload.mode === 'image') {
    await recognitionPanel.value?.startImageTask(payload.file)
  } else {
    await recognitionPanel.value?.startSmsTask(payload.smsText)
  }
}

function onTaskCountChange(payload: { running: number; total: number }) {
  recognitionRunningCount.value = payload.running
  recognitionTotalCount.value = payload.total
}

function expandRecognitionPanel() {
  recognitionPanel.value?.show()
}

// --- Modal (Create / Edit) ---
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)

const form = reactive<ParcelCreateRequest & { arrivedDateTs: number | null }>({
  courierCompany: '',
  trackingNumber: '',
  pickupCode: '',
  remark: '',
  attachmentUrl: '',
  ownerName: '',
  arrivedDateTs: null,
})

const phoneTail = ref<string | null>(null)

const formRules: FormRules = {
  courierCompany: { required: true, message: '请输入快递公司', trigger: 'blur' },
  trackingNumber: { required: true, message: '请输入运单号', trigger: 'blur' },
}

async function handleFileChange({ fileList }: { fileList: UploadFileInfo[] }) {
  const file = fileList[0]?.file
  if (!file) return
  try {
    const res = await uploadFile(file, 'parcel')
    form.attachmentUrl = res.data.data.accessUrl
    message.success('上传成功')
  } catch {
    message.error('上传失败')
  }
}

function resetForm() {
  form.courierCompany = ''
  form.trackingNumber = ''
  form.pickupCode = ''
  form.remark = ''
  form.attachmentUrl = ''
  form.ownerName = ''
  form.arrivedDateTs = null
  phoneTail.value = null
  editId.value = null
}

function openCreate() {
  isEdit.value = false
  resetForm()
  fetchPhoneTail()
  showModal.value = true
}

async function openEdit(row: ParcelVO) {
  isEdit.value = true
  editId.value = row.id
  form.courierCompany = row.courierCompany
  form.trackingNumber = row.trackingNumber
  form.pickupCode = row.pickupCode ?? ''
  form.remark = row.remark ?? ''
  form.attachmentUrl = row.attachmentUrl ?? ''
  form.ownerName = row.ownerName ?? ''
  form.arrivedDateTs = null
  fetchPhoneTail()
  showModal.value = true
}

async function fetchPhoneTail() {
  try {
    const res = await getPhoneTail()
    phoneTail.value = res.data.data
  } catch {
    phoneTail.value = null
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      const payload: ParcelUpdateRequest = {}
      if (form.courierCompany) payload.courierCompany = form.courierCompany
      if (form.trackingNumber) payload.trackingNumber = form.trackingNumber
      payload.pickupCode = form.pickupCode || undefined
      payload.remark = form.remark || undefined
      payload.attachmentUrl = form.attachmentUrl || undefined
      payload.ownerName = form.ownerName || undefined
      if (form.arrivedDateTs) payload.arrivedDate = new Date(form.arrivedDateTs).toISOString().split('T')[0]
      await updateParcel(editId.value, payload)
      message.success('更新成功')
    } else {
      const data: ParcelCreateRequest = { ...form }
      if (form.arrivedDateTs) data.arrivedDate = new Date(form.arrivedDateTs).toISOString().split('T')[0]
      delete (data as any).arrivedDateTs
      await createParcel(data)
      message.success('创建成功')
    }
    showModal.value = false
    fetchList()
  } catch {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// --- Detail Drawer ---
const showDetail = ref(false)
const detail = ref<ParcelVO | null>(null)

async function openDetail(row: ParcelVO) {
  try {
    const res = await getParcel(row.id)
    detail.value = res.data.data
    showDetail.value = true
  } catch {
    message.error('获取详情失败')
  }
}

// --- Pickup ---
async function handlePickup(row: ParcelVO) {
  try {
    await pickUpParcel(row.id)
    message.success('已标记为取件')
    fetchList()
  } catch {
    message.error('取件操作失败')
  }
}

// --- Receive ---
async function handleReceive(row: ParcelVO) {
  try {
    await receiveParcel(row.id)
    message.success('已标记为收货')
    fetchList()
  } catch {
    message.error('收货操作失败')
  }
}

// --- Delete ---
const showDeleteConfirm = ref(false)
const deleteTarget = ref<number | null>(null)

function confirmDelete(row: ParcelVO) {
  deleteTarget.value = row.id
  showDeleteConfirm.value = true
}

async function handleDelete() {
  if (!deleteTarget.value) return
  try {
    await deleteParcel(deleteTarget.value)
    message.success('删除成功')
    showDeleteConfirm.value = false
    fetchList()
  } catch {
    message.error('删除失败')
  }
}

// --- Tracking ---
const showTracking = ref(false)
const trackingLoading = ref(false)
const trackingData = ref<TrackingVO | null>(null)

async function handleTracking(row: ParcelVO) {
  trackingLoading.value = true
  trackingData.value = null
  showTracking.value = true
  try {
    const res = await queryParcelTracking(row.id)
    trackingData.value = res.data.data
  } catch {
    message.error('物流查询失败')
    trackingData.value = { trackingNumber: row.trackingNumber, courierCode: '', courierName: null, state: 'error', stateLabel: '查询失败', message: '接口调用异常', tracks: [] }
  } finally {
    trackingLoading.value = false
  }
}

// --- Columns ---
function renderActions(row: ParcelVO) {
  return h(NSpace, { size: 'small' }, () => {
    const btns = [
      h(NButton, { size: 'tiny', quaternary: true, onClick: () => openDetail(row) }, {
        icon: () => h(EyeOutline),
      }),
      h(NButton, { size: 'tiny', quaternary: true, onClick: () => handleTracking(row) }, {
        icon: () => h(LocateOutline),
      }),
    ]
    if (row.status === 'PENDING_PICKUP') {
      btns.push(
        h(NButton, { size: 'tiny', quaternary: true, onClick: () => openEdit(row) }, {
          icon: () => h(CreateOutline),
        }),
        h(NButton, { size: 'tiny', type: 'primary', secondary: true, onClick: () => handlePickup(row) }, {
          default: () => '取件',
        }),
      )
    } else if (row.status === 'PICKED_UP') {
      btns.push(
        h(NButton, { size: 'tiny', type: 'success', secondary: true, onClick: () => handleReceive(row) }, {
          default: () => '收货',
        }),
      )
    } else if (row.status === 'RECEIVED') {
      btns.push(
        h(NButton, { size: 'tiny', type: 'error', quaternary: true, onClick: () => confirmDelete(row) }, {
          default: () => '删除',
        }),
      )
    }
    return btns
  })
}

const columns: DataTableColumn<ParcelVO>[] = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '快递公司', key: 'courierCompany', width: 110 },
  { title: '运单号', key: 'trackingNumber', width: 160 },
  { title: '取件码', key: 'pickupCode', width: 90 },
  { title: '归属', key: 'ownerName', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const tagType = row.status === 'PENDING_PICKUP' ? 'warning' : row.status === 'RECEIVED' ? 'info' : 'success'
      return h(NTag, { type: tagType, size: 'small' }, { default: () => statusMap[row.status] })
    },
  },
  {
    title: '天数',
    key: 'daysAtStation',
    width: 70,
    render(row) {
      const d = row.daysAtStation ?? 0
      const type = d >= 3 ? 'error' : d >= 2 ? 'warning' : 'default'
      return h(NTag, { type, size: 'small' }, { default: () => `${d}天` })
    },
  },
  {
    title: '备注',
    key: 'remark',
    ellipsis: { tooltip: true },
    width: 140,
  },
  {
    title: '商品',
    key: 'productName',
    width: 100,
    ellipsis: { tooltip: true },
  },
  {
    title: '来源',
    key: 'source',
    width: 70,
    render(row) {
      const sourceMap: Record<string, { label: string; type: string }> = {
        MANUAL: { label: '手动', type: 'default' },
        API: { label: 'API', type: 'primary' },
        OCR: { label: '识别', type: 'success' },
      }
      const s = sourceMap[row.source] || { label: row.source, type: 'default' }
      return h(NTag, { size: 'small', type: s.type as any }, { default: () => s.label })
    },
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 160,
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: renderActions,
  },
]

onMounted(fetchList)
</script>
