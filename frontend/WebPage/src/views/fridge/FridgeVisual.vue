<template>
  <div class="visual-wrapper" :class="{ 'is-mobile': isMobile }">
    <div class="visual-stage" :style="stageStyle">
      <!-- 冰箱外壳阴影 -->
      <div class="fridge-shadow" />

      <!-- 冰箱门（侧开） -->
      <template v-if="showLeftDoor">
        <div class="fridge-door fridge-door--left" :style="{ transform: 'rotateY(-30deg)' }">
          <div class="door-inner">
            <div
              v-for="i in safeTemplate.doorShelfCount"
              :key="`door-left-${i}`"
              class="door-shelf"
              @dragover.prevent="onDragOverDoor($event, 'LEFT', i - 1)"
              @drop.prevent="onDropDoor($event, 'LEFT', i - 1)"
            >
              <div v-if="!leftDoorItems[i - 1]?.length" class="door-shelf-hint">左门 L{{ i }}</div>
              <div class="door-shelf-chips">
                <div
                  v-for="(it, idx) in leftDoorItems[i - 1] || []"
                  :key="`${it.id}-${idx}`"
                  class="chip"
                  :class="expiryClass(it.daysToExpiry)"
                  draggable="true"
                  @dragstart="onDragStart($event, it.id)"
                  @dragend="$emit('drag-end')"
                  @click="$emit('item-click', it)"
                >
                  <span class="emoji">{{ it.categoryIcon || '🍽️' }}</span>
                  <span class="name">{{ it.name }}</span>
                  <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-if="showRightDoor">
        <div class="fridge-door fridge-door--right" :style="{ transform: 'rotateY(30deg)' }">
          <div class="door-inner">
            <template v-if="safeTemplate.doorShelfCount">
              <div
                v-for="i in safeTemplate.doorShelfCount"
                :key="`door-right-${i}`"
                class="door-shelf"
                @dragover.prevent="onDragOverDoor($event, 'RIGHT', i - 1)"
                @drop.prevent="onDropDoor($event, 'RIGHT', i - 1)"
              >
                <div v-if="!rightDoorItems[i - 1]?.length" class="door-shelf-hint">右门 L{{ i }}</div>
                <div class="door-shelf-chips">
                  <div
                    v-for="(it, idx) in rightDoorItems[i - 1] || []"
                    :key="`${it.id}-${idx}`"
                    class="chip"
                    :class="expiryClass(it.daysToExpiry)"
                    draggable="true"
                    @dragstart="onDragStart($event, it.id)"
                    @dragend="$emit('drag-end')"
                    @click="$emit('item-click', it)"
                  >
                    <span class="emoji">{{ it.categoryIcon || '🍽️' }}</span>
                    <span class="name">{{ it.name }}</span>
                    <span v-if="(it.quantity ?? 1) > 1" class="qty">×{{ it.quantity }}</span>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="door-empty-hint">{{ rightDoorLabel }}</div>
            </template>
          </div>
        </div>
      </template>

      <!-- 冰箱主体 -->
      <div class="fridge-body" :class="`fridge-body--${safeTemplate.layout.toLowerCase()}`">
        <div class="fridge-body-inner">
          <component :is="layoutComponent" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, defineComponent, type PropType, type CSSProperties } from 'vue'
import type { FridgeItemVO, FridgeTemplateVO, FridgeZone } from '../../types/fridge'
import { buildSubZone, buildDoorSubZone, groupItems, type GroupedItems } from '../../utils/subZoneMapping'
import { expandByQuantity } from '../../utils/fridgeExpand'

interface DropPayload {
  itemId: number
  zone: FridgeZone | null
  subZone: string | null
}

const props = defineProps({
  template: {
    type: Object as PropType<FridgeTemplateVO | null>,
    default: null,
  },
  items: {
    type: Array as PropType<FridgeItemVO[]>,
    required: true,
  },
  isMobile: {
    type: Boolean,
    default: false,
  },
})

// 兜底：null 时使用经典单开
const safeTemplate = computed<FridgeTemplateVO>(() => props.template ?? {
  id: 0,
  name: '默认',
  layout: 'CLASSIC',
  fridgeLayers: 3,
  freezerLayers: 2,
  chillerLayers: 0,
  doorShelfCount: 3,
  default: false,
  system: false,
})

const emit = defineEmits<{
  drop: [payload: DropPayload]
  'drag-start': [id: number]
  'drag-end': []
  'item-click': [item: FridgeItemVO]
}>()

const stageStyle = computed<CSSProperties>(() =>
  props.isMobile ? {} : { perspective: '2200px' },
)

