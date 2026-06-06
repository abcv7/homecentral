# uni-app POC 报告

> Phase L.1-L.4 调酒台/冰箱跨端 POC 探索

## 目标

验证 uni-app 是否能**复用**主 Web 项目 (`frontend/WebPage`) 的 stores/utils/api/types，仅重写 view 层。

## 实施

| Phase | 内容 | 状态 |
|-------|------|------|
| L.1 | 起 `frontend/uniapp-poc/` (uni-app 3.0.0 + Vite + Vue 3 + TS + Pinia) | ✅ |
| L.2 | vite/tsconfig alias 指向主项目 `src/{stores,utils,api,types}` | ✅ |
| L.3 | fridge view 改写为 `<view>/<text>` (跨端 H5/小程序/App 兼容) | ✅ |
| L.4 | 本报告 + 决策门 | ✅ |

## 目录结构

```
frontend/uniapp-poc/
├── package.json         # uni-app 3 + Vue 3.5 + Pinia 2
├── tsconfig.json        # 继承 @vue/tsconfig
├── vite.config.ts       # @dcloudio/vite-plugin-uni + alias 到主项目
└── src/
    ├── App.vue          # uni-app 入口
    ├── main.ts          # Pinia 注册
    ├── pages.json       # 路由表 (单页: fridge)
    └── pages/fridge/
        └── index.vue    # 跨端版冰箱列表 (120 行 vs 主项目 931 行)
```

## 复用率统计 (单页面：fridge)

| 类别 | 主项目 WebPage | uni-app POC | 复用率 |
|------|---------------|-------------|--------|
| api/fridge.ts | 152 行 | 0 (直接 import 共享) | **100%** |
| types/fridge.ts | ~70 行 | 0 (直接 import 共享) | **100%** |
| composables/useFridgeData.ts | 80 行 | 0 (本视图未用) | 0% (本视图) |
| utils/fridgeExpand.ts | ~80 行 | 0 (未用) | 0% (本视图) |
| view (fridge/index.vue) | 931 行 | 120 行 | **13%** (重写为 uni-app 组件) |

**核心复用价值**：`api/` 和 `types/` 全部共享 → 不需要为 uni-app 写后端 wrapper。

## 改写差异 (fridge view)

| 元素 | Web | uni-app |
|------|-----|---------|
| 容器 | `<div>` | `<view>` |
| 文本 | `<span>` / `<p>` | `<text>` (必须 `<text>` 包裹文本节点) |
| 样式作用域 | `<style scoped>` | `<style scoped>` (注意 `scoped` 改为 H5 scoped) |
| 列表 | `v-for` over Vue | 同 (uni-app 直接支持 Vue 3 SFC) |
| 事件 | `@click` | `@tap` (H5 兼容) |
| 路由 | `vue-router` | `pages.json` 声明式 |
| 状态持久化 | Pinia + localStorage | Pinia + `uni.setStorage` |

## 跑起来

```bash
cd frontend/uniapp-poc
npm install
npm run dev:h5    # H5 模式
npm run build:h5  # 静态构建到 dist/build/h5
```

## 不可复用项 (需重写)

| 项目 | 原因 | 替代方案 |
|------|------|---------|
| Naive UI 组件库 | 桌面优先,移动端表现差 | uni-ui 或 uView Plus |
| VitePWA | uni-app 自带 manifest | uni-app manifest.json |
| 3D CSS (fridge 3D view) | Web 专属,小程序无 perspective | 改 2D 简化版 |
| Pinia 持久化插件 | 用 localStorage | `uni.setStorage` 包装 |
| HTML5 drag-drop | 小程序/App 无此 API | Touch event 模拟 |
| `env(safe-area-inset-*)` | web 专属 | uni-app `--status-bar-height` |

## 全量 8 view 复用工作量估算

| 视图 | Web 行数 | 改写预估 (1x) | 复用 | 主要工作 |
|------|---------|---------------|------|---------|
| dashboard | 200 | 80 (uni-app 卡片) | 中 | layout 适配 |
| parcel | 600 | 350 | 中 | 移动端列表 + 拖拽→touch |
| life | 350 | 200 | 中 | 3 tab 简化为 swiper |
| fridge | 931 | 120 (本 POC) | **低** | 3D → 2D |
| workshop | 250 | 150 | 中 | Naive UI → uni-ui |
| friend | 200 | 120 | 高 | 列表/分组/状态机 |
| profile | 180 | 100 | 中 | 表单/邮箱 |
| settings | 150 | 80 | 中 | 表单/密码 |

**总估算：8 view 重写 ≈ 14 人日**（中等复杂度的 PWA → 小程序迁移参考）

## 决策门 (Day 3 末)

**问题：要不要正式做 uni-app 小程序版本？**

| 维度 | 评估 |
|------|------|
| 复用价值 | API + types 全复用 → **中** |
| 移动端体验 | uni-app 编译到小程序体积小、性能接近原生 → **中** |
| 维护成本 | uni-app 升级周期短(每年 1-2 个大版本),锁版本成本 → **中** |
| 需求场景 | 当前主用户在 PWA (web), 已有"添加到主屏" → **低** |
| 投入产出 | 14 人日换 8 view 小程序可用 → 短期内 ROI **低** |

**结论**：POC 完成,代码入仓 `feature/uniapp-poc` 分支。
**暂不合并 main** — 等待真实"小程序分发"需求(目前无)。
若日后需要小程序/APP,本 POC 是良好起点。

## 相关 commit

- `git log feature/uniapp-poc` (L.1-L.4 commits)
- `git show feature/uniapp-poc:frontend/uniapp-poc/` 全部源码
- `docs/uniapp-poc-report.md` 本文件
