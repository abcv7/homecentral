CREATE TABLE api_account (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    api_key VARCHAR(256),
    customer VARCHAR(128),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, provider)
);

CREATE INDEX idx_api_account_user ON api_account(user_id);
