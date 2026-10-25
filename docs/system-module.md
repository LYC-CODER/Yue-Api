# 系统管理模块功能说明文档

> 当前实现说明：后端接口统一前缀为 `/api/v1`，例如用户接口是 `/api/v1/users`，通知公告是 `/api/v1/notices`，系统配置是 `/api/v1/configs`。  
> 文档中较早出现的 `/api/system/*` 路径属于历史设计示例，实际调用请以控制器代码和 Knife4j `/doc.html` 为准。

## 模块概述

系统管理模块是Real-Time-Api项目的核心管理功能集合，提供了用户管理、角色管理、菜单管理、部门管理、字典管理、通知公告、系统配置等基础系统功能。该模块采用RBAC（Role-Based Access Control）权限模型，支持灵活的角色权限配置和数据权限控制。

## 功能架构

```
com.lycorder.system
├── controller      # 控制器层
├── dto            # 数据传输对象
├── entity         # 实体类
├── enums          # 枚举类
├── mapper         # 数据访问层
├── service        # 业务逻辑层
├── vo             # 视图对象
└── query          # 查询条件对象
```

## 核心功能

### 1. 用户管理

#### 1.1 功能列表
- 用户CRUD操作
- 用户状态管理（启用/禁用）
- 密码重置
- 角色分配
- 部门分配
- 批量导入导出

#### 1.2 用户实体设计
```java
@TableName("sys_user")
public class User extends BaseEntity {
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private String nickname;    // 昵称
    private String email;       // 邮箱
    private String mobile;      // 手机号
    private Integer status;     // 状态（0-正常，1-禁用）
    private Long deptId;        // 部门ID
    private String password;    // 密码
    private Integer sex;        // 性别
    private String avatar;      // 头像
    private LocalDateTime lastLoginTime;  // 最后登录时间
}
```

#### 1.3 用户查询功能
支持多条件组合查询：
```java
@GetMapping("/page")
public Result<PageResult<UserVO>> getUserPage(@Valid UserQuery query) {
    PageResult<UserVO> page = userService.getUserPage(query);
    return Result.success(page);
}
```

查询条件包括：
- 用户名（模糊查询）
- 手机号（精确查询）
- 邮箱（模糊查询）
- 状态
- 部门ID
- 创建时间范围

### 2. 角色管理

#### 2.1 功能列表
- 角色CRUD操作
- 菜单权限分配
- 数据权限配置
- 角色成员管理

#### 2.2 数据权限模型
支持以下数据权限类型：
```java
public enum DataScope {
    ALL(1, "全部数据权限"),
    CUSTOM(2, "自定义数据权限"),
    DEPT(3, "本部门数据权限"),
    DEPT_AND_CHILD(4, "本部门及子部门数据权限"),
    SELF(5, "仅本人数据权限");
}
```

#### 2.3 角色权限配置
```java
@PostMapping("/menu")
public Result<Void> saveRoleMenu(@RequestBody RoleMenuDTO dto) {
    roleService.saveRoleMenu(dto.getRoleId(), dto.getMenuIds());
    return Result.success();
}
```

### 3. 菜单管理

#### 3.1 菜单类型
- 目录：一级菜单
- 菜单：功能页面
- 按钮：操作权限

#### 3.2 菜单属性
```java
public class Menu extends BaseEntity {
    private Long menuId;        // 菜单ID
    private String menuName;    // 菜单名称
    private Integer menuType;   // 菜单类型（M-目录，C-菜单，F-按钮）
    private Long parentId;      // 父菜单ID
    private Integer sort;       // 排序
    private String path;        // 路由地址
    private String component;   // 组件路径
    private String icon;        // 图标
    private String perms;       // 权限标识
    private Integer status;     // 菜单状态
    private Integer visible;    // 显示状态
}
```

#### 3.3 路由构建
```java
@GetMapping("/routes")
public Result<List<RouteVO>> getRoutes() {
    List<RouteVO> routes = menuService.buildRoutes();
    return Result.success(routes);
}
```

