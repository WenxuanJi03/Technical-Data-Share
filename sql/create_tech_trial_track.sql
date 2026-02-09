-- ============================================
-- OE试制跟踪表 (对应 DX-OE试制跟踪表)
-- 约35列，按工序阶段分组
-- ============================================
DROP TABLE IF EXISTS tech_trial_track;
CREATE TABLE tech_trial_track (
  track_id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '跟踪ID',

  -- ========== 基础信息 ==========
  mold_code             VARCHAR(50)   NOT NULL                  COMMENT '模号（如07024C13-M4）',
  product_spec          VARCHAR(100)  DEFAULT NULL              COMMENT '产品规格',
  mold_type             VARCHAR(50)   DEFAULT NULL              COMMENT '模具类型（首模等）',
  surface_status        VARCHAR(50)   DEFAULT NULL              COMMENT '表面状态（精车/全涂等）',
  machine_type          VARCHAR(50)   DEFAULT NULL              COMMENT '上机类型（小批量等）',
  machine_count         INT           DEFAULT NULL              COMMENT '上机次数',
  plan_machine_time     VARCHAR(30)   DEFAULT NULL              COMMENT '预计上机时间',

  -- ========== 热工阶段 ==========
  hot_machine_date      VARCHAR(30)   DEFAULT NULL              COMMENT '热工上机日期',
  round_keep_time       VARCHAR(20)   DEFAULT NULL              COMMENT '保圆时间',
  hot_production        VARCHAR(500)  DEFAULT NULL              COMMENT '热工生产情况',
  improve_record        VARCHAR(500)  DEFAULT NULL              COMMENT '改善记录',
  hot_improve_person    VARCHAR(50)   DEFAULT NULL              COMMENT '热工改善负责人',

  -- ========== 旋压阶段 ==========
  spin_machine_date     VARCHAR(30)   DEFAULT NULL              COMMENT '旋压上机日期',
  spin_machine_station  VARCHAR(50)   DEFAULT NULL              COMMENT '旋压机台',
  spin_production       VARCHAR(500)  DEFAULT NULL              COMMENT '旋压生产情况',
  mold_modify_record    VARCHAR(500)  DEFAULT NULL              COMMENT '改模记录2',
  spin_improve_person   VARCHAR(50)   DEFAULT NULL              COMMENT '旋压改善负责人',

  -- ========== 粗车阶段 ==========
  rough_machine_date    VARCHAR(30)   DEFAULT NULL              COMMENT '粗车上机日期',
  rough_production      VARCHAR(500)  DEFAULT NULL              COMMENT '粗车生产情况',
  rough_improve_person  VARCHAR(50)   DEFAULT NULL              COMMENT '粗车问题改善负责人',
  improve_plan          VARCHAR(500)  DEFAULT NULL              COMMENT '改善方案',

  -- ========== 精车+涂装阶段 ==========
  fine_machine_date     VARCHAR(30)   DEFAULT NULL              COMMENT '精车上机日期',
  fine_production       VARCHAR(500)  DEFAULT NULL              COMMENT '精车生产情况',
  paint_machine_date    VARCHAR(30)   DEFAULT NULL              COMMENT '涂装上机日期',
  paint_production      VARCHAR(500)  DEFAULT NULL              COMMENT '涂装生产情况',
  paint_improve_person  VARCHAR(50)   DEFAULT NULL              COMMENT '涂装问题改善负责人',

  -- ========== 实验/总结 ==========
  impact_test_date      VARCHAR(30)   DEFAULT NULL              COMMENT '冲击试验日',
  impact_test_result    VARCHAR(200)  DEFAULT NULL              COMMENT '冲击试验结果',
  complete_date         VARCHAR(30)   DEFAULT NULL              COMMENT '生产完成日期',
  fail_product_trace    VARCHAR(500)  DEFAULT NULL              COMMENT '失效产品溯源',
  fail_analysis         VARCHAR(500)  DEFAULT NULL              COMMENT '实验失效分析',
  production_summary    VARCHAR(500)  DEFAULT NULL              COMMENT '本次生产总结',
  improve_measures      VARCHAR(500)  DEFAULT NULL              COMMENT '改善措施简述',
  lessons_learned       VARCHAR(500)  DEFAULT NULL              COMMENT '经验教训总结',
  all_process_done      VARCHAR(10)   DEFAULT '否'              COMMENT '全序是否完成（是/否）',

  -- ========== 系统字段 ==========
  del_flag              CHAR(1)       DEFAULT '0'               COMMENT '删除标志（0存在 2删除）',
  create_by             VARCHAR(64)   DEFAULT ''                COMMENT '创建者',
  create_time           DATETIME      DEFAULT NULL              COMMENT '创建时间',
  update_by             VARCHAR(64)   DEFAULT ''                COMMENT '更新者',
  update_time           DATETIME      DEFAULT NULL              COMMENT '更新时间',
  remark                VARCHAR(500)  DEFAULT NULL              COMMENT '备注',
  PRIMARY KEY (track_id),
  KEY idx_mold_code (mold_code),
  KEY idx_mold_type (mold_type),
  KEY idx_surface_status (surface_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='OE试制跟踪表（对应DX-OE试制跟踪表）';

-- ========== 菜单（放在"开发科"下面）==========
-- 注意：如果"开发科"的menu_id不同，请修改下方 parent_id
-- 可以先在 sys_menu 表中查找：SELECT menu_id FROM sys_menu WHERE menu_name = '开发科'

-- 目录页
INSERT INTO sys_menu VALUES(2700, 'OE试制跟踪', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name LIKE '%开发%' AND menu_type = 'M' LIMIT 1) tmp), 2, 'trialTrack', 'tech/trialTrack/index', '', '', 1, 0, 'C', '0', '0', 'tech:trialTrack:list', 'skill', 'admin', sysdate(), '', NULL, 'OE试制跟踪菜单');

-- 按钮权限
INSERT INTO sys_menu VALUES(2701, '跟踪查询', 2700, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2702, '跟踪新增', 2700, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2703, '跟踪修改', 2700, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2704, '跟踪删除', 2700, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2705, '跟踪导出', 2700, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2706, '跟踪导入', 2700, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trialTrack:import', '#', 'admin', sysdate(), '', NULL, '');

-- admin角色权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2700), (1, 2701), (1, 2702), (1, 2703), (1, 2704), (1, 2705), (1, 2706);
