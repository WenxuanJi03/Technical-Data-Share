-- ============================================================
-- 修复 C 类型菜单 perms 字段
-- 问题：若 perms 字段为空，ry 角色的权限列表中就不会包含
--        tech:process:list / tech:trialTrack:list 等，
--        导致 ry 访问页面时后端返回 403 "当前操作没有权限"
-- ============================================================

-- -------- 诊断查询（先运行，查看当前状态）--------
SELECT menu_id, menu_name, menu_type, perms
FROM sys_menu
WHERE menu_name IN ('试制流程', 'OE试制跟踪', '试制任务', '试制通知单', '移动端首页')
  AND menu_type = 'C';

-- -------- 修复：确保各 C 类型菜单的 perms 字段正确 --------
-- 1. 试制流程（注意：若原 perms 为 tech:trial:process，需改为 tech:process:list 才能匹配后端校验）
UPDATE sys_menu
SET perms = 'tech:process:list'
WHERE menu_name = '试制流程'
  AND menu_type = 'C';

-- 2. OE试制跟踪（若有该菜单）
UPDATE sys_menu
SET perms = 'tech:trialTrack:list'
WHERE menu_name = 'OE试制跟踪'
  AND menu_type = 'C'
  AND (perms IS NULL OR perms = '');

-- 3. 试制任务
UPDATE sys_menu
SET perms = 'tech:trial:list'
WHERE menu_name = '试制任务'
  AND menu_type = 'C'
  AND (perms IS NULL OR perms = '');

-- 4. 试制通知单
UPDATE sys_menu
SET perms = 'tech:notice:list'
WHERE menu_name = '试制通知单'
  AND menu_type = 'C'
  AND (perms IS NULL OR perms = '');

-- -------- 验证修复结果 --------
SELECT menu_id, menu_name, menu_type, perms
FROM sys_menu
WHERE menu_name IN ('试制流程', 'OE试制跟踪', '试制任务', '试制通知单')
  AND menu_type = 'C';

-- ============================================================
-- 修复后操作步骤：
-- 1. 执行以上 SQL
-- 2. 重启后端 Java 服务（Spring Security 权限缓存需刷新）
--    或者：让 ry 先退出登录，再重新登录（Token 重新加载权限）
-- 3. ry 重新登录后访问"试制流程"页面，不再出现 403 错误
-- 4. ry 点击热工阶段的"录入/上传"和"意见"等操作按钮应正常显示
-- ============================================================
