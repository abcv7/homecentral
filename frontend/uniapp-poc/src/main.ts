import { createSSRApp } from 'vue'
import * as Pinia from 'pinia'

export function createApp() {
  const app = createSSRApp({})
  app.use(Pinia.createPinia())
  return { app, Pinia }
}
