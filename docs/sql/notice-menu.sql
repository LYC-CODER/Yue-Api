-- ============================================================
-- 通知公告：字典、菜单、按钮权限初始化脚本
-- 适用数据库：MySQL 8.x
-- 执行前请确认 sys_dict、sys_menu、sys_role、sys_role_menu 已存在
-- 脚本可重复执行，不会重复插入同编码/同权限的字典和菜单
-- ============================================================

-- 1. 通知类型字典
INSERT INTO sys_dict (dict_code, name, status, remark, is_deleted, create_time, update_time)
SELECT 'notice_type', '通知类型', 1, '通知公告类型', 0, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict WHERE dict_code = 'notice_type');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_type', '通知', '1', 1, 1, '系统通知', 'primary', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_type' AND value = '1');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_type', '公告', '2', 2, 1, '系统公告', 'success', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_type' AND value = '2');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_type', '提醒', '3', 3, 1, '系统提醒', 'warning', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_type' AND value = '3');

-- 2. 通知等级字典
INSERT INTO sys_dict (dict_code, name, status, remark, is_deleted, create_time, update_time)
SELECT 'notice_level', '通知等级', 1, '通知公告等级', 0, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict WHERE dict_code = 'notice_level');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_level', '低', 'L', 1, 1, '普通通知', 'info', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_level' AND value = 'L');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_level', '中', 'M', 2, 1, '重要通知', 'warning', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_level' AND value = 'M');

INSERT INTO sys_dict_item (dict_code, label, value, sort, status, remark, tag_type, create_time, update_time)
SELECT 'notice_level', '高', 'H', 3, 1, '紧急通知', 'danger', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item WHERE dict_code = 'notice_level' AND value = 'H');

-- 3. 通知管理菜单（挂在“系统管理”目录下）
SET @system_id = (
    SELECT id FROM sys_menu
    WHERE route_path = '/system' AND parent_id = 0
    LIMIT 1
);
SET @system_tree = (
    SELECT tree_path FROM sys_menu
    WHERE id = @system_id
    LIMIT 1
);

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @system_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id),
    '通知公告', 'M', 'NoticeManage', 'notice', 'system/notice/index',
    NULL, 0, 0, 1, 5, 'message', NULL, NULL,
    NOW(), NOW()
WHERE @system_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @system_id AND route_path = 'notice'
  );

SET @notice_menu_id = (
    SELECT id FROM sys_menu
    WHERE parent_id = @system_id AND route_path = 'notice'
    LIMIT 1
);

-- 如果菜单已存在，补齐组件/图标等配置
UPDATE sys_menu
SET component = 'system/notice/index',
    name = '通知公告',
    route_name = 'NoticeManage',
    icon = 'message',
    visible = 1
WHERE id = @notice_menu_id;

-- 4. 通知管理按钮权限
INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '查询通知', 'B', NULL, NULL, NULL, 'sys:notice:list',
    0, 0, 1, 0, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:list'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '新增通知', 'B', NULL, NULL, NULL, 'sys:notice:create',
    0, 0, 1, 1, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:create'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '修改通知', 'B', NULL, NULL, NULL, 'sys:notice:update',
    0, 0, 1, 2, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:update'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '删除通知', 'B', NULL, NULL, NULL, 'sys:notice:delete',
    0, 0, 1, 3, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:delete'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '发布通知', 'B', NULL, NULL, NULL, 'sys:notice:publish',
    0, 0, 1, 4, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:publish'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @notice_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @notice_menu_id),
    '撤回通知', 'B', NULL, NULL, NULL, 'sys:notice:revoke',
    0, 0, 1, 5, NULL, NULL, NULL, NOW(), NOW()
WHERE @notice_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @notice_menu_id AND perm = 'sys:notice:revoke'
  );

-- 5. 给 ADMIN 角色授权（ROOT 权限校验会自动放行）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.id = @notice_menu_id
  OR m.parent_id = @notice_menu_id
WHERE r.code = 'ADMIN'
  AND r.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- 6. 刷新权限缓存：重启后端，或执行一次角色菜单保存操作
