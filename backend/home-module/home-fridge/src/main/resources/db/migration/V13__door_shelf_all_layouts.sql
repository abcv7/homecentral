-- 补齐所有门型的门搁板层数默认值
-- 1) 系统预置:CLASSIC / BOTTOM_FREEZER 之前 door_shelf_count = 0,导致单开门无法配置门搁板
-- 2) 已有用户从系统预置克隆的副本(命名规则 "原名（我的副本）")同步回填
-- 约束:door_shelf_count BETWEEN 0 AND 5(由 V8 设置)
UPDATE fridge_template
SET door_shelf_count = CASE layout
    WHEN 'CLASSIC'        THEN 3
    WHEN 'BOTTOM_FREEZER' THEN 3
    WHEN 'SIDE_BY_SIDE'   THEN 4
    WHEN 'THREE_DOOR'     THEN 4
END
WHERE is_system = TRUE
  AND layout IN ('CLASSIC', 'BOTTOM_FREEZER', 'SIDE_BY_SIDE', 'THREE_DOOR');

UPDATE fridge_template
SET door_shelf_count = CASE layout
    WHEN 'CLASSIC'        THEN 3
    WHEN 'BOTTOM_FREEZER' THEN 3
    WHEN 'SIDE_BY_SIDE'   THEN 4
    WHEN 'THREE_DOOR'     THEN 4
END
WHERE is_system = FALSE
  AND name IN (
    '经典单开（冷冻在上）（我的副本）',
    '冷冻在下（我的副本）',
    '对开门（我的副本）',
    '法式三门（冷藏+解冻+冷冻）（我的副本）'
  );
