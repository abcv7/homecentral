<template>
  <div v-if="visible" class="recognition-panel">
    <n-card :bordered="true" size="small" style="width:440px;box-shadow:0 4px 12px rgba(0,0,0,0.15);">
      <template #header>
        <n-space align="center" justify="space-between">
          <n-space align="center" :size="6">
            <span>快递识别</span>
            <n-tag v-if="tasks.length > 0" size="small" :type="runningCount > 0 ? 'info' : 'default'">
              {{ tasks.length }} / {{ TASK_LIMIT }}
              <template v-if="runningCount > 0">· 进行中 {{ runningCount }}</template>
            </n-tag>
          </n-space>
          <n-button text @click="close">
            <template #icon><n-icon><close-outline /></n-icon></template>
          </n-button>
        </n-space>
      </template>

      <template v-if="tasks.length === 0">
        <n-empty description="暂无任务" size="small" />
      </template>

      <template v-else>
        <!-- Task Tabs -->
        <n-tabs
          ref="tabsRef"
          :value="activeTaskId || ''"
          @update:value="(v: string) => (activeTaskId = v)"
          type="line"
          size="small"
          :tabs-padding="4"
          class="recognition-tabs"
        >
          <n-tab-pane
            v-for="t in tasks"
            :key="t.id"
            :name="t.id"
            :tab="renderTabLabel(t)"
            display-directive="show"
          />
        </n-tabs>

        <n-divider style="margin: 8px 0;" />

        <!-- Active Task Body -->
        <div v-if="activeTask" class="task-body">
          <RecognitionTaskBody
            :task="activeTask"
            :importing="importingTaskId === activeTask.id"
            @import="(t, sel) => handleImport(t, sel, false)"
            @retry="handleRetry"
            @cancel="handleCancelTask"
            @cancel-auto-import="handleCancelAutoImport"
            @remove="handleRemoveTask"
          />
        </div>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, h, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { NButton, NTag, NIcon, NSpace, NEmpty, NTabs, NTabPane, NDivider, useMessage, useDialog } from 'naive-ui'
import { CloseOutline, ImageOutline, ChatboxOutline } from '@vicons/ionicons5'
import RecognitionTaskBody from './RecognitionTaskBody.vue'
import type { RecognitionTask, TaskStatus, PendingServerTask } from './recognition-task'
import { createImageTask, createSmsTask, runTask, getAccessToken, restoreFromPending } from './recognition-task'
import { getPendingRecognitions, getRecognitionById, claimRecognition as claimRecognitionApi, discardRecognition as discardRecognitionApi } from '../api/parcel'

const emit = defineEmits<{
  (e: 'imported'): void
  (e: 'taskCountChange', payload: { running: number; total: number }): void
}>()

const message = useMessage()
const dialog = useDialog()

const visible = ref(false)
const tasks = ref<RecognitionTask[]>([])
const activeTaskId = ref<string | null>(null)
const importingTaskId = ref<string | null>(null)
const tabsRef = ref<InstanceType<typeof NTabs> | null>(null)

const TASK_LIMIT = 5
const COUNTDOWN_SECONDS = 10
const POLL_INTERVAL_MS = 3000

const activeTask = computed(() =>
  tasks.value.find(t => t.id === activeTaskId.value) || null,
)

const runningCount = computed(
  () => tasks.value.filter(t => t.status === 'progress').length,
)

watch(
  [runningCount, () => tasks.value.length],
  ([run, total]) => {
    emit('taskCountChange', { running: run, total })
  },
  { immediate: true },
)

watch(tasks, (curr) => {
  if (!visible.value) return
  for (const t of curr) {
    if (t.status === 'progress' || t._notified) continue
    if (t.status === 'result') {
      t._notified = true
      if (t.id !== activeTaskId.value) {
        const taskId = t.id
        const taskTitle = t.title
        const taskCount = t.parcels.length
        message.success(
          () =>
            h(
              'span',
              {
                style: 'cursor:pointer;text-decoration:underline;',
                onClick: () => switchToTask(taskId),
              },
              `识别完成：${taskTitle} 共 ${taskCount} 个包裹（点击查看）`,
            ),
          { duration: 5000 },
        )
      }
    } else if (t.status === 'error') {
      t._notified = true
      message.error(`识别失败：${t.title}`)
    }
  }
}, { deep: true })

watch(
  () => tasks.value.map(t => t.status),
  () => {
    nextTick(() => {
      try {
        const tabsNav = (tabsRef.value as any)?.$el?.querySelector?.('.n-tabs-nav')
        const inst = (tabsNav as any)?.__vnode?.component?.proxy
        if (inst && typeof inst.syncBarPosition === 'function') {
          inst.syncBarPosition()
        }
      } catch { /* ignore */ }
    })
  },
  { deep: true },
)

