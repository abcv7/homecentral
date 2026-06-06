<template>
  <n-drawer
    :show="show"
    :width="isMobile ? undefined : 420"
    :height="isMobile ? '85vh' : undefined"
    :placement="isMobile ? 'bottom' : 'right'"
    @update:show="$emit('update:show', $event)"
  >
    <n-drawer-content title="🧊 冰箱配置" closable>
      <FridgeTemplatePanel
        v-if="show"
        :template="template"
        :templates="templates"
        :last-saved-at="lastSavedAt"
        :stats="stats"
        @update-template="$emit('update-template', $event)"
        @template-changed="$emit('template-changed', $event)"
        @templates-refresh="$emit('templates-refresh')"
        @open-first-run="$emit('open-first-run')"
      />
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { NDrawer, NDrawerContent } from 'naive-ui'
import FridgeTemplatePanel from './FridgeTemplatePanel.vue'
import type { FridgeTemplateVO } from '../../types/fridge'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()

defineProps<{
  show: boolean
  template: FridgeTemplateVO | null
  templates: FridgeTemplateVO[]
  lastSavedAt: string | null
  stats: { total: number; expired: number; expiringSoon: number; fresh: number }
}>()

defineEmits<{
  'update:show': [v: boolean]
  'update-template': [t: FridgeTemplateVO]
  'template-changed': [id: number]
  'templates-refresh': []
  'open-first-run': []
}>()
</script>
