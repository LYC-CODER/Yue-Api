# Real-Api Maven 多模块重构实施方案

## 0. 目标总览

将单体 Maven 项目 `Real-Time-Api` 拆分为 **1 个 root 聚合工程 + 8 个子模块**，采用**单一 Git 仓库 + Maven 子模块**方式，模块依赖严格单向、无环。

```
Real-Api (root, packaging=pom)
├── real-common      基础工具层（零内部依赖）
├── real-core        Web 核心层
├── real-security    安全框架层
├── real-plugin      插件配置层
├── real-system      系统管理业务
├── real-auth        认证授权业务
├── real-platform    平台能力（文件/实时通信）
└── real-boot        启动装配模块（唯一可执行 jar）
```

依赖方向（自底向上）：

```
real-boot
 ├─ real-auth ──┐
 ├─ real-system ─┼─> real-security ─> real-core ─> real-common
 ├─ real-platform ───────────────────^      ▲
 └─ real-plugin ────────────────────────────┘
```

---

## 1. 第一阶段：解环（不拆模块，先改代码）

这是整个重构的**前置条件**。以下 5 处循环依赖必须在单模块内先解开，否则模块拆分无法编译。

### 1.1 `common → security`：`common/config/SecurityConfig.java`
- **动作**：物理移动到 `com.lycorder.security.config.SecurityConfig`。
- 同时移动的：`common/config/PasswordEncoderConfig.java`、`common/config/property/SecurityProperties.java`。

### 1.2 `common → plugin`：`common/config/MybatisConfig.java`
- **动作**：移动到 `com.lycorder.plugin.mybatis.config.MybatisConfig`。

### 1.3 `common → core`：`common/annotation/ValidField.java` 引用 `core/validator/FieldValidator.java`
- **动作**：将 `FieldValidator` 移入 `common/validator/`（注解和校验器同属"通用校验"语义，放最底层的 common 最稳妥，core 本身也依赖 common）。

### 1.4 `core → system`：`core/aspect/LogAspect.java` 引用 system 的日志实体/Service
- **动作**：事件解耦。
  1. 在 `core` 新增 `com.lycorder.core.event.LogEvent`（纯 POJO，字段与 SysLog 对齐）。
  2. `LogAspect` 不再 new 实体、不再注入 system 的 Service，改为 `applicationEventPublisher.publishEvent(new LogEvent(...))`。
  3. 在 `system/listener/` 新增 `LogEventListener`（`@EventListener` / `@Async`），负责把 `LogEvent` 转成 `SysLog` 落库。

### 1.5 `security → system`：`security/service/SysUserDetailsService.java` 引用 `SysUserMapper`/`SysUser` 实体
- **动作**：依赖倒置。
  1. security 模块定义接口：`com.lycorder.security.service.UserDetailsProvider`，方法如 `UserAuthInfo loadByUsername(String username)` / `loadByPhone(...)` / `loadByEmail(...)`（security 里已有 `UserAuthInfo` 这个模型，正好作为契约 DTO）。
  2. 将 `SysUserDetailsService` 移动到 `system/service/impl/`，改名为 `SysUserDetailsProvider implements UserDetailsProvider`，内部继续用 `SysUserMapper`。
  3. security 的过滤器/Token 管理器一律只依赖 `UserDetailsProvider` 接口，由 Spring 在 boot 装配时注入实现。

### 1.6 顺带修正：`common → platform`
- `common/config/WebSocketConfig.java` 引用了 platform 的实时通信内容 → 移入 `platform/realtime/config/`（platform 模块预留了 realtime 包，正好归位）。

### 第一阶段验收
- 单模块项目可正常 `mvn clean package`。
- 启动后登录、日志记录、接口文档、文件上传四个核心链路回归通过。

---

## 2. 第二阶段：搭建多模块骨架

