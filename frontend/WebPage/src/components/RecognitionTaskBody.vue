<template>
  <div class="task-body-wrapper">
    <n-space align="center" justify="space-between" style="margin-bottom: 8px;">
      <n-space align="center" :size="6">
        <n-tag size="small" :type="statusType">{{ statusLabel }}</n-tag>
        <n-text depth="3" style="font-size:12px;">{{ task.title }}</n-text>
      </n-space>
      <n-button text size="tiny" @click="$emit('remove', task)">
        <template #icon><n-icon><close-outline /></n-icon></template>
      </n-button>
    </n-space>

    <!-- Progress -->
    <div v-if="task.status === 'progress'">
      <n-progress type="line" :percentage="task.progressPercent" :status="'info'" :show-indicator="true" />
      <n-text depth="3" style="font-size:13px;display:block;margin-top:6px;">
        {{ task.progressMessage }}
      </n-text>
    </div>

    <!-- Result -->
    <div v-else-if="task.status === 'result'">
      <n-space vertical :size="8">
        <n-text strong style="font-size:13px;">
          识别完成，共 {{ task.parcels.length }} 个包裹
        </n-text>

        <div v-if="(task.countdown ?? 0) > 0" class="auto-import-banner">
          <n-space align="center" justify="space-between" :size="8">
            <n-space align="center" :size="6">
              <n-tag :type="countdownTagType" size="small">
                {{ task.countdown }}s 后自动入库
              </n-tag>
              <n-text depth="3" style="font-size:12px;">关闭页面也会自动入库</n-text>
            </n-space>
            <n-button size="tiny" @click="$emit('cancel-auto-import', task)">
              取消
            </n-button>
          </n-space>
          <n-progress
            type="line"
            :percentage="countdownPercent"
            :color="countdownColor"
            :show-indicator="false"
            style="margin-top:6px;"
          />
        </div>

        <n-data-table
          :columns="resultColumns"
          :data="task.parcels"
          :bordered="true"
          :size="'small'"
          :max-height="280"
          :row-key="(row: any) => row.index"
        />

        <n-space justify="end" style="margin-top:8px;">
          <n-button size="small" @click="$emit('cancel', task)">放弃</n-button>
          <n-button
            type="primary"
            size="small"
            :loading="importing"
            :disabled="selectedCount === 0"
            @click="onImport"
          >
            确认导入 ({{ selectedCount }} 个)
          </n-button>
        </n-space>
      </n-space>
    </div>

    <!-- Import Result -->
    <div v-else-if="task.status === 'imported' && task.importResult">
      <n-space vertical :size="12">
        <n-result
          :status="task.importResult.failureCount > 0 ? 'warning' : 'success'"
          :title="`成功导入 ${task.importResult.successCount} 个`"
          :description="task.importResult.failureCount > 0 ? `${task.importResult.failureCount} 个取件码重复，已跳过` : '所有包裹已成功导入'"
          size="small"
        />

        <div v-if="task.importResult.failureItems.length > 0">
          <n-text strong style="font-size:12px;">失败详情：</n-text>
          <n-list :show-divider="true" :bordered="true" size="small" style="max-height:160px;overflow-y:auto;">
            <n-list-item v-for="(item, idx) in task.importResult.failureItems" :key="idx">
              <n-thing>
                <template #header>
                  <n-text style="font-size:12px;">{{ item.item }}</n-text>
                </template>
                <template #description>
                  <n-text depth="3" style="font-size:11px;">{{ item.reason }}</n-text>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </div>

        <n-space justify="end">
          <n-button size="small" @click="$emit('remove', task)">关闭</n-button>
        </n-space>
      </n-space>
    </div>

    <!-- Error -->
    <div v-else-if="task.status === 'error'">
      <n-result status="error" title="识别失败" :description="task.errorMessage" size="small">
        <template #footer>
          <n-space>
            <n-button size="small" @click="$emit('cancel', task)">关闭</n-button>
            <n-button size="small" type="primary" @click="$emit('retry', task)">
              <template #icon><n-icon><refresh-outline /></n-icon></template>
              重试
            </n-button>
          </n-space>
        </template>
      </n-result>
    </div>

    <!-- Cancelled -->
    <div v-else-if="task.status === 'cancelled'">
      <n-result status="info" title="已取消" description="此任务已取消" size="small" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { NButton, NIcon, NSpace, NTag, NText, NProgress, NDataTable, NResult, NList, NListItem, NThing } from 'naive-ui'