### 4. 部门管理

#### 4.1 功能列表
- 部门CRUD操作
- 部门树形结构
- 部门负责人设置
- 部门状态管理

#### 4.2 部门树构建
```java
@GetMapping("/tree")
public Result<List<DeptTreeVO>> getDeptTree() {
    List<DeptTreeVO> tree = deptService.buildDeptTree();
    return Result.success(tree);
}
```

### 5. 岗位管理

#### 5.1 功能列表
- 岗位CRUD操作
- 岗位排序
- 状态管理

#### 5.2 岗位实体
```java
public class Post extends BaseEntity {
    private Long postId;        // 岗位ID
    private String postCode;    // 岗位编码
    private String postName;    // 岗位名称
    private Integer sort;       // 排序
    private String remark;      // 备注
    private Integer status;     // 状态
}
```

### 6. 字典管理

#### 6.1 功能列表
- 字典类型管理
- 字典数据管理
- 字典缓存

#### 6.2 字典使用
```java
@GetMapping("/dict/{dictType}")
public Result<List<DictDataVO>> getDictData(@PathVariable String dictType) {
    List<DictDataVO> dictData = dictService.getDictData(dictType);
    return Result.success(dictData);
}
```

### 7. 系统配置

#### 7.1 功能列表
- 系统配置分页查询
- 按配置名称 / 配置 key 搜索
- 新增、编辑、删除配置
- 配置 key 唯一校验
- 配置值维护

#### 7.2 配置实体

```java
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    private String configName;   // 配置名称
    private String configKey;    // 配置 key，全局唯一
    private String configValue;  // 配置值
    private String remark;       // 备注
    private Long createBy;       // 创建人
    private Long updateBy;       // 更新人
    private Integer isDeleted;   // 逻辑删除
}
```

#### 7.3 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/configs` | 配置分页 |
| GET | `/api/v1/configs/{id}/form` | 编辑表单 |
| POST | `/api/v1/configs` | 新增 |
| PUT | `/api/v1/configs/{id}` | 修改 |
| DELETE | `/api/v1/configs/{ids}` | 批量删除 |

权限标识：

```text
sys:config:list
sys:config:create
sys:config:update
sys:config:delete
```

初始化脚本：

```text
docs/sql/config-menu.sql
```

---

### 8. 通知公告

#### 8.1 功能列表
- 通知公告分页、新增、编辑、删除
- 发布、撤回
- 全体用户 / 指定用户
- 用户通知已读、未读
- 全部已读
- 顶栏未读数量

#### 8.2 表结构

`sys_notice` 通知主表：

```text
id, title, content, type, level, target_type,
target_user_ids, publisher_id, publish_status,
publish_time, revoke_time, create_by,
create_time, update_by, update_time, is_deleted
```

`sys_user_notice` 用户接收状态表：

```text
id, notice_id, user_id, is_read, read_time,
create_time, update_time, is_deleted
```

发布状态：

```text
0  = 未发布
1  = 已发布
-1 = 已撤回
```

目标类型：

```text
1 = 全体用户
2 = 指定用户
```

#### 8.3 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/notices` | 管理端分页 |
| POST | `/api/v1/notices` | 新增 |
| GET | `/api/v1/notices/{id}/form` | 编辑表单 |
| PUT | `/api/v1/notices/{id}` | 修改 |
| DELETE | `/api/v1/notices/{ids}` | 批量删除 |
| PUT | `/api/v1/notices/{id}/publish` | 发布 |
| PUT | `/api/v1/notices/{id}/revoke` | 撤回 |
| GET | `/api/v1/notices/{id}/detail` | 管理端详情 |
| GET | `/api/v1/notices/my` | 我的通知 |
| GET | `/api/v1/notices/my/{id}` | 阅读详情并标记已读 |
| PUT | `/api/v1/notices/read-all` | 全部已读 |
| GET | `/api/v1/notices/unread-count` | 未读数量 |

权限标识：

