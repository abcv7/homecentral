// 联调脚本
// 1) 启动 chromium headless
// 2) 注入假 token 绕过 auth guard
// 3) 导航到 /workshop
// 4) 验证:页面加载、添加原料、切换模式、点击卡片
// 5) 截图 + 收集 console error

import { chromium } from 'playwright-core'
import { writeFileSync } from 'node:fs'

const BASE = 'http://127.0.0.1:5180'
const SHOT_DIR = 'C:\\Users\\30403\\AppData\\Local\\Temp\\opencode\\workshop-shots'
const CHROME = 'C:\\Users\\30403\\AppData\\Local\\ms-playwright\\chromium-1200\\chrome-win64\\chrome.exe'

const log = (...a) => console.log('[联调]', ...a)
const errors = []
const consoleMessages = []

const browser = await chromium.launch({
  executablePath: CHROME,
  headless: true,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
})

const ctx = await browser.newContext({
  viewport: { width: 1280, height: 800 },
})
const page = await ctx.newPage()

page.on('console', (m) => {
  consoleMessages.push(`[${m.type()}] ${m.text()}`)
  if (m.type() === 'error') errors.push(`CONSOLE ERROR: ${m.text()}`)
})
page.on('pageerror', (e) => errors.push(`PAGE ERROR: ${e.message}`))
page.on('requestfailed', (r) => {
  const u = r.url()
  if (u.includes('/api/') || u.includes('/data/')) {
    errors.push(`REQUEST FAILED: ${u} -> ${r.failure()?.errorText}`)
  }
})

// === 1. 注入假 token 绕过 auth guard ===
await page.goto(`${BASE}/login`, { waitUntil: 'domcontentloaded' })
await page.evaluate(() => {
  localStorage.setItem('access_token', 'fake.jwt.token')
  localStorage.setItem('refresh_token', 'fake.refresh.token')
  localStorage.setItem('username', '联调测试员')
})
log('✓ 注入假 token')

// === 2. 导航到 /workshop ===
await page.goto(`${BASE}/workshop`, { waitUntil: 'networkidle' })
log('✓ 导航到 /workshop')

// === 3. 验证 hero 标题 ===
const heroTitle = await page.textContent('.hero-title')
log('hero title:', heroTitle)
if (!heroTitle || !heroTitle.includes('调酒台')) {
  errors.push(`hero 标题不正确: ${heroTitle}`)
}

await page.waitForTimeout(500)
await page.screenshot({ path: `${SHOT_DIR}\\01-initial.png`, fullPage: true })
log('  截图: 初始空态')

// === 4. 添加 1 个原料: 搜索 "伏特加" ===
const search = page.locator('input[placeholder*="搜索原料"]').first()
await search.waitFor({ state: 'visible', timeout: 5000 })
await search.click()
await search.fill('伏特加')
await page.waitForTimeout(800)
await page.screenshot({ path: `${SHOT_DIR}\\02-search-vodka.png`, fullPage: true })
log('  截图: 搜索伏特加')

// 直接调 store 模拟用户点击 (Naive UI v-binder teleport 难以模拟,绕开)
const addResult1 = await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const store = pinia._s.get('workshop')
  store.addIngredient(282)   // 伏特加
  return Array.from(store.selectedIds)
})
log('  store.selectedIds:', addResult1)
await page.waitForTimeout(500)
log('✓ 注入 伏特加 (id=282)')

// 验证 search 框的 tag 出现
const tagCount1 = await page.locator('.selected-bar .n-tag').count()
log('  tag 数:', tagCount1)
if (tagCount1 === 0) errors.push('添加伏特加后 selected bar 无 tag')

// === 5. 验证:结果不为空 ===
const resultCount = await page.locator('.result-title').first().textContent()
log('结果标题:', resultCount)

const cardCount = await page.locator('.n-card.card').count()
log('严格模式 卡片数:', cardCount)
if (cardCount === 0) {
  errors.push('严格模式无结果(添加伏特加后)')
}

await page.screenshot({ path: `${SHOT_DIR}\\03-strict-results.png`, fullPage: true })

