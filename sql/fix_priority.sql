-- 修改 priority 字段长度，支持中文
ALTER TABLE tech_project MODIFY COLUMN priority VARCHAR(50) COMMENT '优先级';