function switchToTask(id: string) {
  activeTaskId.value = id
  visible.value = true
}

function show() {
  visible.value = true
}

function close() {
  const running = tasks.value.filter(t => t.status === 'progress')
  const hasCountdown = tasks.value.some(t => t.status === 'result' && (t.countdown ?? 0) > 0)
  if (running.length > 0) {
    dialog.warning({
      title: '关闭识别面板',
      content: `还有 ${running.length} 个任务正在识别中，关闭后可在右下角气泡中查看进度。继续关闭吗？`,
      positiveText: '继续关闭',
      negativeText: '取消',
      onPositiveClick: () => {
        visible.value = false
      },
    })
    return
  }
  if (hasCountdown) {
    dialog.warning({
      title: '关闭识别面板',
      content: '有任务正在进行自动入库倒计时，关闭面板仍会继续。确定关闭吗？',
      positiveText: '继续关闭',
      negativeText: '取消',
      onPositiveClick: () => {
        visible.value = false
      },
    })
    return
  }
  visible.value = false
}

function newId(): string {
  return `task_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

async function startImageTask(file: File) {
  if (tasks.value.length >= TASK_LIMIT) {
    message.warning(`最多同时进行 ${TASK_LIMIT} 个识别任务，请先关闭已完成的任务`)
    return false
  }
  const id = newId()
  const task = createImageTask(id, file)
  tasks.value.push(task)
  activeTaskId.value = id
  visible.value = true
  await runTask(task, getAccessToken)
  if (task.status === 'result' && task.pendingId) {
    startCountdown(task)
  }
  return true
}

async function startSmsTask(smsText: string) {
  if (tasks.value.length >= TASK_LIMIT) {
    message.warning(`最多同时进行 ${TASK_LIMIT} 个识别任务，请先关闭已完成的任务`)
    return false
  }
  const id = newId()
  const task = createSmsTask(id, smsText)
  tasks.value.push(task)
  activeTaskId.value = id
  visible.value = true
  await runTask(task, getAccessToken)
  if (task.status === 'result' && task.pendingId) {
    startCountdown(task)
  }
  return true
}

function startCountdown(task: RecognitionTask) {
  if (task.countdownTimerId) {
    clearInterval(task.countdownTimerId)
  }
  task.countdown = COUNTDOWN_SECONDS
  task.countdownTimerId = window.setInterval(() => {
    if (task.countdown === undefined) return
    task.countdown -= 1
    if (task.countdown <= 0) {
      stopCountdown(task)
      void autoImport(task)
    }
  }, 1000)
}

function stopCountdown(task: RecognitionTask) {
  if (task.countdownTimerId) {
    clearInterval(task.countdownTimerId)
    task.countdownTimerId = undefined
  }
}

async function autoImport(task: RecognitionTask) {
  const selected = task.parcels
    .map((p, idx) => (p.selected ? idx : -1))
    .filter(idx => idx >= 0)
  if (selected.length === 0) {
    message.warning('没有选中任何快递，自动入库已跳过')
    return
  }
  await handleImport(task, selected, true)
}

async function handleImport(task: RecognitionTask, selected: number[], isAuto = false) {
  importingTaskId.value = task.id
  try {
    const requests = selected.map(idx => {
      const p = task.parcels[idx]
      return {
        courierCompany: p.courierCompany || '未知',
        trackingNumber: p.trackingNumber || '未知',
        pickupCode: p.pickupCode || undefined,
        ownerName: p.ownerName || undefined,
        arrivedDate: p.arrivedDate || undefined,
        productName: p.productName || undefined,
      }
    })

    const res = await fetch('/api/parcel/batch-import', {
      method: 'POST',
      body: JSON.stringify(requests),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAccessToken()}`,
      },
    })
    const data = await res.json()
    if (data.success && data.data) {
      task.importResult = {
        successCount: data.data.successCount,
        failureCount: data.data.failureCount,
        failureItems: data.data.failureItems || [],
      }
      task.status = 'imported'
      stopCountdown(task)
      task.countdown = undefined
      if (task.pendingId) {
        try { await claimRecognitionApi(task.pendingId) } catch { /* ignore */ }
      }
      emit('imported')
      if (isAuto) {
        const failed = data.data.failureCount || 0
        if (failed > 0) {
          message.warning(`已自动导入 ${data.data.successCount} 个，${failed} 个失败`)
        } else {
          message.success(`已自动导入 ${data.data.successCount} 个包裹`)
        }
      }
    } else {
      message.error(data.message || '导入失败')
    }
  } catch (err) {
    message.error('导入失败: ' + (err instanceof Error ? err.message : '未知错误'))
  } finally {
    importingTaskId.value = null
  }
}

