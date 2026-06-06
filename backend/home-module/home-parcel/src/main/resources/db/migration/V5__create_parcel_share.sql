CREATE TABLE parcel_share (
    id BIGSERIAL PRIMARY KEY,
    parcel_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(parcel_id, shared_with_user_id)
);

CREATE INDEX idx_parcel_share_parcel ON parcel_share(parcel_id);
CREATE INDEX idx_parcel_share_user ON parcel_share(shared_with_user_id);
