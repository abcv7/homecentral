CREATE TABLE couple_binding (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(requester_id, target_id)
);

CREATE INDEX idx_couple_requester ON couple_binding(requester_id);
CREATE INDEX idx_couple_target ON couple_binding(target_id);
