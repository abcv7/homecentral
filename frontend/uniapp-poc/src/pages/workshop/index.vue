<script setup lang="ts">
/**
 * 调酒台 view (uni-app 版)
 * - 输入原料 id (或从原料列表选) → 调后端 /api/workshop/recommend
 * - 4 个 tier 分组 (full / miss-1 / miss-2 / miss-3+)
 * - 简化版:原料用搜索框选,完整原料面板留待 H5
 */
import { computed, ref } from 'vue'
import { onShow as uniOnShow } from '@dcloudio/uni-app'
import { loadIngredients, recommendCocktails } from '@/api/workshop'
import type { IngredientIndex, ResultTier } from '@/types/workshop'

const ingredients = ref<IngredientIndex[]>([])
const selectedIds = ref<Set<number>>(new Set())
const mode = ref<'STRICT' | 'MAIN'>('STRICT')
const keyword = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

const results = ref<Array<{
  tier: ResultTier
  cocktailId: number
  name: string
  nameEn: string
  missing: number
  matched: number
  ratio: number
}>>([])

async function loadIngredientsCache() {
  if (ingredients.value.length > 0) return
  ingredients.value = await loadIngredients()
}

async function search() {
  await loadIngredientsCache()
  // 不需要调接口,前端过滤即可
}

const filteredIngredients = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return ingredients.value.slice(0, 50)
  return ingredients.value.filter((i: IngredientIndex) =>
    i.name_zh.includes(k) || i.name_en.toLowerCase().includes(k) || i.aliases.some((a: string) => a.toLowerCase().includes(k)),
  ).slice(0, 50)
})

function toggle(id: number) {
  const next = new Set(selectedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedIds.value = next
}

async function runRecommend() {
  if (selectedIds.value.size === 0) {
    results.value = []
    return
  }
  loading.value = true
  error.value = null
  try {
    const res = await recommendCocktails(Array.from(selectedIds.value), mode.value)
    results.value = (res ?? []).map((r) => ({
      tier: r.tier,
      cocktailId: r.cocktail.id,
      name: r.cocktail.nameZh,
      nameEn: r.cocktail.nameEn,
      missing: r.missingCount,
      matched: r.matchedIngredients.length,
      ratio: r.matchedIngredients.length + r.missingCount > 0
        ? r.matchedIngredients.length / (r.matchedIngredients.length + r.missingCount)
        : 0,
    }))
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

uniOnShow(() => loadIngredientsCache())

const tierGroups = computed(() => {
  const groups: Record<ResultTier, typeof results.value> = {
    'full': [],
    'miss-1': [],
    'miss-2': [],
    'miss-3+': [],
  }
  for (const r of results.value) groups[r.tier].push(r)
  return groups
})

const TIER_LABELS: Record<ResultTier, string> = {
  'full': '✅ 完美',
  'miss-1': '🟡 缺 1',
  'miss-2': '🟠 缺 2',
  'miss-3+': '🔴 缺 3+',
}

function clearAll() {
  selectedIds.value = new Set()
  results.value = []
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">🍸 调酒台</text>
      <text class="subtitle">{{ ingredients.length }} 原料 · 选 {{ selectedIds.size }}</text>
    </view>

    <view class="mode-bar">
      <button :class="['mode-btn', mode === 'STRICT' ? 'active' : '']" @click="mode = 'STRICT'">严格 (全部原料)</button>
      <button :class="['mode-btn', mode === 'MAIN' ? 'active' : '']" @click="mode = 'MAIN'">主料 (主料必备)</button>
      <button class="mode-btn ghost" @click="clearAll">清空</button>
    </view>

    <view class="search">
      <input v-model="keyword" placeholder="搜索原料..." class="search-input" @confirm="search" />
    </view>

    <view class="ingredient-list">
      <view
        v-for="i in filteredIngredients"
        :key="i.id"
        :class="['ing', selectedIds.has(i.id) ? 'selected' : '']"
        @click="toggle(i.id)"
      >
        <text class="ing-name">{{ i.name_zh }}</text>
        <text v-if="i.name_en" class="ing-en">{{ i.name_en }}</text>
      </view>
    </view>

    <button class="run-btn" :disabled="selectedIds.size === 0 || loading" @click="runRecommend">
      {{ loading ? '计算中…' : `推荐 (${selectedIds.size})` }}
    </button>

    <view v-if="error" class="error">错误: {{ error }}</view>

    <view v-for="tier in (['full','miss-1','miss-2','miss-3+'] as ResultTier[])" :key="tier" class="tier">
      <text class="tier-title">{{ TIER_LABELS[tier] }} ({{ tierGroups[tier].length }})</text>
      <view
        v-for="r in tierGroups[tier]"
        :key="r.cocktailId"
        class="result"
      >
        <text class="result-name">{{ r.name }}</text>
        <text v-if="r.nameEn" class="result-en">{{ r.nameEn }}</text>
        <text class="result-meta">{{ r.matched }} 命中 / {{ r.missing }} 缺</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 16px; padding-bottom: 80px; }
.header { margin-bottom: 12px; }
.title { font-size: 24px; font-weight: 700; display: block; }
.subtitle { font-size: 13px; color: #888; margin-top: 4px; display: block; }
.mode-bar { display: flex; gap: 6px; margin-bottom: 12px; }
.mode-btn { font-size: 12px; padding: 6px 10px; background: #f5f5f5; color: #333; border: 1px solid #e5e5e5; border-radius: 4px; flex: 1; }
.mode-btn.active { background: #1e293b; color: #fff; border-color: #1e293b; }
.mode-btn.ghost { background: transparent; flex: 0 0 60px; }
.search { margin-bottom: 12px; }
.search-input { width: 100%; padding: 8px 12px; background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; font-size: 14px; }
.ingredient-list { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; max-height: 200px; overflow-y: auto; }
.ing { padding: 4px 10px; background: #f5f5f5; border: 1px solid #e5e5e5; border-radius: 12px; display: flex; flex-direction: column; }
.ing.selected { background: #1e293b; color: #fff; border-color: #1e293b; }
.ing-name { font-size: 12px; }
.ing-en { font-size: 10px; opacity: 0.6; }
.run-btn { width: 100%; padding: 12px; background: #f97316; color: #fff; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; margin-bottom: 16px; }
.run-btn:disabled { background: #d1d5db; }
.error { color: #ef4444; font-size: 13px; margin-bottom: 8px; }
.tier { margin-bottom: 12px; }
.tier-title { font-size: 14px; font-weight: 600; display: block; margin-bottom: 6px; }
.result { background: #fff; padding: 10px 12px; border-radius: 6px; margin-bottom: 6px; }
.result-name { font-size: 14px; font-weight: 600; display: block; }
.result-en { font-size: 11px; color: #888; display: block; }
.result-meta { font-size: 11px; color: #888; display: block; margin-top: 2px; }
</style>
