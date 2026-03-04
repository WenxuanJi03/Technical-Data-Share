-- ============================================================
-- 璇曞埗娴佺▼淇敼锛氱儹澶勭悊闃舵銆佺簿杞?娑傝鎷嗗垎銆佹楠ゆ墿灞?6鈫?
-- 鍏煎 MySQL 5.7锛堥€氳繃瀛樺偍杩囩▼鍒ゆ柇鍒楁槸鍚﹀瓨鍦級
-- ============================================================

DROP PROCEDURE IF EXISTS add_col_if_not_exists;

DELIMITER $$
CREATE PROCEDURE add_col_if_not_exists(
  IN tbl_name  VARCHAR(64),
  IN col_name  VARCHAR(64),
  IN col_def   VARCHAR(500)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = tbl_name
      AND COLUMN_NAME  = col_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN ', col_def);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- ===== 1. tech_trial_track锛氱儹澶勭悊闃舵瀛楁 =====
CALL add_col_if_not_exists('tech_trial_track', 'heat_receive_count',    "`heat_receive_count`   INT(10)       DEFAULT NULL COMMENT '鐑鐞?鎺ユ敹鏁伴噺' AFTER `spin_improve_status`");
CALL add_col_if_not_exists('tech_trial_track', 'heat_transfer_count',   "`heat_transfer_count`  INT(10)       DEFAULT NULL COMMENT '鐑鐞?杞笅鏁伴噺' AFTER `heat_receive_count`");
CALL add_col_if_not_exists('tech_trial_track', 'heat_transfer_time',    "`heat_transfer_time`   VARCHAR(20)   DEFAULT NULL COMMENT '鐑鐞?涓嬭浆鏃堕棿' AFTER `heat_transfer_count`");
CALL add_col_if_not_exists('tech_trial_track', 'heat_flow_sheet_image', "`heat_flow_sheet_image` VARCHAR(500) DEFAULT NULL COMMENT '鐑鐞?娴佽浆鍗曠収鐗? AFTER `heat_transfer_time`");

-- ===== 2. tech_trial_track锛氱簿杞﹁礋璐ｄ汉 =====
CALL add_col_if_not_exists('tech_trial_track', 'fine_improve_person',   "`fine_improve_person`  VARCHAR(64)   DEFAULT NULL COMMENT '绮捐溅璐熻矗浜? AFTER `fine_production`");

-- ===== 3. tech_trial_process锛歴tep7 / step8 =====
CALL add_col_if_not_exists('tech_trial_process', 'step7_status',      "`step7_status`      VARCHAR(20)  DEFAULT NULL COMMENT '姝ラ7鐘舵€?      AFTER `step6_status`");
CALL add_col_if_not_exists('tech_trial_process', 'step8_status',      "`step8_status`      VARCHAR(20)  DEFAULT NULL COMMENT '姝ラ8鐘舵€?      AFTER `step7_status`");
CALL add_col_if_not_exists('tech_trial_process', 'step7_deadline',    "`step7_deadline`    DATETIME     DEFAULT NULL COMMENT '姝ラ7鎴鏃ユ湡'  AFTER `step6_deadline`");
CALL add_col_if_not_exists('tech_trial_process', 'step8_deadline',    "`step8_deadline`    DATETIME     DEFAULT NULL COMMENT '姝ラ8鎴鏃ユ湡'  AFTER `step7_deadline`");
CALL add_col_if_not_exists('tech_trial_process', 'step7_responsible', "`step7_responsible` VARCHAR(64)  DEFAULT NULL COMMENT '姝ラ7璐熻矗浜?    AFTER `step6_responsible`");
CALL add_col_if_not_exists('tech_trial_process', 'step8_responsible', "`step8_responsible` VARCHAR(64)  DEFAULT NULL COMMENT '姝ラ8璐熻矗浜?    AFTER `step7_responsible`");
CALL add_col_if_not_exists('tech_trial_process', 'step7_files',       "`step7_files`       TEXT         DEFAULT NULL COMMENT '姝ラ7鏂囦欢JSON'  AFTER `step6_files`");
CALL add_col_if_not_exists('tech_trial_process', 'step8_files',       "`step8_files`       TEXT         DEFAULT NULL COMMENT '姝ラ8鏂囦欢JSON'  AFTER `step7_files`");
CALL add_col_if_not_exists('tech_trial_process', 'step7_comments',    "`step7_comments`    TEXT         DEFAULT NULL COMMENT '姝ラ7鎰忚JSON'  AFTER `step6_comments`");
CALL add_col_if_not_exists('tech_trial_process', 'step8_comments',    "`step8_comments`    TEXT         DEFAULT NULL COMMENT '姝ラ8鎰忚JSON'  AFTER `step7_comments`");

-- 娓呯悊涓存椂瀛樺偍杩囩▼
DROP PROCEDURE IF EXISTS add_col_if_not_exists;