function handleCancelAutoImport(task: RecognitionTask) {
  stopCountdown(task)
  task.countdown = undefined
  if (task.pendingId) {
    discardRecognitionApi(task.pendingId).catch(() => { /* ignore */ })
  }
  message.info(`已取消自动入库：${task.title}`)
  removeTask(task.id, 600)
}

async function handleRetry(task: RecognitionTask) {
  task.status = 'progress'
  task.progressPercent = 10
  task.progressMessage = '正在重试...'
  task.parcels = []
  task.errorMessage = ''
  task.importResult = undefined
  task._notified = false
  task.countdown = undefined
  task.controller = new AbortController()
  await runTask(task, getAccessToken)
  if ((task.status as TaskStatus) === 'result' && task.pendingId) {
    startCountdown(task)
  }
}

function handleCancelTask(task: RecognitionTask) {
  if (task.status === 'result' && (task.countdown ?? 0) > 0) {
    dialog.warning({
      title: '取消自动入库',
      content: `「${task.title}」正在自动入库倒计时，确定取消吗？取消后该结果将被丢弃。`,
      positiveText: '取消入库',
      negativeText: '继续等待',
      onPositiveClick: () => handleCancelAutoImport(task),
    })
    return
  }
  if (task.status !== 'progress') {
    if (task.pendingId) {
      discardRecognitionApi(task.pendingId).catch(() => { /* ignore */ })
    }
    removeTask(task.id)
    return
  }
  dialog.warning({
    title: '取消任务',
    content: `「${task.title}」正在识别中，确定取消吗？`,
    positiveText: '取消任务',
    negativeText: '继续识别',
    onPositiveClick: () => {
      task.controller.abort()
      task.status = 'cancelled'
      message.info(`已取消：${task.title}`)
      if (task.pendingId) {
        discardRecognitionApi(task.pendingId).catch(() => { /* ignore */ })
      }
      removeTask(task.id, 800)
    },
  })
}

function handleRemoveTask(task: RecognitionTask) {
  if (task.pendingId && (task.status === 'progress' || task.status === 'result')) {
    discardRecognitionApi(task.pendingId).catch(() => { /* ignore */ })
  }
  removeTask(task.id)
}

function removeTask(id: string, delay = 0) {
  const remove = () => {
    const idx = tasks.value.findIndex(t => t.id === id)
    if (idx < 0) return
    tasks.value.splice(idx, 1)
    if (activeTaskId.value === id) {
      activeTaskId.value = tasks.value[0]?.id || null
    }
  }
  if (delay > 0) setTimeout(remove, delay)
  else remove()
}

function renderTabLabel(t: RecognitionTask) {
  const icon = t.mode === 'image' ? ImageOutline : ChatboxOutline
  return () =>
    h(
      NSpace,
      { size: 4, align: 'center', wrap: false, style: 'max-width:140px;' },
      {
        default: () => [
          h(
            NIcon,
            { size: 12, color: statusColor(t.status) },
            { default: () => h(icon) },
          ),
          h(
            'span',
            { style: 'font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:90px;' },
            shortTitle(t),
          ),
          h(
            NButton,
            {
              text: true,
              size: 'tiny',
              style: 'padding:0 2px;height:14px;line-height:1;',
              onClick: (e: MouseEvent) => {
                e.stopPropagation()
                handleCancelTask(t)
              },
            },
            { default: () => h(NIcon, { size: 10 }, { default: () => h(CloseOutline) }) },
          ),
        ],
      },
    )
}

function shortTitle(t: RecognitionTask): string {
  if (t.mode === 'image') {
    const n = t.title.replace(/^图片/, '')
    return `图${n}`
  }
  const n = t.title.replace(/^短信/, '')
  return `短信${n}`
}

function statusColor(s: TaskStatus): string {
  switch (s) {
    case 'progress': return '#2080f0'
    case 'result': return '#18a058'
    case 'autoImporting': return '#f0a020'
    case 'imported': return '#18a058'
    case 'error': return '#d03050'
    case 'cancelled': return '#909399'
    default: return '#909399'
  }
}

