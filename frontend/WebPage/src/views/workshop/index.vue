<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { NAlert, NEmpty, NSpin, NText } from 'naive-ui'
import { storeToRefs } from 'pinia'
import { useWorkshopStore } from '../../stores/workshop'
import type { ResultTier } from '../../types/workshop'
import IngredientSearchBox from './components/IngredientSearchBox.vue'
import SelectedIngredientsBar from './components/SelectedIngredientsBar.vue'
import ModeSwitcher from './components/ModeSwitcher.vue'
import ResultsTabs from './components/ResultsTabs.vue'

const store = useWorkshopStore()
const { selectedIds, mode, ingredientsById, loading, error, results, tierGroups } =
  storeToRefs(store)

const activeTier = ref<ResultTier>('full')

// 切换模式时:回到 full 层级
watch(mode, () => {
  activeTier.value = 'full'
})
// 结果变化时:若当前 tab 空,自动跳到第一个有结果的
watch(tierGroups, (g) => {
  const order: ResultTier[] = ['full', 'miss-1', 'miss-2', 'miss-3+']
  if (g[activeTier.value].length === 0) {
    const first = order.find((t) => g[t].length > 0)
    if (first) activeTier.value = first
  }
})

onMounted(() => store.ensureLoaded())

const totalIngredients = computed(() => ingredientsById.value.size)
const resultCount = computed(() => results.value.length)
</script>

<template>
  <div class="workshop">
    <header class="hero">
      <div class="hero-left">
        <h1 class="hero-title">🍸 调酒台</h1>
        <p class="hero-sub">
          勾选手头的原料,自动告诉你能调哪些鸡尾酒。
          <NText v-if="totalIngredients > 0" depth="3" style="font-size: 13px;">
            数据 {{ totalIngredients }} 原料
          </NText>
        </p>
      </div>
    </header>

    <NAlert
      v-if="error"
      type="error"
      :title="'加载失败'"
      :show-icon="true"
      style="margin-bottom: 16px;"
    >
      {{ error }}
    </NAlert>

    <section class="control-panel">
      <div class="row">
        <IngredientSearchBox
          :ingredients="Array.from(ingredientsById.values())"
          :selected-ids="selectedIds"
          @pick="store.addIngredient"
        />
      </div>
      <div class="row">
        <ModeSwitcher v-model="mode" />
      </div>
      <div class="row">
        <SelectedIngredientsBar
          :selected-ids="selectedIds"
          :ingredients-by-id="ingredientsById"
          @remove="store.removeIngredient"
          @clear="store.clearSelected"
        />
      </div>
    </section>

    <section class="result-panel">
      <div class="result-header">
        <span class="result-title">推荐结果</span>
        <NText depth="3" style="font-size: 13px;">{{ resultCount }} 款</NText>
      </div>

      <div v-if="loading" class="loading">
        <NSpin size="medium" />
        <span style="margin-left: 12px;">正在加载数据…</span>
      </div>

      <NEmpty
        v-else-if="selectedIds.size === 0"
        description="请先在搜索框添加你拥有的原料"
        style="padding: 60px 0;"
      />

      <ResultsTabs
        v-else
        v-model:active-tier="activeTier"
        :groups="tierGroups"
        :ingredients-by-id="ingredientsById"
      />
    </section>
  </div>
</template>

<style scoped>
.workshop {
  padding: 8px 0 40px;
  max-width: 1200px;
  margin: 0 auto;
}
.hero {
  margin-bottom: 20px;
}
.hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}
.hero-sub {
  font-size: 14px;
  color: var(--text-color-2, #555);
  margin: 6px 0 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
.control-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}
.result-panel {
  margin-top: 12px;
}
.result-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}
.result-title {
  font-size: 18px;
  font-weight: 600;
}
.loading {
  display: flex;
  align-items: center;
  padding: 60px 0;
  justify-content: center;
}
</style>
