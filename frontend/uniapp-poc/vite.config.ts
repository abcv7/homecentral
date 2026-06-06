import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'node:path'

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      // 共享主项目 stores/utils/api/types
      '@shared-stores': path.resolve(__dirname, '../WebPage/src/stores'),
      '@shared-utils': path.resolve(__dirname, '../WebPage/src/utils'),
      '@shared-api': path.resolve(__dirname, '../WebPage/src/api'),
      '@shared-types': path.resolve(__dirname, '../WebPage/src/types'),
    },
  },
})