import { CloseOutline, RefreshOutline } from '@vicons/ionicons5'
import type { DataTableColumn } from 'naive-ui'
import type { RecognitionTask, RecognizedParcel } from './recognition-task'

const props = defineProps<{
  task: RecognitionTask
  importing: boolean
}>()

const emit = defineEmits<{
  (e: 'import', task: RecognitionTask, selectedIndices: number[]): void
  (e: 'retry', task: RecognitionTask): void
  (e: 'cancel', task: RecognitionTask): void
  (e: 'cancel-auto-import', task: RecognitionTask): void
  (e: 'remove', task: RecognitionTask): void
}>()

const selectedCount = computed(
  () => props.task.parcels.filter(p => p.selected).length,
)

const COUNTDOWN_TOTAL = 10

const countdownPercent = computed(() => {
  const c = props.task.countdown ?? 0
  return Math.max(0, Math.min(100, (c / COUNTDOWN_TOTAL) * 100))
})

const countdownColor = computed(() => {
  const c = props.task.countdown ?? 0
  if (c <= 3) return '#d03050'
  if (c <= 5) return '#f0a020'
  return '#2080f0'
})

const countdownTagType = computed(() => {
  const c = props.task.countdown ?? 0
  if (c <= 3) return 'error' as const
  if (c <= 5) return 'warning' as const
  return 'info' as const
})

const statusLabel = computed(() => {
  switch (props.task.status) {
    case 'progress': return '识别中'
    case 'result': return '可导入'
    case 'importing': return '导入中'
    case 'imported': return '已导入'
    case 'error': return '失败'
    case 'cancelled': return '已取消'
    default: return ''
  }
})

const statusType = computed(() => {
  switch (props.task.status) {
    case 'progress': return 'info' as const
    case 'result': return 'success' as const
    case 'importing': return 'info' as const
    case 'imported': return 'success' as const
    case 'error': return 'error' as const
    case 'cancelled': return 'default' as const
    default: return 'default' as const
  }
})

const resultColumns: DataTableColumn<RecognizedParcel>[] = [
  {
    title: '',
    key: 'selected',
    width: 36,
    render(row) {
      return h(
        NButton,
        {
          size: 'tiny',
          quaternary: true,
          type: row.selected ? 'success' : 'default',
          onClick: () => { row.selected = !row.selected },
        },
        { default: () => (row.selected ? '✓' : '○') },
      )
    },
  },
  { title: '快递公司', key: 'courierCompany', width: 90, ellipsis: { tooltip: true } },
  { title: '取件码', key: 'pickupCode', width: 70, ellipsis: { tooltip: true } },
  { title: '运单号', key: 'trackingNumber', width: 110, ellipsis: { tooltip: true } },
  {
    title: '置信度',
    key: 'confidence',
    width: 60,
    render(row) {
      const type = row.confidence === 'HIGH' ? 'success' : row.confidence === 'MEDIUM' ? 'warning' : 'error'
      return h(NTag, { type, size: 'small' }, { default: () => row.confidence })
    },
  },
]

function onImport() {
  const selected = props.task.parcels
    .map((p, idx) => (p.selected ? idx : -1))
    .filter(idx => idx >= 0)
  emit('import', props.task, selected)
}
</script>

<style scoped>
.task-body-wrapper {
  font-size: 13px;
}

.auto-import-banner {
  padding: 8px 10px;
  background: var(--info-color-suppl, rgba(32, 128, 240, 0.08));
  border-radius: 4px;
}
</style>
