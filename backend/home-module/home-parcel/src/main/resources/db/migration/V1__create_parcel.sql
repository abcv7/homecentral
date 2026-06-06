CREATE TABLE parcel (
    id BIGSERIAL PRIMARY KEY,
    courier_company VARCHAR(128) NOT NULL,
    tracking_number VARCHAR(255) NOT NULL,
    pickup_code VARCHAR(64),
    status VARCHAR(32) DEFAULT 'PENDING_PICKUP',
    remark TEXT,
    attachment_url VARCHAR(512),
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_parcel_status ON parcel(status);
CREATE INDEX idx_parcel_created_by ON parcel(created_by);
CREATE INDEX idx_parcel_tracking ON parcel(tracking_number);
