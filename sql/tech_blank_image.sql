-- ----------------------------
-- 毛胚图表 tech_blank_image
-- ----------------------------
DROP TABLE IF EXISTS `tech_blank_image`;
CREATE TABLE `tech_blank_image` (
  `blank_id`     bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mold_no`      varchar(50)   DEFAULT NULL             COMMENT '模号（如 009）',
  `model_code`   varchar(50)   DEFAULT NULL             COMMENT '型号（如 00919F03）',
  `version`      varchar(20)   DEFAULT NULL             COMMENT '版本（A/B/C）',
  `release_date` varchar(30)   DEFAULT NULL             COMMENT '下发时间',
  `blank_image`  varchar(500)  DEFAULT NULL             COMMENT '毛胚图图片路径',
  `del_flag`     char(1)       DEFAULT '0'              COMMENT '删除标志（0正常 1删除）',
  `create_by`    varchar(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`  datetime      DEFAULT NULL             COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`  datetime      DEFAULT NULL             COMMENT '更新时间',
  `remark`       varchar(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`blank_id`),
  UNIQUE KEY `uk_model_code` (`model_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='毛胚图表';

-- ============================================
-- 毛胚图 Web端菜单 + 权限按钮
-- parent_id=2000 表示放在"项目管理"目录下（与产品清单同级）
-- 如果你的"项目管理"parent_id不是2000，请替换为实际值
-- 也可以直接在 Web 端 系统管理→菜单管理 中手动添加
-- ============================================

-- 毛胚图菜单（C=页面菜单）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2800, '毛胚图', 2000, 8, 'blankImage', 'tech/blankImage/index', '', '', 1, 0, 'C', '0', '0', 'tech:blankImage:list', 'picture', 'admin', NOW(), '', NULL, '毛胚图管理菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2800);

-- 查询权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2801, '毛胚图查询', 2800, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:query', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2801);

-- 新增权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2802, '毛胚图新增', 2800, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:add', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2802);

-- 修改权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2803, '毛胚图修改', 2800, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:edit', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2803);

-- 删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2804, '毛胚图删除', 2800, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:remove', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2804);

-- 导入权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2805, '毛胚图导入', 2800, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:import', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2805);

-- 导出权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2806, '毛胚图导出', 2800, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:blankImage:export', '#', 'admin', NOW(), '', NULL, ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2806);

-- 给管理员角色分配所有毛胚图权限（role_id=1 为超级管理员）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2800), (1, 2801), (1, 2802), (1, 2803), (1, 2804), (1, 2805), (1, 2806);