```text
sys:notice:list
sys:notice:create
sys:notice:update
sys:notice:delete
sys:notice:publish
sys:notice:revoke
```

初始化脚本：

```text
docs/sql/notice-menu.sql
```

详细说明见项目根目录 `Yue-通知公告功能说明.md`。

## 数据权限控制

### 1. 权限注解
```java
@DataPermission(deptAlias = "u", userAlias = "u")
@GetMapping("/list")
public Result<List<UserVO>> getUserList() {
    List<UserVO> userList = userService.getUserList();
    return Result.success(userList);
}
```

### 2. 权限过滤
根据用户的数据权限范围，自动添加SQL过滤条件：
```sql
-- 全部权限：无过滤
-- 自定义权限：AND u.dept_id IN (配置的部门列表)
-- 本部门权限：AND u.dept_id = 当前用户部门ID
-- 本部门及子部门：AND u.dept_id IN (当前部门及子部门)
-- 仅本人：AND u.user_id = 当前用户ID
```

## 性能优化

### 1. 缓存策略
- 用户信息缓存（30分钟）
- 菜单树缓存（1小时）
- 字典数据缓存（24小时）
- 部门树缓存（2小时）

### 2. 数据库优化
- 关键字段索引
- 分页查询优化
- 批量操作支持

### 3. 查询优化
```java
// 使用MyBatis Plus的LambdaQueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(StringUtils.isNotBlank(username), User::getUsername, username)
       .eq(StringUtils.isNotBlank(mobile), User::getMobile, mobile)
       .eq(status != null, User::getStatus, status)
       .between(beginTime != null && endTime != null, User::getCreateTime, beginTime, endTime);
```

## API接口列表

### 用户管理
- `GET /api/v1/users/list` - 用户分页/列表
- `GET /api/v1/users/{userId}/form` - 用户表单
- `POST /api/v1/users/saveUser` - 新增用户
- `PUT /api/v1/users/{userId}` - 更新用户
- `DELETE /api/v1/users/{ids}` - 删除用户
- `PUT /api/v1/users/{userId}/password/reset` - 管理员重置密码
- `GET /api/v1/users/me` - 当前用户信息
- `GET /api/v1/users/options` - 用户下拉选项
- `POST /api/v1/users/import` - 导入用户
- `GET /api/v1/users/export` - 导出用户
- `GET /api/v1/users/template` - 下载导入模板

### 角色管理
- `GET /api/v1/roles` - 角色分页
- `GET /api/v1/roles/options` - 角色下拉选项
- `POST /api/v1/roles` - 新增角色
- `GET /api/v1/roles/{roleId}/form` - 角色表单
- `PUT /api/v1/roles/{id}` - 更新角色
- `DELETE /api/v1/roles/{ids}` - 删除角色
- `PUT /api/v1/roles/{roleId}/status` - 修改角色状态
- `GET /api/v1/roles/{roleId}/menuIds` - 获取角色菜单
- `PUT /api/v1/roles/{roleId}/menus` - 保存角色菜单

### 菜单管理
- `GET /api/v1/menus` - 获取菜单列表
- `GET /api/v1/menus/options` - 菜单下拉选项
- `GET /api/v1/menus/routes` - 获取当前用户路由
- `GET /api/v1/menus/{id}/form` - 获取菜单表单
- `POST /api/v1/menus` - 新增菜单
- `PUT /api/v1/menus/{id}` - 更新菜单
- `DELETE /api/v1/menus/{id}` - 删除菜单

### 部门管理
- `GET /api/v1/depts` - 获取部门列表
- `GET /api/v1/depts/options` - 获取部门下拉选项
- `POST /api/v1/depts` - 新增部门
- `GET /api/v1/depts/{id}/form` - 获取部门表单
- `PUT /api/v1/depts/{id}` - 更新部门
- `DELETE /api/v1/depts/{ids}` - 删除部门

