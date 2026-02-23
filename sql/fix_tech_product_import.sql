-- ============================================
-- 修复产品清单导入失败
-- 原因1: 时间列 VARCHAR(30) 太短，Excel 里可能是长文本
-- 原因2: fea_result 等列含 emoji(📄等)，需 utf8mb4
-- 在 phpMyAdmin 中执行本脚本即可
-- ============================================

-- 1. 表改为 utf8mb4，支持 emoji
ALTER TABLE tech_product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 所有原 VARCHAR(30) 的时间/短文本列 改为 VARCHAR(100)
ALTER TABLE tech_product
  MODIFY COLUMN transfer_time        VARCHAR(100) DEFAULT NULL COMMENT '转移时间',
  MODIFY COLUMN internal_eval_time  VARCHAR(100) DEFAULT NULL COMMENT '内评时间',
  MODIFY COLUMN quality_check_time   VARCHAR(100) DEFAULT NULL COMMENT '工检清单时间',
  MODIFY COLUMN mold_open_time       VARCHAR(100) DEFAULT NULL COMMENT '开模时间',
  MODIFY COLUMN quality_arrival_time VARCHAR(100) DEFAULT NULL COMMENT '工检全部到厂时间',
  MODIFY COLUMN sample_wheel_time    VARCHAR(100) DEFAULT NULL COMMENT '样轮到厂时间',
  MODIFY COLUMN paint_arrival_time   VARCHAR(100) DEFAULT NULL COMMENT '油漆到厂时间',
  MODIFY COLUMN drawing_issue_time   VARCHAR(100) DEFAULT NULL COMMENT '图纸文件全部签发时间',
  MODIFY COLUMN first_mold_arrival   VARCHAR(100) DEFAULT NULL COMMENT '首模到厂时间',
  MODIFY COLUMN first_machine_time   VARCHAR(100) DEFAULT NULL COMMENT '首上机时间',
  MODIFY COLUMN sample_submit_time   VARCHAR(100) DEFAULT NULL COMMENT '送样时间',
  MODIFY COLUMN sample_pass_time     VARCHAR(100) DEFAULT NULL COMMENT '样件合格时间',
  MODIFY COLUMN trial_summary_time   VARCHAR(100) DEFAULT NULL COMMENT '试制总结时间',
  MODIFY COLUMN batch_summary_time   VARCHAR(100) DEFAULT NULL COMMENT '小批量总结时间',
  MODIFY COLUMN impact_delivery_time VARCHAR(100) DEFAULT NULL COMMENT '影响交付时间',
  MODIFY COLUMN mass_prod_time       VARCHAR(100) DEFAULT NULL COMMENT '量产时间',
  MODIFY COLUMN latest_change_time   VARCHAR(100) DEFAULT NULL COMMENT '最新变更断点关闭时间';

-- 3. FEA 等可能含 emoji 或长句的列 放宽长度
ALTER TABLE tech_product
  MODIFY COLUMN fea_result VARCHAR(500) DEFAULT NULL COMMENT 'FEA分析结果';
