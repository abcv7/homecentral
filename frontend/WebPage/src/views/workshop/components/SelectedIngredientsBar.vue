<script setup lang="ts">
import { computed } from 'vue'
import { NButton, NEmpty, NSpace, NTag } from 'naive-ui'
import type { IngredientIndex } from '../../../types/workshop'

const props = defineProps<{
  selectedIds: Set<number>
  ingredientsById: Map<number, IngredientIndex>
}>()

const emit = defineEmits<{
  (e: 'remove', id: number): void
  (e: 'clear'): void
}>()

const chips = computed(() =>
  Array.from(props.selectedIds)
    .map((id) => ({ id, ing: props.ingredientsById.get(id) }))
    .filter((c) => c.ing)
    .map((c) => ({ id: c.id, name: c.ing!.name_zh })),
)
</script>

<template>
  <div class="selected-bar">
    <div class="bar-header">
      <span class="bar-title">已选原料</span>
      <NButton
        v-if="chips.length > 0"
        text
        type="warning"
        size="small"
        @click="emit('clear')"
      >
        清空
      </NButton>
    </div>
    <NEmpty
      v-if="chips.length === 0"
      description="尚未选择原料"
      size="small"
      style="padding: 12px 0;"
    />
    <NSpace v-else size="small" :wrap="true" :wrap-item="false">
      <NTag
        v-for="chip in chips"
        :key="chip.id"
        closable
        type="info"
        :bordered="false"
        @close="emit('remove', chip.id)"
      >
        {{ chip.name }}
      </NTag>
    </NSpace>
  </div>
</template>

<style scoped>
.selected-bar {
  padding: 12px 14px;
  background: var(--card-color, #fff);
  border-radius: 8px;
  border: 1px solid var(--border-color-1, #eee);
}
.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.bar-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-color-2, #555);
}
</style>
