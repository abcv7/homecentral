// 探针:看 autocomplete 下拉结构
import { chromium } from 'playwright-core'
const browser = await chromium.launch({
  executablePath: 'C:\\Users\\30403\\AppData\\Local\\ms-playwright\\chromium-1200\\chrome-win64\\chrome.exe',
  headless: true,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
})
const ctx = await browser.newContext({ viewport: { width: 1280, height: 800 } })
const page = await ctx.newPage()
await page.goto('http://127.0.0.1:5180/login')
await page.evaluate(() => {
  localStorage.setItem('access_token', 'fake.jwt.token')
  localStorage.setItem('username', 'test')
})
await page.goto('http://127.0.0.1:5180/workshop', { waitUntil: 'networkidle' })
await page.waitForTimeout(800)

const search = page.locator('input[placeholder*="搜索原料"]').first()
await search.click()
await search.fill('伏特加')
await page.waitForTimeout(800)

// 探查下拉结构
const dropdownInfo = await page.evaluate(() => {
  const dd = document.querySelector('.n-base-select-menu, .v-binder-follower-content, [class*="select-menu"], [class*="select-option"]')
  if (!dd) return { found: false }
  const html = dd.outerHTML.slice(0, 500)
  const allOpts = Array.from(document.querySelectorAll('.n-base-select-option'))
  return {
    found: true,
    tagName: dd.tagName,
    className: dd.className,
    optCount: allOpts.length,
    firstOptText: allOpts[0]?.textContent?.slice(0, 50),
    firstOptHTML: allOpts[0]?.outerHTML?.slice(0, 300),
    sampleHtml: html,
  }
})
console.log(JSON.stringify(dropdownInfo, null, 2))

// 用 JS 触发 mousedown
const clickResult = await page.evaluate(() => {
  const opt = document.querySelector('.n-base-select-option')
  if (!opt) return 'no opt found'
  const ev = new MouseEvent('mousedown', { bubbles: true, cancelable: true, view: window, button: 0 })
  opt.dispatchEvent(ev)
  return 'dispatched mousedown'
})
console.log('JS mousedown:', clickResult)
await page.waitForTimeout(500)

const cardCount = await page.locator('.n-card.card').count()
const tagCount = await page.locator('.n-tag').count()
console.log('点击后 card 数:', cardCount, 'tag 数:', tagCount)

const selectedIds = await page.evaluate(() => {
  // 探查 Pinia store 状态
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const store = pinia._s.get('workshop')
  return {
    selectedIds: Array.from(store.selectedIds),
    cocktails: store.cocktails.length,
    loading: store.loading,
  }
})
console.log('store 状态:', JSON.stringify(selectedIds, null, 2))

await browser.close()
