-- ============================================================
-- 系统配置：数据表、菜单、按钮权限初始化脚本
-- 适用数据库：MySQL 8.x
-- 脚本可重复执行
-- ============================================================

-- 1. 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置key',
  `config_value` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 2. 系统配置菜单（挂在“系统管理”目录下）
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
    '系统配置', 'M', 'SysConfig', 'config', 'system/config/index',
    NULL, 0, 0, 1, 6, 'setting', NULL, NULL,
    NOW(), NOW()
WHERE @system_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @system_id AND route_path = 'config'
  );

SET @config_menu_id = (
    SELECT id FROM sys_menu
    WHERE parent_id = @system_id AND route_path = 'config'
    LIMIT 1
);

-- 菜单已存在时补齐配置
UPDATE sys_menu
SET component = 'system/config/index',
    name = '系统配置',
    route_name = 'SysConfig',
    icon = 'setting',
    visible = 1
WHERE id = @config_menu_id;

-- 3. 按钮权限
INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @config_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @config_menu_id),
    '查询配置', 'B', NULL, NULL, NULL, 'sys:config:list',
    0, 0, 1, 0, NULL, NULL, NULL, NOW(), NOW()
WHERE @config_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @config_menu_id AND perm = 'sys:config:list'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @config_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @config_menu_id),
    '新增配置', 'B', NULL, NULL, NULL, 'sys:config:create',
    0, 0, 1, 1, NULL, NULL, NULL, NOW(), NOW()
WHERE @config_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @config_menu_id AND perm = 'sys:config:create'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @config_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @config_menu_id),
    '修改配置', 'B', NULL, NULL, NULL, 'sys:config:update',
    0, 0, 1, 2, NULL, NULL, NULL, NOW(), NOW()
WHERE @config_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @config_menu_id AND perm = 'sys:config:update'
  );

INSERT INTO sys_menu (
    parent_id, tree_path, name, type, route_name, route_path, component,
    perm, always_show, keep_alive, visible, sort, icon, redirect, params,
    create_time, update_time
)
SELECT
    @config_menu_id,
    CONCAT(COALESCE(@system_tree, '0'), ',', @system_id, ',', @config_menu_id),
    '删除配置', 'B', NULL, NULL, NULL, 'sys:config:delete',
    0, 0, 1, 3, NULL, NULL, NULL, NOW(), NOW()
WHERE @config_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE parent_id = @config_menu_id AND perm = 'sys:config:delete'
  );

-- 4. 给 ADMIN 角色授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
  ON m.id = @config_menu_id
  OR m.parent_id = @config_menu_id
WHERE r.code = 'ADMIN'
  AND r.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- 5. 刷新权限缓存：重启后端，或执行一次角色菜单保存操作
