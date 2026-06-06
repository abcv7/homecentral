<script setup lang="ts">
import { computed, ref } from 'vue'
import { NAutoComplete, NIcon, type AutoCompleteOption } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import type { IngredientIndex } from '../../../types/workshop'

const props = defineProps<{
  ingredients: IngredientIndex[]
  selectedIds: Set<number>
}>()

const emit = defineEmits<{
  (e: 'pick', id: number): void
}>()

const keyword = ref('')
const norm = (s: string) => s.replace(/\s+/g, '').toLowerCase()

interface Match extends AutoCompleteOption {
  id: number
  name_zh: string
  name_en: string
  cocktail_count: number
}

const matches = computed<Match[]>(() => {
  const k = norm(keyword.value.trim())
  if (!k) return []
  const out: Match[] = []
  for (const ing of props.ingredients) {
    if (props.selectedIds.has(ing.id)) continue
    const hay = norm(ing.name_zh) + '|' + norm(ing.name_en) + '|' + ing.aliases.join('|')
    if (hay.includes(k)) {
      out.push({
        id: ing.id,
        label: `${ing.name_zh}${ing.name_en ? ' / ' + ing.name_en : ''}（${ing.cocktail_count} 款）`,
        name_zh: ing.name_zh,
        name_en: ing.name_en,
        cocktail_count: ing.cocktail_count,
      })
      if (out.length >= 30) break
    }
  }
  return out.sort((a, b) => b.cocktail_count - a.cocktail_count)
})

function handleSelect(m: Match | string | null) {
  if (!m || typeof m === 'string') return
  emit('pick', m.id)
  keyword.value = ''
}
</script>

<template>
  <NAutoComplete
    v-model:value="keyword"
    :options="matches"
    placeholder="搜索原料,如 伏特加 / Gin / 糖"
    clearable
    @select="handleSelect"
  >
    <template #prefix>
      <NIcon><SearchOutline /></NIcon>
    </template>
  </NAutoComplete>
</template>

<style scoped>
:deep(.n-input) {
  --n-height: 42px;
}
</style>
