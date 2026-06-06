<template>
  <div class="panel">
    <div class="panel-header">
      <div class="title-row">
        <div class="icon-box">
          <n-icon size="18" color="#6366f1"><settings-outline /></n-icon>
        </div>
        <div>
          <div class="title">冰箱配置</div>
          <div class="subtitle">调整即自动保存</div>
        </div>
      </div>
      <n-tag v-if="lastSavedAt" size="small" type="success" round>
        已保存 {{ lastSavedAt }}
      </n-tag>
    </div>

    <n-space vertical :size="20">
      <!-- 快速切换系统门型 -->
      <div class="section">
        <n-button
          type="primary"
          block
          strong
          @click="$emit('open-first-run')"
        >
          <template #icon><n-icon><sparkles-outline /></n-icon></template>
          重新选择门型
        </n-button>
        <div class="hint">随时切换为系统预置门型（自动克隆为你的副本）</div>
      </div>

      <!-- 模板切换 -->
      <div class="section">
        <div class="section-label">模板切换</div>
        <n-select
          v-model:value="selectedTemplateId"
          :options="templateOptions"
          placeholder="选择模板"
          @update:value="onTemplateChange"
        />
        <div v-if="currentTemplate" class="current-name">
          当前：<b>{{ currentTemplate.name }}</b>
          <n-tag v-if="currentTemplate.system" size="tiny" type="info" style="margin-left:6px">系统预置</n-tag>
        </div>
        <n-space :size="6" style="margin-top:8px" :wrap="true">
          <n-button size="tiny" @click="showSaveAsDialog = true">另存为新模板</n-button>
          <n-popconfirm
            v-if="canDelete"
            :positive-text="null"
            :negative-text="'取消'"
            @positive-click="onDelete"
          >
            <template #trigger>
              <n-button size="tiny" type="error" ghost :loading="deleting">删除模板</n-button>
            </template>
            确定要删除模板「{{ currentTemplate?.name }}」吗？此操作不可撤销。
          </n-popconfirm>
          <n-tooltip v-else-if="currentTemplate?.default">
            <template #trigger>
              <n-button size="tiny" type="error" ghost disabled>删除模板</n-button>
            </template>
            当前正在使用的默认模板不可删除，请先切换其他模板
          </n-tooltip>
          <n-tooltip v-else-if="currentTemplate?.system">
            <template #trigger>
              <n-button size="tiny" type="error" ghost disabled>删除模板</n-button>
            </template>
            系统预置模板不可删除
          </n-tooltip>
        </n-space>
      </div>

      <!-- 门型选择 -->
      <div class="section">
        <div class="section-label">门型</div>
        <n-radio-group v-model:value="local.layout" @update:value="emitChange">
          <n-space :size="6" wrap>
            <n-radio-button value="CLASSIC">经典单开</n-radio-button>
            <n-radio-button value="BOTTOM_FREEZER">冷冻在下</n-radio-button>
            <n-radio-button value="SIDE_BY_SIDE">对开门</n-radio-button>
            <n-radio-button value="THREE_DOOR">法式三门</n-radio-button>
          </n-space>
        </n-radio-group>
      </div>

      <!-- 层数控制 -->
      <div class="section">
        <div class="section-label">层数控制</div>
        <div class="layer-rows">
          <div class="layer-row">
            <span>🥬 冷藏</span>
            <n-input-number
              v-model:value="local.fridgeLayers"
              :min="1" :max="5"
              size="small"
              @update:value="emitChange"
            />
          </div>
          <div class="layer-row">
            <span>❄️ 冷冻</span>
            <n-input-number
              v-model:value="local.freezerLayers"
              :min="1" :max="5"
              size="small"
              @update:value="emitChange"
            />
          </div>
          <div v-if="local.layout === 'THREE_DOOR'" class="layer-row">
            <span>🧊 解冻</span>
            <n-input-number
              v-model:value="local.chillerLayers"
              :min="0" :max="3"
              size="small"
              @update:value="emitChange"
            />
          </div>
          <div class="layer-row">
            <span>🚪 门搁板</span>
            <n-input-number
              v-model:value="local.doorShelfCount"
              :min="0" :max="5"
              size="small"
              @update:value="emitChange"
            />
          </div>
        </div>
      </div>

      <!-- 临期统计 -->
      <div class="section">
        <div class="section-label">临期概览</div>
        <div class="stats">
          <div class="stat-item stat-total">
            <div class="num">{{ stats.total }}</div>
            <div class="lbl">总食材</div>
          </div>
          <div class="stat-item stat-expired">
            <div class="num">{{ stats.expired }}</div>
            <div class="lbl">已过期</div>
          </div>
          <div class="stat-item stat-soon">
            <div class="num">{{ stats.expiringSoon }}</div>
            <div class="lbl">3天内到期</div>
          </div>
          <div class="stat-item stat-fresh">
            <div class="num">{{ stats.fresh }}</div>
            <div class="lbl">新鲜</div>
          </div>
        </div>
      </div>
    </n-space>

    <!-- 另存为新模板 -->
    <n-modal
      v-model:show="showSaveAsDialog"
      preset="card"
      title="另存为新模板"
      style="width: 420px"
    >
      <n-form-item label="模板名称">
        <n-input v-model:value="newTemplateName" placeholder="如：我家冰箱" />
      </n-form-item>
      <template #action>
        <n-space justify="end">
          <n-button @click="showSaveAsDialog = false">取消</n-button>
          <n-button type="primary" :loading="savingTemplate" @click="onSaveAs">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import {
  NSpace, NIcon, NTag, NSelect, NRadioGroup, NRadioButton,
  NInputNumber, NButton, NModal, NFormItem, NInput, NPopconfirm, NTooltip, useMessage,
} from 'naive-ui'
import { SettingsOutline, SparklesOutline } from '@vicons/ionicons5'
import type { FridgeLayout, FridgeTemplateVO } from '../../types/fridge'
import { createTemplate, deleteTemplate } from '../../api/fridge'

