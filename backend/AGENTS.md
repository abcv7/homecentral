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

- `home-module-api/`: DTO, VO, enums, Feign interfaces (6 API modules)
- `home-module/`: 5 microservices (auth, parcel, life, file, notification)
- `home-gateway/`: Spring Cloud Gateway entry
- `frontend/WebPage/`: Vue 3 + TypeScript + Naive UI

## Module Boundaries

- `home-module-api/*`: 放跨服务的 DTO/VO/枚举/Feign 接口
- `home-module/*`: 业务微服务，**不放**跨服务 DTO 或 Feign 契约
- 若 API 签名变化，必须同时更新：① `home-module-api` 契约 → ② 服务提供方 → ③ 服务消费方

## Services

| Module | Purpose | Port | Entry Class |
|--------|---------|------|-------------|
| home-auth | Login, JWT token, member identity | 18101 | HomeAuthApplication.java |
| home-parcel | Parcel entry, pickup status, couple binding | 18102 | HomeParcelApplication.java |
| home-life | Shopping memos, anniversaries, reminders | 18103 | HomeLifeApplication.java |
| home-file | MinIO object storage | 18104 | HomeFileApplication.java |
| home-notification | Reminder dispatch | 18105 | HomeNotificationApplication.java |
| home-gateway | API routing | 18080 | HomeGatewayApplication.java |

## Infra

```bash
cd infra && docker compose up -d
```

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 65432 | homecentral / homecentral123 |
| Redis | 16379 | - |
| RabbitMQ | 35672(AMQP) / 45670(Web) | homecentral / homecentral123 |
| MinIO | 19000/19001 | homecentral / homecentral123 |
| Nacos (HTTP) | 18081→8080 / 18848→8848 | nacos / nacos |
| Nacos (gRPC) | 19848→9848 | — |
| Seata TC | 18091→8091 | — |

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
- **同 name 多 Feign 客户端**：必须加 `contextId`，否则 Spring Cloud OpenFeign 报 `BeanDefinitionOverrideException`（`home-auth.FeignClientSpecification`）。已加：`memberEmailClient` / `memberInfoClient` / `parcelAuthFeignClient`

## 邮件（Email）业务规则

| 规则 | 说明 |
|------|------|
| 品牌 | **栖物集 · HOME CENTRAL**（暂定，配置项 `home.notification.mail.from-nickname`）|
| 发件人 | `栖物集 <abcv6@qq.com>`，QQ SMTP 465 SSL |
| 模板目录 | `home-notification/src/main/resources/templates/email/` |
| 模板替换 | `EmailTemplateService.render(path, Map)` 简单 `{{var}}` 占位符（不引 Thymeleaf）|
| 主题前缀 | `【栖物集】...` |
| 配色 | 暖橙渐变 `#fbbf24 → #f97316` + 强调红 `#ef4444` + 安全区淡橙 `#fff7ed` |
| 验证码样式 | 6 格独立数字盒（`EmailRenderSupport.renderCodeBoxes`）|

### EmailMessage 继承层级（`home-notification-api`）

```
EmailMessage (abstract, Jackson @JsonTypeInfo+@JsonSubTypes)
├── VerificationCodeEmail (abstract)
│   ├── EmailChangeCodeEmail        type=email_change_code
│   └── PasswordChangeCodeEmail     type=password_change_code
├── FriendAcceptEmail               type=friend_accept
└── PurchaseNoticeEmail             type=purchase_notice
    └── Variant: PARTNER | GROUP
```

- 消费方构造 `new XxxEmail(...)` 后调 `mailClient.sendMessage(msg)`，服务端 `EmailDispatcher` 用 `instanceof` 路由
- 主题与模板路径由子类 `getDefaultSubject()` / `getTemplatePath()` 提供
- Jackson 多态：`@JsonTypeInfo(property="type")` **必须 `visible=false`（默认）**，否则子类需要 `type` 字段
- 新增邮件类型：① API 加子类 + `@JsonTypeName` + `@JsonSubTypes.Type` 注册 ② `EmailDispatcher` 加 `instanceof` 分支

## 快递（Parcel）业务规则

