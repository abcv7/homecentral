-- 允许 PENDING 状态的食材 zone 字段为 NULL
-- PENDING = 采购篮中，还未放入任何温区
ALTER TABLE home_fridge.fridge_item ALTER COLUMN zone DROP NOT NULL;
