# SQL 脚本说明

## 脚本列表

| 脚本 | 说明 |
| --- | --- |
| `notice-menu.sql` | 通知公告表、字典、菜单、按钮权限、ADMIN 授权 |
| `config-menu.sql` | 系统配置表、菜单、按钮权限、ADMIN 授权 |

## 执行顺序

```bash
mysql -u root -p real_time_api < docs/sql/notice-menu.sql
mysql -u root -p real_time_api < docs/sql/config-menu.sql
```

## 说明

1. 两个脚本都可以重复执行。
2. `sys_notice`、`sys_user_notice`、`sys_config` 已存在时不会重复建表。
3. 脚本只给 `ADMIN` 角色授权；其他角色需要在菜单管理中手动分配按钮权限。
4. 执行菜单脚本后重启后端，让权限缓存重新加载。
5. 生产环境执行前请先备份数据库。
