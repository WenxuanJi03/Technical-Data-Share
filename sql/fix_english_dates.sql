-- ================================================================
-- 修复已导入的英文日期格式数据
-- 将 "Thu Jun 13 00:00:00 CST 2024" 格式转换为 "2024-06-13"
-- ================================================================

-- ============ tech_trial_track 表 ============
UPDATE tech_trial_track SET hot_machine_date = DATE_FORMAT(STR_TO_DATE(hot_machine_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE hot_machine_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET plan_machine_time = DATE_FORMAT(STR_TO_DATE(plan_machine_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE plan_machine_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET spin_machine_date = DATE_FORMAT(STR_TO_DATE(spin_machine_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE spin_machine_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET rough_machine_date = DATE_FORMAT(STR_TO_DATE(rough_machine_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE rough_machine_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET fine_machine_date = DATE_FORMAT(STR_TO_DATE(fine_machine_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE fine_machine_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET paint_machine_date = DATE_FORMAT(STR_TO_DATE(paint_machine_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE paint_machine_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET impact_test_date = DATE_FORMAT(STR_TO_DATE(impact_test_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE impact_test_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_trial_track SET complete_date = DATE_FORMAT(STR_TO_DATE(complete_date, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE complete_date REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';

-- ============ tech_product 表 ============
UPDATE tech_product SET transfer_time = DATE_FORMAT(STR_TO_DATE(transfer_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE transfer_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET internal_eval_time = DATE_FORMAT(STR_TO_DATE(internal_eval_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE internal_eval_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET quality_check_time = DATE_FORMAT(STR_TO_DATE(quality_check_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE quality_check_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET mold_open_time = DATE_FORMAT(STR_TO_DATE(mold_open_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE mold_open_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET quality_arrival_time = DATE_FORMAT(STR_TO_DATE(quality_arrival_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE quality_arrival_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET sample_wheel_time = DATE_FORMAT(STR_TO_DATE(sample_wheel_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE sample_wheel_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET paint_arrival_time = DATE_FORMAT(STR_TO_DATE(paint_arrival_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE paint_arrival_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET drawing_issue_time = DATE_FORMAT(STR_TO_DATE(drawing_issue_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE drawing_issue_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET first_mold_arrival = DATE_FORMAT(STR_TO_DATE(first_mold_arrival, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE first_mold_arrival REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET first_machine_time = DATE_FORMAT(STR_TO_DATE(first_machine_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE first_machine_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET sample_submit_time = DATE_FORMAT(STR_TO_DATE(sample_submit_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE sample_submit_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET sample_pass_time = DATE_FORMAT(STR_TO_DATE(sample_pass_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE sample_pass_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET trial_summary_time = DATE_FORMAT(STR_TO_DATE(trial_summary_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE trial_summary_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET batch_summary_time = DATE_FORMAT(STR_TO_DATE(batch_summary_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE batch_summary_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET impact_delivery_time = DATE_FORMAT(STR_TO_DATE(impact_delivery_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE impact_delivery_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET mass_prod_time = DATE_FORMAT(STR_TO_DATE(mass_prod_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE mass_prod_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
UPDATE tech_product SET latest_change_time = DATE_FORMAT(STR_TO_DATE(latest_change_time, '%a %b %d %H:%i:%s CST %Y'), '%Y-%m-%d') WHERE latest_change_time REGEXP '^[A-Z][a-z]{2} [A-Z][a-z]{2}';
