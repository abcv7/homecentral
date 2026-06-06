# AGENTS.md

## Project Overview

Self-hosted home central control system (parcel, shopping memos, anniversaries, reminders).

## Build & Verify

```bash
# JDK 25 + Maven 构建
$env:JAVA_HOME = "...\environment\jdk-25"
& "...\environment\apache-maven-home\bin\mvn.cmd" -f pom.xml test

# 前端构建
cd frontend\WebPage && npm run build
```

## Structure

- `home-module-api/`: DTO, VO, enums, Feign interfaces (8 API modules)
- `home-module/`: 7 microservices (auth, parcel, life, file, notification, fridge, friend)
- `home-gateway/`: Spring Cloud Gateway entry
- `frontend/WebPage/`: Vue 3 + TypeScript + Naive UI

## Module Boundaries

- `home-module-api/*`: 放跨服务的 DTO/VO/枚举/Feign 接口
- `home-module/*`: 业务微服务，**不放**跨服务 DTO 或 Feign 契约
- 若 API 签名变化，必须同时更新：① `home-module-api` 契约 → ② 服务提供方 → ③ 服务消费方

## Services

| Module | Purpose | Port | Entry Class |
|--------|---------|------|-------------|
| home-auth | Login, JWT token, member identity, email | 18101 | HomeAuthApplication.java |
| home-parcel | Parcel entry, pickup status, friend-based visibility | 18102 | HomeParcelApplication.java |
| home-life | Shopping memos, anniversaries, reminders | 18103 | HomeLifeApplication.java |
| home-file | MinIO object storage | 18104 | HomeFileApplication.java |
| home-notification | Email gateway (QQ SMTP 465 SSL) | 18105 | HomeNotificationApplication.java |
| home-fridge | 冰箱食材管理、分类、拍照识别、临期统计、采购 | 18106 | HomeFridgeApplication.java |
| **home-friend** | 好友关系 + 自定义分组（FRIEND/COUPLE/FAMILY/CUSTOM） | 18107 | HomeFriendApplication.java |
| home-ai | 公共 AI 抽象（OpenAI VLM 通用调用） | - | AiClient.java |
| home-gateway | API routing | 18080 | HomeGatewayApplication.java |

## Infra (Profile 化总配置)

**位置**：`backend/infra/configs/{local,staging,prod,common}.yml` + `README.md` + `MIGRATION.md`

| 文件 | 用途 |
|------|------|
| `configs/local.yml` | 当前生效（远程 140.210.15.186）|
| `configs/staging.yml` | 模板（`STAGING_HOST` / `CHANGE_ME` 占位）|
| `configs/prod.yml` | 模板（全部 `${ENV}` 占位 + nacos seata registry）|
| `configs/common.yml` | 跨 profile 共享（schema 前缀、SiliconFlow base-url）|
| `configs/README.md` | 3 种 profile 切换方式 + 添加新 profile 步骤 |
| `configs/MIGRATION.md` | 13 中间件迁移清单 + 6 步流程 + 7 常见迁移坑 |
| `infra-config.yml` | 改写为 shim（import 转发到 `configs/local.yml` + `configs/common.yml`，向后兼容）|

**3 种 Profile 切换方式**：
- A. 硬切换：编辑 `application-local.yml` 的 import 路径
- B. `spring.profiles.active=staging` 启动
- C. `INFRA_PROFILE` 环境变量

**服务入口**：每个服务 `src/main/resources/application-local.yml` 的 `spring.config.import` 指向：
- 微服务（`backend/home-module/home-xxx/`）：`optional:file:../../infra/configs/local.yml` + `../../infra/configs/common.yml`
- Gateway（`backend/home-gateway/`）：`optional:file:../infra/configs/local.yml` + `../infra/configs/common.yml`

**凭据策略**：`prod.yml` 全部 `${ENV}` 占位（不入 git）；`local.yml` 含远程真实凭据（仅本地开发用）

**业务 schema 声明**：各服务 `application-local.yml` 声明自己 schema（`app.datasource.schema: home_auth` / `home_parcel` / `home_life` / `home_file` / `home_notification` / `home_fridge` / `home_friend`）

**Feign 内部 URL**：跨服务调用 URL 在消费方 `application-local.yml` 显式声明（直连模式，非 LB）

