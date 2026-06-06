<script setup lang="ts">
import { computed } from 'vue'
import { NDescriptions, NDescriptionsItem, NImage, NModal, NSpace, NTag, NText } from 'naive-ui'
import type { CocktailIndex, IngredientIndex } from '../../../types/workshop'

const props = defineProps<{
  show: boolean
  cocktail: CocktailIndex
  ingredientsById: Map<number, IngredientIndex>
}>()
const emit = defineEmits<{ (e: 'update:show', v: boolean): void }>()

const allNames = computed(() =>
  props.cocktail.ingredient_ids.map(
    (id: number) => props.ingredientsById.get(id)?.name_zh || `#${id}`,
  ),
)
const mainNames = computed(() =>
  props.cocktail.main_ingredient_ids.map(
    (id: number) => props.ingredientsById.get(id)?.name_zh || `#${id}`,
  ),
)
</script>

<template>
  <NModal
    :show="show"
    preset="card"
    style="width: 720px; max-width: 92vw;"
    :title="cocktail.name_zh + (cocktail.name_en ? ' / ' + cocktail.name_en : '')"
    :bordered="false"
    size="huge"
    @update:show="(v) => emit('update:show', v)"
  >
    <div class="detail-root">
      <div class="cover">
        <NImage
          v-if="cocktail.cover"
          :src="cocktail.cover"
          object-fit="cover"
          preview-disabled
          style="width: 100%; aspect-ratio: 4/3; border-radius: 8px;"
        />
        <div v-else class="cover-fallback">🍸</div>
      </div>

      <NDescriptions
        :column="1"
        size="small"
        bordered
        label-placement="top"
      >
        <NDescriptionsItem label="主料">
          <NSpace v-if="mainNames.length > 0" :size="4" :wrap="true" :wrap-item="false">
            <NTag v-for="n in mainNames" :key="n" type="success" :bordered="false" size="small">{{ n }}</NTag>
          </NSpace>
          <NText v-else depth="3">—</NText>
        </NDescriptionsItem>
        <NDescriptionsItem label="全部原料">
          <NSpace v-if="allNames.length > 0" :size="4" :wrap="true" :wrap-item="false">
            <NTag v-for="n in allNames" :key="n" :bordered="false" size="small">{{ n }}</NTag>
          </NSpace>
          <NText v-else depth="3">—</NText>
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.recipe_zh" label="配方">
          <span style="white-space: pre-wrap;">{{ cocktail.recipe_zh }}</span>
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.method_zh" label="调制方法">
          <span style="white-space: pre-wrap;">{{ cocktail.method_zh }}</span>
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.aroma" label="香气">
          {{ cocktail.aroma }}
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.taste" label="口感">
          {{ cocktail.taste }}
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.style" label="风格">
          {{ cocktail.style }}
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.scene" label="场合">
          {{ cocktail.scene }}
        </NDescriptionsItem>
        <NDescriptionsItem v-if="cocktail.history" label="故事">
          <span style="white-space: pre-wrap;">{{ cocktail.history }}</span>
        </NDescriptionsItem>
      </NDescriptions>
    </div>
  </NModal>
</template>

<style scoped>
.detail-root {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.cover-fallback {
  width: 100%;
  aspect-ratio: 4/3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 96px;
  background: #f5f5f5;
  border-radius: 8px;
}
</style>