### 2.1 root `pom.xml` 改造
- `packaging` 改为 `pom`；保留 `spring-boot-starter-parent` 作为 parent。
- 所有版本号（properties）**全部保留在 root**，新增 `<dependencyManagement>` 统一声明内部模块版本。
- 新增 `<modules>`：

```xml
<modules>
    <module>real-common</module>
    <module>real-core</module>
    <module>real-security</module>
    <module>real-plugin</module>
    <module>real-system</module>
    <module>real-auth</module>
    <module>real-platform</module>
    <module>real-boot</module>
</modules>
```

- `spring-boot-maven-plugin` 从 root 的 `<build><plugins>` 中**移除**，只放到 `real-boot`（只有启动模块需要打可执行 jar）。

### 2.2 各子模块 pom 模板要点
- parent 指向 root `Real-Time-Api`，不重复声明版本。
- 依赖原则：**需要什么引什么**，内部模块依赖不写 `<version>`（由 root dependencyManagement 提供）。

| 模块 | 内部依赖 | 主要第三方依赖 |
|---|---|---|
| real-common | 无 | hutool, lombok, fastjson, ip2region, fastexcel, spring-web（注解用，provided） |
| real-core | common | spring-boot-starter-web, aop, cache, caffeine |
| real-security | core | spring-boot-starter-security, data-redis, redisson, jwt 相关 |
| real-plugin | common | mybatis-plus, knife4j, springdoc |
| real-system | security | mybatis-plus, mysql, druid, mapstruct |
| real-auth | security | （基本无新增） |
| real-platform | core | minio, websocket |
| real-boot | 全部 | spring-boot-maven-plugin |

### 2.3 骨架先空跑
- 先建 8 个空模块（只有 pom + 空 `src/main/java`），`mvn clean install` 通过后再开始迁代码。

---

## 3. 第三阶段：逐层迁移（类清单）

> 包名建议统一调整为 `com.lycorder.{module}.*` → 即**包结构不变**，只改物理归属，import 无需批量改动，迁移成本最低。

### 3.1 real-common（36+1 个类）
```
annotation/   DataPermission, Log, RepeatSubmit, ValidField
base/         BaseEntity, BaseQuery, IBaseEnum
constant/     JwtClaimConstants, RedisConstants, SecurityConstants, SystemConstants
enums/        CaptchaTypeEnum, DataScopeEnum, EnvEnum, LogModuleEnum,
              RequestMethodEnum, StatusEnum
model/        KeyValue, Option
util/         DateUtils, ExcelUtils, IPUtils
validator/    FieldValidator        ← 从 core 迁入（解环 1.3）
config/property/ CaptchaProperties  ← 纯属性类，随 common 走
resources:    data/ip2region.xdb
```

### 3.2 real-core（10 个类）
```
aspect/       LogAspect(已事件化), RepeatSubmitAspect
exception/    BusinessException, GlobalExceptionHandler
result/       ExcelResult, IResultCode, PageResult, Result, ResultCode, WebResponseWriter
config/       CaffeineConfig, CorsConfig, JacksonConfig, RedisConfig,
              RedisCacheConfig, WebMvcConfig    ← 从 common/config 迁入
```

### 3.3 real-security（14+3 个类）
```
filter/       CaptchaValidationFilter, TokenAuthenticationFilter
handler/      ApiAccessDeniedHandler, ApiAuthenticationEntryPoint
model/        AuthenticationToken, OnlineUser, SysUserDetails, UserAuthInfo
service/      PermissionService, UserDetailsProvider(新接口)
token/        JwtTokenManager, RedisTokenManager, TokenManager
util/         SecurityUtils
config/       SecurityConfig, PasswordEncoderConfig,
              property/SecurityProperties        ← 从 common 迁入
```
**注意**：`SysUserDetailsService` 不随本模块走，见 3.5。

### 3.4 real-plugin（3+2 个类）
```
mybatis/      ApiDataPermissionHandler, ApiMetaObjectHandler
mybatis/config/ MybatisConfig                   ← 从 common 迁入
knife4j/      OpenApiConfig                     ← 从 common/config 迁入
```