const showLeftDoor = computed(() => safeTemplate.value.layout === 'SIDE_BY_SIDE' || safeTemplate.value.layout === 'THREE_DOOR')
// 右门恒显示：单开门（CLASSIC / BOTTOM_FREEZER）复用对开门的右门视觉
const showRightDoor = computed(() => true)
// 单开门无门搁板时，右门显示提示
const rightDoorLabel = computed(() => {
  if (safeTemplate.value.layout === 'CLASSIC' || safeTemplate.value.layout === 'BOTTOM_FREEZER') {
    return '冷藏门'
  }
  return '右门'
})

const grouped = computed<GroupedItems>(() => groupItems(props.items))

const leftDoorItems = computed(() => {
  const layers = Object.values(grouped.value.doorLeft || {})
  return layers.map((layer) => expandByQuantity(layer || []))
})
const rightDoorItems = computed(() => {
  const layers = Object.values(grouped.value.doorRight || {})
  return layers.map((layer) => expandByQuantity(layer || []))
})

function onDragStart(e: DragEvent, id: number) {
  if (!e.dataTransfer) return
  e.dataTransfer.setData('text/plain', String(id))
  e.dataTransfer.effectAllowed = 'move'
  emit('drag-start', id)
}

function onDragOverDoor(_e: DragEvent, _side: 'LEFT' | 'RIGHT', _layer: number) {
  // 占位：让 drop 事件触发（preventDefault on dragover）
}

function onDropDoor(e: DragEvent, side: 'LEFT' | 'RIGHT', layer: number) {
  const id = parseInt(e.dataTransfer?.getData('text/plain') ?? '0', 10)
  if (!id) return
  const sub = buildDoorSubZone(side, layer)
  // 门搁板的 zone 推断：
  // - 对开门：左门=FROZEN, 右门=REFRIGERATED
  // - 法式三门：两侧门都属于冷藏区
  let zone: FridgeZone | null = null
  if (safeTemplate.value.layout === 'SIDE_BY_SIDE') {
    zone = side === 'LEFT' ? 'FROZEN' : 'REFRIGERATED'
  } else if (safeTemplate.value.layout === 'THREE_DOOR') {
    zone = 'REFRIGERATED'
  }
  emit('drop', { itemId: id, zone, subZone: sub })
}

function onDropLayer(zone: FridgeZone, layer: number) {
  return (e: DragEvent) => {
    const id = parseInt(e.dataTransfer?.getData('text/plain') ?? '0', 10)
    if (!id) return
    const sub = buildSubZone(zone, layer)
    emit('drop', { itemId: id, zone, subZone: sub })
  }
}

function renderShelf(zone: FridgeZone, layer: number) {
  const raw: FridgeItemVO[] = (grouped.value[zoneKey(zone) as 'fridge' | 'freezer' | 'chiller'] || {})[layer] || []
  const items = expandByQuantity(raw)
  return h(
    'div',
    {
      class: 'shelf',
      onDragover: (e: DragEvent) => e.preventDefault(),
      onDrop: onDropLayer(zone, layer),
    },
    [
      h('div', { class: 'shelf-glass' }),
      h('div', { class: 'shelf-level' }, `Level ${layer + 1}`),
      h(
        'div',
        { class: 'shelf-content' },
        items.length === 0
          ? [h('div', { class: 'shelf-empty' }, '放至此处')]
          : items.map((it, idx) =>
              h(
                'div',
                {
                  key: `${it.id}-${idx}`,
                  class: ['chip', expiryClass(it.daysToExpiry)],
                  draggable: true,
                  onDragstart: (e: DragEvent) => onDragStart(e, it.id),
                  onDragend: () => emit('drag-end'),
                  onClick: () => emit('item-click', it),
                },
                [
                  h('span', { class: 'emoji' }, it.categoryIcon || '🍽️'),
                  h('span', { class: 'name' }, it.name),
                  (it.quantity ?? 1) > 1 ? h('span', { class: 'qty' }, `×${it.quantity}`) : null,
                ],
              ),
            ),
      ),
    ],
  )
}

function zoneKey(zone: FridgeZone): 'fridge' | 'freezer' | 'chiller' {
  return zone === 'REFRIGERATED' ? 'fridge' : 'freezer'
}

function renderZone(zone: FridgeZone, count: number) {
  const k = zoneKey(zone)
  const layers = Array.from({ length: count }, (_, i) => renderShelf(zone, i))
  const cls = ['zone', `zone--${k}`]
  return h('div', { class: cls, style: { flexGrow: count || 1 } }, [renderZoneHeader(k, count), ...layers])
}

function renderZoneHeader(zone: 'fridge' | 'freezer' | 'chiller', count: number) {
  const meta = {
    fridge:  { emoji: '🥬', name: '冷藏区' },
    freezer: { emoji: '❄️', name: '冷冻区' },
    chiller: { emoji: '🧊', name: '解冻区' },
  }[zone]
  return h('div', { class: 'zone-header' }, [
    h('span', { class: 'zh-emoji' }, meta.emoji),
    h('span', { class: 'zh-name' }, meta.name),
    h('span', { class: 'zh-count' }, `${count} 层`),
  ])
}

