# 系统管理模块功能说明文档

## 模块概述

系统管理模块是Real-Time-Api项目的核心管理功能集合，提供了用户管理、角色管理、菜单管理、部门管理等基础系统功能。该模块采用RBAC（Role-Based Access Control）权限模型，支持灵活的角色权限配置和数据权限控制。

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

### 7. 参数设置

#### 7.1 功能列表
- 系统参数配置
- 参数分组管理
- 参数缓存

#### 7.2 参数配置示例
```yaml
# 系统参数
sys.user.initPassword: 123456
sys.user.maxRetry: 5
sys.file.maxSize: 10MB
sys.sms.expire: 300
```

### 8. 通知公告

#### 8.1 功能列表
- 公告发布
- 公告分类
- 已读未读状态
- 公告推送

#### 8.2 公告实体
```java
public class Notice extends BaseEntity {
    private Long noticeId;      // 公告ID
    private String title;       // 标题
    private Integer type;       // 类型（1-通知，2-公告）
    private String content;     // 内容
    private Integer status;     // 状态
    private Long senderId;      // 发布人
    private LocalDateTime sendTime;  // 发布时间
}
```

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
- `GET /api/system/user/page` - 分页查询用户
- `GET /api/system/user/{id}` - 获取用户详情
- `POST /api/system/user` - 新增用户
- `PUT /api/system/user` - 更新用户
- `DELETE /api/system/user/{ids}` - 删除用户
- `PUT /api/system/user/status` - 修改用户状态
- `PUT /api/system/user/reset-pwd` - 重置密码
- `POST /api/system/user/import` - 导入用户
- `GET /api/system/user/export` - 导出用户

### 角色管理
- `GET /api/system/role/page` - 分页查询角色
- `GET /api/system/role/{id}` - 获取角色详情
- `POST /api/system/role` - 新增角色
- `PUT /api/system/role` - 更新角色
- `DELETE /api/system/role/{ids}` - 删除角色
- `GET /api/system/role/menu/{roleId}` - 获取角色菜单
- `POST /api/system/role/menu` - 保存角色菜单
- `GET /api/system/role/dept/{roleId}` - 获取角色数据权限
- `POST /api/system/role/dept` - 保存角色数据权限

### 菜单管理
- `GET /api/system/menu/list` - 获取菜单列表
- `GET /api/system/menu/tree` - 获取菜单树
- `GET /api/system/menu/routes` - 获取路由信息
- `GET /api/system/menu/{id}` - 获取菜单详情
- `POST /api/system/menu` - 新增菜单
- `PUT /api/system/menu` - 更新菜单
- `DELETE /api/system/menu/{id}` - 删除菜单

### 部门管理
- `GET /api/system/dept/list` - 获取部门列表
- `GET /api/system/dept/tree` - 获取部门树
- `GET /api/system/dept/{id}` - 获取部门详情
- `POST /api/system/dept` - 新增部门
- `PUT /api/system/dept` - 更新部门
- `DELETE /api/system/dept/{id}` - 删除部门

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