<script setup lang="ts">
import { computed } from 'vue'
import { NTabPane, NTabs } from 'naive-ui'
import type { ResultTier, WorkshopResult } from '../../../types/workshop'
import CocktailResultCard from './CocktailResultCard.vue'

const props = defineProps<{
  groups: Record<ResultTier, WorkshopResult[]>
  ingredientsById: Map<number, { id: number; name_zh: string; name_en: string; aliases: string[]; cocktail_count: number }>
  activeTier: ResultTier
}>()

const emit = defineEmits<{ (e: 'update:activeTier', t: ResultTier): void }>()

interface Tab {
  key: ResultTier
  label: string
  count: number
  color: string
}

const tabs = computed<Tab[]>(() => [
  { key: 'full', label: '完全能做', count: props.groups.full.length, color: '#67c23a' },
  { key: 'miss-1', label: '差 1 样', count: props.groups['miss-1'].length, color: '#e6a23c' },
  { key: 'miss-2', label: '差 2 样', count: props.groups['miss-2'].length, color: '#f56c6c' },
  { key: 'miss-3+', label: '差 3+ 样', count: props.groups['miss-3+'].length, color: '#909399' },
])
</script>

<template>
  <NTabs
    :value="props.activeTier"
    type="line"
    animated
    @update:value="(v) => emit('update:activeTier', v as ResultTier)"
  >
    <NTabPane
      v-for="tab in tabs"
      :key="tab.key"
      :name="tab.key"
      :tab="`${tab.label} (${tab.count})`"
    >
      <div v-if="tab.count === 0" class="empty">该层级暂无结果</div>
      <div v-else class="grid">
        <CocktailResultCard
          v-for="r in props.groups[tab.key]"
          :key="r.cocktail.id"
          :result="r"
          :ingredients-by-id="props.ingredientsById"
        />
      </div>
    </NTabPane>
  </NTabs>
</template>

<style scoped>
.empty {
  padding: 60px 0;
  text-align: center;
  color: var(--text-color-3, #999);
  font-size: 14px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-top: 16px;
}
</style>
