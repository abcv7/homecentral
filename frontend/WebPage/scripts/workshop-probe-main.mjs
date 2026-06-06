// 专项:只测主料模式,直接调 store
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
await page.waitForTimeout(1500)

// 直接调 store 切到主料
await page.evaluate(() => {
  const app = document.querySelector('#app').__vue_app__
  const pinia = app.config.globalProperties.$pinia
  const s = pinia._s.get('workshop')
  s.setMode('main')
  s.addIngredient(282)
})

// 多次 poll 直到 DOM 稳定
for (let i = 0; i < 10; i++) {
  const ok = await page.evaluate(() => {
    const t = document.querySelector('.result-title + .n-text')?.textContent
    return t === '100 款'
  })
  if (ok) break
  await page.waitForTimeout(200)
}

const final = await page.evaluate(() => ({
  mode: document.querySelector('#app').__vue_app__.config.globalProperties.$pinia._s.get('workshop').mode,
  resultCount: document.querySelector('.result-title + .n-text')?.textContent,
  activeTab: document.querySelector('.n-tabs-tab--active')?.textContent,
  cards: document.querySelectorAll('.n-card.card').length,
  radioButtons: Array.from(document.querySelectorAll('.n-radio-button')).map(el => ({
    text: el.textContent?.trim(),
    classes: el.className,
  })),
}))
console.log('最终状态:', JSON.stringify(final, null, 2))

await page.screenshot({
  path: 'C:\\Users\\30403\\AppData\\Local\\Temp\\opencode\\workshop-shots\\04b-main-only.png',
  fullPage: true,
  animations: 'disabled',
})
await browser.close()