function renderDivider(axis: 'x' | 'y' = 'x') {
  return h('div', { class: `divider divider--${axis}` })
}

// 4 种门型
const ZoneClassic = defineComponent({
  setup() {
    return () => h('div', { class: 'layout-classic' }, [
      renderZone('FROZEN', safeTemplate.value.freezerLayers),
      renderDivider('x'),
      renderZone('REFRIGERATED', safeTemplate.value.fridgeLayers),
    ])
  },
})

const ZoneBottomFreezer = defineComponent({
  setup() {
    return () => h('div', { class: 'layout-bottom-freezer' }, [
      renderZone('REFRIGERATED', safeTemplate.value.fridgeLayers),
      renderDivider('x'),
      renderZone('FROZEN', safeTemplate.value.freezerLayers),
    ])
  },
})

const ZoneSideBySide = defineComponent({
  setup() {
    return () => h('div', { class: 'layout-side-by-side' }, [
      renderZone('FROZEN', safeTemplate.value.freezerLayers),
      renderDivider('y'),
      renderZone('REFRIGERATED', safeTemplate.value.fridgeLayers),
    ])
  },
})

const ZoneThreeDoor = defineComponent({
  setup() {
    return () => h('div', { class: 'layout-three-door' }, [
      renderZone('REFRIGERATED', safeTemplate.value.fridgeLayers),
      renderDivider('x'),
      renderZone('CHILLER' as FridgeZone, safeTemplate.value.chillerLayers || 1),
      renderDivider('x'),
      renderZone('FROZEN', safeTemplate.value.freezerLayers),
    ])
  },
})

const layoutComponent = computed(() => {
  switch (safeTemplate.value.layout) {
    case 'BOTTOM_FREEZER': return ZoneBottomFreezer
    case 'SIDE_BY_SIDE': return ZoneSideBySide
    case 'THREE_DOOR': return ZoneThreeDoor
    default: return ZoneClassic
  }
})

function expiryClass(days?: number) {
  if (days == null) return ''
  if (days < 0) return 'chip--expired'
  if (days <= 3) return 'chip--red'
  if (days <= 7) return 'chip--yellow'
  return 'chip--green'
}
</script>

<style>
.visual-wrapper {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 8px;
}
.visual-stage {
  position: relative;
  width: 100%;
  max-width: 560px;
  transform-style: preserve-3d;
}

/* 移动端：禁用 3D，所有门用普通堆叠 */
.visual-wrapper.is-mobile .visual-stage {
  transform: none !important;
}
.visual-wrapper.is-mobile .fridge-door {
  display: none;
}
.visual-wrapper.is-mobile .fridge-body {
  width: 100% !important;
  margin: 0 !important;
}

.fridge-shadow {
  position: absolute;
  bottom: -24px;
  left: 5%;
  width: 90%;
  height: 24px;
  background: rgba(15, 23, 42, 0.25);
  filter: blur(16px);
  border-radius: 50%;
  z-index: -1;
  pointer-events: none;
}

.fridge-body {
  position: relative;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1 40%, #94a3b8);
  border-radius: 28px;
  padding: 14px;
  box-shadow:
    0 30px 60px rgba(15, 23, 42, 0.25),
    inset 0 2px 4px rgba(255, 255, 255, 0.8);
  border: 4px solid #94a3b8;
  z-index: 5;
}
.fridge-body--classic,
.fridge-body--bottom_freezer,
.fridge-body--three_door {
  max-width: 480px;
}
.fridge-body--side_by_side {
  max-width: 100%;
}
.fridge-body-inner {
  background: #f8fafc;
  border-radius: 20px;
  box-shadow: inset 0 8px 24px rgba(15, 23, 42, 0.08);
  overflow: hidden;
  padding: 12px;
  min-height: 720px;
  display: flex;
}

.fridge-door {
  position: absolute;
  top: 0;
  width: 40%;
  height: 100%;
  background: linear-gradient(to right, #cbd5e1, #e2e8f0);
  border: 10px solid #94a3b8;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.1), 30px 15px 40px rgba(0, 0, 0, 0.2);
  z-index: 10;
  transform-style: preserve-3d;
  transition: transform 0.6s ease;
}
.fridge-door--left {
  right: 100%;
  border-right-width: 10px;
  border-left-width: 0;
  border-radius: 28px 0 0 28px;
  transform-origin: right center;
  background: linear-gradient(to left, #cbd5e1, #e2e8f0);
}
.fridge-door--right {
  left: 100%;
  border-left-width: 10px;
  border-right-width: 0;
  border-radius: 0 28px 28px 0;
  transform-origin: left center;
}
.door-inner {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  gap: 8px;
  justify-content: space-around;
}
.door-shelf {
  width: 90%;
  margin: 0 auto;
  min-height: 50px;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.3));
  border-radius: 10px;
  border-top: 3px solid rgba(255, 255, 255, 0.9);
  border-left: 1px solid rgba(148, 163, 184, 0.5);
  border-right: 1px solid rgba(148, 163, 184, 0.5);
  border-bottom: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: flex-end;
  padding: 6px;
  position: relative;
}
.door-shelf-hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: rgba(71, 85, 105, 0.4);
  font-weight: 700;
  pointer-events: none;
}
.door-shelf-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  width: 100%;
  align-items: flex-end;
}