## Infra (Docker 本地基础设施)

```bash
cd infra && docker compose up -d
```

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 65432 | homecentral / homecentral123 |
| Redis | 16379 | - |
| RabbitMQ | 15672/25672 | homecentral / homecentral123 |
| MinIO | 19000/19001 | homecentral / homecentral123 |
| Nacos | 18081 | nacos / nacos |

## Tech Stack

| Component | Version |
|-----------|---------|
| JDK | **25.0.3 LTS** (OpenJDK Microsoft) — 路径: `environment/jdk-25` |
| Maven | 3.9.13 — 路径: `environment/apache-maven-home` |
| Spring Boot | **4.0.3** |
| Spring Cloud | **2025.1.1** |
| Spring Cloud Alibaba | **2025.1.0.0** |
| Spring AI | 2.0.0-M6 (已定义未使用) |
| MyBatis-Plus | **3.5.16** (`mybatis-plus-spring-boot4-starter`) |
| Flyway | 11.8.0 |
| Lombok | **已移除** — 全部实体/服务使用手动 getter/setter + 构造函数 |
| Vue | 3.x + Vite 8 + Naive UI |
| Database | PostgreSQL 16 |

## Coding Conventions

- Service interfaces: `I*Service`, impl: `*ServiceImpl`
- Package layering: `controller`, `service`, `service/impl`, `mapper`, `entity`, `dto`, `vo`
- API responses: `Result<T>` or `Result<Page<T>>`
- **不使用 Lombok** — 所有实体和服务类使用手动编写的 getter/setter/构造函数
- 所有 Controller 测试使用 `@WebMvcTest` from `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- Controller 测试中 `ObjectMapper` 使用 `new ObjectMapper()` 而非 `@Autowired`
- 测试使用 `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` 纯 Mockito 模式
- Prefer minimal edits, do not refactor unrelated code

## JDK 25 注意事项

- **Lombok**: 不可用。所有实体类需要手写 getter/setter，服务类需要手写构造函数
- **Mockito**: 使用 `mock-maker-subclass`（在 `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` 中配置）
- **byte-buddy-agent**: JDK 25 限制 JVM Attach API，Mockito inline mock maker 无法工作

## Module Update Order

Cross-service changes: `home-module-api` → provider service → consumer service → gateway → verify

## Transaction Rules

- Transaction boundaries belong in service-layer methods
- Same-class transactional entry must go through Spring proxy (inject the service interface)
- Never call transactional methods via `this.xxx()`

## Feign Rules

- Feign 声明与 Provider 返回模型、字段类型、时间格式必须严格一致
- 遇到时间字段（如 `LocalDateTime`）时，重点检查 `@JsonFormat` 与序列化兼容
- Feign 排查建议：先开启对应客户端 `loggerLevel: FULL`，再定位具体字段反序列化错误

## 快递（Parcel）业务规则

| 规则 | 说明 |
|------|------|
| 状态流转 | PENDING_PICKUP(待取件) → PICKED_UP(已取件) → RECEIVED(已收货) |
| 排序规则 | `days_at_station DESC`（到站天数越多排越前） |
| 归属人 | 自由文本输入，默认当前用户 |
| 数据来源 | `MANUAL`(手动录入) 或 `API`(快递100) |
| 删除限制 | **仅** `status=RECEIVED` 可软删除（不限 source） |
| 可见性 | 通过 `FriendClient.getVisibleUsers(userId)` 拉取所有 ACCEPTED 关系朋友的包裹 |
| 显式分享 | 同样可针对单个包裹分享给指定用户 |
| 快递100 API | 在设置页配置 API Key，支持物流轨迹查询 |

## 好友（Friend）业务规则

| 规则 | 说明 |
|------|------|
| 分组类型 | `FRIEND`(朋友) / `COUPLE`(情侣) / `FAMILY`(家人) / `CUSTOM`(自定义) |
| 关系状态 | `PENDING`(待对方接受) → `ACCEPTED`(已接受) / `REJECTED`(已拒绝) / `BLOCKED`(已拉黑) |
| 流程 | owner 发起 → friendUserId 接受 → 双方互见各自 ACCEPTED 关系 |
| 删除分组 | 硬删除；分组下所有 rel.group_id 置 NULL（关系本身保留） |
| 删除关系 | 硬删除（不分 owner 主动 / friend 主动，谁都可 unbind） |
| 接受通知 | friend 接受后自动给 owner 发邮件（home-notification/mail） |
| 采购邮件 | 采购确认时携带 `groupId`，邮件目标 = 该组所有 ACCEPTED 成员 |
| 唯一约束 | `(owner_id, friend_user_id)` 唯一索引防重复邀请 |
| 数据迁移 | V3 迁移脚本把 `home_parcel.couple_binding` 数据转入 `friend_relationship` (type=COUPLE) |

## 冰箱（Fridge）业务规则

| 规则 | 说明 |
|------|------|
| 存放区域 | `REFRIGERATED`(冷藏) / `FROZEN`(冷冻)，支持 `subZone` 自由文本（如"冷藏-中层"）|
| 状态流转 | `ACTIVE`(在用) → `CONSUMED`(已消耗) / `DISCARDED`(已丢弃) |
| 分类 | 独立表 `fridge_category`，`is_system=true` 预置 10 个分类（酒水/蔬菜/水果/生鲜/乳制品/肉类/调味品/主食/零食/其他），只读 |
| 排序 | `expiry_date ASC, created_at DESC`（临期优先）|
| 数据来源 | `MANUAL`(手动录入) 或 `AI`(拍照识别) |
| 临期高亮 | `<=3` 天红色、`<=7` 天黄色、过期红色 + 数字提示 |
| 拍照识别 | 前端先调 home-file 上传 → home-fridge/recognize 拿预填表单 → 批量入库 |
| 删除限制 | 软删除，无状态限制 |
| 采购邮件 | 确认时 `partnerEmail` 走组外通知；`groupId` 走组内 ACCEPTED 成员通知 |
| AI 抽象 | 调 `home-ai/AiClient`（OpenAI VLM）；key 统一下沉到 `infra-config.yml` |

## 个人资料（Profile）业务规则

| 规则 | 说明 |
|------|------|
| 字段 | `username`(只读) / `role` / `nickname`(可改) / `phone`(可改) / `email`(可改) / `password`(可改) |
| 邮箱变更 | 填新邮箱 → 6 位码发到**新邮箱** → 输入码 → 写入；新邮箱不可与其他用户重复 |
| 密码变更 | 6 位码发到**当前邮箱** → 输入码 + 新密码 → BCrypt 写入；改完自动登出 |
| 验证码 | Redis 存储，key=`auth:code:{purpose}:{email}`，TTL=300s，6 位数字（`SecureRandom`），一次性消费 |
| 邮件发送 | home-auth Feign → home-notification/MailClient.send() → QQ SMTP 465 SSL 真实发送（`mail.enabled=true`）|
| MemberVO | 增 `email` + `role` 字段（不破坏旧调用方，新字段可为 null）|
| admin 邮箱 | `abcv7@qq.com`（原 `abcv6@qq.com`，已 DB 更新）|

## 邮件（Email）业务规则

| 规则 | 说明 |
|------|------|
| 品牌 | **栖物集 · HOME CENTRAL**（暂定名，配置项 `home.notification.mail.from-nickname`）|
| 发件人 | `栖物集 <abcv6@qq.com>`，QQ SMTP 465 SSL，`mail.enabled=true` 真实发送 |
| 主题前缀 | `【栖物集】...` |
| 模板目录 | `home-notification/src/main/resources/templates/email/` |
| 模板替换 | `EmailTemplateService.render(path, Map)` 简单 `{{var}}` 占位符替换（不引 Thymeleaf）|
| 模板列表 | `verification-code.html`（验证码共用）+ `notification.html`（好友/采购通知共用）|
| 配色 | 暖橙渐变 `#fbbf24 → #f97316` + 强调红 `#ef4444` + 安全区淡橙 `#fff7ed` |
| 验证码样式 | 6 格独立数字盒（`EmailRenderSupport.renderCodeBoxes`），共 5min TTL |
| 验证码主题 | `【栖物集】您正在更换绑定邮箱` / `【栖物集】您正在修改登录密码` |
| 通知主题 | `【栖物集】好友接受了您的邀请` / `【栖物集】家庭采购清单已确认` |

