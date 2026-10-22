# 数据权限实现原理

## 概述

数据权限是指用户在其功能权限之外，对数据资源的访问控制。与功能权限（能做什么操作）不同，数据权限控制的是能看到哪些数据。本系统通过自定义注解和MyBatis拦截器实现灵活的数据权限控制。

## 设计原理

### 1. 核心概念

#### 1.1 数据权限类型
系统支持以下五种数据权限类型：

```java
public enum DataScope {
    ALL(1, "全部数据权限"),           // 可查看所有数据
    CUSTOM(2, "自定义数据权限"),       // 可查看指定部门的数据
    DEPT(3, "本部门数据权限"),         // 只能查看本部门数据
    DEPT_AND_CHILD(4, "本部门及子部门数据权限"), // 可查看本部门及所有子部门数据
    SELF(5, "仅本人数据权限");         // 只能查看自己的数据
}
```

#### 1.2 权限模型
采用RBAC（Role-Based Access Control）模型，通过角色关联数据权限：
- 用户 → 角色（多对多）
- 角色 → 数据权限（一对一）

### 2. 实现架构

```
权限控制流程：
用户请求 → 解析用户角色 → 获取数据权限 → 修改SQL → 返回过滤后的数据

核心组件：
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ @DataPermission │───▶│ DataPermissionInterceptor │───▶│ SQL重写引擎      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ 权限注解解析器   │    │ 权限规则引擎      │    │ SQL生成器        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 实现细节

### 1. 自定义注解

#### 1.1 @DataPermission注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 部门表别名
     */
    String deptAlias() default "";

    /**
     * 用户表别名
     */
    String userAlias() default "";

    /**
     * 权限类型（支持多个）
     */
    DataScope[] scopes() default {};

    /**
     * 排除的部门ID（管理员查看所有数据时使用）
     */
    String excludeDeptId() default "";
}
```

#### 1.2 使用示例
```java
@DataPermission(deptAlias = "d", userAlias = "u")
@GetMapping("/list")
public Result<List<UserVO>> getUserList() {
    List<UserVO> userList = userService.getUserList();
    return Result.success(userList);
}
```

### 2. MyBatis拦截器实现

#### 2.1 拦截器核心代码
```java
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataPermissionInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        // 获取SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        // 获取Mapper方法上的@DataPermission注解
        MappedStatement mappedStatement = getMappedStatement(statementHandler);
        String mapperId = mappedStatement.getId();
        Class<?> mapperClass = Class.forName(mapperId.substring(0, mapperId.lastIndexOf(".")));
        String methodName = mapperId.substring(mapperId.lastIndexOf(".") + 1);
        Method method = getMethodByName(mapperClass, methodName);

        if (method != null && method.isAnnotationPresent(DataPermission.class)) {
            DataPermission dataPermission = method.getAnnotation(DataPermission.class);

            // 获取当前用户的数据权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null) {
                String permissionSql = buildPermissionSql(originalSql, dataPermission, loginUser);

                // 使用反射修改SQL
                Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, permissionSql);
            }
        }

        return invocation.proceed();
    }
}
```

#### 2.2 SQL构建逻辑
```java
private String buildPermissionSql(String originalSql, DataPermission dataPermission, LoginUser loginUser) {
    // 获取用户的数据权限范围
    Set<String> dataScopes = loginUser.getDataScopes();

    // 如果包含全部权限，直接返回原SQL
    if (dataScopes.contains(DataScope.ALL.getCode())) {
        return originalSql;
    }

    StringBuilder permissionSql = new StringBuilder(originalSql);

    // 构建WHERE条件
    List<String> conditions = new ArrayList<>();

    // 处理不同数据权限类型
    for (String scope : dataScopes) {
        switch (DataScope.getByCode(scope)) {
            case SELF:
                if (StringUtils.isNotBlank(dataPermission.userAlias())) {
                    conditions.add(dataPermission.userAlias() + ".user_id = " + loginUser.getUserId());
                }
                break;

            case DEPT:
                if (StringUtils.isNotBlank(dataPermission.deptAlias())) {
                    conditions.add(dataPermission.deptAlias() + ".dept_id = " + loginUser.getDeptId());
                }
                break;

            case DEPT_AND_CHILD:
                if (StringUtils.isNotBlank(dataPermission.deptAlias())) {
                    Set<Long> deptIds = deptService.getDeptAndChildIds(loginUser.getDeptId());
                    conditions.add(dataPermission.deptAlias() + ".dept_id IN (" +
                        deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")");
                }
                break;

            case CUSTOM:
                if (StringUtils.isNotBlank(dataPermission.deptAlias())) {
                    Set<Long> customDeptIds = roleService.getCustomDeptIds(loginUser.getRoles());
                    if (!customDeptIds.isEmpty()) {
                        conditions.add(dataPermission.deptAlias() + ".dept_id IN (" +
                            customDeptIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")");
                    }
                }
                break;
        }
    }

    // 将条件拼接到SQL
    if (!conditions.isEmpty()) {
        String whereCondition = " AND (" + String.join(" OR ", conditions) + ")";

        // 判断原SQL是否已有WHERE条件
        String lowerSql = originalSql.toLowerCase();
        if (lowerSql.contains("where")) {
            permissionSql.append(whereCondition);
        } else {
            permissionSql.append(" WHERE 1=1").append(whereCondition);
        }
    }

    return permissionSql.toString();
}
```

