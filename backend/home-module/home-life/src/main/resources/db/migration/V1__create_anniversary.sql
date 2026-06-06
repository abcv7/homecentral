CREATE TABLE anniversary (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    remind_before_days INT DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_anniversary_event_date ON anniversary(event_date);
CREATE INDEX idx_anniversary_created_by ON anniversary(created_by);
