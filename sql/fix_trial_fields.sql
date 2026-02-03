-- 修改 tech_trial 表字段长度，支持中文
USE DWS;

-- 修改 status 字段长度
ALTER TABLE tech_trial MODIFY COLUMN status VARCHAR(50) COMMENT '试制状态';

-- 修改 result 字段长度
ALTER TABLE tech_trial MODIFY COLUMN result VARCHAR(100) COMMENT '试制结果';

-- 修改 priority 字段（如果存在）
ALTER TABLE tech_trial ADD COLUMN IF NOT EXISTS priority VARCHAR(50) COMMENT '优先级';

-- 修改 cycle 字段（如果存在）
ALTER TABLE tech_trial ADD COLUMN IF NOT EXISTS cycle INT COMMENT '周期(天)';

-- 修改 trial_count 字段（如果存在）
ALTER TABLE tech_trial ADD COLUMN IF NOT EXISTS trial_count INT DEFAULT 0 COMMENT '试制次数';

-- 修改 is_whole_mold 字段（如果存在）
ALTER TABLE tech_trial ADD COLUMN IF NOT EXISTS is_whole_mold TINYINT DEFAULT 0 COMMENT '是否整模试制';
