-- ============================================================
-- 移动端 App 首页六大菜单入口权限
-- 执行后，在【系统管理 > 角色管理】中为对应角色勾选以下权限：
--   移动端 > 移动端-试制任务、移动端-新产品评审、……
-- 勾选后该角色的用户登录移动端 App 首页才能看到对应菜单卡片，
-- 否则菜单卡片不显示（只读/无入口）。
-- 执行本脚本后同时为 admin 角色（role_id=1）分配所有 mobile 权限，
-- 确保管理员账号始终可见全部菜单。
-- ============================================================

-- ---------- 1. 在"移动端"父菜单下添加六个功能入口权限（F 类型按钮权限）----------

-- 1-1 移动端-试制任务
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2760, '移动端-试制任务', m.menu_id, 11, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:task:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-试制任务卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2760);

-- 1-2 移动端-新产品评审
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2761, '移动端-新产品评审', m.menu_id, 12, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:review:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-新产品评审卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2761);

-- 1-3 移动端-文件夹
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2762, '移动端-文件夹', m.menu_id, 13, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:files:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-文件夹卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2762);

-- 1-4 移动端-工单管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2763, '移动端-工单管理', m.menu_id, 14, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:workorder:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-工单管理卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2763);

-- 1-5 移动端-试制流程
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2764, '移动端-试制流程', m.menu_id, 15, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:trialProcess:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-试制流程卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2764);

-- 1-6 移动端-轮毂识别
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon,
                      create_by, create_time, update_by, update_time, remark)
SELECT 2765, '移动端-轮毂识别', m.menu_id, 16, '#', '', '', '', 1, 0, 'F', '0', '0',
       'mobile:scan:view', '#', 'admin', NOW(), '', NULL, '移动端首页菜单-轮毂识别卡片权限'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '移动端' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2765);


-- ---------- 2. 为 admin 角色（role_id=1）自动分配全部移动端菜单权限（保证管理员始终全功能可见）----------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 2760), (1, 2761), (1, 2762), (1, 2763), (1, 2764), (1, 2765);


-- ---------- 操作说明 ----------
-- 执行完本脚本后：
-- 1. 登录系统管理后台 > 系统管理 > 角色管理
-- 2. 找到需要设置的角色（如"热工工程师"），点击【修改】
-- 3. 展开菜单权限树：移动端 > 勾选需要的菜单项（移动端-试制任务 / 移动端-试制流程 等）
-- 4. 保存角色，让该角色的用户重新登录后生效
-- 注意：对于试制流程节点编辑权限，仍需在"试制流程节点编辑权限"下勾选对应阶段
