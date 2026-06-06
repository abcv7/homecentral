-- =============================================
-- 02-init-flyway-history.sql
-- 永久脚本：在已建表的 home_* schema 上登记 flyway_schema_history，
-- 让 Spring Boot 启动时 Flyway 不会重复跑迁移
--
-- 用法：
--   psql "postgresql://pgsql:XGYnJPysCNJsLeea@140.210.15.186:65432/home" \
--        -f 02-init-flyway-history.sql
--
-- 适用场景：
--   - 迁移到新服务器后用 Java 工具 (ApplyMigrations.java) 跑了 SQL
--   - 备份恢复后 schema 已有表但 flyway_schema_history 缺失
--   - 重新跑 CI 流水线时确保 Flyway 不会因为缺历史而报错
--
-- 顺序：必须先 01-init-schemas.sql (建 schema) → 02 (跑迁移) → 03 (建本历史表)
-- =============================================

-- home_auth: 3 migrations
CREATE TABLE IF NOT EXISTS home_auth.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_auth.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1', 'create member', 'SQL', 'V1__create_member.sql', NULL, 'pgsql', 0, TRUE),
    (2, '2', 'alter member add phone', 'SQL', 'V2__alter_member_add_phone.sql', NULL, 'pgsql', 0, TRUE),
    (3, '3', 'alter member add email', 'SQL', 'V3__alter_member_add_email.sql', NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_parcel: 9 migrations (V7 有两个)
CREATE TABLE IF NOT EXISTS home_parcel.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_parcel.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1',  'create parcel',                     'SQL', 'V1__create_parcel.sql',                          NULL, 'pgsql', 0, TRUE),
    (2, '2',  'alter parcel add deleted',          'SQL', 'V2__alter_parcel_add_deleted.sql',                NULL, 'pgsql', 0, TRUE),
    (3, '3',  'alter parcel add source owner',     'SQL', 'V3__alter_parcel_add_source_owner.sql',           NULL, 'pgsql', 0, TRUE),
    (4, '4',  'create couple binding',             'SQL', 'V4__create_couple_binding.sql',                   NULL, 'pgsql', 0, TRUE),
    (5, '5',  'create parcel share',               'SQL', 'V5__create_parcel_share.sql',                     NULL, 'pgsql', 0, TRUE),
    (6, '6',  'create api account',                'SQL', 'V6__create_api_account.sql',                      NULL, 'pgsql', 0, TRUE),
    (7, '7',  'create pending recognition',        'SQL', 'V7__create_pending_recognition.sql',              NULL, 'pgsql', 0, TRUE),
    (8, '7',  'noop',                              'SQL', 'V7__noop.sql',                                    NULL, 'pgsql', 0, TRUE),
    (9, '8',  'add product name',                  'SQL', 'V8__add_product_name.sql',                        NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_life: 3 migrations (V1×3, 不同表)
CREATE TABLE IF NOT EXISTS home_life.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_life.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1', 'create anniversary',    'SQL', 'V1__create_anniversary.sql',    NULL, 'pgsql', 0, TRUE),
    (2, '1', 'create reminder rule',  'SQL', 'V1__create_reminder_rule.sql',  NULL, 'pgsql', 0, TRUE),
    (3, '1', 'create shopping memo',  'SQL', 'V1__create_shopping_memo.sql',  NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_file: 1 migration
CREATE TABLE IF NOT EXISTS home_file.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_file.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1', 'create file metadata', 'SQL', 'V1__create_file_metadata.sql', NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_notification: 1 migration
CREATE TABLE IF NOT EXISTS home_notification.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_notification.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1', 'create notification', 'SQL', 'V1__create_notification.sql', NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_fridge: 5 migrations (V1, V2, V8, V9, V10)
CREATE TABLE IF NOT EXISTS home_fridge.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_fridge.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1',  'create fridge category',          'SQL', 'V1__create_fridge_category.sql',            NULL, 'pgsql', 0, TRUE),
    (2, '2',  'create fridge item',              'SQL', 'V2__create_fridge_item.sql',                NULL, 'pgsql', 0, TRUE),
    (3, '8',  'add fridge template',             'SQL', 'V8__add_fridge_template.sql',               NULL, 'pgsql', 0, TRUE),
    (4, '9',  'allow pending zone null',         'SQL', 'V9__allow_pending_zone_null.sql',           NULL, 'pgsql', 0, TRUE),
    (5, '10', 'create fridge shopping history',  'SQL', 'V10__create_fridge_shopping_history.sql',   NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;

-- home_friend: 3 migrations
CREATE TABLE IF NOT EXISTS home_friend.flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT NOW(),
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
INSERT INTO home_friend.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
    (1, '1', 'create friend schema',                          'SQL', 'V1__create_friend_schema.sql',                              NULL, 'pgsql', 0, TRUE),
    (2, '2', 'create friend tables',                          'SQL', 'V2__create_friend_tables.sql',                              NULL, 'pgsql', 0, TRUE),
    (3, '3', 'migrate couple binding to friend relationship', 'SQL', 'V3__migrate_couple_binding_to_friend_relationship.sql',     NULL, 'pgsql', 0, TRUE)
ON CONFLICT DO NOTHING;
