# HBuilderX 工作流

`frontend/uniapp-poc` 是 HomeCentral 跨端 POC 项目,基于 `uni-app` + `vue3` + `ts`。

本文档说明如何用 **HBuilderX** 打开、运行、编译到各端(H5 / 微信小程序 / App),以及 CLI 备选方案。

---

## 1. 项目结构

```
frontend/uniapp-poc/
├── package.json              # 依赖 + scripts (CLI 编译入口)
├── vite.config.ts            # alias: @ → src/ , @shared-* → ../WebPage/src/*
├── tsconfig.json             # extends @vue/tsconfig + paths
├── src/
│   ├── manifest.json         # 应用配置 (HBuilderX 必需)
│   ├── pages.json            # 路由 + tabBar (uni-app 路由核心)
│   ├── App.vue               # 顶层 onLaunch/onShow
│   ├── main.ts               # createSSRApp + createPinia
│   ├── api/                  # 本地 api 层 (基于 uni.request)
│   │   ├── request.ts        # request shim (替代 axios)
│   │   ├── fridge.ts
│   │   ├── parcel.ts
│   │   └── workshop.ts
│   ├── utils/
│   │   └── token.ts          # uni.getStorageSync (替代 localStorage)
│   ├── types/                # 共享 web types (用 @shared-types/*)
│   ├── stores/               # 共享 web stores (用 @shared-stores/*, 目前未用)
│   ├── pages/                # uni-app 页面 (不是 web 的 views)
│   │   ├── fridge/index.vue
│   │   ├── parcel/index.vue
│   │   └── workshop/index.vue
│   └── pages.json
└── docs/
    └── hbuilderx-workflow.md # 本文档
```

---

## 2. HBuilderX 导入项目

### 2.1 安装 HBuilderX
- 下载地址: <https://www.dcloud.io/hbuilderx.html>
- 推荐下载 **标准版** (Vue 3 + TS 友好)
- 解压即用,无需安装

### 2.2 打开项目
1. 启动 HBuilderX
2. 菜单 → **文件** → **导入** → **从本地目录导入**
3. 选择 `frontend/uniapp-poc/` 目录
4. HBuilderX 会自动识别为 **uni-app 项目**(看 manifest.json)

### 2.3 安装依赖
- HBuilderX 内置 npm 终端:菜单 → **视图** → **终端**
- 在项目根目录执行:
  ```bash
  npm install
  ```
- 或右键 `package.json` → **外部命令** → **npm install**

> ⚠️ **依赖较重** (uni-app 编译 + Vue 3 + Pinia + Vite),首次安装 2-3 分钟。

---

## 3. 运行

### 3.1 H5 (推荐先用 H5 验证)
1. 菜单 → **运行** → **运行到浏览器** → **Chrome**
2. 自动打开 `http://localhost:8081`(端口可能冲突,自行调整)
3. 调试:Chrome DevTools

### 3.2 微信小程序
1. 先装 **微信开发者工具** <https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html>
2. 微信开发者工具 → **设置** → **安全设置** → 开启 **服务端口**
3. HBuilderX 菜单 → **运行** → **运行到小程序模拟器** → **微信开发者工具**
4. 首次会要求登录 + 输入 appid(测试用可以选"测试号")

### 3.3 App (Android/iOS)
1. 菜单 → **运行** → **运行到手机或模拟器** → **Android/iOS App 基座**
2. 需要先装 **手机模拟器**(夜神/雷电/MuMu)或真机 + USB 调试

### 3.4 其他平台
- 支付宝小程序、百度小程序、抖音小程序、QQ 小程序、快应用、鸿蒙:菜单 → **运行** → **运行到小程序模拟器/手机或模拟器** → 对应选项
- 当前 POC 重点验证 **H5 + 微信小程序**

---

## 4. 编译发行

### 4.1 H5 发行
1. 菜单 → **发行** → **网站-PC Web 或手机 H5**
2. 选择发行路径(默认 `unpackage/dist/build/h5`)
3. 产物可部署到 nginx / 静态托管

### 4.2 微信小程序发行
1. 菜单 → **发行** → **小程序-微信**
2. 输入小程序的 **appid** + **项目名称**
3. 产物在 `unpackage/dist/build/mp-weixin/`
4. 用微信开发者工具打开该目录 → **上传** → 提交审核

### 4.3 App 发行
1. 菜单 → **发行** → **原生 App-云打包**
2. 选择 Android/iOS + 证书配置
3. 云端打包完成后下载 .apk / .ipa

---

## 5. CLI 备选方案 (无 HBuilderX)

HBuilderX 不是必须的,可以用 CLI 编译:

```bash
cd frontend/uniapp-poc
npm install

# H5
npm run dev:h5          # 开发
npm run build:h5        # 发行

# 微信小程序
npm run dev:mp-weixin
npm run build:mp-weixin
```

> CLI 模式需要往 `package.json` 加 scripts:
> ```json
> "scripts": {
>   "dev:h5": "uni",
>   "build:h5": "uni build",
>   "dev:mp-weixin": "uni -p mp-weixin",
>   "build:mp-weixin": "uni build -p mp-weixin"
> }
> ```

---

## 6. 后端联调 (本地开发)

POC 需要调用后端 (`http://localhost:18080` Gateway),各端配置:

### 6.1 H5
- `manifest.json` 的 `h5.devServer.proxy` 已配 `/api` → `http://localhost:18080`
- 直接 `npm run dev:h5` 即可联调

### 6.2 微信小程序
- 微信开发者工具 → 详情 → **不校验合法域名**(开发期间)
- 或在 `manifest.json` 的 `mp-weixin` 加:
  ```json
  "mp-weixin": {
    "urlCheck": false
  }
  ```
