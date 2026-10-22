# API接口文档

## 概述

本文档详细描述了Real-Time-Api系统的所有RESTful API接口，包括认证授权、系统管理、文件管理等模块的接口定义、请求参数、响应格式等信息。

## 接口规范

### 1. 通用规范

#### 1.1 基础URL
```
开发环境：http://localhost:8080
生产环境：https://api.realtimesystem.com
```

#### 1.2 请求头
```http
Content-Type: application/json;charset=UTF-8
Authorization: Bearer {access_token}
X-Requested-With: XMLHttpRequest
```

#### 1.3 响应格式
所有接口返回统一格式的JSON数据：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1740936000000
}
```

#### 1.4 状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 2. 分页规范

#### 2.1 请求参数
```json
{
  "pageNum": 1,     // 页码，从1开始
  "pageSize": 10,   // 每页条数
  "sort": "createTime", // 排序字段
  "order": "desc"   // 排序方式：asc/desc
}
```

#### 2.2 响应格式
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [],
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "hasNext": true,
    "hasPrev": false
  }
}
```

## 认证授权API

### 1. 用户登录

#### 1.1 账号密码登录
```http
POST /api/auth/login
```

**请求参数：**
```json
{
  "username": "admin",
  "password": "123456",
  "captchaCode": "8kj4",
  "captchaKey": "captcha:123456789"
}
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6ImI5NmQ5M2Q5LTM3YzYtNGY4OS1hNDI4LTRjNDNmMWE3MWU2NyJ9.xxx",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2tleSI6ImI5NmQ5M2Q5LTM3YzYtNGY4OS1hNDI4LTRjNDNmMWE3MWU2NyJ9.yyy",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://xxx.com/avatar.jpg",
      "roles": ["admin"],
      "permissions": ["system:user:add", "system:user:edit"]
    }
  }
}
```

#### 1.2 手机号登录
```http
POST /api/auth/login/mobile
```

**请求参数：**
```json
{
  "mobile": "13800138000",
  "smsCode": "123456"
}
```

#### 1.3 邮箱登录
```http
POST /api/auth/login/email
```

**请求参数：**
```json
{
  "email": "admin@example.com",
  "emailCode": "123456"
}
```

### 2. 令牌刷新
```http
POST /api/auth/refresh
```

