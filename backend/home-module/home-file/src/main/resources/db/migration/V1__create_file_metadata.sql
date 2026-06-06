CREATE TABLE file_metadata (
    id BIGSERIAL PRIMARY KEY,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    original_filename VARCHAR(255),
    content_type VARCHAR(128),
    file_size BIGINT,
    business_type VARCHAR(64),
    business_id VARCHAR(64),
    bucket VARCHAR(128) DEFAULT 'homecentral',
    uploaded_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_file_metadata_business ON file_metadata(business_type, business_id);
CREATE INDEX idx_file_metadata_uploaded_by ON file_metadata(uploaded_by);