| 规则 | 说明 |
|------|------|
| 状态流转 | PENDING_PICKUP(待取件) → PICKED_UP(已取件) → RECEIVED(已收货) |
| 排序规则 | `days_at_station DESC`（到站天数越多排越前） |
| 归属人 | 自由文本输入，默认当前用户 |
| 数据来源 | `MANUAL`(手动录入) 或 `API`(阿里云快递查询) |
| 删除限制 | **仅** `status=RECEIVED` 可软删除（不限 source） |
| 情侣绑定 | 一方发起 → 对方确认 → 绑定后互相可见全部包裹 |
| 显式分享 | 即使非情侣也可针对单个包裹分享给指定用户 |
| 阿里云快递 API | 在设置页配置 AppCode，支持物流轨迹查询（`AliyunExpressService.queryWithDiscern` 自动识别快递公司）|

## Current Status (2025-06)

### Tests: 252 total, 0 failures, JDK 25 ✅

| Module | Tests | Status |
|--------|:-----:|:------:|
| home-common-api | 4 | ✅ |
| home-auth | 38 | ✅ |
| home-ai | 5 | ✅ |
| home-parcel | 41 | ✅ |
| home-life | 24 | ✅ |
| home-file | 5 | ✅ |
| home-notification | **19** (+11 邮件模板) | ✅ |
| home-fridge | **97** (+1 采购邮件) | ✅ |
| home-friend | **14** (+1 好友邮件) | ✅ |

### Feature Status

| Feature | Backend | Frontend | Notes |
|---------|:-------:|:--------:|-------|
| JWT Auth (login/refresh) | ✅ | ✅ | Redis-backed token |
| Parcel CRUD | ✅ | ✅ | 分页查询 |
| Pickup → Receive → Delete | ✅ | ✅ | 状态机流转 |
| Owner name + Source | ✅ | ✅ | 文本归属 + 手动/API标签 |
| Days at station sorting | ✅ | ✅ | 3d+红色, 2d黄色 |
| Couple binding | ✅ | ✅ | 发起/确认/解除 |
| Parcel sharing | ✅ | ❌ | 后端API就绪，前端待实现 |
| Aliyun Express tracking | ✅ | ❌ | `AliyunExpressService` 就绪（`queryWithDiscern` / `queryByNumber`），前端 AppCode 设置页待实现 |
| **Parcel auto-import (10s+TTL)** | ✅ | ✅ | DB 持久化 + 前端 10s 倒计时 + 服务端 24h 兜底 |
| **Multi-task recognition panel** | ✅ | ✅ | 5 并发上限 + Toast 通知 + 浮动气泡 |
| DB schemas initialized | ✅ | - | 6 schemas created |
| Flyway migrations | ✅ | - | V1-V7 |

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
- 禁止使用全局环境变量，所有工具路径在 `D:\Users\30403\Desktop\HomeCentralControlSystem\environment`

## Gateway 5.x 配置注意

**症状：** Gateway 启动正常（端口监听、actuator health 正常），但所有路由返回 404。

**原因：** Spring Cloud Gateway 5.x 中 `GatewayProperties` 的配置前缀从 `spring.cloud.gateway` 变更为 `spring.cloud.gateway.server.webflux`。
若 routes 仍写在 `spring.cloud.gateway.routes` 下，Gateway 会静默忽略，不加载任何路由。

**修复：** routes 必须嵌套在 `spring.cloud.gateway.server.webflux.routes` 下：
```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: home-auth
              uri: http://localhost:18101
              predicates:
                - Path=/api/auth/**
```

**排查方法：** 访问 Gateway 的 `/actuator/gateway/routes` 端点，若返回空数组 `[]` 而非路由列表，即为该问题。

**注意：** `lb://` 前缀需要 `spring-cloud-starter-loadbalancer` 依赖。网络受限时改用直连 `http://localhost:18xxx`。

## 基础设施连接配置说明

### Nacos 端口选择（v3 gRPC 兼容）
Nacos v3 客户端使用 gRPC 进行服务注册，gRPC 端口 = `server-addr` 端口 + 1000。
`infra-config.yml` 中 `server-addr` 须使用 `18848`（而非 `18081`），因为：
- `18848 + 1000 = 19848` ✅ 匹配 Nacos 容器 gRPC 端口映射
- `18081 + 1000 = 19081` ❌ 与实际 gRPC 端口 19848 不匹配

