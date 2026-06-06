CREATE TABLE reminder_rule (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    cron_expression VARCHAR(128) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    last_triggered_at TIMESTAMPTZ,
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_reminder_rule_enabled ON reminder_rule(enabled);
CREATE INDEX idx_reminder_rule_created_by ON reminder_rule(created_by);
