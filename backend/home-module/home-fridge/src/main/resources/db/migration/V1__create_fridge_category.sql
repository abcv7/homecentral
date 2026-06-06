-- 食材分类表
-- 同一用户下分类名唯一；系统预置分类（created_by = NULL）作为公共基础数据
CREATE TABLE fridge_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    sort_order INT DEFAULT 0,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fridge_category_user_name UNIQUE (name, created_by)
);

CREATE INDEX idx_fridge_category_created_by ON fridge_category(created_by);
CREATE INDEX idx_fridge_category_system ON fridge_category(is_system);

-- 系统预置分类种子
INSERT INTO fridge_category (name, icon, color, sort_order, is_system, created_by) VALUES
    ('酒水',     '🍺', '#3B82F6', 10, TRUE,  NULL),
    ('蔬菜',     '🥬', '#10B981', 20, TRUE,  NULL),
    ('水果',     '🍎', '#EF4444', 30, TRUE,  NULL),
    ('生鲜',     '🐟', '#06B6D4', 40, TRUE,  NULL),
    ('乳制品',   '🥛', '#F59E0B', 50, TRUE,  NULL),
    ('肉类',     '🥩', '#DC2626', 60, TRUE,  NULL),
    ('调味品',   '🧂', '#8B5CF6', 70, TRUE,  NULL),
    ('主食',     '🍚', '#F97316', 80, TRUE,  NULL),
    ('零食',     '🍪', '#EC4899', 90, TRUE,  NULL),
    ('其他',     '📦', '#6B7280', 100, TRUE, NULL);
