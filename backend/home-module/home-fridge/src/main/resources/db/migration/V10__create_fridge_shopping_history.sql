-- 采购历史表
-- 一次确认购买 = 一个 batch（batch_id UUID），含若干条目
-- 永久保留，无 cron 清理
CREATE TABLE fridge_shopping_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    batch_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT REFERENCES fridge_category(id) ON DELETE SET NULL,
    sub_zone VARCHAR(50),
    quantity NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit VARCHAR(20),
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    partner_email VARCHAR(120),
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent_at TIMESTAMPTZ,
    purchased_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fridge_shopping_history_user ON fridge_shopping_history(user_id);
CREATE INDEX idx_fridge_shopping_history_batch ON fridge_shopping_history(batch_id);
CREATE INDEX idx_fridge_shopping_history_user_purchased ON fridge_shopping_history(user_id, purchased_at DESC);