### 3. 用户权限获取

#### 3.1 登录时获取权限
```java
@Service
public class PermissionService {

    public Set<String> getDataPermissions(Long userId) {
        // 获取用户的所有角色
        List<Role> roles = roleMapper.selectRolesByUserId(userId);

        Set<String> dataScopes = new HashSet<>();

        for (Role role : roles) {
            DataScope dataScope = DataScope.getByCode(role.getDataScope());
            dataScopes.add(dataScope.getCode());

            // 如果是自定义权限，获取配置的部门列表
            if (dataScope == DataScope.CUSTOM) {
                List<Long> deptIds = roleDeptMapper.selectDeptIdsByRoleId(role.getRoleId());
                loginUser.addCustomDeptIds(deptIds);
            }
        }

        // 如果没有数据权限，默认只能看自己
        if (dataScopes.isEmpty()) {
            dataScopes.add(DataScope.SELF.getCode());
        }

        return dataScopes;
    }
}
```

### 4. 部门树形结构处理

#### 4.1 获取部门及子部门
```java
@Service
public class DeptService {

    public Set<Long> getDeptAndChildIds(Long deptId) {
        Set<Long> deptIds = new HashSet<>();
        deptIds.add(deptId);

        // 递归获取所有子部门
        List<Dept> children = deptMapper.selectChildrenByParentId(deptId);
        for (Dept child : children) {
            deptIds.addAll(getDeptAndChildIds(child.getDeptId()));
        }

        return deptIds;
    }
}
```

## 使用示例

### 1. 用户管理数据权限
```java
@RestController
@RequestMapping("/api/system/user")
public class UserController {

    @DataPermission(deptAlias = "u")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> getUserPage(@Valid UserQuery query) {
        // SQL将被自动添加数据权限过滤条件
        // 如：AND (u.dept_id = 当前用户部门ID OR u.user_id = 当前用户ID)
        PageResult<UserVO> page = userService.getUserPage(query);
        return Result.success(page);
    }
}
```

### 2. 部门管理数据权限
```java
@DataPermission(deptAlias = "d")
@GetMapping("/tree")
public Result<List<DeptTreeVO>> getDeptTree() {
    // 根据数据权限返回不同的部门树
    List<DeptTreeVO> tree = deptService.buildDeptTree();
    return Result.success(tree);
}
```

### 3. 复杂查询的数据权限
```java
@DataPermission(deptAlias = "d", userAlias = "u",
                scopes = {DataScope.DEPT, DataScope.SELF})
@GetMapping("/statistics")
public Result<StatisticsVO> getStatistics() {
    // 支持复杂SQL的数据权限控制
    StatisticsVO statistics = reportService.getStatistics();
    return Result.success(statistics);
}
```

## 高级特性

### 1. 动态权限切换
```java
@Service
public class DynamicPermissionService {

    // 临时切换数据权限
    public <T> T executeWithPermission(DataScope scope, Supplier<T> supplier) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Set<String> originalScopes = loginUser.getDataScopes();

        try {
            // 设置新的权限范围
            loginUser.setDataScopes(Collections.singleton(scope.getCode()));

            // 执行操作
            return supplier.get();
        } finally {
            // 恢复原权限
            loginUser.setDataScopes(originalScopes);
        }
    }
}
```

### 2. 权限缓存
```java
@Component
public class PermissionCache {

    @Cacheable(value = "dataPermission", key = "#userId")
    public Set<String> getUserDataPermissions(Long userId) {
        return permissionService.getDataPermissions(userId);
    }

    @CacheEvict(value = "dataPermission", key = "#userId")
    public void refreshUserPermissions(Long userId) {
        // 刷新用户权限缓存
    }
}
```

### 3. 自定义权限规则
```java
@Component
public class CustomPermissionHandler {

    public boolean checkCustomPermission(String permissionKey, Object resource) {
        // 实现自定义权限校验逻辑
        LoginUser loginUser = SecurityUtils.getLoginUser();

        // 根据业务规则判断是否有权限
        switch (permissionKey) {
            case "dept:manager":
                return isDeptManager(loginUser, resource);
            case "project:owner":
                return isProjectOwner(loginUser, resource);
            default:
                return false;
        }
    }
}
```

## 性能优化

