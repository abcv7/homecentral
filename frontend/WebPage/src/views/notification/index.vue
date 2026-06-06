<template>
  <n-space vertical :size="16">
    <n-space justify="space-between">
      <n-h2 style="margin:0">通知中心</n-h2>
      <n-button size="small" secondary type="primary" @click="handleReadAll" :disabled="unreadCount === 0">
        全部标为已读
      </n-button>
    </n-space>

    <n-spin :show="loading">
      <n-empty v-if="!loading && list.length === 0" description="暂无通知" />
      <n-list v-else>
        <n-list-item v-for="item in list" :key="item.id">
          <template #prefix>
            <n-badge v-if="!item.read" dot processing />
          </template>
          <n-thing :title="item.title" :title-extra="formatTime(item.notifyTime)">
            <template v-if="item.content">{{ item.content }}</template>
            <template #action>
              <n-button v-if="!item.read" size="tiny" quaternary @click="handleRead(item.id)">
                标为已读
              </n-button>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>
    </n-spin>
  </n-space>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listNotifications, readNotification, readAllNotifications } from '../../api/notification'
import type { NotificationVO } from '../../types/api'

const message = useMessage()
const list = ref<NotificationVO[]>([])
const loading = ref(false)

const unreadCount = computed(() => list.value.filter((n) => !n.read).length)

function formatTime(time: string) {
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function fetchList() {
  loading.value = true
  try { list.value = (await listNotifications()).data.data }
  catch { message.error('加载通知失败') }
  finally { loading.value = false }
}

async function handleRead(id: number) {
  try {
    await readNotification(id)
    const item = list.value.find((n) => n.id === id)
    if (item) item.read = true
  } catch { message.error('操作失败') }
}

async function handleReadAll() {
  try {
    await readAllNotifications()
    list.value.forEach((n) => (n.read = true))
    message.success('已全部标为已读')
  } catch { message.error('操作失败') }
}

onMounted(fetchList)
</script>
