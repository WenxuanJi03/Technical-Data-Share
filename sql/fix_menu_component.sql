-- 修复菜单组件路径
-- 将错误的组件路径修正为正确的 tech/xxx/index 格式

-- 先查看当前菜单配置
-- SELECT menu_id, menu_name, path, component FROM sys_menu WHERE component LIKE '%trial%' OR component LIKE '%project%' OR component LIKE '%mold%' OR component LIKE '%change%' OR component LIKE '%document%';

-- 修复项目管理相关菜单
UPDATE sys_menu SET component = 'tech/project/index' WHERE menu_name = '项目列表' AND component IS NOT NULL AND component != 'tech/project/index';
UPDATE sys_menu SET component = 'tech/stage/index' WHERE menu_name = '评审列表' AND component IS NOT NULL AND component != 'tech/stage/index';
UPDATE sys_menu SET component = 'tech/change/index' WHERE menu_name = '变更管理' AND component IS NOT NULL AND component != 'tech/change/index';
UPDATE sys_menu SET component = 'tech/member/index' WHERE menu_name = '查询与办理' AND component IS NOT NULL AND component != 'tech/member/index';

-- 修复试制管理相关菜单
UPDATE sys_menu SET component = 'tech/trial/index' WHERE menu_name = '试制任务' AND component IS NOT NULL AND component != 'tech/trial/index';
UPDATE sys_menu SET component = 'tech/process/index' WHERE menu_name = '试制流程' AND component IS NOT NULL AND component != 'tech/process/index';
UPDATE sys_menu SET component = 'tech/workshop/index' WHERE menu_name = '车间管理' AND component IS NOT NULL AND component != 'tech/workshop/index';

-- 修复模具技术科菜单
UPDATE sys_menu SET component = 'tech/mold/index' WHERE menu_name = '模具技术科' AND menu_type = 'C' AND component IS NOT NULL AND component != 'tech/mold/index';
UPDATE sys_menu SET component = 'tech/mold/index' WHERE menu_name = '模具档案' AND component IS NOT NULL AND component != 'tech/mold/index';

-- 修复技术文档菜单
UPDATE sys_menu SET component = 'tech/document/index' WHERE menu_name = '技术文档' AND component IS NOT NULL AND component != 'tech/document/index';

-- 如果上面的更新没有匹配到，尝试用模糊匹配
UPDATE sys_menu SET component = 'tech/project/index' WHERE component LIKE '%project/list%' OR component LIKE '%project/index%';
UPDATE sys_menu SET component = 'tech/trial/index' WHERE component LIKE '%trial/task%' OR component LIKE '%trial/index%';
UPDATE sys_menu SET component = 'tech/stage/index' WHERE component LIKE '%stage%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/change/index' WHERE component LIKE '%change%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/member/index' WHERE component LIKE '%member%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/process/index' WHERE component LIKE '%process%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/workshop/index' WHERE component LIKE '%workshop%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/mold/index' WHERE component LIKE '%mold%' AND menu_type = 'C';
UPDATE sys_menu SET component = 'tech/document/index' WHERE component LIKE '%document%' AND menu_type = 'C';

-- 验证修复结果
SELECT menu_id, menu_name, path, component FROM sys_menu WHERE component LIKE 'tech/%';
