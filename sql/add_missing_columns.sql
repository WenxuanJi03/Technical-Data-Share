-- ================================================================
-- 为 OE试制跟踪表 新增3个字段 (对应WPS中存在但代码中缺失的列)
-- ================================================================

-- 改善情况 (旋压阶段)
ALTER TABLE tech_trial_track ADD COLUMN spin_improve_status varchar(500) DEFAULT NULL COMMENT '改善情况' AFTER spin_front_distance_image;

-- 实验说明
ALTER TABLE tech_trial_track ADD COLUMN test_description varchar(500) DEFAULT NULL COMMENT '实验说明' AFTER impact_test_result;

-- 实验关闭情况
ALTER TABLE tech_trial_track ADD COLUMN test_close_status varchar(500) DEFAULT NULL COMMENT '实验关闭情况' AFTER test_description;
