# 基础设施迁移指南

## 适用场景

- 中间件服务器迁移（IP/端口变更）
- 凭据轮换
- 切换部署环境（local → staging → prod）
- 数据库升级或拆分

## 迁移清单

迁移时检查并修改以下配置：

| 中间件 | 配置项 | 默认值 (local) | 必改项 |
|--------|--------|---------------|--------|
| PostgreSQL | `spring.datasource.url` | `jdbc:postgresql://140.210.15.186:65432/home?sslmode=disable` | ✅ host/port/db |
| PostgreSQL | `spring.datasource.username` | `pgsql` | ✅ 用户名 |
| PostgreSQL | `spring.datasource.password` | `XGYnJPysCNJsLeea` | ✅ 密码 |
| Redis | `spring.data.redis.host` / `port` / `password` | `140.210.15.186:16379` | ✅ |
| Nacos | `spring.cloud.nacos.discovery.server-addr` | `140.210.15.186:18848` | ✅ gRPC 端口 = server + 1000 |
| Nacos | `spring.cloud.nacos.discovery.username/password` | `nacos/...` | ✅ |
| Nacos | `spring.cloud.nacos.config.*` | 同上 | ✅ |
| Elasticsearch | `spring.elasticsearch.uris` | `http://140.210.15.186:19200` | ✅ |
| Elasticsearch | `spring.elasticsearch.username/password` | `elastic/...` | ✅ |
| RabbitMQ | `spring.rabbitmq.host/port/username/password` | `140.210.15.186:15672` | ✅ |
| Seata TC | `seata.service.grouplist.default` | `140.210.15.186:18091` | ✅ |
| MinIO | `app.minio.endpoint` | `http://140.210.15.186:19000` | ✅ |
| MinIO | `app.minio.access-key` | `emebjWWdY8w3hKwzU226` | ✅ |
| MinIO | `app.minio.secret-key` | `bVr6H6wJgJ7mknbGzBhZXS69yTCOAbxngDzYUkky` | ✅ |
| MinIO | `app.minio.bucket` | `homecentral` | ✅ 建议独立 bucket |
| OpenAI/SiliconFlow | `spring.ai.openai.api-key` | `sk-...` | ✅ |

## 迁移流程

### Step 1: 备份当前配置

```bash
cp -r backend/infra/configs backend/infra/configs.bak-$(date +%Y%m%d)
```

### Step 2: 创建新 profile

```bash
# 复制 local.yml 到新 profile
cp backend/infra/configs/local.yml backend/infra/configs/staging.yml
# 编辑 staging.yml，填入新环境的 IP/账号/密码
```

### Step 3: 验证新配置可达

```bash
# 测连通
Test-NetConnection <new-host> -Port <port>

# 测 DB
psql -h <new-host> -p 65432 -U pgsql -d home -c '\dt'

# 测 MinIO
curl -u <ak>:<sk> http://<host>:19000/
```

### Step 4: 切换服务 profile

按 `configs/README.md` 的 3 种方式选一，推荐方式 B/C。

### Step 5: 启动验证

1. 启动一个服务，先看启动日志（确认 seata / DB / redis 连通）
2. 跑后端测试：`mvn test`（不需要真连外部，但应能通过）
3. 跑联调：
   ```bash
   # Auth login
   curl -X POST http://localhost:18080/api/auth/login -d '{"username":"admin","password":"admin123"}'
   # Parcel list
   curl "http://localhost:18080/api/parcel?pageNum=1&pageSize=10" -H "X-User-Id: 1"
   ```

### Step 6: 旧 profile 归档

迁移成功后：
1. 把旧 profile 改名加后缀 `.deprecated-YYYYMMDD`
2. 在 `configs/README.md` 顶部记录归档原因

## 常见迁移坑

| 现象 | 原因 | 解决 |
|------|------|------|
| 启动卡在 Nacos 注册 | gRPC 端口 = server-addr + 1000，写错 | 18848 → gRPC 19848 |
| 启动报 `no password was provided` | 新 host 启用了 SCRAM，yml 没声明 username/password | 在 `application-local.yml` 显式写 `spring.datasource.username/password` |
| 启动报 `Failed to get available servers: service.vgroupMapping.default_tx_group configuration item is required` | seata yml 没加载（相对路径错误） | 改成 `../../infra/configs/local.yml` 相对路径 |
| 跨服务调用 `Load balancer does not contain an instance for the service home-xxx` | Feign 没配直连 URL | 在 `spring.cloud.openfeign.client.config.{name}.url` 加直连 |
| MinIO 报 `The specified bucket does not exist` | 新 bucket 没建 | home-file 启动时会自动 `makeBucket`（已内置） |
| 启动后 `home-auth` 报 `Using generated security password` | `SecurityFilterChain` 没 `@Order` | 加 `@Order(0)` + `securityMatcher` |
| 邮件 535 认证失败 | SMTP 凭据过期 | 重新生成 QQ 邮箱授权码，更新 `home.notification.mail` 配置 |

## 历史迁移

- **2026-06**: MinIO 凭据从 `D6gllgRwooN4qcHuVtWm/...` 切到 `emebjWWdY8w3hKwzU226/...`（老凭据保留在 `local.yml` 注释中作回滚用）
