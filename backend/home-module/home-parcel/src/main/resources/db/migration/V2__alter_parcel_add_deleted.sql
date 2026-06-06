ALTER TABLE parcel ADD COLUMN deleted BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_parcel_deleted ON parcel(deleted);