.door-empty-hint {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: rgba(71, 85, 105, 0.55);
  font-weight: 700;
  letter-spacing: 0.5px;
}

.layout-classic,
.layout-bottom-freezer,
.layout-three-door {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
  flex: 1;
}

.layout-side-by-side {
  display: flex;
  flex-direction: row;
  gap: 6px;
  width: 100%;
  flex: 1;
}

.zone {
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  border-radius: 18px;
  padding: 0;
  position: relative;
  overflow: hidden;
  border: 2px solid;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
}
.zone--fridge {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  border-color: #10b981;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.15);
}
.zone--freezer {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.15);
}
.zone--chiller {
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  border-color: #8b5cf6;
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.15);
}

.zone-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  font-weight: 800;
  letter-spacing: 0.04em;
  background: rgba(255, 255, 255, 0.55);
  border-bottom: 1px solid rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}
.zone--fridge .zone-header { color: #047857; }
.zone--freezer .zone-header { color: #1d4ed8; }
.zone--chiller .zone-header { color: #6d28d9; }
.zh-emoji { font-size: 14px; line-height: 1; }
.zh-name  {
  text-transform: uppercase;
  font-size: 11px;
  font-weight: 800;
}
.zh-count {
  margin-left: auto;
  opacity: 0.7;
  font-size: 10px;
  font-weight: 600;
}

.shelf {
  position: relative;
  min-height: 60px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 4px 8px 12px;
  transition: background 0.2s;
}
.shelf:hover {
  background: rgba(255, 255, 255, 0.25);
}
.shelf-glass {
  position: absolute;
  bottom: 0;
  left: 8px;
  right: 8px;
  height: 12px;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.2));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 0 0 12px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 6px 12px -4px rgba(15, 23, 42, 0.15);
  pointer-events: none;
}
.shelf-level {
  position: absolute;
  top: 4px;
  right: 8px;
  font-size: 9px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.4);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  pointer-events: none;
}
.shelf-content {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 6px;
  min-height: 40px;
  padding: 0 4px;
}
.shelf-empty {
  width: 100%;
  text-align: center;
  font-size: 11px;
  color: rgba(15, 23, 42, 0.35);
  font-weight: 700;
  padding: 6px 0;
}

.divider {
  background: linear-gradient(to bottom, #cbd5e1, #f1f5f9, #cbd5e1);
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1), inset 0 1px 2px rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.5);
  position: relative;
  z-index: 4;
}
.divider--x {
  height: 6px;
  width: 100%;
  margin: 2px 0;
}
.divider--y {
  width: 6px;
  height: auto;
  margin: 0 2px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #1e293b;
  cursor: grab;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.15s, box-shadow 0.15s;
}
.chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}
.chip:active {
  cursor: grabbing;
}
.chip .emoji {
  font-size: 14px;
}
.chip .qty {
  font-size: 10px;
  font-weight: 800;
  padding: 1px 5px;
  border-radius: 6px;
  background: #fef3c7;
  color: #b45309;
  margin-left: 2px;
}
.chip--expired,
.chip--red {
  border-color: rgba(220, 38, 38, 0.4);
  background: #fff1f2;
  color: #991b1b;
}
.chip--yellow {
  border-color: rgba(245, 158, 11, 0.4);
  background: #fffbeb;
  color: #92400e;
}
.chip--green {
  border-color: rgba(22, 163, 74, 0.4);
  background: #f0fdf4;
  color: #166534;
}

/* 移动端：chip 缩小 */
.visual-wrapper.is-mobile .chip {
  font-size: 10px;
  padding: 3px 6px;
}
.visual-wrapper.is-mobile .shelf {
  min-height: 48px;
}
.visual-wrapper.is-mobile .zone {
  border-width: 1px;
  border-radius: 14px;
}
.visual-wrapper.is-mobile .zone-header {
  padding: 4px 8px;
  font-size: 10px;
}
.visual-wrapper.is-mobile .zh-emoji { font-size: 12px; }
.visual-wrapper.is-mobile .zh-name { font-size: 9px; }
.visual-wrapper.is-mobile .zh-count { font-size: 9px; }
</style>
