-- V2__create_friend_tables.sql
-- Groups: each user owns 0..N groups (FRIEND/COUPLE/FAMILY/CUSTOM).
CREATE TABLE IF NOT EXISTS home_friend.friend_group (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'FRIEND',
    color VARCHAR(7),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_friend_group_type CHECK (type IN ('FRIEND', 'COUPLE', 'FAMILY', 'CUSTOM'))
);

CREATE INDEX IF NOT EXISTS idx_friend_group_owner ON home_friend.friend_group(owner_id);

-- Relationships: directed from owner -> friend. Status tracks accept flow.
CREATE TABLE IF NOT EXISTS home_friend.friend_relationship (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    group_id BIGINT REFERENCES home_friend.friend_group(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invite_email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    invite_email_sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    responded_at TIMESTAMPTZ,
    CONSTRAINT chk_friend_rel_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED')),
    CONSTRAINT uq_friend_rel_owner_friend UNIQUE (owner_id, friend_user_id)
);

CREATE INDEX IF NOT EXISTS idx_friend_rel_owner ON home_friend.friend_relationship(owner_id);
CREATE INDEX IF NOT EXISTS idx_friend_rel_friend ON home_friend.friend_relationship(friend_user_id);
CREATE INDEX IF NOT EXISTS idx_friend_rel_status ON home_friend.friend_relationship(status);
CREATE INDEX IF NOT EXISTS idx_friend_rel_owner_status ON home_friend.friend_relationship(owner_id, status);
