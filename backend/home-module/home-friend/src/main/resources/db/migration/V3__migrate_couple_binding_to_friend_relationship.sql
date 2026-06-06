-- V3__migrate_couple_binding_to_friend_relationship.sql
-- One-time data migration from home_parcel.couple_binding into home_friend.friend_relationship.
-- This is a best-effort migration; we keep the source rows for safety.
DO $$
DECLARE
    rec RECORD;
    new_group_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'home_parcel' AND table_name = 'couple_binding') THEN
        RETURN;
    END IF;

    FOR rec IN
        SELECT id, requester_id, target_id, status, created_at, updated_at
        FROM home_parcel.couple_binding
        WHERE status = 'ACCEPTED'
    LOOP
        INSERT INTO home_friend.friend_group (owner_id, name, type, color, created_at)
        VALUES (rec.requester_id, '情侣', 'COUPLE', '#FF6B81', rec.created_at)
        RETURNING id INTO new_group_id;

        INSERT INTO home_friend.friend_relationship
            (owner_id, friend_user_id, group_id, status, created_at, responded_at)
        VALUES (rec.requester_id, rec.target_id, new_group_id, 'ACCEPTED', rec.created_at, rec.updated_at)
        ON CONFLICT (owner_id, friend_user_id) DO NOTHING;
    END LOOP;
END $$;
