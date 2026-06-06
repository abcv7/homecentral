import { defineConfig } from 'vite'
import * as path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// @dcloudio/vite-plugin-uni 是 CJS 导出,esbuild bundle 后 default 嵌套
const uniMod = await import('@dcloudio/vite-plugin-uni')
type UniPluginFn = (opts?: Record<string, unknown>) => unknown
const uni = (uniMod as unknown as { default: { default: UniPluginFn } }).default.default

export default defineConfig({
  plugins: [uni()] as never,
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@shared-stores': path.resolve(__dirname, '../WebPage/src/stores'),
      '@shared-utils': path.resolve(__dirname, '../WebPage/src/utils'),
      '@shared-api': path.resolve(__dirname, '../WebPage/src/api'),
      '@shared-types': path.resolve(__dirname, '../WebPage/src/types'),
    },
  },
})
