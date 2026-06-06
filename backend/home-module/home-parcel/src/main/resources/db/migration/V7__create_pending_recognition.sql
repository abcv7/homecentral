CREATE TABLE pending_recognition (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    input_hash VARCHAR(64),
    result_json TEXT,
    failure_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_pending_rec_user_status
    ON pending_recognition(user_id, status);

CREATE INDEX idx_pending_rec_expires
    ON pending_recognition(expires_at)
    WHERE status IN ('processing', 'completed');

CREATE INDEX idx_pending_rec_user_created
    ON pending_recognition(user_id, created_at DESC);
