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
      <text class="subtitle">{{ ingredients.length }} 原料 · 已选 {{ selectedIds.size }}</text>
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
        :class="['result', `result-${tier}`]"
      >
        <text class="result-name">{{ r.name }}</text>
        <text v-if="r.nameEn" class="result-en">{{ r.nameEn }}</text>
        <text class="result-meta">{{ r.matched }} 命中 / {{ r.missing }} 缺</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 16px 20px;
  padding-bottom: 80px;
  background: var(--qwu-bg, #faf8f5);
  min-height: 100vh;
}
.header {
  margin-bottom: 16px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  display: block;
  color: var(--qwu-text, #1c1917);
  letter-spacing: -0.5px;
}
.subtitle {
  font-size: 13px;
  color: var(--qwu-text-muted, #a8a29e);
  margin-top: 2px;
  display: block;
}
.mode-bar {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}
.mode-btn {
  font-size: 12px;
  padding: 7px 12px;
  background: var(--qwu-card, #ffffff);
  color: var(--qwu-text-secondary, #78716c);
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: var(--qwu-radius-xs, 6px);
  flex: 1;
  font-weight: 500;
}
.mode-btn.active {
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border-color: var(--qwu-primary, #f97316);
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.mode-btn.ghost {
  background: transparent;
  flex: 0 0 60px;
  border-color: var(--qwu-border, #e7e5e4);
  color: var(--qwu-text-muted, #a8a29e);
}
.search {
  margin-bottom: 14px;
}
.search-input {
  width: 100%;
  padding: 10px 12px;
  background: var(--qwu-card, #ffffff);
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: var(--qwu-radius-xs, 6px);
  font-size: 14px;
  box-sizing: border-box;
  color: var(--qwu-text, #1c1917);
}
.search-input:focus {
  border-color: var(--qwu-primary, #f97316);
  outline: none;
}
.ingredient-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  max-height: 200px;
  overflow-y: auto;
}
.ing {
  padding: 6px 12px;
  background: var(--qwu-card, #ffffff);
  border: 1.5px solid var(--qwu-border, #e7e5e4);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s;
}
.ing.selected {
  background: var(--qwu-primary, #f97316);
  color: #fff;
  border-color: var(--qwu-primary, #f97316);
  box-shadow: 0 2px 6px rgba(249, 115, 22, 0.25);
}
.ing-name {
  font-size: 12px;
  font-weight: 500;
}
.ing-en {
  font-size: 10px;
  opacity: 0.6;
}
.run-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #f97316, #f59e0b);
  color: #fff;
  border: none;
  border-radius: var(--qwu-radius-sm, 10px);
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.3);
}
.run-btn:disabled {
  background: var(--qwu-border, #e7e5e4);
  box-shadow: none;
}
.error {
  color: var(--qwu-danger, #ef4444);
  font-size: 13px;
  margin-bottom: 8px;
  background: var(--qwu-danger-light, #fef2f2);
  padding: 6px 10px;
  border-radius: var(--qwu-radius-xs, 6px);
}
.tier {
  margin-bottom: 16px;
}
.tier-title {
  font-size: 14px;
  font-weight: 700;
  display: block;
  margin-bottom: 8px;
  color: var(--qwu-text, #1c1917);
}
.result {
  background: var(--qwu-card, #ffffff);
  padding: 12px 14px;
  border-radius: var(--qwu-radius-sm, 10px);
  margin-bottom: 8px;
  box-shadow: var(--qwu-shadow, 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04));
  border-left: 4px solid var(--qwu-border, #e7e5e4);
}
.result-full {
  border-left-color: var(--qwu-success, #10b981);
  background: var(--qwu-success-light, #ecfdf5);
}
.result-miss-1 {
  border-left-color: var(--qwu-accent, #fbbf24);
  background: var(--qwu-accent-light, #fef9c3);
}
.result-miss-2 {
  border-left-color: var(--qwu-primary, #f97316);
  background: var(--qwu-primary-light, #fff7ed);
}
.result-miss-3\+ {
  border-left-color: var(--qwu-danger, #ef4444);
  background: var(--qwu-danger-light, #fef2f2);
}
.result-name {
  font-size: 14px;
  font-weight: 600;
  display: block;
  color: var(--qwu-text, #1c1917);
}
.result-en {
  font-size: 11px;
  color: var(--qwu-text-muted, #a8a29e);
  display: block;
}
.result-meta {
  font-size: 11px;
  color: var(--qwu-text-secondary, #78716c);
  display: block;
  margin-top: 2px;
}
</style>
