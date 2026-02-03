-- =============================================
-- 鎶€鏈绠＄悊绯荤粺 - 鑿滃崟鍒濆鍖栬剼鏈?-- =============================================

-- 鍒犻櫎鑻ヤ緷榛樿鑿滃崟锛堜繚鐣欑郴缁熺鐞嗗拰绯荤粺鐩戞帶锛?DELETE FROM sys_menu WHERE menu_id IN (3, 4);
UPDATE sys_menu SET order_num = 7 WHERE menu_id = 2;
UPDATE sys_menu SET order_num = 9, visible = '1' WHERE menu_id = 1;

-- 涓€绾ц彍鍗?- 椤圭洰绠＄悊
INSERT INTO sys_menu VALUES(2000, '椤圭洰绠＄悊', 0, 1, 'project', NULL, '', '', 1, 0, 'M', '0', '0', '', 'folder', 'admin', sysdate(), '', NULL, '椤圭洰绠＄悊鐩綍');
INSERT INTO sys_menu VALUES(2001, '椤圭洰鍒楄〃', 2000, 1, 'list', 'project/list/index', '', '', 1, 0, 'C', '0', '0', 'project:list:list', 'list', 'admin', sysdate(), '', NULL, '椤圭洰鍒楄〃鑿滃崟');
INSERT INTO sys_menu VALUES(2002, '鏌ヨ涓庡姙鐞?, 2000, 2, 'query', 'project/query/index', '', '', 1, 0, 'C', '0', '0', 'project:query:list', 'search', 'admin', sysdate(), '', NULL, '鏌ヨ涓庡姙鐞嗚彍鍗?);
INSERT INTO sys_menu VALUES(2003, '璇勫鍒楄〃', 2000, 3, 'review', 'project/review/index', '', '', 1, 0, 'C', '0', '0', 'project:review:list', 'checkbox', 'admin', sysdate(), '', NULL, '璇勫鍒楄〃鑿滃崟');
INSERT INTO sys_menu VALUES(2004, '鍙樻洿绠＄悊', 2000, 4, 'change', 'project/change/index', '', '', 1, 0, 'C', '0', '0', 'project:change:list', 'edit', 'admin', sysdate(), '', NULL, '鍙樻洿绠＄悊鑿滃崟');

-- 涓€绾ц彍鍗?- 璇曞埗绠＄悊
INSERT INTO sys_menu VALUES(2100, '璇曞埗绠＄悊', 0, 2, 'trial', NULL, '', '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate(), '', NULL, '璇曞埗绠＄悊鐩綍');
INSERT INTO sys_menu VALUES(2101, '璇曞埗浠诲姟', 2100, 1, 'task', 'trial/task/index', '', '', 1, 0, 'C', '0', '0', 'trial:task:list', 'skill', 'admin', sysdate(), '', NULL, '璇曞埗浠诲姟鑿滃崟');

-- 涓€绾ц彍鍗?- 绉诲姩绔?INSERT INTO sys_menu VALUES(2200, '绉诲姩绔?, 0, 3, 'mobile', NULL, '', '', 1, 0, 'M', '0', '0', '', 'phone', 'admin', sysdate(), '', NULL, '绉诲姩绔洰褰?);

-- 涓€绾ц彍鍗?- 妯″叿鎶€鏈
INSERT INTO sys_menu VALUES(2300, '妯″叿鎶€鏈', 0, 4, 'mold', NULL, '', '', 1, 0, 'M', '0', '0', '', 'build', 'admin', sysdate(), '', NULL, '妯″叿鎶€鏈鐩綍');

-- 涓€绾ц彍鍗?- 鏉冮檺璁よ瘉
INSERT INTO sys_menu VALUES(2400, '鏉冮檺璁よ瘉', 0, 5, 'auth', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', NULL, '鏉冮檺璁よ瘉鐩綍');

-- 涓€绾ц彍鍗?- 鎿嶄綔璁板綍
INSERT INTO sys_menu VALUES(2500, '鎿嶄綔璁板綍', 0, 6, 'operlog', NULL, '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', sysdate(), '', NULL, '鎿嶄綔璁板綍鐩綍');

-- 涓篴dmin瑙掕壊鍒嗛厤鎵€鏈夋柊鑿滃崟鏉冮檺
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2000), (1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2100), (1, 2101), (1, 2200), (1, 2300), (1, 2400), (1, 2500);