**请求参数：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.xxx"
}
```

### 3. 获取验证码

#### 3.1 图形验证码
```http
GET /api/auth/captcha
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "captchaKey": "captcha:123456789",
    "captchaBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "expireTime": 300
  }
}
```

#### 3.2 发送短信验证码
```http
POST /api/auth/sms-code
```

**请求参数：**
```json
{
  "mobile": "13800138000",
  "captchaCode": "8kj4",
  "captchaKey": "captcha:123456789"
}
```

#### 3.3 发送邮箱验证码
```http
POST /api/auth/email-code
```

**请求参数：**
```json
{
  "email": "admin@example.com",
  "captchaCode": "8kj4",
  "captchaKey": "captcha:123456789"
}
```

### 4. 退出登录
```http
POST /api/auth/logout
```

## 系统管理API

### 1. 用户管理

#### 1.1 分页查询用户
```http
GET /api/system/user/page?pageNum=1&pageSize=10&username=admin&status=0
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 50,
    "list": [
      {
        "userId": 1,
        "username": "admin",
        "nickname": "超级管理员",
        "email": "admin@example.com",
        "mobile": "13800138000",
        "sex": 1,
        "avatar": "https://xxx.com/avatar.jpg",
        "status": 0,
        "deptId": 100,
        "deptName": "总部",
        "roleIds": [1],
        "roleNames": ["超级管理员"],
        "createTime": "2024-01-01 10:00:00",
        "lastLoginTime": "2024-03-01 15:30:00"
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "pages": 5
  }
}
```

#### 1.2 获取用户详情
```http
GET /api/system/user/{userId}
```

#### 1.3 新增用户
```http
POST /api/system/user
```

**请求参数：**
```json
{
  "username": "testuser",
  "nickname": "测试用户",
  "password": "123456",
  "email": "test@example.com",
  "mobile": "13900139000",
  "sex": 1,
  "deptId": 101,
  "roleIds": [2, 3],
  "status": 0,
  "remark": "测试用户"
}
```

#### 1.4 更新用户
```http
PUT /api/system/user
```

#### 1.5 删除用户
```http
DELETE /api/system/user/{userIds}
```

#### 1.6 重置密码
```http
PUT /api/system/user/reset-pwd
```

**请求参数：**
```json
{
  "userId": 10,
  "password": "123456"
}
```

#### 1.7 修改用户状态
```http
PUT /api/system/user/status
```

**请求参数：**
```json
{
  "userId": 10,
  "status": 1
}
```

#### 1.8 导入用户
```http
POST /api/system/user/import
```

**请求参数：**
```http
Content-Type: multipart/form-data
file: 用户导入模板.xlsx
```

#### 1.9 导出用户
```http
GET /api/system/user/export
```

#### 1.10 下载导入模板
```http
GET /api/system/user/import-template
```

### 2. 角色管理

#### 2.1 分页查询角色
```http
GET /api/system/role/page?pageNum=1&pageSize=10&roleName=管理员&status=0
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [
      {
        "roleId": 1,
        "roleName": "超级管理员",
        "roleKey": "admin",
        "roleSort": 1,
        "dataScope": 1,
        "menuCheckStrictly": true,
        "deptCheckStrictly": true,
        "status": 0,
        "remark": "超级管理员",
        "createTime": "2024-01-01 10:00:00",
        "permissions": ["system:user:add", "system:user:edit"]
      }
    ]
  }
}
```

#### 2.2 获取角色详情
```http
GET /api/system/role/{roleId}
```

#### 2.3 新增角色
```http
POST /api/system/role
```

**请求参数：**
```json
{
  "roleName": "普通用户",
  "roleKey": "user",
  "roleSort": 2,
  "dataScope": 5,
  "menuIds": [1, 2, 3],
  "deptIds": [100, 101],
  "status": 0,
  "remark": "普通用户角色"
}
```

#### 2.4 更新角色
```http
PUT /api/system/role
```

#### 2.5 删除角色
```http
DELETE /api/system/role/{roleIds}
```

#### 2.6 获取角色菜单权限
```http
GET /api/system/role/menu/{roleId}
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "checkedKeys": [1, 2, 3, 4],
    "menus": [
      {
        "id": 1,
        "label": "系统管理",
        "children": [
          {
            "id": 2,
            "label": "用户管理"
          }
        ]
      }
    ]
  }
}
```

#### 2.7 保存角色菜单权限
```http
POST /api/system/role/menu
```

**请求参数：**
```json
{
  "roleId": 2,
  "menuIds": [1, 2, 3, 4, 5]
}
```

#### 2.8 获取角色数据权限
```http
GET /api/system/role/dept/{roleId}
```

#### 2.9 保存角色数据权限
```http
POST /api/system/role/dept
```

### 3. 菜单管理

#### 3.1 获取菜单列表
```http
GET /api/system/menu/list
```

#### 3.2 获取菜单树
```http
GET /api/system/menu/tree
```

#### 3.3 获取路由信息
```http
GET /api/system/menu/routes
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "path": "/system",
      "component": "Layout",
      "redirect": "/system/user",
      "name": "System",
      "meta": {
        "title": "系统管理",
        "icon": "system",
        "roles": ["admin"]
      },
      "children": [
        {
          "path": "user",
          "component": "system/user/index",
          "name": "User",
          "meta": {
            "title": "用户管理",
            "icon": "user",
            "roles": ["admin"]
          }
        }
      ]
    }
  ]
}
```

#### 3.4 获取菜单详情
```http
GET /api/system/menu/{menuId}
```

#### 3.5 新增菜单
```http
POST /api/system/menu
```

**请求参数：**
```json
{
  "menuName": "测试菜单",
  "parentId": 0,
  "orderNum": 1,
  "path": "test",
  "component": "test/index",
  "menuType": "C",
  "visible": 0,
  "status": 0,
  "perms": "system:test:list",
  "icon": "test",
  "isFrame": 1,
  "isCache": 0
}
```

#### 3.6 更新菜单
```http
PUT /api/system/menu
```

#### 3.7 删除菜单
```http
DELETE /api/system/menu/{menuId}
```

### 4. 部门管理

#### 4.1 获取部门列表
```http
GET /api/system/dept/list
```

#### 4.2 获取部门树
```http
GET /api/system/dept/tree
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 100,
      "label": "总公司",
      "children": [
        {
          "id": 101,
          "label": "技术部",
          "children": [
            {
              "id": 103,
              "label": "研发部"
            },
            {
              "id": 104,
              "label": "测试部"
            }
          ]
        }
      ]
    }
  ]
}
```

#### 4.3 获取部门详情
```http
GET /api/system/dept/{deptId}
```

#### 4.4 新增部门
```http
POST /api/system/dept
```

**请求参数：**
```json
{
  "parentId": 100,
  "deptName": "新部门",
  "orderNum": 1,
  "leader": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "status": 0
}
```

#### 4.5 更新部门
```http
PUT /api/system/dept
```

#### 4.6 删除部门
```http
DELETE /api/system/dept/{deptId}
```

### 5. 字典管理

#### 5.1 获取字典类型列表
```http
GET /api/system/dict/type/page?pageNum=1&pageSize=10
```

#### 5.2 获取字典数据列表
```http
GET /api/system/dict/data/list?dictType=sys_user_sex
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "dictCode": 1,
      "dictSort": 1,
      "dictLabel": "男",
      "dictValue": "0",
      "dictType": "sys_user_sex",
      "cssClass": "",
      "listClass": "",
      "isDefault": "Y",
      "status": 0
    },
    {
      "dictCode": 2,
      "dictSort": 2,
      "dictLabel": "女",
      "dictValue": "1",
      "dictType": "sys_user_sex",
      "cssClass": "",
      "listClass": "",
      "isDefault": "N",
      "status": 0
    }
  ]
}
```

## 文件管理API

### 1. 文件上传

#### 1.1 单文件上传
```http
POST /api/file/upload
```

**请求参数：**
```http
Content-Type: multipart/form-data
file: 文件
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "fileId": 1,
    "fileName": "test.jpg",
    "fileSize": 102400,
    "fileType": "jpg",
    "url": "https://xxx.com/file/2024/03/01/test_123456.jpg",
    "md5": "a1b2c3d4e5f6"
  }
}
```

#### 1.2 多文件上传
```http
POST /api/file/upload/batch
```

#### 1.3 分片上传
```http
POST /api/file/upload/chunk
```

**请求参数：**
```json
{
  "file": "分片文件",
  "md5": "文件MD5",
  "chunkIndex": 0,
  "chunkSize": 1048576,
  "totalChunks": 10
}
```

#### 1.4 合并分片
```http
POST /api/file/upload/merge
```

**请求参数：**
```json
{
  "md5": "文件MD5",
  "fileName": "test.zip",
  "totalChunks": 10
}
```

### 2. 文件下载

#### 2.1 文件下载
```http
GET /api/file/download/{fileId}
```

#### 2.2 图片预览
```http
GET /api/file/preview/{fileId}
```

#### 2.3 获取文件信息
```http
GET /api/file/info/{fileId}
```

### 3. 文件管理

#### 3.1 分页查询文件
```http
GET /api/file/page?pageNum=1&pageSize=10&fileName=test&fileType=jpg
```

#### 3.2 删除文件
```http
DELETE /api/file/{fileIds}
```

## 系统监控API

### 1. 在线用户

#### 1.1 获取在线用户列表
```http
GET /api/monitor/online/page?pageNum=1&pageSize=10
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 5,
    "list": [
      {
        "tokenId": "123456789",
        "userName": "admin",
        "deptName": "总部",
        "ipaddr": "192.168.1.100",
        "loginLocation": "北京市",
        "browser": "Chrome 12",
        "os": "Windows 10",
        "loginTime": "2024-03-01 15:30:00"
      }
    ]
  }
}
```

#### 1.2 强退用户
```http
DELETE /api/monitor/online/{tokenId}
```

### 2. 操作日志

#### 2.1 分页查询操作日志
```http
GET /api/monitor/operlog/page?pageNum=1&pageSize=10&operName=admin&businessType=1
```

### 3. 登录日志

#### 3.1 分页查询登录日志
```http
GET /api/monitor/logininfor/page?pageNum=1&pageSize=10&userName=admin&status=0
```

### 4. 服务监控

#### 4.1 获取服务器信息
```http
GET /api/monitor/server
```

**响应数据：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "cpu": {
      "cpuNum": 8,
      "total": 10.5,
      "sys": 5.2,
      "used": 68.5,
      "wait": 0.5,
      "free": 31.5
    },
    "mem": {
      "total": 16.0,
      "used": 10.5,
      "free": 5.5,
      "usage": 65.6
    },
    "sys": {
      "computerName": "server-01",
      "osName": "Windows 10",
      "osArch": "amd64",
      "userDir": "D:\\app"
    },
    "jvm": {
      "name": "Java HotSpot(TM) 64-Bit Server VM",
      "version": "17.0.1",
      "startTime": "2024-01-01 08:00:00",
      "runTime": "72小时",
      "home": "D:\\java",
      "total": 4.0,
      "used": 2.5,
      "free": 1.5,
      "usage": 62.5
    }
  }
}
```