### 3.5 real-system（85+1 个类，整体平移）
```
controller(6) converter(6) enums(2) listener(1+新增LogEventListener)
mapper(9) model/*(bo2/dto3/entity9/form11/query8/vo10)
service(9) service/impl(9+新增SysUserDetailsProvider)
resources:    mapper/*.xml（10 个 XML 随模块走）
```

### 3.6 real-auth（5 个类）
```
controller/AuthController, model/dto/LoginRequest, model/vo/CaptchaVO
service/AuthService, service/impl/AuthServiceImpl
config/CaptchaConfig                              ← 从 common/config 迁入
```

### 3.7 real-platform（5+1 个类）
```
file/controller/FileController, file/model/FileInfo
file/service/FileService, file/service/impl/{LocalFileService, MinioFileService}
realtime/config/WebSocketConfig                   ← 从 common/config 迁入
```

### 3.8 real-boot
```
com.lycorder.ApiSystemApplication
resources: application.yml, application-dev.yml, application-prod.yml,
           banner.txt, META-INF/additional-spring-configuration-metadata.json,
           templates/（codegen + excel）
根目录: check_charset.sql、docs/ 保持不动
```
- 启动类 `@SpringBootApplication` 的 `scanBasePackages` 保持 `com.lycorder` 即可扫到所有模块。
- `@MapperScan("com.lycorder.**.mapper")` 检查覆盖范围。

---

## 4. 迁移顺序与验证（每一层都是可编译的检查点）

| 步骤 | 动作 | 验证 |
|---|---|---|
| 1 | 迁 real-common | `mvn install -pl real-common` |
| 2 | 迁 real-core | `-pl real-core -am` |
| 3 | 迁 real-security、real-plugin | 同上 |
| 4 | 迁 real-system | 同上（mapper xml 一起拷） |
| 5 | 迁 real-auth、real-platform | 同上 |
| 6 | 迁 real-boot，原 src 清空 | `mvn clean package` 全量 |
| 7 | 启动回归 | 登录/JWT、接口文档 /doc.html、文件上传、日志落库、验证码 |

---

## 5. 风险与注意事项

1. **MyBatis XML 位置**：XML 从 `src/main/resources/mapper/` 迁到 real-system 后，确认 `mybatis-plus.mapper-locations` 用 `classpath*:mapper/**/*.xml`（带 `*`），否则跨 jar 扫描不到。
2. **ip2region.xdb**：随 real-common 走，读取方式若是 `ClassPathResource` 则无需改动。
3. **Spring 配置元数据**：`additional-spring-configuration-metadata.json` 中引用的属性类若跨模块，注意合并；建议放 real-boot。
4. **MapStruct**：`mapstruct-processor` 需要在 real-system 的 `annotationProcessorPaths` 里重新配置（连同 lombok、lombok-mapstruct-binding），顺序：lombok → mapstruct → binding。
5. **Knife4j 分组文档**：若按包分组，模块拆分后包名不变，无需调整。
6. **不要做的事**：本阶段不做任何业务逻辑改动、不改包名、不升级依赖版本——重构与功能变更必须分开提交。

---

## 6. 提交计划（建议的 commit 粒度）

```
refactor: 解环 - LogAspect 改为事件发布机制
refactor: 解环 - UserDetailsProvider 接口下沉 security，实现移入 system
refactor: 解环 - 配置类归位（Security/Mybatis/WebSocket/Captcha）
refactor: 解环 - FieldValidator 并入 common
refactor: 搭建 Maven 多模块骨架（8 个子模块）
refactor: 迁移 common/core 层
refactor: 迁移 security/plugin 层
refactor: 迁移 system/auth/platform 业务层
refactor: 迁移 boot 启动模块，清理原单体目录
```

每个 commit 都应保持项目可编译、可启动。