### EmailMessage 继承体系（`home-notification-api`）

```
EmailMessage (abstract, Jackson @JsonTypeInfo+@JsonSubTypes)
├── VerificationCodeEmail (abstract)
│   ├── EmailChangeCodeEmail        type=email_change_code
│   └── PasswordChangeCodeEmail     type=password_change_code
├── FriendAcceptEmail               type=friend_accept
└── PurchaseNoticeEmail             type=purchase_notice
    └── Variant: PARTNER | GROUP
```

- 消费方：`mailClient.sendMessage(new EmailChangeCodeEmail(...))`
- 服务端 `EmailDispatcher` 用 `instanceof` 路由到模板并 `templateService.render(...)` → `mailService.send(...)`
- Jackson 多态：`@JsonTypeInfo(property="type")`（**注意 `visible=false`，否则子类需 `type` 字段**）
- Feign 冲突：同名 `@FeignClient` 需加 `contextId`（已为 `MemberEmailClient`/`MemberInfoClient`/`AuthFeignClient` 加 `contextId`）

### 调用方改造点

| 调用方 | 旧 | 新 |
|--------|----|----|
| AuthServiceImpl (验证码) | inline HTML + `mailClient.send(MailSendRequest)` | `new EmailChangeCodeEmail(...).toRequest()` → `mailClient.sendMessage(...)` |
| FriendRelationshipServiceImpl (好友接受) | inline HTML | `new FriendAcceptEmail(...)` → `mailClient.sendMessage(...)`，并用 `MemberInfoClient` 拿昵称 |
| ShoppingServiceImpl (采购) | inline HTML + `buildEmailBody` | `new PurchaseNoticeEmail(variant, items)` → `mailClient.sendMessage(...)` |

