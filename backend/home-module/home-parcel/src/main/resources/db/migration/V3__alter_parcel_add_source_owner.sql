ALTER TABLE parcel ADD COLUMN source VARCHAR(16) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE parcel ADD COLUMN owner_name VARCHAR(64);
ALTER TABLE parcel ADD COLUMN owner_user_id BIGINT;
ALTER TABLE parcel ADD COLUMN arrived_date DATE;
ALTER TABLE parcel ADD COLUMN days_at_station INT DEFAULT 0;
ALTER TABLE parcel ADD COLUMN api_tracking_raw JSONB;

CREATE INDEX idx_parcel_source ON parcel(source);
CREATE INDEX idx_parcel_owner_user_id ON parcel(owner_user_id);
CREATE INDEX idx_parcel_days_at_station ON parcel(days_at_station DESC);