- 同样依赖 `h5.devServer.proxy` 不会生效,**小程序里直接走** `uni.request('http://localhost:18080/...')` 需要后端允许跨域

### 6.3 App
- 走 `app-plus` 模块,manifest 配 `app-plus.ssl` 等
- 本地联调需要后端允许跨域 + 手机能访问到开发机 IP

---

## 7. 复用 web 代码的策略

| 层级 | 复用率 | 备注 |
|------|:------:|------|
| **types** | 100% | vite alias `@shared-types/*` → `../WebPage/src/types/*` |
| **stores** | 30-50% | web 用 localStorage + axios,小程序要适配。**当前未直接复用** |
| **utils** | 50% | 同上,token 改 uni.storage;request 改 uni.request |
| **views (web) → pages (uni-app)** | 0% | **必须改写**:div→view、Naive UI→内置组件、CSS 简化 |
| **api 函数** | 0% | web 用 axios,uni-app 用 uni.request,**本地 shim 适配** |

### 7.1 已移植 view
- `pages/fridge/index.vue` — 列表 + 临期统计 + 吃了/丢弃
- `pages/parcel/index.vue` — 列表 + 4 状态过滤 + 取件/收货
- `pages/workshop/index.vue` — 原料搜索 + 严格/主料模式 + 4 tier 分组

### 7.2 未移植(留待 H5 完整页)
- **登录页** (`pages/login/index.vue`):web 有完整表单 + 验证码 + JWT,小程序可简化但需要
- **个人资料** (`pages/profile/index.vue`):web 6 端点,小程序可只保留改昵称/手机
- **生活** (`pages/life/index.vue`):购物/纪念/提醒 3 tab
- **好友** (`pages/friend/index.vue`):分组 + 邀请 + 接受
- **设置** (`pages/settings/index.vue`):寄件人偏好 + 通知

### 7.3 改写规则 (web → uni-app)
| web | uni-app | 说明 |
|-----|---------|------|
| `<div>` | `<view>` | 块级 |
| `<span>`/`<p>` | `<text>` | 行内,必须包文本节点 |
| `<a href="...">` | `<navigator url="...">` 或 `uni.navigateTo` | |
| `v-model` | 同 | |
| `<n-button>` | `<button class="btn">` | 自定义 CSS |
| `<n-input>` | `<input>` | placeholder/v-model 通用 |
| `<n-data-table>` | `<view v-for>` | 列表卡片 |
| `display: grid` | 不用 | 小程序不支持 |
| `localStorage` | `uni.setStorageSync` | 同步 API |
| `fetch` / `axios` | `uni.request` | 异步,Promise 风格 |
| `window.location` | `uni.reLaunch` | |
| `atob(token)` | `uni.decodeBase64` 或 wx API | |
| `env(safe-area-inset-*)` | `--status-bar-height` 内置 | |

---

## 8. 已知坑

| 坑 | 表现 | 解决 |
|---|------|------|
| **axios 不兼容小程序** | 小程序报 `XMLHttpRequest is not defined` | 用本地 `src/api/request.ts` 替代,基于 `uni.request` |
| **localStorage 不存在** | 小程序报 `localStorage is not defined` | 用本地 `src/utils/token.ts`,基于 `uni.getStorageSync` |
| **@dcloudio/types 缺失** | TS 编译报 `uni.*` 不存在 | `tsconfig.json` 已加 `"types": ["@dcloudio/types"]` |
| **pages.json 必须存在** | HBuilderX 打不开项目 | 已在 `src/pages.json` |
| **manifest.json 必须存在** | HBuilderX 打不开项目 | 已在 `src/manifest.json` |
| **tabBar iconPath 找不到** | 编译报 `static/tabbar/xxx.png` 不存在 | 当前用纯文字 tab,后续加图标 |
| **@shared-api 拉到 axios** | 小程序报 axios 找不到 | **不要用** `@shared-api/fridge`,用本地 `@/api/fridge` |
| **vite.config CJS 互操作** | `uni is not a function` | 插件用 `await import()` + `.default.default` 取函数 (esbuild bundle 导致双层 default) |
| **缺少 index.html** | `Could not resolve entry module "index.html"` | 项目根目录需要 `index.html` (uni-app H5 入口) |
| **vite alias `@` 缺失** | TS 报 `Cannot find module '@/api/...'` | `vite.config.ts` 已加 `'@': src/` |
| **HBuilderX 缓存陈旧** | 改了文件不生效 | 菜单 → **运行** → **停止运行**,再启动 |

---

## 9. 验证清单

跑起来后,逐项验证:

- [ ] H5 模式 3 个 tab 都能切
- [ ] 冰箱 tab 显示临期统计数字 (调 `/api/fridge/items/expiring`)
- [ ] 冰箱 tab 列出 item (调 `/api/fridge/items`)
- [ ] 点了"吃了"按钮,item 数量 -1
- [ ] 驿站 tab 默认全部,切到"待取件"只显示 PENDING_PICKUP
- [ ] 调酒台 tab 加载原料 (调 `/api/workshop/ingredients`)
- [ ] 选几个原料 → 点推荐 → 显示 4 tier 分组结果
- [ ] 401 自动 refresh 一次,然后强制重登 (后端无 token 调 `/api/auth/me`)
- [ ] 微信小程序模式同样过一遍
- [ ] 改了 `../WebPage/src/types/fridge.ts` 字段,POC 实时反映(共享 types 验证)

---

## 10. 后续

- 加 tabBar 图标 (放 `static/tabbar/*.png`)
- 加登录页 (`pages/login/index.vue`)
- 加 Profile 简化版
- 把 web 的 `useFridgeData` 之类的 composable 抽到 `@shared-utils/composables/`,在 POC 复用
- 评估是否把 `auth store` 改造成 uni-app 友好版