const props = defineProps<{
  template: FridgeTemplateVO | null
  templates: FridgeTemplateVO[]
  lastSavedAt: string | null
  stats: { total: number; expired: number; expiringSoon: number; fresh: number }
}>()

const emit = defineEmits<{
  'update-template': [t: FridgeTemplateVO]
  'template-changed': [id: number]
  'templates-refresh': []
  'open-first-run': []
}>()

const message = useMessage()
const local = reactive({
  layout: 'CLASSIC' as FridgeLayout,
  fridgeLayers: 3,
  freezerLayers: 2,
  chillerLayers: 0,
  doorShelfCount: 3,
})

const selectedTemplateId = ref<number | null>(null)
const showSaveAsDialog = ref(false)
const newTemplateName = ref('')
const savingTemplate = ref(false)
const deleting = ref(false)

const canDelete = computed(() => {
  const t = props.template
  if (!t) return false
  if (!t.id || t.id === 0) return false
  if (t.system) return false
  if (t.default) return false
  return true
})

const templateOptions = computed(() =>
  props.templates.map((t) => ({
    label: `${t.name}${t.system ? '（系统）' : ''}${t.default ? ' · 当前' : ''}`,
    value: t.id,
  })),
)

const currentTemplate = computed(() => props.template)

// 同步外部模板到 local
watch(
  () => props.template,
  (t) => {
    if (t) {
      local.layout = t.layout
      local.fridgeLayers = t.fridgeLayers
      local.freezerLayers = t.freezerLayers
      local.chillerLayers = t.chillerLayers
      local.doorShelfCount = t.doorShelfCount
      selectedTemplateId.value = t.id
    }
  },
  { immediate: true },
)

let changeTimer: ReturnType<typeof setTimeout> | null = null
function emitChange() {
  if (changeTimer) clearTimeout(changeTimer)
  changeTimer = setTimeout(() => {
    if (!props.template) return
    if (props.template.system) {
      message.warning('系统预置模板不可直接修改，请先"另存为新模板"再调整')
      return
    }
    emit('update-template', {
      ...props.template,
      layout: local.layout,
      fridgeLayers: local.fridgeLayers,
      freezerLayers: local.freezerLayers,
      chillerLayers: local.chillerLayers,
      doorShelfCount: local.doorShelfCount,
    })
  }, 500)
}

async function onTemplateChange(id: number) {
  emit('template-changed', id)
}

async function onSaveAs() {
  if (!newTemplateName.value.trim()) {
    message.warning('请输入模板名称')
    return
  }
  savingTemplate.value = true
  try {
    const res = await createTemplate({
      name: newTemplateName.value.trim(),
      layout: local.layout,
      fridgeLayers: local.fridgeLayers,
      freezerLayers: local.freezerLayers,
      chillerLayers: local.chillerLayers,
      doorShelfCount: local.doorShelfCount,
    })
    if (res.data?.success) {
      message.success('模板已创建')
      showSaveAsDialog.value = false
      newTemplateName.value = ''
      emit('templates-refresh')
    } else {
      message.error(res.data?.message ?? '保存失败')
    }
  } finally {
    savingTemplate.value = false
  }
}

async function onDelete() {
  if (!props.template?.id) return
  deleting.value = true
  try {
    const res = await deleteTemplate(props.template.id)
    if (res.data?.success) {
      message.success('模板已删除')
      emit('templates-refresh')
    } else {
      message.error(res.data?.message ?? '删除失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? e?.message ?? '删除失败')
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.panel {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid #f1f5f9;
  padding: 18px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.icon-box {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #c7d2fe;
}
.title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}
.subtitle {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}
.section-label {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}
.hint {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 6px;
  line-height: 1.4;
}
.current-name {
  font-size: 12px;
  color: #475569;
  margin-top: 6px;
}
.layer-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.layer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: #334155;
  font-weight: 600;
}
.stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.stat-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 10px 12px;
  text-align: center;
}
.stat-item .num {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}
.stat-item .lbl {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}
.stat-total .num { color: #1e293b; }
.stat-expired .num { color: #dc2626; }
.stat-soon .num { color: #f59e0b; }
.stat-fresh .num { color: #16a34a; }
</style>
