-- ============================================
-- 试制流程节点编辑权限（与OE试制跟踪、移动端试制流程联动）
-- 分配后在角色管理中一键生效，适用于：PC试制流程、OE试制跟踪、移动端试制流程
-- 只有拥有对应权限的角色可修改该节点，其他人只能查看
-- ============================================

-- 在 试制管理(2100) 下添加“试制流程节点编辑权限”父菜单（仅用于权限分组，visible=1 隐藏于菜单）
-- 使用 2750 作为父节点，2751-2756 为 6 个阶段权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2750, '试制流程节点编辑权限', 2100, 10, '#', '', '', '', 1, 0, 'M', '1', '0', '', 'edit', 'admin', NOW(), '', NULL, '试制流程六节点编辑权限（联动OE试制跟踪、移动端），分配后三端同步生效'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2750);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2751, '基础信息-可编辑', 2750, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:base:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-基础信息节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2751);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2752, '热工阶段-可编辑', 2750, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:hot:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-热工阶段节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2752);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2753, '旋压阶段-可编辑', 2750, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:spin:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-旋压阶段节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2753);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2754, '粗车阶段-可编辑', 2750, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:rough:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-粗车阶段节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2754);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2755, '精车涂装阶段-可编辑', 2750, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:finePaint:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-精车/涂装阶段节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2755);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2756, '实验总结-可编辑', 2750, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:phase:test:edit', '#', 'admin', NOW(), '', NULL, '可编辑试制流程/OE试制跟踪/移动端-实验/总结节点'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2756);

-- 为 admin 角色分配全部 6 个节点编辑权限（可选，按需取消注释）
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2750), (1, 2751), (1, 2752), (1, 2753), (1, 2754), (1, 2755), (1, 2756);