## Current Status (2025-06)

### Tests: 257 total, 0 failures, JDK 25 ✅

| Module | Tests | Status |
|--------|:-----:|:------:|
| home-common-api | 4 | ✅ |
| home-auth | **38** (+21 Profile) | ✅ |
| home-ai | 5 | ✅ |
| home-parcel | 41 | ✅ |
| home-life | 24 | ✅ |
| home-file | 5 | ✅ |
| home-notification-api | **8** (+3 Greeting) | ✅ |
| home-notification | **21** (+2 MailService +11 模板) | ✅ |
| home-fridge | **97** (+1 采购邮件) | ✅ |
| **home-friend** | **14** (+1 好友邮件) | ✅ |

### Feature Status

| Feature | Backend | Frontend | Notes |
|---------|:-------:|:--------:|-------|
| JWT Auth (login/refresh) | ✅ | ✅ | Redis-backed token |
| **Profile (me/email/password)** | ✅ | ✅ | 6 端点 + Profile.vue + 邮箱验证码 |
| Parcel CRUD | ✅ | ✅ | 分页查询 |
| Pickup → Receive → Delete | ✅ | ✅ | 状态机流转 |
| Owner name + Source | ✅ | ✅ | 文本归属 + 手动/API标签 |
| Days at station sorting | ✅ | ✅ | 3d+红色, 2d黄色 |
| **Friend groups** | ✅ | ✅ | FRIEND/COUPLE/FAMILY/CUSTOM + 自定义色标 |
| **Friend invite/accept/reject/unbind** | ✅ | ✅ | PENDING→ACCEPTED 状态机 |
| **Accept email notification** | ✅ | ✅ | friend 接受 → 自动给 owner 发邮件 |
| Parcel sharing | ✅ | ❌ | 后端API就绪，前端待实现 |
| Express 100 tracking | ✅ | ❌ | Kuaidi100Tracker 就绪，前端配置页待实现 |
| Fridge CRUD | ✅ | ✅ | 冷藏/冷冻 + 子区域 + 分类 |
| Fridge Status flow | ✅ | ✅ | ACTIVE → CONSUMED/DISCARDED |
| Fridge quantity=N cards | ✅ | ✅ | 同 itemId 复制 N 张；点任一卡 consume-one |
| Fridge Categories | ✅ | ✅ | 10 系统预置 + 用户自定义 |
| Expiry highlight | ✅ | ✅ | ≤3天红、≤7天黄、过期红 |
| Fridge photo AI | ✅ | ✅ | home-ai/AiClient 调用 OpenAI VLM |
| Fridge 3D view + door rotate | ✅ | ✅ | CSS 3D perspective + SIDE_BY_SIDE 左/右门 rotateY |
| Fridge template system | ✅ | ✅ | 4 系统预置 + 用户自定义 + 硬删除 |
| Fridge 购物确认 + 历史复用 | ✅ | ✅ | 一次确认=一批；整批/单条复用 |
| **采购邮件 (组内)** | ✅ | ✅ | 选 groupId → ACCEPTED 成员发邮件 |
| **采购邮件 (组外)** | ✅ | ✅ | partnerEmail 字段触发 |
| AI common abstraction | ✅ | - | home-ai 模块：泛型 + 图片压缩 + JSON 解析 |
| Email gateway (QQ SMTP) | ✅ | - | home-notification；**真实发送模式已启用**（sender=abcv6@qq.com）|
| DB schemas initialized | ✅ | - | 7 schemas created |
| Flyway migrations | ✅ | - | V1-V12 |

