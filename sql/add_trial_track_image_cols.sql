-- OE试制跟踪 新增各阶段图片字段 2026-03-05
-- 使用 TEXT 类型存储多张图片URL（逗号分隔），TEXT 不计入行大小限制
ALTER TABLE tech_trial_track
  ADD COLUMN base_image   TEXT DEFAULT NULL COMMENT '基础信息阶段-上传图片',
  ADD COLUMN rough_image  TEXT DEFAULT NULL COMMENT '粗车阶段-上传图片',
  ADD COLUMN fine_image   TEXT DEFAULT NULL COMMENT '精车阶段-上传图片',
  ADD COLUMN test_image   TEXT DEFAULT NULL COMMENT '实验/总结阶段-上传图片';

