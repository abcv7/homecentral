<template>
  <n-modal
    :show="show"
    preset="card"
    :title="mode === 'edit' ? '编辑食材' : '快速新增食材'"
    :style="isMobile ? { width: '94vw', maxWidth: '420px' } : { width: '460px' }"
    :mask-closable="false"
    @update:show="(v) => $emit('update:show', v)"
  >
    <n-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      label-placement="top"
      @submit.prevent
    >
      <n-form-item label="食材名称" path="name">
        <n-input v-model:value="form.name" placeholder="如：鸡蛋" maxlength="50" />
      </n-form-item>
      <n-form-item label="数量">
        <n-input-number
          v-model:value="form.quantity"
          :min="1"
          :precision="0"
          :step="1"
          placeholder="默认 1"
          style="width: 100%"
        />
      </n-form-item>
      <n-form-item label="分类">
        <n-select
          v-model:value="form.categoryId"
          :options="categoryOptions"
          placeholder="选择分类"
          clearable
          filterable
        />
      </n-form-item>
      <n-form-item label="过期日期">
        <n-space vertical :size="6" style="width:100%">
          <n-scrollbar x-scrollable style="max-width:100%">
            <n-space :size="4" wrap>
              <n-button
                v-for="opt in expiryPresets"
                :key="opt.days"
                size="tiny"
                :type="lastPreset === opt.days ? 'primary' : 'default'"
                :ghost="lastPreset !== opt.days"
                @click="setExpiryByDays(opt.days)"
              >
                {{ opt.label }}
              </n-button>
            </n-space>
          </n-scrollbar>
          <n-date-picker
            v-model:value="form.expiryDateTs"
            type="date"
            placeholder="自定义日期"
            clearable
            style="width:100%"
            @update:value="lastPreset = null"
          />
        </n-space>
      </n-form-item>
      <template v-if="mode === 'create'">
        <n-form-item label="目标区域">
          <n-radio-group v-model:value="form.zone">
            <n-radio-button value="REFRIGERATED">冷藏</n-radio-button>
            <n-radio-button value="FROZEN">冷冻</n-radio-button>
          </n-radio-group>
          <div class="zone-hint">选择后将直接放入第 1 层；也可保存到采购篮再拖动到合适位置</div>
        </n-form-item>
        <n-form-item label="放入方式">
          <n-radio-group v-model:value="form.placement">
            <n-radio-button value="basket">入采购篮（拖拽归位）</n-radio-button>
            <n-radio-button value="direct">直接入库（顶层）</n-radio-button>
          </n-radio-group>
        </n-form-item>
      </template>
    </n-form>
    <template #action>
      <n-space justify="end">
        <n-button @click="onCancel">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="onSubmit">
          {{ mode === 'edit' ? '保存修改' : '保存' }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { reactive, ref, watch, computed } from 'vue'
import {
  NModal, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NButton, NScrollbar,
  NDatePicker, NRadioGroup, NRadioButton, useMessage,
} from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { quickCreateItem, updateItem } from '../../api/fridge'
import type { FridgeCategoryVO, FridgeItemVO, FridgeZone } from '../../types/fridge'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

interface Props {
  show: boolean
  categories: FridgeCategoryVO[]
  mode?: 'create' | 'edit'
  initial?: FridgeItemVO | null
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'create',
  initial: null,
})
const emit = defineEmits<{
  'update:show': [v: boolean]
  saved: []
}>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const form = reactive<{
  name: string
  categoryId?: number
  quantity: number
  expiryDateTs?: number | null
  zone: FridgeZone
  placement: 'basket' | 'direct'
}>({
  name: '',
  categoryId: undefined,
  quantity: 1,
  expiryDateTs: null,
  zone: 'REFRIGERATED',
  placement: 'basket',
})

const lastPreset = ref<number | null>(null)

const categoryOptions = computed(() =>
  props.categories.map((c) => ({
    label: `${c.icon ?? ''} ${c.name}`.trim(),
    value: c.id,
  })),
)

const formRules = {
  name: { required: true, message: '请输入食材名称', trigger: 'blur' },
}

function parseExpiryTs(iso?: string | null): number | null {
  if (!iso) return null
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso)
  if (!m) return null
  const ts = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3])).getTime()
  return Number.isFinite(ts) ? ts : null
}

function resetForm() {
  form.name = ''
  form.categoryId = undefined
  form.quantity = 1
  form.expiryDateTs = null
  form.zone = 'REFRIGERATED'
  form.placement = 'basket'
  lastPreset.value = null
}

function prefillFromInitial() {
  if (!props.initial) return
  const it = props.initial
  form.name = it.name ?? ''
  form.categoryId = it.categoryId
  form.quantity = it.quantity ?? 1
  form.expiryDateTs = parseExpiryTs(it.expiryDate)
  form.zone = it.zone ?? 'REFRIGERATED'
  form.placement = 'direct'
  lastPreset.value = null
}

watch(
  () => [props.show, props.mode, props.initial?.id],
  ([show]) => {
    if (!show) return
    if (props.mode === 'edit') {
      prefillFromInitial()
    } else {
      resetForm()
    }
  },
)

function pad(n: number) { return n < 10 ? `0${n}` : `${n}` }
function toIsoDate(ts: number) {
  const d = new Date(ts)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function setExpiryByDays(days: number) {
  const base = Date.now()
  const d = new Date(base)
  d.setDate(d.getDate() + days)
  form.expiryDateTs = d.getTime()
  lastPreset.value = days
}

const expiryPresets = [
  { label: '今天', days: 0 },
  { label: '+1天', days: 1 },
  { label: '+3天', days: 3 },
  { label: '+7天', days: 7 },
  { label: '+14天', days: 14 },
  { label: '+30天', days: 30 },
  { label: '+90天', days: 90 },
]

function onCancel() {
  emit('update:show', false)
}

async function onSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (props.mode === 'edit' && props.initial) {
      const payload = {
        name: form.name.trim(),
        categoryId: form.categoryId,
        quantity: form.quantity,
        purchaseDate: props.initial.purchaseDate ?? toIsoDate(Date.now()),
        expiryDate: form.expiryDateTs ? toIsoDate(form.expiryDateTs) : undefined,
        zone: props.initial.zone,
        subZone: props.initial.subZone ?? null,
      }
      const res = await updateItem(props.initial.id, payload as any)
      if (res.data?.success) {
        message.success('已保存修改')
        emit('update:show', false)
        emit('saved')
      } else {
        message.error(res.data?.message ?? '保存失败')
      }
    } else {
      const payload = {
        name: form.name.trim(),
        categoryId: form.categoryId,
        quantity: form.quantity,
        purchaseDate: toIsoDate(Date.now()),
        expiryDate: form.expiryDateTs ? toIsoDate(form.expiryDateTs) : undefined,
        zone: form.placement === 'direct' ? form.zone : null,
        subZone: form.placement === 'direct' ? `${form.zone === 'REFRIGERATED' ? 'REFRIGERATED' : 'FROZEN'}-L1` : null,
      }
      const res = await quickCreateItem(payload)
      if (res.data?.success) {
        message.success(form.placement === 'direct' ? '已直接入库' : '已加入采购篮')
        emit('update:show', false)
        emit('saved')
      } else {
        message.error(res.data?.message ?? '保存失败')
      }
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.zone-hint {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