### Seata
Seata Server 已部署在远程服务器（`140.210.15.186:18091`），使用 `registry.type: file` 直连。

各 microservices 通过 `infra-config.yml` 中的 `seata.service.grouplist.default` 配置连接。
需要为每个业务 schema 创建 `undo_log` 表（Seata AT 模式必需）。
当前无 `@GlobalTransactional` 使用时，可通过 `seata.enable-auto-data-source-proxy: false` 跳过代理。

### 数据库连接
- 外部数据库 `140.210.15.186:65432` 可能间歇性不可达（网络变动或防火墙限制）。
- Gateway 使用 `http://localhost:18xxx` 直连 URI 而非 `lb://` 前缀。
- 添加 `?sslmode=disable` 到 JDBC URL 可避免 PostgreSQL 驱动在服务端无 SSL 时的 EOF 错误（`java.io.EOFException` at `enableSSL`）。
- 若数据库不可达，`dbhub` 和微服务 Druid 连接池均会阻塞/超时，表现为服务启动成功但 HTTP 请求全部超时。

### 数据库连接
- 外部数据库 `140.210.15.186:65432` 可能间歇性不可达（网络变动或防火墙限制）。
- Gateway 使用 `http://localhost:18xxx` 直连 URI 而非 `lb://` 前缀。
- 添加 `?sslmode=disable` 到 JDBC URL 可避免 PostgreSQL 驱动在服务端无 SSL 时的 EOF 错误（`java.io.EOFException` at `enableSSL`）。
- 若数据库不可达，`dbhub` 和微服务 Druid 连接池均会阻塞/超时，表现为服务启动成功但 HTTP 请求全部超时。

## 常见故障排查速查

| 症状 | 最可能原因 | 验证方法 | 修复 |
|------|-----------|----------|------|
| Gateway 返回 404 | 路由配置前缀未使用 `server.webflux` | `GET /actuator/gateway/routes` 返回 `[]` | 改为 `spring.cloud.gateway.server.webflux.routes` |
| Auth 返回 403 | `SecurityFilterChain` 无 `@Order`，被 Boot 4.x 默认链覆盖 | 日志可见 `Using generated security password` | 添加 `@Order(0)` + `securityMatcher` |
| Auth 启动卡死/超时 | Seata TC 端口 18091 不可达 | 日志 `connect timed out: /140.210.15.186:18091` | 检查 Seata Server 是否运行；临时方案 `seata.enabled: false` |
| Auth 启动卡死/超时 | Nacos v3 gRPC 端口 19848 不可达 | 日志卡在 Nacos 注册阶段 | 检查 `server-addr` 是否为 18848（gRPC=19848）；临时方案 `nacos.discovery.enabled: false` |
| 服务启动 `undo_log table not exist` | 缺少 Seata AT 模式回滚表 | `in AT mode, undo_log table not exist` | 在每个业务 schema 创建 `undo_log` 表，或 `enable-auto-data-source-proxy: false` |
| 服务启动但 HTTP 超时 | DB 连接失败（SSL/网络） | 日志 `create connection SQLException` + EOF | 添加 `?sslmode=disable`，检查网络连通性 |
| 登录返回 500 密码错误 | 数据库密码 hash 与明文不匹配 | 日志 `RuntimeException: 用户名或密码错误` | 用 bcryptjs 重新生成 hash 并 UPDATE |
| 启动时 RabbitMQ 认证失败 | 凭据与服务端不匹配 | `AuthenticationFailureException: ACCESS_REFUSED` | 修正 `infra-config.yml` 中 `spring.rabbitmq.password` |
| Gateway ES health DOWN (media_type_header) | ES 7.x 不接受 `elasticsearch-java 9.x` 默认 Content-Type | `media_type_header_exception` | 添加 `EsContentTypeCustomizer` 覆盖为 `application/json` |
| Gateway ES health DOWN (localhost:9200) | Gateway 未从 `infra-config.yml` 继承 ES 配置 | `HttpHostConnectException: localhost:9200` | 在 `application-local.yml` 显式添加 `spring.elasticsearch.uris` |
| Gateway ES health DOWN (security_exception) | ES 认证凭据未从 `infra-config.yml` 继承 | `missing authentication credentials` | 在 `application-local.yml` 显式添加 `spring.elasticsearch.username/password` |
