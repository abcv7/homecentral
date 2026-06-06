-- 食材条目表
-- 一个用户可能在多门冰箱上重复名字；为简化允许重名，删除/状态变化时按 ID 操作
CREATE TABLE fridge_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    zone VARCHAR(20) NOT NULL,
    sub_zone VARCHAR(50),
    category_id BIGINT REFERENCES fridge_category(id) ON DELETE SET NULL,
    quantity NUMERIC(10,2),
    unit VARCHAR(20),
    purchase_date DATE,
    expiry_date DATE,
    image_url TEXT,
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ai_confidence JSONB,
    ai_raw JSONB,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    consumed_at TIMESTAMPTZ,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fridge_item_zone ON fridge_item(zone);
CREATE INDEX idx_fridge_item_category ON fridge_item(category_id);
CREATE INDEX idx_fridge_item_expiry ON fridge_item(expiry_date);
CREATE INDEX idx_fridge_item_status ON fridge_item(status);
CREATE INDEX idx_fridge_item_created_by ON fridge_item(created_by);
CREATE INDEX idx_fridge_item_status_expiry ON fridge_item(status, expiry_date);
