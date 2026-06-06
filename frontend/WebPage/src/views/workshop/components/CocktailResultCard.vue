<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NImage, NSpace, NTag, NText } from 'naive-ui'
import type { IngredientIndex, WorkshopResult } from '../../../types/workshop'
import CocktailDetailModal from './CocktailDetailModal.vue'
import { ref } from 'vue'

const props = defineProps<{
  result: WorkshopResult
  ingredientsById: Map<number, IngredientIndex>
}>()

const modalShow = ref(false)

const cover = computed(() => props.result.cocktail.cover)
const name = computed(() => props.result.cocktail.name_zh)
const nameEn = computed(() => props.result.cocktail.name_en)

const missingNames = computed(() =>
  props.result.missing.map((id: number) => props.ingredientsById.get(id)?.name_zh || `#${id}`),
)

const ratioPct = computed(() => Math.round(props.result.ratio * 100))
const ratioColor = computed(() => {
  const r = props.result.ratio
  if (r === 1) return '#67c23a'
  if (r >= 0.66) return '#e6a23c'
  if (r >= 0.5) return '#f56c6c'
  return '#909399'
})

function openDetail() {
  modalShow.value = true
}
</script>

<template>
  <NCard
    hoverable
    class="card"
    :bordered="true"
    content-style="padding: 0;"
    @click="openDetail"
  >
    <div class="cover-wrap">
      <NImage
        v-if="cover"
        :src="cover"
        object-fit="cover"
        preview-disabled
        :img-props="{ loading: 'lazy', style: 'width:100%;height:100%;object-fit:cover;' }"
        style="width: 100%; height: 100%;"
      />
      <div v-else class="cover-fallback">🍸</div>
      <div class="ratio-badge" :style="{ background: ratioColor }">
        {{ ratioPct }}%
      </div>
    </div>
    <div class="body">
      <div class="title">{{ name }}</div>
      <div v-if="nameEn" class="title-en">{{ nameEn }}</div>
      <NSpace v-if="missingNames.length > 0" :size="4" :wrap="true" :wrap-item="false" style="margin-top: 8px;">
        <NText depth="3" style="font-size: 12px;">缺:</NText>
        <NTag
          v-for="m in missingNames"
          :key="m"
          size="small"
          type="warning"
          :bordered="false"
        >
          {{ m }}
        </NTag>
      </NSpace>
      <NSpace v-else :size="4" :wrap="true" :wrap-item="false" style="margin-top: 8px;">
        <NText style="font-size: 12px; color: #67c23a;">✓ 原料齐全</NText>
      </NSpace>
    </div>
    <CocktailDetailModal
      v-model:show="modalShow"
      :cocktail="result.cocktail"
      :ingredients-by-id="ingredientsById"
    />
  </NCard>
</template>

<style scoped>
.card {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  overflow: hidden;
}
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}
.cover-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f5f5f5;
  overflow: hidden;
}
.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
}
.ratio-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 12px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}
.body {
  padding: 12px 14px 14px;
}
.title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
}
.title-en {
  font-size: 12px;
  color: var(--text-color-3, #999);
  margin-top: 2px;
}
</style>