## Key Files

- `pom.xml` — parent aggregator (Boot 4.0.3, Cloud 2025.1.1, MyBatis-Plus 3.5.16)
- `infra/docker-compose.yml` — local infrastructure
- `infra/postgres/init/` — schema initialization
- `home-module/home-parcel/` — Parcel module (core business logic)
- `home-module/home-auth/` — Auth module (JWT + Member)

## 执行纪律（不变）

- **需求不清必须先确认**：存在歧义或多种理解时，必须先提问确认
- **按歧义和风险决定是否等确认**：需求清晰且低风险 → 回显理解后直接执行
- **进度实时可见**：除单文件微调外，执行类任务必须显示结构化进度面板

### 任务分级

| 级别 | 适用场景 | 流程要求 |
|------|----------|----------|
| **L0** | Bug修复、配置微调、单文件改动 | 直接执行并验证 |
| **L1** | 多文件联动、中等功能开发、局部重构 | 最小上下文收集+明确步骤+验证 |
| **L2** | 跨模块改动、新模块、数据库/核心流程调整 | 完整闭环：澄清→方案→确认→实施→验证→交付 |

### 工作流

| 阶段 | 目标 | 关键动作 |
|------|------|----------|
| **A. 上下文收集** | 获得足够推进交付的信息 | 定位相关文件/模块/配置 |
| **B. 方案规划** | 把需求变成可执行方案 | 功能拆解、接口/数据流、异常处理 |
| **C. 实施** | 按确认方案落地 | 小步修改可验证；先读后改 |
| **D. 验证** | 证明交付物可信 | 按风险选择验证方式 |
| **E. 交付** | 说明结果与风险 | 完成了什么、改了什么、验证了什么 |

### 编码原则

- 实现优先级：正确性 > 可验证性 > 可维护性 > 优雅性
- 注释应说明意图、逻辑思路、约束条件、坑点与边界情况
- 重复不足三次不急于抽象；避免"聪明技巧"牺牲可读性

### 预提交检查清单

- 边界检查：跨服务契约位于 `home-module-api`
- 返回检查：新/改接口遵循 `Result<T>` 或 `Result<Page<T>>`
- 事务检查：同类事务入口不使用 `this` 调用
- Feign 检查：提供方响应与 Feign/DTO 完全对齐
- 构建检查：相关模块 Maven 构建已执行（JDK 25）
- 数据检查：涉及 schema 时已评估 SQL 脚本与初始化脚本的一致性
- **跨服务 Feign 检查**：消费方 `application-local.yml` 必须显式 `spring.cloud.openfeign.client.config.{name}.url`（直连模式），否则启动后 HTTP 调用报 `Load balancer does not contain an instance for the service home-xxx`

## 全栈联调结论 (2025-06)

8 模块 Playwright + REST + DB 三角验证：**8 通过 / 1 跳过（MinIO DOWN）/ 3 BUG 修复**

| 联调项 | 状态 | 关键发现 |
|--------|:----:|----------|
| Auth | ✅ | admin/admin123 → JWT → dashboard |
| Parcel CRUD | ✅ | 状态机 PENDING_PICKUP → PICKED_UP → RECEIVED → 软删 |
| Life | ✅ | 3 tab (购物/纪念/提醒) 全部通过 |
| File | ⏭️ | MinIO 19000 DOWN 跳过；冰箱拍照识别无法 E2E |
| Notification | ✅ | **修复 BUG #1**（`/api/notification` 500）|
| Fridge | ✅ | 增删改 + 3D + 配置 + 采购确认 |
| Friend | ✅ | **修复 BUG #2 / BUG #3**（可见性单向 / DELETE 路由 404）|
| Cross-service | ✅ | parcel 可见性 + 采购邮件（组） + basket clear |

