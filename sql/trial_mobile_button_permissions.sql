-- ============================================
-- 试制管理、移动端 - 按钮权限补充
-- 解决：ry 能看到试制管理、移动端菜单，但操作时报「当前操作没有权限」
-- 原因：若依中 C 类型菜单仅控制页面可见性，F 类型按钮权限控制新增/修改/删除等操作，二者需同时分配
-- ============================================
-- 执行后，在【系统管理 > 角色管理】中为 ry 的角色勾选：
--   1. 试制流程：展开后勾选「流程查询、流程新增、流程修改、流程删除、流程导出」
--   2. 试制任务：展开后勾选「任务查询、任务新增、任务修改、任务删除、任务导出」
--   3. OE试制跟踪：展开后勾选需要的按钮（跟踪查询、跟踪新增、跟踪修改等）
--   4. 移动端、试制通知单：同上，按需勾选子按钮
-- 分配完成后让 ry 重新登录以刷新权限
-- ============================================

-- ---------- 1. 试制流程（tech:process:*）按钮权限 ----------
-- 父菜单：试制流程（需已存在，通常为试制管理2100下的C类型菜单）
-- 若试制流程菜单不存在，请先确认 sys_menu 中有 menu_name='试制流程' 的记录

-- 流程查询 (tech:process:query) - 查看详情、列表加载
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2720, '流程查询', m.menu_id, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:process:query', '#', 'admin', NOW(), '', NULL, '试制流程-查询'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '试制流程' AND menu_type = 'C' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2720);

-- 流程新增 (tech:process:add) - 发起试制
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2721, '流程新增', m.menu_id, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:process:add', '#', 'admin', NOW(), '', NULL, '试制流程-新增'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '试制流程' AND menu_type = 'C' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2721);

-- 流程修改 (tech:process:edit) - 编辑、阶段完成等
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2722, '流程修改', m.menu_id, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:process:edit', '#', 'admin', NOW(), '', NULL, '试制流程-修改'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '试制流程' AND menu_type = 'C' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2722);

-- 流程删除 (tech:process:remove)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2723, '流程删除', m.menu_id, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:process:remove', '#', 'admin', NOW(), '', NULL, '试制流程-删除'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '试制流程' AND menu_type = 'C' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2723);

-- 流程导出 (tech:process:export)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2724, '流程导出', m.menu_id, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:process:export', '#', 'admin', NOW(), '', NULL, '试制流程-导出'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '试制流程' AND menu_type = 'C' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2724);


-- ---------- 2. 试制任务（tech:trial:*）按钮权限 ----------
-- 父菜单：试制任务 (menu_id=2101)
-- 注意：若 试制任务 菜单的 perms 为 trial:task:list，需改为 tech:trial:list 以匹配控制器
UPDATE sys_menu SET perms = 'tech:trial:list' WHERE menu_id = 2101 AND (perms = 'trial:task:list' OR perms = '' OR perms IS NULL);

-- 任务查询
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2730, '任务查询', 2101, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:query', '#', 'admin', NOW(), '', NULL, '试制任务-查询'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2730);

-- 任务新增
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2731, '任务新增', 2101, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:add', '#', 'admin', NOW(), '', NULL, '试制任务-新增'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2731);

-- 任务修改
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2732, '任务修改', 2101, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:edit', '#', 'admin', NOW(), '', NULL, '试制任务-修改'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2732);

-- 任务删除
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2733, '任务删除', 2101, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:remove', '#', 'admin', NOW(), '', NULL, '试制任务-删除'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2733);

-- 任务导出
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2734, '任务导出', 2101, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tech:trial:export', '#', 'admin', NOW(), '', NULL, '试制任务-导出'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2734);


-- ---------- 3. 确保 试制流程 菜单 perms 正确 ----------
UPDATE sys_menu SET perms = 'tech:process:list' WHERE menu_name = '试制流程' AND menu_type = 'C' AND (perms = '' OR perms IS NULL);


-- ---------- 4. 为 admin 角色分配新增的按钮权限（可选）----------
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
--   (1, 2720), (1, 2721), (1, 2722), (1, 2723), (1, 2724),
--   (1, 2730), (1, 2731), (1, 2732), (1, 2733), (1, 2734);
