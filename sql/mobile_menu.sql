-- =============================================
-- 移动端菜单配置
-- 在phpMyAdmin中执行此SQL
-- =============================================

-- 移动端目录（一级菜单，parent_id=2200 已在tech_menu.sql中创建）
-- 如果2200不存在，先创建一级目录：
-- INSERT INTO sys_menu VALUES(2200, '移动端', 0, 3, 'mobile', NULL, '', '', 1, 0, 'M', '0', '0', '', 'phone', 'admin', sysdate(), '', NULL, '移动端目录');

-- 移动端子菜单
INSERT INTO sys_menu VALUES(2201, '移动端首页', 2200, 1, 'menu', 'tech/mobile/index', '', '', 1, 0, 'C', '0', '0', 'tech:mobile:list', 'dashboard', 'admin', sysdate(), '', NULL, '移动端首页菜单');
INSERT INTO sys_menu VALUES(2202, '试制任务', 2200, 2, 'task', 'tech/mobile/task', '', '', 1, 0, 'C', '1', '0', 'tech:mobile:list', 'skill', 'admin', sysdate(), '', NULL, '移动端试制任务（隐藏）');
INSERT INTO sys_menu VALUES(2203, '任务详情', 2200, 3, 'detail', 'tech/mobile/detail', '', '', 1, 0, 'C', '1', '0', 'tech:mobile:list', 'form', 'admin', sysdate(), '', NULL, '移动端任务详情（隐藏）');

-- 为admin角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2201), (1, 2202), (1, 2203);
