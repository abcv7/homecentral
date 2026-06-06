<template>
  <n-drawer
    :show="show"
    :height="'auto'"
    placement="bottom"
    :native-scrollbar="false"
    @update:show="(v: boolean) => $emit('update:show', v)"
  >
    <n-drawer-content :show-icon="false" :native-scrollbar="false" :closable="false">
      <div class="picker">
        <div class="sheet-handle" />
        <div class="picker-head">
          <h3>移动到...</h3>
          <n-button quaternary circle size="large" @click="$emit('update:show', false)" aria-label="关闭">
            <n-icon size="22"><close-outline /></n-icon>
          </n-button>
        </div>

        <div v-if="item" class="picker-item">
          <span class="emoji">{{ item.categoryIcon || '🍽️' }}</span>
          <span class="name">{{ item.name }}</span>
          <span class="current-loc">当前：{{ currentLabel }}</span>
        </div>

        <!-- 区域选择 -->
        <div class="zone-row">
          <button
            v-for="z in zoneOptions"
            :key="z.value ?? 'basket'"
            class="zone-pill"
            :class="{ active: selectedZone === z.value }"
            @click="selectZone(z.value)"
          >
            <span class="emoji">{{ z.emoji }}</span>
            <span>{{ z.label }}</span>
          </button>
        </div>

        <!-- 层号选择 -->
        <div v-if="selectedZone" class="layer-grid">
          <button
            v-for="(opt, idx) in layerOptions"
            :key="idx"
            class="layer-btn"
            :class="{ active: selectedLayer === opt.layer }"
            @click="selectedLayer = opt.layer"
          >
            <span class="lbl">{{ opt.label }}</span>
            <span v-if="opt.hint" class="hint">{{ opt.hint }}</span>
          </button>
        </div>

        <!-- 备注 / 提示 -->
        <p class="tip">
          选择后立即移动。如需批量操作请到桌面端。
        </p>

        <n-space :size="8" style="margin-top:8px">
          <n-button block size="large" @click="$emit('update:show', false)">取消</n-button>
          <n-button
            block
            type="primary"
            size="large"
            strong
            :disabled="selectedZone == null && selectedLayer == null"
            @click="onConfirm"
          >
            确认移动
          </n-button>
        </n-space>
      </div>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NDrawer, NDrawerContent, NButton, NIcon, NSpace } from 'naive-ui'
import { CloseOutline } from '@vicons/ionicons5'
import type { FridgeItemVO, FridgeTemplateVO, FridgeZone } from '../../types/fridge'
import { buildSubZone, resolvePosition } from '../../utils/subZoneMapping'

const props = defineProps<{
  show: boolean
  item: FridgeItemVO | null
  template: FridgeTemplateVO | null
}>()

const emit = defineEmits<{
  'update:show': [v: boolean]
  confirm: [payload: { zone: FridgeZone | null; subZone: string | null }]
}>()

const selectedZone = ref<FridgeZone | null>(null)
const selectedLayer = ref<number | null>(null)

watch(
  () => [props.show, props.item, props.template] as const,
  ([v]) => {
    if (!v || !props.item) return
    const pos = resolvePosition(props.item)
    const t = props.template
    if (pos.location === 'basket') {
      selectedZone.value = 'REFRIGERATED'
      selectedLayer.value = 0
    } else if (pos.location === 'fridge') {
      selectedZone.value = 'REFRIGERATED'
      selectedLayer.value = pos.layer
    } else if (pos.location === 'freezer') {
      selectedZone.value = 'FROZEN'
      selectedLayer.value = pos.layer
    } else if (pos.location === 'chiller') {
      selectedZone.value = null // 用 null 表达 CHILLER（types 未声明）
      selectedLayer.value = pos.layer
    } else if (pos.location === 'doorLeft' || pos.location === 'doorRight') {
      // 桌面端门搁板在移动端不开放；落到冷藏第 1 层
      selectedZone.value = 'REFRIGERATED'
      selectedLayer.value = 0
    }
    // suppress unused warning
    void t
  },
  { immediate: true },
)