### 1. SQL优化
```sql
-- 创建复合索引提高查询性能
CREATE INDEX idx_user_dept_status ON sys_user(dept_id, status);
CREATE INDEX idx_role_data_scope ON sys_role(data_scope);

-- 使用JOIN替代子查询
SELECT u.* FROM sys_user u
INNER JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE d.ancestors LIKE CONCAT('%', ?, '%');
```

### 2. 缓存策略
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES));
        return cacheManager;
    }
}
```

### 3. 分页优化
```java
// 先获取符合条件的ID列表，再进行分页查询
private PageResult<UserVO> getUsersWithPermission(UserQuery query) {
    // 1. 获取有权限的部门ID列表
    Set<Long> permittedDeptIds = getPermittedDeptIds();

    // 2. 查询符合条件的用户ID总数
    long total = userMapper.countUsersByDeptIds(permittedDeptIds, query);

    // 3. 分页查询用户ID列表
    List<Long> userIds = userMapper.selectUserIdsByDeptIds(
        permittedDeptIds, query, PageUtils.getPage());

    // 4. 根据ID列表查询用户详情
    List<UserVO> userList = userMapper.selectUsersByIds(userIds);

    return new PageResult<>(total, userList);
}
```

## 测试用例

### 1. 单元测试
```java
@SpringBootTest
public class DataPermissionTest {

    @Test
    public void testDeptPermission() {
        // 模拟部门管理员登录
        LoginUser deptManager = mockLoginUser(DataScope.DEPT);

        // 执行查询
        List<UserVO> users = userService.getUserList();

        // 验证只返回本部门用户
        assertTrue(users.stream().allMatch(u -> u.getDeptId().equals(deptManager.getDeptId())));
    }

    @Test
    public void testSelfPermission() {
        // 模拟普通用户登录
        LoginUser normalUser = mockLoginUser(DataScope.SELF);

        // 执行查询
        List<UserVO> users = userService.getUserList();

        // 验证只返回自己的数据
        assertEquals(1, users.size());
        assertEquals(normalUser.getUserId(), users.get(0).getUserId());
    }
}
```

### 2. 性能测试
```java
@Test
public void testPermissionPerformance() {
    // 准备测试数据
    prepareTestData(10000);

    // 测试带权限查询的性能
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 100; i++) {
        userService.getUserPage(new UserQuery());
    }

    long endTime = System.currentTimeMillis();

    // 验证查询性能
    assertTrue("查询性能过慢", (endTime - startTime) / 100 < 100); // 平均耗时<100ms
}
```

## 常见问题

### Q1: 数据权限不生效？
A: 检查以下几点：
- 是否正确添加@DataPermission注解
- 表别名是否与SQL中的一致
- 用户是否分配了角色
- 角色是否配置了数据权限

### Q2: 如何处理复杂SQL的数据权限？
A: 对于复杂SQL，可以：
- 使用子查询包装原始SQL
- 在应用层进行数据过滤
- 自定义PermissionHandler

### Q3: 性能问题如何优化？
A: 优化建议：
- 添加合适的索引
- 使用缓存减少权限查询
- 优化SQL结构
- 分页查询

### Q4: 如何实现数据权限的实时变更？
A: 通过Redis发布订阅机制：
```java
// 权限变更时发布消息
redisTemplate.convertAndSend("permission:changed", userId);

// 监听权限变更消息
@RedisListener(topics = "permission:changed")
public void onPermissionChanged(String userId) {
    permissionCache.refreshUserPermissions(Long.valueOf(userId));
}
```

## 最佳实践

1. **权限最小化原则**: 只给用户分配必要的数据权限
2. **避免权限膨胀**: 定期审查和清理不必要的权限
3. **缓存合理使用**: 平衡数据实时性和性能
4. **SQL优化**: 为权限字段添加索引
5. **测试覆盖**: 确保各种权限场景都有测试用例

## 扩展功能

### 1. 行级权限
```java
// 支持基于条件的行级权限
@DataPermission(
    deptAlias = "d",
    customCondition = "status = 1 AND amount > 1000"
)
public List<Order> getOrders() {
    // 只能查看状态为1且金额大于1000的订单
}
```

### 2. 列级权限
```java
// 支持字段级别的权限控制
@DataPermission(
    deptAlias = "d",
    excludeColumns = {"salary", "bonus"}
)
public List<Employee> getEmployees() {
    // 普通用户无法查看薪资相关字段
}
```

### 3. 动态权限规则
```java
// 支持SpEL表达式定义动态权限
@DataPermission(
    deptAlias = "d",
    spel = "@permissionService.getCustomCondition(#root)"
)
public List<Project> getProjects() {
    // 通过SpEL动态获取权限条件
}
```

## 更新计划

- [ ] 支持分布式环境下的权限同步
- [ ] 添加权限审计功能
- [ ] 实现权限继承机制
- [ ] 支持跨系统的权限集成
- [ ] 添加权限性能监控
- [ ] 支持图形化的权限配置界面
- [ ] 实现权限变更的审批流程
- [ ] 添加权限测试工具