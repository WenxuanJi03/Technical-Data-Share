-- =============================================
-- 产品试制通知单 - 建表 + 菜单权限配置
-- 在phpMyAdmin中直接执行此SQL即可
-- =============================================

-- ========== 第一步：创建数据表 ==========
DROP TABLE IF EXISTS tech_trial_notice;
CREATE TABLE tech_trial_notice (
  notice_id       BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '通知单ID',
  notice_code     VARCHAR(50)   DEFAULT ''              COMMENT '通知单编号',
  wheel_code      VARCHAR(50)   DEFAULT ''              COMMENT '轮型号',
  trial_status    VARCHAR(50)   DEFAULT ''              COMMENT '试制状态',
  trial_type      VARCHAR(50)   DEFAULT ''              COMMENT '试制类型',
  dev_type        VARCHAR(50)   DEFAULT ''              COMMENT '开发类型',
  customer_name   VARCHAR(100)  DEFAULT ''              COMMENT '客户名称',
  responsible     VARCHAR(50)   DEFAULT ''              COMMENT '负责人',
  surface_status  VARCHAR(50)   DEFAULT ''              COMMENT '表面状态',
  size_spec       VARCHAR(100)  DEFAULT ''              COMMENT '尺寸规格',
  urgency         VARCHAR(20)   DEFAULT '正常'          COMMENT '紧急程度',
  trial_quantity  INT(11)       DEFAULT 0               COMMENT '试制数量',
  sample_quantity INT(11)       DEFAULT 0               COMMENT '计划交样数量',
  trial_start     DATE          DEFAULT NULL            COMMENT '试制开始时间',
  sample_date     DATE          DEFAULT NULL            COMMENT '送样时间',
  craft_process   VARCHAR(100)  DEFAULT ''              COMMENT '工艺流程',
  experiment_items VARCHAR(500) DEFAULT ''              COMMENT '实验项目(逗号分隔)',
  wheel_image     VARCHAR(500)  DEFAULT ''              COMMENT '轮毂图片',
  process_data    TEXT                                  COMMENT '工序数据JSON',
  status          VARCHAR(20)   DEFAULT '待启动'        COMMENT '通知单状态',
  del_flag        CHAR(1)       DEFAULT '0'             COMMENT '删除标志(0正常 2删除)',
  create_by       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL            COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL            COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (notice_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='产品试制通知单';


-- ========== 第二步：添加菜单 ==========
-- 注意：父菜单"试制管理"的ID是2100，路径是trial
-- 如果2110-2116这些ID已被占用，请手动修改为其他未使用的ID

-- 试制通知单 - 菜单页面（显示在左侧导航栏）
INSERT INTO sys_menu VALUES(2110, '试制通知单', 2100, 2, 'notice', 'tech/notice/index', '', '', 1, 0, 'C', '0', '0', 'tech:notice:list', 'documentation', 'admin', sysdate(), '', NULL, '产品试制通知单菜单');

-- 试制通知单详情 - 隐藏页面（不显示在导航栏，通过列表页跳转访问）
INSERT INTO sys_menu VALUES(2116, '通知单详情', 2100, 3, 'notice-detail', 'tech/notice/detail', '', '', 1, 0, 'C', '1', '0', 'tech:notice:query', 'form', 'admin', sysdate(), '', NULL, '通知单详情页（隐藏）');

-- 试制通知单 - 按钮权限
INSERT INTO sys_menu VALUES(2111, '通知单查询', 2110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:notice:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2112, '通知单新增', 2110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:notice:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2113, '通知单修改', 2110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:notice:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2114, '通知单删除', 2110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:notice:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES(2115, '通知单导出', 2110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:notice:export', '#', 'admin', sysdate(), '', NULL, '');


-- ========== 第三步：为admin角色分配权限 ==========
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 2110), (1, 2111), (1, 2112), (1, 2113), (1, 2114), (1, 2115), (1, 2116);
