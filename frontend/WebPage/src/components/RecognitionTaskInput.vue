<template>
  <n-modal
    :show="show"
    preset="card"
    title="新建识别任务"
    style="width:480px;max-width:90vw;"
    :mask-closable="!submitting"
    @update:show="(v: boolean) => emit('update:show', v)"
  >
    <n-tabs v-model:value="mode" type="segment" size="small">
      <n-tab-pane name="image" tab="图片识别">
        <n-upload
          :default-upload="false"
          :max="1"
          accept="image/*"
          :disabled="submitting"
          @change="onFileChange"
        >
          <n-upload-dragger>
            <n-icon size="48" :depth="3"><image-outline /></n-icon>
            <n-text style="font-size:14px;">点击或拖拽图片到此区域</n-text>
            <n-text depth="3" style="font-size:12px;">支持快递面单、短信截图、订单截图</n-text>
          </n-upload-dragger>
        </n-upload>
        <n-text v-if="file" depth="3" style="font-size:12px;margin-top:4px;display:block;">
          已选择：{{ file.name }}（{{ formatSize(file.size) }}）
        </n-text>
      </n-tab-pane>

      <n-tab-pane name="sms" tab="短信识别">
        <n-input
          v-model:value="smsText"
          type="textarea"
          placeholder="粘贴短信内容，例如：&#10;【妈妈驿站】取货码 1-1-0048，您有尾号0048的包裹已到雁田南园中路17号102"
          :rows="5"
          :autosize="{ minRows: 3, maxRows: 8 }"
          :disabled="submitting"
        />
      </n-tab-pane>
    </n-tabs>

    <template #footer>
      <n-space justify="end">
        <n-button :disabled="submitting" @click="onCancel">取消</n-button>
        <n-button type="primary" :disabled="!canSubmit" @click="onConfirm">
          开始识别
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { NIcon, NText, NSpace } from 'naive-ui'
import { ImageOutline } from '@vicons/ionicons5'

const props = defineProps<{
  show: boolean
  submitting?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'submit', payload: { mode: 'image'; file: File } | { mode: 'sms'; smsText: string }): void
  (e: 'cancel'): void
}>()

const mode = ref<'image' | 'sms'>('image')
const file = ref<File | null>(null)
const smsText = ref('')

const canSubmit = computed(() => {
  if (props.submitting) return false
  if (mode.value === 'image') return !!file.value
  return !!smsText.value.trim()
})

watch(
  () => props.show,
  (v) => {
    if (v) {
      mode.value = 'image'
      file.value = null
      smsText.value = ''
    }
  },
)

function onFileChange({ fileList }: { fileList: any[] }) {
  const f = fileList[fileList.length - 1]?.file
  file.value = f || null
}

function onCancel() {
  if (props.submitting) return
  emit('cancel')
  emit('update:show', false)
}

function onConfirm() {
  if (!canSubmit.value) return
  if (mode.value === 'image' && file.value) {
    emit('submit', { mode: 'image', file: file.value })
  } else if (mode.value === 'sms') {
    emit('submit', { mode: 'sms', smsText: smsText.value.trim() })
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}
</script>
