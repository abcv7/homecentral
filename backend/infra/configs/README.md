# 总配置（Master Configuration）使用指南

## 概述

`configs/` 目录是本项目的**总配置**。按 profile 拆分，每个 profile 是同一套基础设施的不同实例（不同 IP/账号/密码）。

切换环境 = 切换 profile = 改一个文件。

## 文件清单

| 文件 | 用途 | 何时改 |
|------|------|--------|
| `common.yml` | 跨 profile 共享的固定项（schema 前缀、AI base-url 等） | 几乎不改 |
| `local.yml` | 当前默认 profile：本地 IDE + 远程共享 infra (140.210.15.186) | 改远程 IP / 凭据时 |
| `staging.yml` | 预发布 profile（模板） | 启用 staging 时填入实际值 |
| `prod.yml`  | 生产 profile（模板，全部用 `${ENV}` 占位） | 上线前填入环境变量 |

## Profile 切换步骤

### 方式 A：硬切换（最简单）

1. 编辑每个服务的 `application-local.yml`：
   ```yaml
   spring:
     config:
       import:
         - optional:file:../../infra/configs/staging.yml   # 改这里
         - optional:file:../../infra/configs/common.yml
   ```
2. 重新打包并启动服务。

### 方式 B：通过 SPRING_PROFILES_ACTIVE（推荐）

1. 让所有服务的 `application.yml`（非 `application-local.yml`）按 profile 加载：
   ```yaml
   spring:
     config:
       import:
         - optional:file:../../infra/configs/${spring.profiles.active:local}.yml
         - optional:file:../../infra/configs/common.yml
   ```
2. 启动时指定 profile：
   ```bash
   java -jar home-xxx.jar --spring.profiles.active=staging
   # 或
   java -jar home-xxx.jar -Dspring.profiles.active=staging
   ```

### 方式 C：通过 INFRA_PROFILE 环境变量（最灵活）

1. `application.yml` 改用环境变量：
   ```yaml
   spring:
     config:
       import:
         - optional:file:../../infra/configs/${INFRA_PROFILE:local}.yml
         - optional:file:../../infra/configs/common.yml
   ```
2. 启动时：
   ```bash
   $env:INFRA_PROFILE = "staging"
   java -jar home-xxx.jar
   ```

## 添加新 profile

1. 复制 `local.yml` 为 `xxx.yml`（xxx = profile 名）
2. 修改 IP/端口/账号/密码
3. 让服务指向新 profile（见上文 3 种方式任选）

## 各服务 `application-local.yml` 模板

```yaml
spring:
  config:
    import:
      - optional:file:../../infra/configs/local.yml
      - optional:file:../../infra/configs/common.yml
  cloud:
    nacos:
      config:
        import-check:
          enabled: false
  datasource:
    url: jdbc:postgresql:...   # 业务 schema 仍由各服务自己声明
  ...
```

> 注：`application-local.yml` 内的 datasource url 是业务 schema 专属
> （如 `home_parcel`、`home_friend`），由各服务声明。
> 用户名/密码/IP 走总配置。

## 关键约定

1. **凭据不入 git** — `prod.yml` 全部用 `${ENV}` 占位，实际值走环境变量
2. **业务 schema 不入总配置** — 各服务自己的 schema（`home_auth` / `home_parcel` 等）在 `application-local.yml` 里声明
3. **Feign 内部 URL 不入总配置** — 跨服务调用 URL（`localhost:18107`）在消费方 `application-local.yml` 声明（直连模式，不走 Nacos）
4. **Seata 不开 AT 模式** — 当前无 `@GlobalTransactional` 使用，所有 profile 统一 `enable-auto-data-source-proxy: false`