// === 6. 切换到主料模式 ===
await page.mouse.click(10, 10)   // 点空白处关掉任何浮层
await page.waitForTimeout(200)
// Naive UI NRadioButton 用 update:modelValue,直接调 store
const mainModeResult = await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const store = pinia._s.get('workshop')
  store.setMode('main')
  return {
    mode: store.mode,
    tierGroups: {
      full: store.tierGroups.full.length,
      miss1: store.tierGroups['miss-1'].length,
      miss2: store.tierGroups['miss-2'].length,
      miss3p: store.tierGroups['miss-3+'].length,
    },
  }
})
log('  store.mode:', mainModeResult.mode)
log('  主料模式 分组:', JSON.stringify(mainModeResult.tierGroups))
if (mainModeResult.mode !== 'main') errors.push('setMode("main") 失败')
if (mainModeResult.tierGroups.full === 0 && mainModeResult.tierGroups.miss1 === 0) {
  errors.push('主料模式无结果(全 0)')
}

await page.waitForTimeout(800)
const visualState = await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const s = pinia._s.get('workshop')
  return {
    storeMode: s.mode,
    resultCount: s.results.length,
    activeTabText: document.querySelector('.n-tabs-tab--active')?.textContent,
    visibleCardCount: document.querySelectorAll('.n-card.card').length,
    domResultCount: document.querySelector('.result-title + .n-text')?.textContent,
  }
})
log('  视觉态:', JSON.stringify(visualState))
// 等下一帧并先截
await page.evaluate(() => new Promise((r) => requestAnimationFrame(() => requestAnimationFrame(r))))
await page.screenshot({ path: `${SHOT_DIR}\\04-main-results.png`, fullPage: true, animations: 'disabled' })
const cardCountMain = await page.locator('.n-card.card').count()
log('主料模式 卡片数 (active tab):', cardCountMain)

// === 7. 切回严格 ===
await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  pinia._s.get('workshop').setMode('strict')
})
await page.waitForTimeout(300)

await page.screenshot({ path: `${SHOT_DIR}\\04-main-results.png`, fullPage: true })

// === 8. 切回严格,再添加一个原料: 单糖浆 ===
const strictModeCheck = await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  return app.config.globalProperties.$pinia._s.get('workshop').mode
})
log('  当前 store.mode:', strictModeCheck)
if (strictModeCheck !== 'strict') errors.push('setMode 切回严格失败')
await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const store = pinia._s.get('workshop')
  store.addIngredient(1880)   // 单糖浆 (simple syrup)
})
await page.waitForTimeout(300)
log('✓ 注入 单糖浆 (id=1880)')

const selectedIds2 = await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  return Array.from(pinia._s.get('workshop').selectedIds)
})
log('  现有 selectedIds:', selectedIds2)

const cardCount2 = await page.locator('.n-card.card').count()
log('严格+2 原料 卡片数:', cardCount2)
if (cardCount2 === 0) {
  errors.push('严格模式+2 原料无结果')
}
await page.screenshot({ path: `${SHOT_DIR}\\05-strict-2-ingredients.png`, fullPage: true })

// === 8. 点击第 1 张卡片打开详情 ===
const firstCard = page.locator('.n-card.card').first()
if (await firstCard.count() > 0) {
  await firstCard.click()
  await page.waitForTimeout(600)
  const modalOpen = await page.locator('.n-modal').isVisible().catch(() => false)
  log('详情弹窗可见:', modalOpen)
  await page.screenshot({ path: `${SHOT_DIR}\\06-detail-modal.png`, fullPage: true })
  if (!modalOpen) errors.push('点击卡片未打开详情弹窗')
  await page.keyboard.press('Escape')
  await page.waitForTimeout(300)
} else {
  errors.push('没有卡片可点击')
}

// === 9. 清空 + 验证回退到空态 ===
await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  pinia._s.get('workshop').clearSelected()
})
await page.waitForTimeout(400)
const emptyVisible = await page.locator('.n-empty').first().isVisible().catch(() => false)
log('清空后空态可见:', emptyVisible)
await page.screenshot({ path: `${SHOT_DIR}\\07-cleared.png`, fullPage: true })

// === 10. 移动端断点 ===
await page.setViewportSize({ width: 390, height: 844 })
await page.waitForTimeout(500)
await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const store = pinia._s.get('workshop')
  store.clearSelected()
  store.addIngredient(284)   // Gin
})
await page.waitForTimeout(500)
await page.screenshot({ path: `${SHOT_DIR}\\08-mobile.png`, fullPage: true })
log('  截图: 移动端')

// === 总结 ===
log('---')
log('console 总数:', consoleMessages.length)
log('error 总数:', errors.length)
if (errors.length) {
  log('!!! 联调发现问题:')
  for (const e of errors) log('  -', e)
}

writeFileSync(`${SHOT_DIR}\\console.log`, consoleMessages.join('\n'))
writeFileSync(`${SHOT_DIR}\\errors.log`, errors.join('\n'))

await browser.close()
process.exit(errors.length > 0 ? 1 : 0)
