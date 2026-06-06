#!/usr/bin/env node
/**
 * build_workshop_index.mjs
 * ========================
 * 直接调 enjoycocktail.com 的 Functorz Zion GraphQL API
 * 生成 2 个静态 JSON 索引,落到 public/data/ 供前端调酒台使用。
 *
 * 端点 (无鉴权, 2026-06 实测可用):
 *   POST https://zion-app.functorz.com/zero/mwLZrNjwPZR/api/graphql-v2
 *   Content-Type: application/json
 *   Body: {"query": "<graphql>", "variables": {...}}
 *
 * 流程:
 *   1) count -> 拿 598 鸡尾酒 / 213 酒单 / 701 原料 总数
 *   2) paginate -> 分批拉详情(分页 50/批, 0.3s 限速)
 *   3) 分类主料/可选料 (D 方案: 数量解析 + 白/黑名单 + fallback)
 *   4) dry-run 报告 (前 10 个酒款让人眼抽检)
 *   5) 写入 public/data/cocktails_index.json + ingredients_index.json
 */
import { writeFileSync, mkdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')
const OUT_DIR = resolve(ROOT, 'public/data')
const COCKTAILS_OUT = resolve(OUT_DIR, 'cocktails_index.json')
const INGREDIENTS_OUT = resolve(OUT_DIR, 'ingredients_index.json')

const API = 'https://zion-app.functorz.com/zero/mwLZrNjwPZR/api/graphql-v2'
const FALLBACK_LOCAL = 'C:/Users/30403/AppData/Local/Temp/opencode/enjoycocktail/cocktails_detail.json'
const PAGE_SIZE = 50
const BATCH_SIZE = 10
const SLEEP_MS = 300

// ============================================================
// GraphQL client
// ============================================================
async function gql(query, variables = {}) {
  const res = await fetch(API, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ query, variables }),
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`)
  const json = await res.json()
  if (json.errors) throw new Error('GraphQL: ' + JSON.stringify(json.errors))
  return json.data
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

// ============================================================
// Queries
// ============================================================
const COUNT_QUERY = `
{
  cocktails:  ud_jiukuanjichuxinxibiao1_5f1fed_aggregate(
    where: {ud_shifougongkai_ecbc4e: {_eq: true}, ud_suoshudalei_5e7e08: {_eq: "鸡尾酒"}}
  ) { aggregate { count } }
}
`

const COCKTAIL_DETAIL_QUERY = `
query($ids: [bigint!]) {
  data: ud_jiukuanjichuxinxibiao1_5f1fed(
    where: {id: {_in: $ids}, ud_shifougongkai_ecbc4e: {_eq: true}}
  ) {
    id
    ud_zhongwenmingcheng_f85b58
    ud_waiwenmingcheng_68c654
    ud_bieming_8b623f
    ud_jianjie_ae7261
    ud_liulanliang_562966
    ud_fengmian_ca3d1d { url }
    ud_diaojiuxiangxixinxi_e1f2ee {
      id
      ud_peiliao_949cf0
      ud_zhizuofangfa_f0af1f
      ud_lishiyuwenhua_61087b
      ud_xiangqi_ae41c8
      ud_kougan_7d8da9
      ud_fengge_2be622
      ud_tuijianchangjing_6bf7ad
      ud_guanlianyuanliao_1e1e78 {
        id
        ud_yuanliao_10d257 {
          id
          ud_zhongwenmingcheng_f85b58
          ud_waiwenmingcheng_68c654
        }
      }
    }
  }
}
`

// ============================================================
// 主料/可选料 词典 (D 方案)
// ============================================================
const MAIN_UNITS = new Set([
  '毫升', 'ml', 'ML', 'mL',
  '盎司', 'oz', 'OZ', 'Oz',
  '克', 'g', 'G',
  '茶匙', 'tsp',
  '汤匙', 'tbsp',
  '杯', '杯量', 'parts', 'part', 'Parts',
  'cl', 'CL',
])
const GARNISH_UNITS = new Set([
  '片', '枝', '颗', '个', '块', '角', '圈', '滴', '条', '瓣', '丁', '粒',
  '把', '撮', '枚', '根', '束',
  'dash', 'dashes', 'Dash', 'drop', 'drops',
  '少许', '适量', '杯沿', '边', 'garnish', 'Garnish',
])
const GARNISH_KEYWORDS = [
  '薄荷', '橙片', '橙皮', '橙条', '橙块', '橙卷',
  '柠檬片', '柠檬角', '柠檬皮', '柠檬条', '柠檬卷',
  '青柠片', '青柠角', '青柠皮', '青柠卷',
  '樱桃', '马拉斯奇诺', 'maraschino',
  '橄榄', '洋葱', '芹菜',
  '盐', '粗盐', '海盐', '盐边',
  '糖', '糖边', '糖霜',
  '豆蔻', '肉桂', '肉桂棒', '桂皮', '八角',
  '迷迭香', '百里香', '罗勒', '紫苏',
  '菠萝片', '菠萝角', '菠萝叶', '菠萝块',
  '黄瓜片', '黄瓜条', '黄瓜皮',
  '草莓', '蓝莓', '树莓', '黑莓',
]
const ALWAYS_MAIN_KEYWORDS = [
  '苏打水', '汤力水', '姜汁啤酒', '姜汁汽水', '可乐', '雪碧',
  '牛奶', '奶油', '淡奶油', '鲜奶油', '蛋清', '蛋黄', '鸡蛋', '全蛋',
  '番茄汁', '橙汁', '柠檬汁', '青柠汁', '菠萝汁', '蔓越莓汁', '葡萄柚汁',
  '苹果汁', '西瓜汁', '石榴汁', '石榴糖浆', '红石榴',
  '蜂蜜糖浆', '简单糖浆', '单糖浆', '糖浆', '龙舌兰糖浆', '枫糖浆', '枫糖',
  '咖啡', '浓缩咖啡', 'espresso', '热水', '冰块', '碎冰', '冰',
  '普罗塞克起泡酒', '起泡酒', '香槟', '干姜水',
]

const norm = (s) => (s || '').replace(/\s+/g, '').toLowerCase()

function parsePeiliaoLine(line) {
  const m = line.match(/^\s*([\d./]+)\s*(\S+)\s+(.+?)\s*$/)
  if (m) return { qty: parseFloat(m[1]), unit: m[2].trim(), name: m[3].trim() }
  const m2 = line.match(/^\s*(.+?)\s*$/)
  if (m2 && m2[1].trim()) return { qty: null, unit: '', name: m2[1].trim() }
  return null
}

function classifyUnit(unit) {
  if (!unit) return 'unknown'
  if (MAIN_UNITS.has(unit) || MAIN_UNITS.has(unit.toLowerCase())) return 'main'
  if (GARNISH_UNITS.has(unit) || GARNISH_UNITS.has(unit.toLowerCase())) return 'garnish'
  return 'unknown'
}

const isGarnishName = (name) => GARNISH_KEYWORDS.some((kw) => norm(name).includes(norm(kw)))
const isAlwaysMain = (name) => ALWAYS_MAIN_KEYWORDS.some((kw) => norm(name).includes(norm(kw)))

class IngredientResolver {
  constructor(ingredients) {
    this._byNorm = new Map()
    for (const ing of ingredients) {
      for (const k of [ing.name_zh, ing.name_en]) {
        if (k && !this._byNorm.has(norm(k))) this._byNorm.set(norm(k), ing.id)
      }
    }
    this._keys = Array.from(this._byNorm.keys()).sort((a, b) => b.length - a.length)
  }
  resolve(name) {
    if (!name) return null
    const n = norm(name)
    if (this._byNorm.has(n)) return this._byNorm.get(n)
    for (const k of this._keys) {
      if (k && (k.includes(n) || n.includes(k))) return this._byNorm.get(k)
    }
    return null
  }
}

function classifyCocktailIngredients(cocktail, resolver) {
  const detail = cocktail.ud_diaojiuxiangxixinxi_e1f2ee || {}
  const peiliao = detail.ud_peiliao_949cf0 || ''
  const links = detail.ud_guanlianyuanliao_1e1e78 || []
  const allIds = links.map((g) => g.ud_yuanliao_10d257.id)
  const main = new Set()
  const opt = new Set()

  if (peiliao.trim()) {
    for (const raw of peiliao.split('\n')) {
      for (const sub of raw.split(/[、，,]/).map((s) => s.trim()).filter(Boolean)) {
        const p = parsePeiliaoLine(sub)
        if (!p) continue
        const id = resolver.resolve(p.name)
        if (!id) continue
        if (isAlwaysMain(p.name)) { main.add(id); opt.delete(id) }
        else if (isGarnishName(p.name)) { opt.add(id); main.delete(id) }
        else {
          const c = classifyUnit(p.unit)
          if (c === 'main') { main.add(id); opt.delete(id) }
          else if (c === 'garnish') { opt.add(id); main.delete(id) }
          else { main.add(id); opt.delete(id) }  // unknown -> main(宽松)
        }
      }
    }
  }
  if (main.size === 0) allIds.slice(0, 3).forEach((id) => main.add(id))
  if (main.size > 5) {
    const limited = Array.from(main).slice(0, 5)
    main.clear()
    limited.forEach((id) => main.add(id))
  }
  for (const id of allIds) if (!main.has(id)) opt.add(id)
  return { main: Array.from(main), optional: Array.from(opt) }
}

function buildIngredientIndex(cocktails) {
  const ing = new Map()
  for (const c of cocktails) {
    const detail = c.ud_diaojiuxiangxixinxi_e1f2ee || {}
    for (const g of detail.ud_guanlianyuanliao_1e1e78 || []) {
      const base = g.ud_yuanliao_10d257
      if (!ing.has(base.id)) {
        ing.set(base.id, {
          id: base.id,
          name_zh: base.ud_zhongwenmingcheng_f85b58 || '',
          name_en: base.ud_waiwenmingcheng_68c654 || '',
          aliases: new Set(),
          cocktail_count: 0,
        })
      }
      ing.get(base.id).cocktail_count += 1
    }
  }
  for (const v of ing.values()) {
    const aliases = new Set([norm(v.name_zh), norm(v.name_en), v.name_zh.toLowerCase(), v.name_en.toLowerCase()].filter(Boolean))
    v.aliases = Array.from(aliases)
  }
  return Array.from(ing.values())
}

// ============================================================
// Main
// ============================================================
async function main() {
  // 1) 优先尝试 API;若失败,fallback 到本地缓存
  let cocktailsIn = null
  let source = ''
  try {
    console.log('==> 1) 调用 GraphQL API 拿鸡尾酒详情')
    const cnt = await gql(COUNT_QUERY)
    const total = cnt.cocktails.aggregate[0].count
    console.log(`   共 ${total} 鸡尾酒`)

    const idList = []
    for (let offset = 0; offset < total; offset += PAGE_SIZE) {
      const data = await gql(
        `query($offset:Int!,$limit:Int!){
          data: ud_jiukuanjichuxinxibiao1_5f1fed(
            where:{ud_shifougongkai_ecbc4e:{_eq:true}, ud_suoshudalei_5e7e08:{_eq:"鸡尾酒"}}
            order_by:{id:asc} offset:$offset limit:$limit
          ){ id }
        }`,
        { offset, limit: PAGE_SIZE },
      )
      for (const r of data.data) idList.push(r.id)
      process.stdout.write(`  ids: ${idList.length}/${total}\r`)
      await sleep(SLEEP_MS)
    }
    console.log(`\n  ids 完成: ${idList.length}`)

    cocktailsIn = []
    for (let i = 0; i < idList.length; i += BATCH_SIZE) {
      const ids = idList.slice(i, i + BATCH_SIZE)
      const data = await gql(COCKTAIL_DETAIL_QUERY, { ids })
      cocktailsIn.push(...data.data)
      process.stdout.write(`  details: ${cocktailsIn.length}/${idList.length}\r`)
      await sleep(SLEEP_MS)
    }
    console.log(`\n  details 完成: ${cocktailsIn.length}`)
    source = 'api'
  } catch (e) {
    console.warn('!! API 不可用,fallback 到本地缓存:', e.message)
  }

  if (!cocktailsIn) {
    const { existsSync, readFileSync: rfs } = await import('node:fs')
    if (existsSync(FALLBACK_LOCAL)) {
      console.log(`==> 1) 从本地读取: ${FALLBACK_LOCAL}`)
      cocktailsIn = JSON.parse(rfs(FALLBACK_LOCAL, 'utf-8'))
      console.log(`   共 ${cocktailsIn.length} 鸡尾酒`)
      source = 'local-fallback'
    } else {
      throw new Error('API 不可用且本地无缓存,无法继续')
    }
  }
  console.log(`   数据源: ${source}`)

  console.log('\n==> 3) 构建原料索引')
  const ingList = buildIngredientIndex(cocktailsIn)
  console.log(`   共 ${ingList.length} 原料`)

  console.log('==> 4) 分类主料/可选料')
  const resolver = new IngredientResolver(ingList)
  const cocktailsOut = cocktailsIn.map((c) => {
    const detail = c.ud_diaojiuxiangxixinxi_e1f2ee || {}
    const links = detail.ud_guanlianyuanliao_1e1e78 || []
    const ingredientIds = links.map((g) => g.ud_yuanliao_10d257.id)
    const { main, optional } = classifyCocktailIngredients(c, resolver)
    return {
      id: c.id,
      name_zh: c.ud_zhongwenmingcheng_f85b58 || '',
      name_en: c.ud_waiwenmingcheng_68c654 || '',
      name_alias: c.ud_bieming_8b623f
        ? c.ud_bieming_8b623f.split(/[/\/]/).map((s) => s.trim()).filter(Boolean)
        : [],
      cover: c.ud_fengmian_ca3d1d?.url || null,
      views: c.ud_liulanliang_562966 || 0,
      ingredient_ids: ingredientIds,
      main_ingredient_ids: main,
      recipe_zh: detail.ud_peiliao_949cf0 || '',
      method_zh: detail.ud_zhizuofangfa_f0af1f || '',
      aroma: detail.ud_xiangqi_ae41c8 || '',
      taste: detail.ud_kougan_7d8da9 || '',
      style: detail.ud_fengge_2be622 || '',
      scene: detail.ud_tuijianchangjing_6bf7ad || '',
      history: detail.ud_lishiyuwenhua_61087b || '',
    }
  })

  // Dry-run
  const ingById = new Map(ingList.map((i) => [i.id, i]))
  const fmt = (ids) => ids.map((i) => ingById.get(i)?.name_zh || `?${i}`).join(',')
  console.log('\n==> 5) Dry-run 抽检 (前 10 个)')
  console.log(`${'id'.padStart(5)} ${'name'.padEnd(15)} ${'main'.padEnd(40)} ${'opt'.padEnd(20)}`)
  for (const c of cocktailsOut.slice(0, 10)) {
    console.log(`${String(c.id).padStart(5)} ${c.name_zh.padEnd(15)} ${fmt(c.main_ingredient_ids).padEnd(40)} ${fmt(c.ingredient_ids.filter((i) => !c.main_ingredient_ids.includes(i))).padEnd(20)}`)
  }

  // main 数量分布
  const dist = new Map()
  for (const c of cocktailsOut) dist.set(c.main_ingredient_ids.length, (dist.get(c.main_ingredient_ids.length) || 0) + 1)
  console.log('\n==> main 数量分布:')
  for (const [k, v] of [...dist.entries()].sort((a, b) => a[0] - b[0])) console.log(`  main=${k} 原料: ${v} 款`)

  console.log('\n==> 6) 写入文件')
  mkdirSync(OUT_DIR, { recursive: true })
  const cocktailJson = JSON.stringify(cocktailsOut)
  const ingJson = JSON.stringify(ingList)
  writeFileSync(COCKTAILS_OUT, cocktailJson, 'utf-8')
  writeFileSync(INGREDIENTS_OUT, ingJson, 'utf-8')
  console.log(`   ${COCKTAILS_OUT} (${cocktailJson.length.toLocaleString()} bytes)`)
  console.log(`   ${INGREDIENTS_OUT} (${ingJson.length.toLocaleString()} bytes)`)
  console.log('\n==> 完成')
}

main().catch((e) => {
  console.error('FATAL:', e)
  process.exit(1)
})