## 公共API

### 1. 获取上传令牌
```http
GET /api/common/upload/token
```

### 2. 获取系统配置
```http
GET /api/common/config/{configKey}
```

### 3. 获取字典数据
```http
GET /api/common/dict/{dictType}
```

### 4. 获取地区信息
```http
GET /api/common/region/{parentCode}
```

## 错误码说明

### 1. 系统级错误码
| 错误码 | 说明 |
|--------|------|
| 500 | 系统内部错误 |
| 501 | 功能未实现 |
| 502 | 服务不可用 |
| 503 | 服务维护中 |

### 2. 业务级错误码
| 错误码 | 说明 |
|--------|------|
| 1001 | 参数验证失败 |
| 1002 | 数据已存在 |
| 1003 | 数据不存在 |
| 1004 | 数据已被修改 |
| 1005 | 操作不允许 |

### 3. 认证授权错误码
| 错误码 | 说明 |
|--------|------|
| 2001 | 用户名或密码错误 |
| 2002 | 验证码错误 |
| 2003 | 账号已被锁定 |
| 2004 | 账号已被禁用 |
| 2005 | 令牌已过期 |
| 2006 | 令牌无效 |
| 2007 | 无访问权限 |

### 4. 文件管理错误码
| 错误码 | 说明 |
|--------|------|
| 3001 | 文件上传失败 |
| 3002 | 文件类型不允许 |
| 3003 | 文件大小超出限制 |
| 3004 | 文件不存在 |
| 3005 | 文件下载失败 |

