-- 冰箱模板表
-- 每个用户可保存多套模板（门型 + 各温区层数 + 门搁板数），并激活一个作为当前默认模板
-- 系统预置模板 owner_id = NULL，作为新用户首次激活时的"蓝本"
-- 一个用户同时只能有一个 is_default = TRUE 的模板
CREATE TABLE fridge_template (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT,
    name VARCHAR(64) NOT NULL,
    layout VARCHAR(32) NOT NULL,
    fridge_layers SMALLINT NOT NULL DEFAULT 3,
    freezer_layers SMALLINT NOT NULL DEFAULT 2,
    chiller_layers SMALLINT NOT NULL DEFAULT 0,
    door_shelf_count SMALLINT NOT NULL DEFAULT 3,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_fridge_template_layout CHECK (layout IN ('CLASSIC','BOTTOM_FREEZER','SIDE_BY_SIDE','THREE_DOOR')),
    CONSTRAINT chk_fridge_template_fridge_layers CHECK (fridge_layers BETWEEN 1 AND 5),
    CONSTRAINT chk_fridge_template_freezer_layers CHECK (freezer_layers BETWEEN 1 AND 5),
    CONSTRAINT chk_fridge_template_chiller_layers CHECK (chiller_layers BETWEEN 0 AND 3),
    CONSTRAINT chk_fridge_template_door_shelf_count CHECK (door_shelf_count BETWEEN 0 AND 5)
);

-- 系统预置模板不绑定 owner（NULL），用户模板 owner_id 必填
CREATE INDEX idx_fridge_template_owner ON fridge_template(owner_id);
CREATE INDEX idx_fridge_template_system ON fridge_template(is_system);

-- 一个用户同时只能有一个 is_default = TRUE 的模板（owner_id IS NOT NULL 时）
-- 系统预置没有 owner，全局可以同时存在多个 is_default = FALSE 的预设
CREATE UNIQUE INDEX uq_fridge_template_default_per_user
    ON fridge_template(owner_id)
    WHERE is_default = TRUE AND owner_id IS NOT NULL;

-- 同一 owner 下模板名不可重
CREATE UNIQUE INDEX uq_fridge_template_owner_name
    ON fridge_template(owner_id, name)
    WHERE owner_id IS NOT NULL;

-- 4 套系统预置模板
INSERT INTO fridge_template (owner_id, name, layout, fridge_layers, freezer_layers, chiller_layers, door_shelf_count, is_default, is_system) VALUES
    (NULL, '经典单开（冷冻在上）',   'CLASSIC',         3, 2, 0, 0, FALSE, TRUE),
    (NULL, '冷冻在下',              'BOTTOM_FREEZER',  3, 2, 0, 0, FALSE, TRUE),
    (NULL, '对开门',                'SIDE_BY_SIDE',    3, 3, 0, 4, FALSE, TRUE),
    (NULL, '法式三门（冷藏+解冻+冷冻）', 'THREE_DOOR',  3, 2, 1, 4, FALSE, TRUE);
