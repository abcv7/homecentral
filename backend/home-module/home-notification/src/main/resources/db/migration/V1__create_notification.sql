CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    source_type VARCHAR(64),
    source_id VARCHAR(64),
    read BOOLEAN DEFAULT FALSE,
    notify_time TIMESTAMPTZ DEFAULT NOW(),
    user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_read ON notification(read);
CREATE INDEX idx_notification_notify_time ON notification(notify_time);
