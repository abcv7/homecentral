CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(128),
    role VARCHAR(32) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Admin user: password is 'admin123' BCrypt hashed
INSERT INTO member (username, password, nickname, role, enabled)
VALUES ('admin', '$2b$10$tUVjhA.ERnzIyGSFMihd2Ot4FlUsfoxsydcfr06CzAV49mkssx98m', '管理员', 'ADMIN', TRUE);
