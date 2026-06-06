-- =====================================================
-- 调酒台 / 点酒工作流 (home_workshop schema)
-- =====================================================
-- 数据来源: 外部 enjoycocktail GraphQL (P2 seed V13/V14/V15 灌入)
-- 独立业务: 不依赖 home-fridge 任何表
-- 字符集:   UTF-8
-- 时间字段: TIMESTAMPTZ (项目统一)
-- =====================================================

-- 1) 鸡尾酒主表
CREATE TABLE workshop_cocktail (
    id              BIGSERIAL PRIMARY KEY,
    name_zh         VARCHAR(150) NOT NULL,
    name_en         VARCHAR(200),
    name_alias      TEXT[],
    cover           TEXT,
    views           INT NOT NULL DEFAULT 0,
    recipe_zh       TEXT,
    method_zh       TEXT,
    aroma           VARCHAR(100),
    taste           VARCHAR(100),
    style           VARCHAR(100),
    scene           VARCHAR(100),
    history         TEXT,
    source_url      TEXT,
    last_synced_at  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workshop_cocktail_name_zh ON workshop_cocktail(name_zh);
CREATE INDEX idx_workshop_cocktail_name_en ON workshop_cocktail(name_en);

-- 2) 原料主表
CREATE TABLE workshop_ingredient (
    id                  BIGSERIAL PRIMARY KEY,
    name_zh             VARCHAR(100) NOT NULL,
    name_en             VARCHAR(150),
    aliases             TEXT[],
    default_bottle_ml   NUMERIC(10,2),
    cocktail_count      INT NOT NULL DEFAULT 0,
    last_synced_at      TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workshop_ingredient_name_zh ON workshop_ingredient(name_zh);
CREATE INDEX idx_workshop_ingredient_name_en ON workshop_ingredient(name_en);

-- 3) 鸡尾酒 ↔ 原料 (含主料标记 + 配方推荐用量)
CREATE TABLE workshop_cocktail_ingredient (
    cocktail_id         BIGINT NOT NULL REFERENCES workshop_cocktail(id) ON DELETE CASCADE,
    ingredient_id       BIGINT NOT NULL REFERENCES workshop_ingredient(id) ON DELETE CASCADE,
    is_main             BOOLEAN NOT NULL DEFAULT FALSE,
    planned_amount_ml   NUMERIC(8,2) NOT NULL,
    sort_order          INT NOT NULL DEFAULT 0,
    PRIMARY KEY (cocktail_id, ingredient_id)
);
CREATE INDEX idx_workshop_ci_ingredient ON workshop_cocktail_ingredient(ingredient_id);
CREATE INDEX idx_workshop_ci_main ON workshop_cocktail_ingredient(cocktail_id, is_main);

-- 4) 我的酒柜 (Q1: 独立表, 不与 fridge 混用)
CREATE TABLE workshop_user_bar (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    ingredient_id       BIGINT NOT NULL REFERENCES workshop_ingredient(id) ON DELETE CASCADE,
    bottle_capacity_ml  NUMERIC(10,2),
    remaining_ml        NUMERIC(10,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_bar_status CHECK (status IN ('ACTIVE', 'DEPLETED', 'DISCARDED'))
);
-- 同一用户同一原料 ACTIVE 状态唯一
CREATE UNIQUE INDEX uq_workshop_bar_user_ing_active
    ON workshop_user_bar(user_id, ingredient_id)
    WHERE status = 'ACTIVE';
CREATE INDEX idx_workshop_bar_user ON workshop_user_bar(user_id, status);