## 全栈联调结论 (Profile 扩展, 2025-06)

个人资料 6 端点 + Profile.vue 全链路验证：**6/6 端点 REST 通过**；**邮件真实发送 2/2 通过**

| 端点 | 方法 | 状态 | 说明 |
|------|------|:----:|------|
| `/api/auth/me` | GET | ✅ | 返回 username/nickname/email/phone/role |
| `/api/auth/me/profile` | PUT | ✅ | 改 nickname / phone 立即返回最新 MemberVO |
| `/api/auth/me/email/code` | POST | ✅ | 发码到**新邮箱**（QQ SMTP 465 SSL 真实投递）|
| `/api/auth/me/email` | PUT | ✅ | 6 位码 + newEmail → 写入；防重复 |
| `/api/auth/me/password/code` | POST | ✅ | 发码到**当前邮箱** |
| `/api/auth/me/password` | PUT | ✅ | 6 位码 + newPassword → BCrypt + 自动登出 |

**前端路由**：`/profile`（MainLayout 下拉菜单 + 侧边栏入口）

**关键设计**：
- 验证码 key 统一小写邮箱，避免大小写不一致
- 邮箱变更：新邮箱发码（证明拥有新邮箱）+ 新邮箱不可与其他用户重复
- 密码变更：当前邮箱发码 + 修改后清除 token 强制重登

### 联调发现的 BUG 与修复

| BUG | 症状 | 根因 | 修复 |
|-----|------|------|------|
| **#1** | `/api/notification` 500 SCRAM 错误 | home-notification `application-local.yml` 缺 `datasource.username/password` | 显式声明 `username: pgsql` + `password: XGYnJPysCNJsLeea` |
| **#2** | `getVisibleUserIds` 单向（user 2/3 看不到 user 1 包裹）| 查询只过滤 `ownerId = userId`，漏掉 `friendUserId = userId` 方向 | 改为 `(ownerId = X OR friendUserId = X) AND status=ACCEPTED`；新增 `shouldReturnVisibleUserIdsAsFriendSide` 单测 |
| **#3** | 前端 `DELETE /api/friend/relationships/{id}` → 404 | 后端只暴露 `POST /unbind`，无 DELETE 路由 | 加 `@DeleteMapping("/{id}")` 别名，复用 `unbind()` 逻辑 |
| **#4** | home-parcel 调用 `FriendClient.getVisibleUsers` 报 LoadBalancer 找不到实例 | home-parcel `application-local.yml` 缺 `spring.cloud.openfeign.client.config.home-friend.url` | 追加 `url: http://localhost:18107` |

## ES Content-Type Compatibility

`elasticsearch-java 9.x` 默认发送 `Content-Type: application/vnd.elasticsearch+json; compatible-with=9`，但 ES 7.x 服务器不接受此值。

每个微服务的 `config/EsContentTypeCustomizer` 提供一个 `Rest5ClientOptions` bean，通过 `Rest5ClientOptions.Builder(RequestOptions.DEFAULT.toBuilder()).setHeader(...)` 将 `Content-Type` 和 `Accept` 覆盖为 `application/json`。

原理：`ElasticsearchTransportConfiguration.restClientTransport()` 通过 `ObjectProvider<Rest5ClientOptions>.getIfAvailable()` 获取该 bean，`Rest5ClientHttpClient` 在构造请求时 options 的 `setHeader()` 覆盖请求默认头。

当在 `infra/docker-compose.yml` 中添加 ES 容器后：
1. 在 application.yml 中配置 `spring.elasticsearch.uris`
2. ES Health 会自动变为 UP（已有 Rest5ClientOptions bean 修复 content-type）

## Notes

- Spring AI 2.0.0-M6 defined but NOT actually used in any module
- All clients route through home-gateway
- 所有文档与代码默认 UTF-8（无BOM）编码
- 禁止使用全局环境变量，所有工具路径在 `D:\Users\huangjingxin\桌面\HomeCentralControlSystem\environment`