async function recoverPending() {
  try {
    const res = await getPendingRecognitions()
    if (!res.data?.success) return
    const list: PendingServerTask[] = res.data.data || []
    if (list.length === 0) return
    const restored: RecognitionTask[] = []
    const needPolling: PendingServerTask[] = []
    for (const p of list) {
      const task = restoreFromPending(p)
      if (task) {
        restored.push(task)
        if (p.status === 'processing') {
          needPolling.push(p)
        } else if (p.status === 'completed' && task.status === 'result') {
          startCountdown(task)
        }
      }
    }
    if (restored.length === 0) return
    const existing = new Set(tasks.value.map(t => t.pendingId))
    for (const t of restored) {
      if (t.pendingId && existing.has(t.pendingId)) continue
      tasks.value.push(t)
    }
    if (!activeTaskId.value && tasks.value.length > 0) {
      activeTaskId.value = tasks.value[0].id
    }
    if (needPolling.length > 0) {
      startPolling(needPolling)
    }
    if (restored.length > 0) {
      message.info(`已恢复 ${restored.length} 个待导入识别任务`)
    }
  } catch (err) {
    console.warn('recoverPending failed', err)
  }
}

const pollers = new Map<number, number>()

function startPolling(pendings: PendingServerTask[]) {
  for (const p of pendings) {
    if (pollers.has(p.id)) continue
    const id = window.setInterval(async () => {
      try {
        const res = await getRecognitionById(p.id)
        if (!res.data?.success) return
        const updated: PendingServerTask | null = res.data.data
        if (!updated) {
          stopPolling(p.id)
          return
        }
        const task = tasks.value.find(t => t.pendingId === updated.id)
        if (!task) {
          stopPolling(p.id)
          return
        }
        if (updated.status === 'completed' && task.status === 'progress') {
          const replaced = restoreFromPending(updated)
          if (replaced) {
            Object.assign(task, replaced, { controller: task.controller })
            if ((task.status as TaskStatus) === 'result' && task.pendingId) {
              startCountdown(task)
              stopPolling(p.id)
            }
          }
        } else if (updated.status === 'failed' && task.status === 'progress') {
          task.status = 'error'
          task.errorMessage = updated.failureMessage || 'AI 识别失败'
          task.progressPercent = 100
          stopPolling(p.id)
        } else if (updated.status === 'cancelled' || updated.status === 'imported') {
          stopPolling(p.id)
          removeTask(task.id)
        }
      } catch {
        /* keep polling */
      }
    }, POLL_INTERVAL_MS)
    pollers.set(p.id, id)
  }
}

function stopPolling(pendingId: number) {
  const id = pollers.get(pendingId)
  if (id) {
    clearInterval(id)
    pollers.delete(pendingId)
  }
}

function keepaliveImportAll() {
  for (const task of tasks.value) {
    if (task.status !== 'result') continue
    if (!task.pendingId) continue
    if (task.countdownTimerId) {
      clearInterval(task.countdownTimerId)
      task.countdownTimerId = undefined
    }
    const requests = task.parcels.filter(p => p.selected).map(p => ({
      courierCompany: p.courierCompany || '未知',
      trackingNumber: p.trackingNumber || '未知',
      pickupCode: p.pickupCode || undefined,
      ownerName: p.ownerName || undefined,
      arrivedDate: p.arrivedDate || undefined,
      productName: p.productName || undefined,
    }))
    if (requests.length === 0) continue
    try {
      fetch('/api/parcel/batch-import', {
        method: 'POST',
        body: JSON.stringify(requests),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getAccessToken()}`,
        },
        keepalive: true,
      })
      fetch(`/api/parcel/recognition/${task.pendingId}/claim`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getAccessToken()}`,
        },
        keepalive: true,
      })
    } catch {
      /* best-effort, server TTL will eventually import */
    }
  }
}

function onPageHide() {
  keepaliveImportAll()
}

onMounted(() => {
  void recoverPending()
  window.addEventListener('pagehide', onPageHide)
})

onBeforeUnmount(() => {
  window.removeEventListener('pagehide', onPageHide)
  for (const t of tasks.value) {
    if (t.status === 'progress') {
      try { t.controller.abort() } catch { /* ignore */ }
    }
    if (t.countdownTimerId) {
      clearInterval(t.countdownTimerId)
    }
  }
  for (const id of pollers.values()) {
    clearInterval(id)
  }
  pollers.clear()
})

defineExpose({
  startImageTask,
  startSmsTask,
  show,
  isVisible: () => visible.value,
  taskCount: () => tasks.value.length,
  runningCount: () => runningCount.value,
  hasBackgroundTasks: () => tasks.value.length > 0,
})
</script>

<style scoped>
.recognition-panel {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  animation: slideIn 0.3s ease-out;
}

.recognition-tabs :deep(.n-tabs-nav) {
  overflow-x: auto;
  overflow-y: hidden;
}
.recognition-tabs :deep(.n-tabs-nav-scroll-content) {
  min-width: max-content;
}
.recognition-tabs :deep(.n-tabs-tab) {
  padding: 4px 8px;
}

.task-body {
  min-height: 180px;
  max-height: 60vh;
  overflow-y: auto;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
