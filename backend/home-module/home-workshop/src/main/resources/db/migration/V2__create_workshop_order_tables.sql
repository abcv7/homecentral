-- =====================================================
-- 订单 / 状态机 / 评价 / 通知 / 扣减日志
-- =====================================================

-- 5) 点单主表 (5 步状态机 + 2 退出)
CREATE TABLE workshop_drink_order (
    id              BIGSERIAL PRIMARY KEY,
    requester_id    BIGINT NOT NULL,                          -- 谁想喝
    maker_id        BIGINT NOT NULL,                          -- 谁做
    group_id        BIGINT,                                   -- 通过哪个好友组
    cocktail_id     BIGINT NOT NULL REFERENCES workshop_cocktail(id),
    message         VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    urge_count      INT NOT NULL DEFAULT 0,
    email_sent      BOOLEAN NOT NULL DEFAULT FALSE,
    requested_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    responded_at    TIMESTAMPTZ,
    making_at       TIMESTAMPTZ,
    ready_at        TIMESTAMPTZ,
    rated_at        TIMESTAMPTZ,
    cancel_reason   VARCHAR(200),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_status CHECK (status IN
        ('PENDING', 'ACCEPTED', 'MAKING', 'READY', 'RATED', 'DECLINED', 'CANCELLED')),
    CONSTRAINT chk_order_distinct_users CHECK (requester_id <> maker_id)
);
CREATE INDEX idx_workshop_order_requester ON workshop_drink_order(requester_id, status, requested_at DESC);
CREATE INDEX idx_workshop_order_maker ON workshop_drink_order(maker_id, status, requested_at DESC);
CREATE INDEX idx_workshop_order_cocktail ON workshop_drink_order(cocktail_id);

-- 6) 状态机日志 (含催单 / 调量 / 评价)
CREATE TABLE workshop_drink_order_log (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES workshop_drink_order(id) ON DELETE CASCADE,
    from_status     VARCHAR(20),
    to_status       VARCHAR(20) NOT NULL,
    action_type     VARCHAR(20) NOT NULL DEFAULT 'STATUS',
    operator_id     BIGINT NOT NULL,
    payload         JSONB,
    note            VARCHAR(200),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workshop_log_order ON workshop_drink_order_log(order_id, created_at DESC);
CREATE INDEX idx_workshop_log_type ON workshop_drink_order_log(action_type, created_at DESC);

-- 7) 订单原料 (planned + actual, 男生可改)
CREATE TABLE workshop_drink_order_item (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES workshop_drink_order(id) ON DELETE CASCADE,
    ingredient_id       BIGINT NOT NULL REFERENCES workshop_ingredient(id),
    planned_amount_ml   NUMERIC(8,2) NOT NULL,
    actual_amount_ml    NUMERIC(8,2) NOT NULL,
    is_main             BOOLEAN NOT NULL DEFAULT FALSE,
    deduction_applied   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workshop_order_item_ing UNIQUE (order_id, ingredient_id)
);
CREATE INDEX idx_workshop_oi_order ON workshop_drink_order_item(order_id);

-- 8) 评价 (口感 + 形象 + overall 自动算)
CREATE TABLE workshop_order_rating (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL UNIQUE REFERENCES workshop_drink_order(id) ON DELETE CASCADE,
    user_id             BIGINT NOT NULL,
    taste_score         SMALLINT NOT NULL,
    appearance_score    SMALLINT NOT NULL,
    overall_score       NUMERIC(3,2) GENERATED ALWAYS AS
                            ((taste_score + appearance_score) / 2.0) STORED,
    comment             TEXT,
    rated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_taste_score CHECK (taste_score BETWEEN 1 AND 5),
    CONSTRAINT chk_appearance_score CHECK (appearance_score BETWEEN 1 AND 5)
);
CREATE INDEX idx_workshop_rating_user ON workshop_order_rating(user_id, rated_at DESC);
CREATE INDEX idx_workshop_rating_cocktail ON workshop_order_rating(order_id);

-- 9) 评价照片 (走 home-file 存 file_id)
CREATE TABLE workshop_rating_photo (
    id              BIGSERIAL PRIMARY KEY,
    rating_id       BIGINT NOT NULL REFERENCES workshop_order_rating(id) ON DELETE CASCADE,
    file_id         VARCHAR(64) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_workshop_photo_rating ON workshop_rating_photo(rating_id);

-- 10) 酒柜扣减日志 (审计)
CREATE TABLE workshop_bar_deduction_log (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    ingredient_id       BIGINT NOT NULL,
    bar_id              BIGINT,
    deducted_ml         NUMERIC(8,2) NOT NULL,
    remaining_after     NUMERIC(8,2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workshop_deduction_order ON workshop_bar_deduction_log(order_id);
CREATE INDEX idx_workshop_deduction_user ON workshop_bar_deduction_log(user_id, created_at DESC);

-- 11) 站内通知
CREATE TABLE workshop_notification (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    type                VARCHAR(30) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    body                TEXT,
    related_order_id    BIGINT,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notif_type CHECK (type IN
        ('ORDER_CREATED', 'URGE', 'ACCEPTED', 'DECLINED', 'MAKING', 'READY', 'RATED', 'CANCELLED'))
);
CREATE INDEX idx_workshop_notif_user_unread ON workshop_notification(user_id, is_read, created_at DESC);
CREATE INDEX idx_workshop_notif_order ON workshop_notification(related_order_id);
