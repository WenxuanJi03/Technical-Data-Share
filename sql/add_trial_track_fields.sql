-- OE试制跟踪 新增字段 2026-02-12
ALTER TABLE tech_trial_track
  ADD COLUMN hot_machine_station        VARCHAR(100)  DEFAULT NULL COMMENT '热工阶段-机台',
  ADD COLUMN hot_check_measure_data     VARCHAR(500)  DEFAULT NULL COMMENT '热工检查站首件测量数据',
  ADD COLUMN hot_check_measure_image    VARCHAR(1000) DEFAULT NULL COMMENT '热工检查站首件测量数据图片',
  ADD COLUMN spin_front_distance_image  VARCHAR(1000) DEFAULT NULL COMMENT '旋压阶段-前距图片',
  ADD COLUMN paint_flow_sheet_image     VARCHAR(1000) DEFAULT NULL COMMENT '精车涂装-流转单照片';
