# Yue-Api 项目文档

## 项目概述

Yue-Api 是一个基于 Spring Boot 3.5.9 构建的企业级实时API管理系统，采用 **Maven 多模块**架构，模块依赖严格单向、无环，提供了完整的认证授权、系统管理、文件管理等功能模块。

## 技术架构

### 核心技术栈
- **框架**: Spring Boot 3.5.9
- **Java版本**: Java 17
- **安全框架**: Spring Security + JWT
- **数据访问**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0.30
- **连接池**: Druid 1.2.24
- **缓存**: Redis 7.x + Redisson + Caffeine
- **文档**: Knife4j (Swagger增强版)
- **对象映射**: MapStruct 1.6.3
- **工具库**: Hutool 5.8.41
- **Excel处理**: FastExcel 1.3.0
- **IP解析**: ip2region 2.7.0

### 架构特点
- Maven 多模块设计，职责分离、依赖单向无环
- 前后端分离架构
- RESTful API设计
- 统一响应封装
- 全局异常处理
- 数据权限控制
- 分布式缓存支持
- 操作日志基于 Spring 事件机制解耦（切面发布事件，业务模块监听落库）

## 模块结构

```
Yue-Api (root, packaging=pom)        # 聚合工程：统一版本与依赖管理
│
├── yue-common      基础工具层（零内部依赖）
│                   注解 / 常量 / 枚举 / 工具 / 基础模型 / 通用校验 / 配置属性
│
├── yue-core        Web 核心层        → yue-common
│                   Result / 全局异常 / 切面 / 日志事件 / 缓存·Web·Redis·Jackson 配置
│
├── yue-security    安全框架层        → yue-core
│                   JWT / Token / 过滤器 / 日志切面 / Spring Security 配置
│                   （只依赖 UserDetailsService 接口，不含业务实现）
│
├── yue-plugin      插件配置层        → yue-common, yue-security
│                   MyBatis-Plus 配置 / 数据权限处理器 / Knife4j 文档配置
│
├── yue-system      系统管理业务      → yue-security, yue-plugin
│                   用户 / 角色 / 菜单 / 部门 / 字典 / 日志
│                   提供 UserDetailsService 实现，监听日志事件落库
│
├── yue-auth        认证授权业务      → yue-security
│                   登录 / 验证码
│
├── yue-platform    平台能力          → yue-core
│                   文件管理（本地/MinIO）/ 实时通信（WebSocket，预留）
│
└── yue-boot        启动装配模块      → 以上全部（唯一可执行 jar）
                    启动类 / application*.yml / 代码生成模板
```

### 依赖方向（自底向上，严格单向）

```
yue-boot
 ├─ yue-auth ──┐
 ├─ yue-system ─┼─> yue-security ─> yue-core ─> yue-common
 ├─ yue-platform ──────────────────^      ▲
 └─ yue-plugin ───────────────────────────┘
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.x

### 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE real_time_api DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本（位于docs/sql目录）
```

### 配置文件
编辑 `yue-boot/src/main/resources/application-dev.yml` 文件，配置数据库和Redis连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: your_username
    password: your_password

  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 构建与启动
```bash
# 全量构建（在根目录执行）
mvn clean install -DskipTests

# 开发环境启动
java -jar yue-boot/target/yue-api.jar --spring.profiles.active=dev

# 生产环境
java -jar yue-boot/target/yue-api.jar --spring.profiles.active=prod
```

### 单独构建某个模块
```bash
# 构建 yue-system 及其依赖的上游模块
mvn install -pl yue-system -am
```

### 访问API文档
启动成功后，访问：http://localhost:8080/doc.html

## 功能特性

### 1. 认证授权（yue-auth / yue-security）
- JWT令牌认证
- 多方式登录（账号密码、手机号、邮箱）
- 验证码支持
- 权限验证
- 单点登录控制

### 2. 系统管理（yue-system）
- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 岗位管理
- 字典管理
- 参数设置
- 通知公告

### 3. 文件管理（yue-platform）
- 文件上传下载
- 多存储策略（本地、MinIO）
- 文件预览
- 断点续传

### 4. 系统监控（yue-system / yue-security）
- 在线用户监控
- 操作日志记录（切面采集 → 事件发布 → 监听落库）
- 登录日志追踪
- 服务监控

### 5. 高级特性
- 数据权限控制
- 防重复提交
- 操作日志注解
- 字段验证注解
- Excel导入导出
- IP地址定位

## 开发规范

### 1. 包命名规范
- 基础包名：`com.lycorder`
- 模块包名：`com.lycorder.{module}`，与 Maven 模块 `yue-{module}` 一一对应
- 功能包名：`com.lycorder.{module}.{function}`

### 2. 模块依赖规范
- 依赖方向必须保持单向：`common ← core ← security ← system/auth`，`platform`、`plugin` 为横向模块
- **禁止**下层模块依赖上层模块（如 common 不得引用 security/system）
- 跨层调用优先通过接口 + Spring 注入或 Spring 事件机制解耦
- 新功能的配置类放入所属模块的 `config` 包，不要放回 common

### 3. 类命名规范
- Controller：`XxxController`
- Service：`XxxService`, `XxxServiceImpl`
- Mapper：`XxxMapper`
- Entity：`Xxx`, `XxxDTO`, `XxxVO`

### 4. API设计规范
- RESTful风格
- 统一前缀：`/api/{module}`
- 统一响应格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

## 部署说明

### 1. 开发环境部署
- 使用Spring Boot内嵌容器
- 详细日志输出

### 2. 生产环境部署
- 独立JAR运行（仅需部署 `yue-boot` 产出的 `yue-api.jar`）
- 配置JVM参数优化
- 日志级别调整为WARN
- 启用生产配置

### 3. Docker部署
```dockerfile
FROM eclipse-temurin:17-jre
COPY yue-boot/target/yue-api.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 技术文档

详细技术文档请参考：
- [多模块重构实施方案](docs/refactor-plan.md)
- [认证模块设计](docs/auth-design.md)
- [系统管理模块](docs/system-module.md)
- [数据权限实现](docs/data-permission.md)
- [缓存策略说明](docs/cache-strategy.md)
- [性能优化方案](docs/performance.md)

## 更新日志

### v1.0.0 (2026-03-20)
- 初始版本发布
- 完成基础架构搭建
- 实现认证授权功能
- 完成系统管理模块
- 集成文件管理功能

### v1.1.0 (2026-10-22)
- 单体项目重构为 Maven 多模块架构（1 个聚合工程 + 8 个子模块）
- 解除包级循环依赖：日志改为事件机制、用户加载改为接口注入、配置类归位
- 启动模块独立（yue-boot），产出唯一可执行 jar

## 支持与贡献

如有问题或建议，欢迎提交Issue或Pull Request。

## 许可证

MIT License