## 调用示例

### 1. Java调用示例
```java
// 使用RestTemplate
RestTemplate restTemplate = new RestTemplate();

// 设置请求头
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.set("Authorization", "Bearer " + accessToken);

// 构建请求
LoginDTO loginDTO = new LoginDTO();
loginDTO.setUsername("admin");
loginDTO.setPassword("123456");

HttpEntity<LoginDTO> request = new HttpEntity<>(loginDTO, headers);

// 发送请求
ResponseEntity<Result> response = restTemplate.postForEntity(
    "http://localhost:8080/api/auth/login",
    request,
    Result.class
);
```

### 2. JavaScript调用示例
```javascript
// 使用axios
axios.post('/api/auth/login', {
  username: 'admin',
  password: '123456'
})
.then(function (response) {
  console.log(response.data);
  // 保存token
  localStorage.setItem('accessToken', response.data.data.accessToken);
})
.catch(function (error) {
  console.log(error);
});

// 带token的请求
axios.get('/api/system/user/page', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
  },
  params: {
    pageNum: 1,
    pageSize: 10
  }
})
.then(function (response) {
  console.log(response.data);
});
```

### 3. cURL调用示例
```bash
# 登录
$ curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 带token访问
$ curl -X GET http://localhost:8080/api/system/user/page?pageNum=1&pageSize=10 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.xxx"
```

## SDK开发

### 1. Java SDK
```xml
<dependency>
    <groupId>com.lycorder</groupId>
    <artifactId>realtime-api-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
// 初始化客户端
RealTimeApiClient client = new RealTimeApiClient.Builder()
    .baseUrl("http://localhost:8080")
    .accessToken("your-access-token")
    .build();

// 调用API
Result<LoginVO> result = client.auth().login("admin", "123456");
PageResult<UserVO> users = client.system().getUserPage(1, 10);
```

### 2. JavaScript SDK
```bash
npm install realtime-api-sdk
```

```javascript
import RealTimeApiClient from 'realtime-api-sdk';

// 初始化客户端
const client = new RealTimeApiClient({
  baseURL: 'http://localhost:8080',
  accessToken: 'your-access-token'
});

// 调用API
const loginResult = await client.auth.login('admin', '123456');
const users = await client.system.getUserPage({ pageNum: 1, pageSize: 10 });
```

## 版本管理

### 1. 版本控制
API版本通过URL路径控制：
```
/v1/api/...    # 第一版API
/v2/api/...    # 第二版API
```

### 2. 兼容性策略
- 小版本更新保持向后兼容
- 大版本更新可能破坏兼容性
- 废弃的API会提前通知并保留6个月的过渡期

### 3. 更新日志
请关注官方文档获取API更新日志。

## 测试环境

### 1. 测试账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| test | 123456 | 普通用户 |

### 2. 测试数据
系统预置了完整的测试数据，包括：
- 组织架构数据
- 用户角色数据
- 菜单权限数据
- 字典数据

### 3. 测试工具
推荐使用以下工具测试API：
- Postman
- Insomnia
- Swagger UI（访问/doc.html）

## 注意事项

1. **令牌有效期**：accessToken默认2小时过期，refreshToken默认7天过期
2. **频率限制**：API调用频率限制为100次/分钟/IP
3. **数据权限**：所有查询接口都会根据用户的数据权限过滤数据
4. **分页限制**：pageSize最大允许100条记录
5. **文件上传**：单文件最大支持10MB，支持格式见配置文件

## 技术支持

如遇到API使用问题，可通过以下方式获取支持：
- 提交Issue到GitHub仓库
- 发送邮件至 support@realtimesystem.com
- 查看在线文档 https://docs.realtimesystem.com