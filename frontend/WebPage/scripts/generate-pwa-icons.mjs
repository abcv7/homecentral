// scripts/generate-pwa-icons.mjs
// Convert public/icons/icon.svg into PNG sizes needed for PWA manifest
import sharp from 'sharp'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { existsSync, mkdirSync } from 'node:fs'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)
const root = join(__dirname, '..')
const svgPath = join(root, 'public', 'icons', 'icon.svg')
const outDir = join(root, 'public', 'icons')

if (!existsSync(svgPath)) {
  console.error(`SVG not found: ${svgPath}`)
  process.exit(1)
}
if (!existsSync(outDir)) mkdirSync(outDir, { recursive: true })

const sizes = [
  { size: 192, name: 'pwa-192x192.png' },
  { size: 512, name: 'pwa-512x512.png' },
  { size: 180, name: 'apple-touch-icon.png' },  // iOS 180x180
  { size: 32, name: 'favicon-32x32.png' },
  { size: 16, name: 'favicon-16x16.png' },
]

// Maskable variant: same SVG but rendered at 512 with safe area
// Safe zone is 80% centered; we keep the design centered within it.
const svgBuffer = (await import('node:fs')).readFileSync(svgPath)

for (const { size, name } of sizes) {
  await sharp(svgBuffer)
    .resize(size, size, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .png()
    .toFile(join(outDir, name))
  console.log(`✓ ${name} (${size}x${size})`)
}

console.log('All PWA icons generated.')