### 字典管理
- `GET /api/v1/dicts` - 字典分页
- `GET /api/v1/dicts/options` - 字典下拉选项
- `POST /api/v1/dicts` - 新增字典
- `GET /api/v1/dicts/{id}/form` - 字典表单
- `PUT /api/v1/dicts/{id}` - 更新字典
- `DELETE /api/v1/dicts/{ids}` - 删除字典
- `GET /api/v1/dicts/{dictCode}/items` - 字典项分页
- `GET /api/v1/dicts/{dictCode}/items/options` - 字典项选项
- `POST /api/v1/dicts/{dictCode}/items` - 新增字典项
- `PUT /api/v1/dicts/{dictCode}/items/{itemId}` - 更新字典项
- `DELETE /api/v1/dicts/{dictCode}/items/{itemIds}` - 删除字典项

### 通知公告
- `GET /api/v1/notices` - 管理端分页
- `POST /api/v1/notices` - 新增
- `GET /api/v1/notices/{id}/form` - 编辑表单
- `PUT /api/v1/notices/{id}` - 修改
- `DELETE /api/v1/notices/{ids}` - 批量删除
- `PUT /api/v1/notices/{id}/publish` - 发布
- `PUT /api/v1/notices/{id}/revoke` - 撤回
- `GET /api/v1/notices/{id}/detail` - 管理端详情
- `GET /api/v1/notices/my` - 我的通知
- `GET /api/v1/notices/my/{id}` - 阅读并标记已读
- `PUT /api/v1/notices/read-all` - 全部已读
- `GET /api/v1/notices/unread-count` - 未读数量

### 系统配置
- `GET /api/v1/configs` - 配置分页
- `GET /api/v1/configs/{id}/form` - 配置表单
- `POST /api/v1/configs` - 新增配置
- `PUT /api/v1/configs/{id}` - 修改配置
- `DELETE /api/v1/configs/{ids}` - 删除配置

## 扩展功能

### 1. 批量操作
支持用户、角色等的批量导入导出：
```java
@PostMapping("/import")
public Result<ImportResultVO> importUsers(@RequestParam("file") MultipartFile file) {
    ImportResultVO result = userService.importUsers(file);
    return Result.success(result);
}
```

### 2. 数据导出
支持Excel格式的数据导出：
```java
@GetMapping("/export")
public void exportUsers(UserQuery query, HttpServletResponse response) {
    userService.exportUsers(query, response);
}
```

### 3. 模板下载
提供导入模板下载功能：
```java
@GetMapping("/import-template")
public void downloadImportTemplate(HttpServletResponse response) {
    userService.downloadImportTemplate(response);
}
```

## 安全控制

### 1. 权限注解
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/page")
public Result<PageResult<UserVO>> getUserPage(@Valid UserQuery query) {
    // 仅管理员可访问
}
```

### 2. 数据过滤
```java
@DataPermission(deptAlias = "d")
@GetMapping("/list")
public Result<List<DeptVO>> getDeptList() {
    // 自动添加数据权限过滤
}
```

## 最佳实践

1. **分页查询**: 所有列表接口必须支持分页
2. **数据权限**: 涉及用户数据的查询必须添加数据权限控制
3. **缓存使用**: 合理使用缓存提高性能
4. **事务控制**: 批量操作使用事务保证数据一致性
5. **日志记录**: 重要操作记录操作日志

## 常见问题

### Q1: 如何添加新的系统参数？
A: 在参数管理界面添加，参数键名需要全局唯一。

### Q2: 数据权限不生效怎么办？
A: 检查@DataPermission注解配置是否正确，确认SQL中别名是否匹配。

### Q3: 如何解决循环依赖的部门结构？
A: 在保存和更新时进行循环依赖检测，避免无限递归。

### Q4: 用户导入失败如何处理？
A: 系统会返回详细的错误信息，包括失败行号和原因，修正后重新导入。

## 更新计划

- [ ] 添加组织架构图展示
- [ ] 支持用户自定义字段
- [ ] 实现角色继承机制
- [ ] 添加在线用户管理
- [ ] 支持多级审核流程