# HomeCentralControlSystem

## Project Goal
- Build a self-hosted home central control system for parcel management, shopping memos, anniversaries, reminders, and later third-party integrations.
- Phase 1 focuses on administrator login, parcel manual entry, parcel attachment upload, shopping memo CRUD, anniversary CRUD, and reminder rules.

## Architecture Baseline
- Backend uses `Java 21 + Spring Boot 3.5.0 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0`.
- Keep the Spring AI baseline on the stable line with `Spring AI 1.1.4`; add concrete model-provider starters only when an AI scenario is implemented.
- Repository structure stays `single repo + Maven multi-module aggregator`, but services run as direct microservices.
- Frontend is planned as `Vue 3` web admin plus `Flutter` mobile app for iOS and Android.

## Module Boundaries
- `home-module-api/`: all cross-service contracts, DTO, VO, enums, and Feign interfaces.
- `home-module/`: business microservices.
- `home-gateway/`: Spring Cloud Gateway entry point.
- `infra/`: local infrastructure orchestration such as Docker Compose and bootstrap SQL.
- Do not define cross-service DTO or Feign contracts inside `home-module/*`.

## Service List
- `home-auth`: accounts, login, token, roles, member identity.
- `home-parcel`: parcel entry, pickup state, parcel attachment reference, future courier API adaptation.
- `home-life`: shopping memos, shopping lists, anniversaries, generic reminder rules.
- `home-file`: object storage abstraction, upload metadata, MinIO integration.
- `home-notification`: reminder dispatch, site notifications, MQ consumers, future push expansion.

## API And Coding Rules
- Public APIs use unified wrappers such as `Result<T>` or `Result<Page<T>>` once shared contracts are introduced.
- Service interfaces follow `I*Service`; implementations follow `*ServiceImpl`.
- Keep package layering consistent: `controller`, `service`, `service/impl`, `mapper`, `entity`, `dto`, `vo`.
- Prefer minimal edits and do not refactor unrelated code while implementing a requirement.
- Update order for cross-service changes is fixed: `home-module-api` -> provider service -> consumer service -> gateway/frontend -> verification.

## Transaction Rules
- Transaction boundaries belong in service-layer methods.
- Same-class transactional entry must go through a Spring proxy; do not call transactional methods via `this.xxx()`.
- Prefer the project pattern of injecting the service interface when proxy-based transactional re-entry is required.

## Infrastructure Rules
- Phase 1 local infrastructure uses PostgreSQL, Redis, RabbitMQ, MinIO, and Nacos.
- Prefer PostgreSQL as the primary database with service-specific schemas in one instance during phase 1.
- Use RabbitMQ for async reminder/notification flows; do not introduce Seata in phase 1.
- Do not introduce Elasticsearch in phase 1; use PostgreSQL indexes first.
- Use Sentinel only where external integration or gateway protection is needed.

## Frontend Coordination
- All clients go through `home-gateway`.
- Keep API field names and contracts aligned with backend DTO/VO definitions.
- Web admin should validate the full golden path before mobile starts feature parity work.

## Delivery Focus For The Current Stage
- Scaffold the Maven aggregator and core service modules.
- Create the local infrastructure compose file.
- Create the project-level agent and project conventions.
- Delay full business entities, database mappers, and UI screens until the skeleton is stable.

## Verification Checklist
- Run `mvn -f pom.xml -DskipTests validate` after structural changes.
- Confirm `infra/docker-compose.yml` can start PostgreSQL, Redis, RabbitMQ, MinIO, and Nacos.
- When API contracts change, verify provider and consumer signatures together.
- When UI work starts, test the golden path in the browser before reporting completion.
