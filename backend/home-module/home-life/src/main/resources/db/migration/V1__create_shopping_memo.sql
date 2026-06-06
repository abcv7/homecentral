CREATE TABLE shopping_memo (
    id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    note TEXT,
    purchased BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_shopping_memo_purchased ON shopping_memo(purchased);
CREATE INDEX idx_shopping_memo_created_by ON shopping_memo(created_by);