const zoneOptions = computed<{ value: FridgeZone | null; label: string; emoji: string }[]>(() => {
  const t = props.template
  const opts: { value: FridgeZone | null; label: string; emoji: string }[] = [
    { value: 'REFRIGERATED', label: '冷藏', emoji: '🥬' },
    { value: 'FROZEN', label: '冷冻', emoji: '❄️' },
  ]
  if (t?.layout === 'THREE_DOOR' && (t.chillerLayers ?? 0) > 0) {
    opts.push({ value: null, label: '解冻', emoji: '🧊' })
  }
  return opts
})

const layerOptions = computed<{ layer: number; label: string; hint?: string }[]>(() => {
  const t = props.template
  if (!t) return [{ layer: 0, label: 'L1' }]
  if (selectedZone.value === 'REFRIGERATED') {
    return Array.from({ length: t.fridgeLayers }, (_, i) => ({ layer: i, label: `L${i + 1}` }))
  }
  if (selectedZone.value === 'FROZEN') {
    return Array.from({ length: t.freezerLayers }, (_, i) => ({ layer: i, label: `L${i + 1}` }))
  }
  if (selectedZone.value === null) {
    return Array.from({ length: t.chillerLayers || 1 }, (_, i) => ({ layer: i, label: `L${i + 1}` }))
  }
  return [{ layer: 0, label: 'L1' }]
})

const currentLabel = computed(() => {
  if (!props.item) return '-'
  const pos = resolvePosition(props.item)
  const labels: Record<typeof pos.location, string> = {
    basket: '采购篮',
    fridge: '冷藏',
    freezer: '冷冻',
    chiller: '解冻',
    doorLeft: '左门',
    doorRight: '右门',
  }
  return `${labels[pos.location]} L${pos.layer + 1}`
})

function selectZone(z: FridgeZone | null) {
  selectedZone.value = z
  selectedLayer.value = 0 // 切区域时默认第一层
}

function onConfirm() {
  const z = selectedZone.value
  const layer = selectedLayer.value ?? 0
  if (z === undefined || z === null) {
    // CHILLER 走 null（用 null 在 types 中表示）
    emit('confirm', { zone: null, subZone: 'CHILLER-L1' })
    return
  }
  emit('confirm', { zone: z, subZone: buildSubZone(z, layer) })
}
</script>

<style scoped>
.picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
.sheet-handle {
  width: 40px;
  height: 4px;
  background: var(--border-1);
  border-radius: 999px;
  margin: 4px auto 4px;
}
.picker-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}
.picker-head h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0;
}
.picker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--bg-hover);
  border-radius: 12px;
  font-size: 13px;
  color: var(--text-2);
}
.picker-item .emoji {
  font-size: 24px;
}
.picker-item .name {
  font-weight: 700;
  color: var(--text-1);
  flex: 1;
}
.picker-item .current-loc {
  font-size: 11px;
  color: var(--text-3);
}

.zone-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.zone-pill {
  appearance: none;
  background: var(--bg-surface);
  border: 1.5px solid var(--border-1);
  border-radius: 12px;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.15s;
}
.zone-pill .emoji {
  font-size: 22px;
}
.zone-pill.active {
  border-color: #f97316;
  background: #fff7ed;
  color: #c2410c;
}
[data-theme='dark'] .zone-pill.active {
  background: #2a1106;
  color: #fdba74;
  border-color: #ea580c;
}
.zone-pill:active {
  transform: scale(0.97);
}

.layer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
}
.layer-btn {
  appearance: none;
  background: var(--bg-surface);
  border: 1.5px solid var(--border-1);
  border-radius: 10px;
  padding: 10px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.15s;
}
.layer-btn .lbl {
  font-size: 14px;
  color: var(--text-1);
}
.layer-btn .hint {
  font-size: 10px;
  font-weight: 500;
  color: var(--text-3);
}
.layer-btn.active {
  border-color: #3b82f6;
  background: #eff6ff;
  color: #1d4ed8;
}
[data-theme='dark'] .layer-btn.active {
  background: #0c1e3e;
  color: #93c5fd;
  border-color: #3b82f6;
}
.layer-btn.active .lbl {
  color: #1d4ed8;
}
[data-theme='dark'] .layer-btn.active .lbl {
  color: #93c5fd;
}
.layer-btn:active {
  transform: scale(0.97);
}

.tip {
  font-size: 11px;
  color: var(--text-3);
  margin: 4px 4px 0;
}
</style>
