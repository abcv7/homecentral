<template>
  <n-modal
    :show="show"
    preset="card"
    title="选择你的冰箱门型"
    :style="isMobile ? { width: '94vw', maxWidth: '480px' } : { width: '720px', maxWidth: '95vw' }"
    :mask-closable="true"
    :close-on-esc="true"
    @update:show="(v: boolean) => emit('update:show', v)"
  >
    <template #header-extra>
      <span class="subtitle">选好后自动保存为你的默认模板</span>
    </template>

    <div class="guide-grid">
      <div
        v-for="t in systemTemplates"
        :key="t.id"
        class="guide-card"
        :class="{ 'is-loading': activatingId === t.id }"
        @click="onSelect(t)"
      >
        <div class="guide-card-svg" v-html="layoutSvg(t.layout)" />
        <div class="guide-card-name">{{ t.name }}</div>
        <div class="guide-card-meta">
          <span v-if="t.fridgeLayers">🥬 {{ t.fridgeLayers }} 层</span>
          <span v-if="t.chillerLayers">🧊 {{ t.chillerLayers }} 层</span>
          <span v-if="t.freezerLayers">❄️ {{ t.freezerLayers }} 层</span>
          <span v-if="t.doorShelfCount">🚪 {{ t.doorShelfCount }} 门</span>
        </div>
        <div v-if="t.layout === currentLayout" class="guide-card-current">当前</div>
        <div v-if="activatingId === t.id" class="guide-card-loading">
          <n-spin size="small" />
        </div>
      </div>
    </div>

    <template #action>
      <n-space justify="end">
        <n-button @click="emit('update:show', false)">关闭</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NModal, NSpace, NButton, NSpin } from 'naive-ui'
import type { FridgeLayout, FridgeTemplateVO } from '../../types/fridge'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

const props = defineProps<{
  show: boolean
  templates: FridgeTemplateVO[]
  currentLayout?: FridgeLayout | null
  activatingId?: number | null
}>()

const emit = defineEmits<{
  'update:show': [boolean]
  select: [template: FridgeTemplateVO]
}>()

const systemTemplates = computed<FridgeTemplateVO[]>(() =>
  props.templates
    .filter((t) => t.system && t.layout !== ('CUSTOM' as FridgeLayout))
    .sort((a, b) => a.id - b.id),
)

function onSelect(t: FridgeTemplateVO) {
  emit('select', t)
}

function layoutSvg(layout: FridgeLayout): string {
  switch (layout) {
    case 'CLASSIC':
      return `<svg viewBox="0 0 80 120" xmlns="http://www.w3.org/2000/svg">
        <rect x="6" y="6" width="68" height="60" rx="6" fill="#dbeafe" stroke="#3b82f6" stroke-width="2"/>
        <rect x="6" y="68" width="68" height="46" rx="6" fill="#d1fae5" stroke="#10b981" stroke-width="2"/>
        <line x1="6" y1="66" x2="74" y2="66" stroke="#94a3b8" stroke-width="1.5"/>
        <text x="40" y="40" text-anchor="middle" font-size="9" fill="#1d4ed8" font-weight="700">❄️ 冷冻</text>
        <text x="40" y="96" text-anchor="middle" font-size="9" fill="#047857" font-weight="700">🥬 冷藏</text>
      </svg>`
    case 'BOTTOM_FREEZER':
      return `<svg viewBox="0 0 80 120" xmlns="http://www.w3.org/2000/svg">
        <rect x="6" y="6" width="68" height="80" rx="6" fill="#d1fae5" stroke="#10b981" stroke-width="2"/>
        <rect x="6" y="88" width="68" height="26" rx="6" fill="#dbeafe" stroke="#3b82f6" stroke-width="2"/>
        <line x1="6" y1="86" x2="74" y2="86" stroke="#94a3b8" stroke-width="1.5"/>
        <text x="40" y="48" text-anchor="middle" font-size="9" fill="#047857" font-weight="700">🥬 冷藏</text>
        <text x="40" y="105" text-anchor="middle" font-size="9" fill="#1d4ed8" font-weight="700">❄️ 冷冻</text>
      </svg>`
    case 'SIDE_BY_SIDE':
      return `<svg viewBox="0 0 80 120" xmlns="http://www.w3.org/2000/svg">
        <rect x="6" y="6" width="32" height="108" rx="6" fill="#d1fae5" stroke="#10b981" stroke-width="2"/>
        <rect x="42" y="6" width="32" height="108" rx="6" fill="#dbeafe" stroke="#3b82f6" stroke-width="2"/>
        <line x1="40" y1="6" x2="40" y2="114" stroke="#94a3b8" stroke-width="1.5"/>
        <text x="22" y="60" text-anchor="middle" font-size="8" fill="#047857" font-weight="700">🥬 冷藏</text>
        <text x="58" y="60" text-anchor="middle" font-size="8" fill="#1d4ed8" font-weight="700">❄️ 冷冻</text>
        <rect x="9" y="80" width="26" height="30" rx="2" fill="none" stroke="#10b981" stroke-dasharray="2 2" stroke-width="0.8"/>
        <rect x="45" y="80" width="26" height="30" rx="2" fill="none" stroke="#3b82f6" stroke-dasharray="2 2" stroke-width="0.8"/>
      </svg>`
    case 'THREE_DOOR':
      return `<svg viewBox="0 0 80 120" xmlns="http://www.w3.org/2000/svg">
        <rect x="6" y="6" width="68" height="48" rx="6" fill="#d1fae5" stroke="#10b981" stroke-width="2"/>
        <rect x="6" y="56" width="68" height="20" rx="4" fill="#ede9fe" stroke="#8b5cf6" stroke-width="2"/>
        <rect x="6" y="78" width="68" height="36" rx="6" fill="#dbeafe" stroke="#3b82f6" stroke-width="2"/>
        <line x1="6" y1="54" x2="74" y2="54" stroke="#94a3b8" stroke-width="1.5"/>
        <line x1="6" y1="76" x2="74" y2="76" stroke="#94a3b8" stroke-width="1.5"/>
        <text x="40" y="30" text-anchor="middle" font-size="8" fill="#047857" font-weight="700">🥬 冷藏</text>
        <text x="40" y="69" text-anchor="middle" font-size="7" fill="#6d28d9" font-weight="700">🧊 解冻</text>
        <text x="40" y="100" text-anchor="middle" font-size="8" fill="#1d4ed8" font-weight="700">❄️ 冷冻</text>
      </svg>`
    default:
      return `<svg viewBox="0 0 80 120" xmlns="http://www.w3.org/2000/svg">
        <rect x="6" y="6" width="68" height="108" rx="6" fill="#e2e8f0" stroke="#94a3b8" stroke-width="2"/>
      </svg>`
  }
}
</script>

<style scoped>
.subtitle {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.guide-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 8px;
}
@media (max-width: 600px) {
  .guide-grid { grid-template-columns: 1fr; }
}

.guide-card {
  position: relative;
  background: #ffffff;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-height: 220px;
  user-select: none;
}
.guide-card:hover {
  border-color: #6366f1;
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(99, 102, 241, 0.15);
}
.guide-card.is-loading {
  opacity: 0.6;
  pointer-events: none;
}
.guide-card-svg {
  width: 96px;
  height: 144px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.guide-card-svg :deep(svg) {
  width: 100%;
  height: 100%;
}
.guide-card-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  line-height: 1.3;
}
.guide-card-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px 10px;
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}
.guide-card-current {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #dcfce7;
  color: #15803d;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 999px;
  letter-spacing: 0.04em;
}
.guide-card-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
}
</style>
