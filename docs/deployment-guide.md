# 部署和开发指南

## 概述

本文档详细介绍了Real-Time-Api项目的部署流程、环境配置、开发规范以及常见问题解决方案，帮助开发者快速搭建开发和生产环境。

## 环境要求

### 1. 开发环境
| 组件 | 版本要求 | 推荐版本 |
|------|----------|----------|
| JDK | 17+ | 17.0.8 |
| Maven | 3.6+ | 3.9.0 |
| MySQL | 8.0+ | 8.0.30 |
| Redis | 7.0+ | 7.0.12 |
| Node.js | 16+ | 18.17.0 |

### 2. 生产环境
| 组件 | 版本要求 | 推荐配置 |
|------|----------|----------|
| JDK | 17+ | 17.0.8 |
| MySQL | 8.0+ | 8.0.30（主从架构） |
| Redis | 7.0+ | 7.0.12（集群模式） |
| Nginx | 1.20+ | 1.25.0 |
| 操作系统 | Linux | CentOS 7+/Ubuntu 20+ |

## 开发环境搭建

### 1. 项目初始化

#### 1.1 克隆项目
```bash
git clone https://github.com/your-org/real-time-api.git
cd real-time-api
```

#### 1.2 导入数据库
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE real_time_api DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行项目初始化脚本（如项目未提供 init.sql，可跳过）
mysql -u root -p real_time_api < docs/sql/init.sql

# 通知公告：建表、菜单、按钮权限、字典
mysql -u root -p real_time_api < docs/sql/notice-menu.sql

# 系统配置：建表、菜单、按钮权限
mysql -u root -p real_time_api < docs/sql/config-menu.sql
```

> 如果数据库已经存在 `sys_notice`、`sys_user_notice`、`sys_config` 表，脚本中的 `CREATE TABLE IF NOT EXISTS` 不会覆盖已有数据。  
> 执行菜单脚本后需要重启后端，或重新保存一次角色菜单，使权限缓存生效。

#### 1.3 配置文件
编辑 `application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

# 文件上传配置
file:
  upload:
    path: /data/upload
    max-size: 10MB
    allow-types: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar

# JWT配置
jwt:
  secret: your-secret-key-here
  expiration: 7200
  refresh-expiration: 604800
```

#### 1.4 启动项目
```bash
# 方式一：Maven插件启动
mvn spring-boot:run -Dspring.profiles.active=dev

# 方式二：IDE启动
# 在IDE中运行RealTimeApiApplication.java

# 方式三：打包后启动
mvn clean package -DskipTests
java -jar target/real-time-api.jar --spring.profiles.active=dev
```

### 2. 前端开发环境

#### 2.1 安装依赖
```bash
cd real-time-ui
npm install
# 或使用yarn
yarn install
```

#### 2.2 配置代理
编辑 `vue.config.js`：
```javascript
module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  }
}
```

#### 2.3 启动前端
```bash
npm run serve
# 或使用yarn
yarn serve
```

## 生产环境部署

### 1. 传统部署

#### 1.1 服务器准备
```bash
# 安装JDK 17
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz
tar -xzf jdk-17_linux-x64_bin.tar.gz
mv jdk-17 /usr/local/java

# 配置环境变量
echo 'export JAVA_HOME=/usr/local/java' >> /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

# 验证安装
java -version
```

#### 1.2 数据库部署
```bash
# 安装MySQL 8.0
wget https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm
yum install mysql80-community-release-el7-5.noarch.rpm
yum install mysql-community-server

# 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 获取临时密码
grep 'temporary password' /var/log/mysqld.log

# 安全配置
mysql_secure_installation
```

#### 1.3 Redis部署
```bash
# 安装Redis
wget http://download.redis.io/releases/redis-7.0.12.tar.gz
tar xzf redis-7.0.12.tar.gz
cd redis-7.0.12
make
make install

# 配置Redis
cp redis.conf /etc/redis.conf
vim /etc/redis.conf
# 修改以下内容：
# bind 0.0.0.0
# requirepass yourpassword
# daemonize yes

# 启动Redis
redis-server /etc/redis.conf
```

#### 1.4 应用部署
```bash
# 创建应用目录
mkdir -p /app/real-time-api
cd /app/real-time-api

# 上传JAR包
# 创建启动脚本
vim start.sh
#!/bin/bash
nohup java -jar real-time-api.jar \
  --spring.profiles.active=prod \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/ \
  > /app/logs/app.log 2>&1 &
echo "应用已启动，PID: $!"

# 创建停止脚本
vim stop.sh
#!/bin/bash
PID=$(ps -ef | grep real-time-api.jar | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
  kill -15 $PID
  echo "应用已停止"
else
  echo "应用未运行"
fi

# 赋予执行权限
chmod +x start.sh stop.sh
```

### 2. Docker部署

#### 2.1 Dockerfile
```dockerfile
FROM openjdk:17-jre-slim

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /app

# 复制JAR包
COPY target/real-time-api.jar app.jar

# 复制配置文件
COPY config/application-prod.yml /app/config/

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

#### 2.2 Docker Compose部署
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0.30
    container_name: real-time-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: real_time_api
      MYSQL_USER: api_user
      MYSQL_PASSWORD: api_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./doc/sql:/docker-entrypoint-initdb.d
    command: [
      '--character-set-server=utf8mb4',
      '--collation-server=utf8mb4_unicode_ci',
      '--default-time-zone=+8:00'
    ]

  redis:
    image: redis:7.0.12
    container_name: real-time-redis
    restart: always
    command: redis-server --requirepass redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  app:
    build: .
    container_name: real-time-api
    restart: always
    depends_on:
      - mysql
      - redis
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
      - ./upload:/app/upload
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
      SPRING_DATASOURCE_USERNAME: api_user
      SPRING_DATASOURCE_PASSWORD: api_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redis_password

volumes:
  mysql_data:
  redis_data:
```

#### 2.3 启动服务
```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### 3. Kubernetes部署

#### 3.1 ConfigMap配置
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: real-time-api-config
data:
  application-prod.yml: |
    spring:
      datasource:
        url: jdbc:mysql://mysql-service:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
        username: api_user
        password: api_password
      redis:
        host: redis-service
        port: 6379
        password: redis_password
    server:
      port: 8080
```

#### 3.2 Deployment配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: real-time-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: real-time-api
  template:
    metadata:
      labels:
        app: real-time-api
    spec:
      containers:
      - name: app
        image: your-registry/real-time-api:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        volumeMounts:
        - name: config
          mountPath: /app/config
        - name: logs
          mountPath: /app/logs
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: config
        configMap:
          name: real-time-api-config
      - name: logs
        emptyDir: {}
```

#### 3.3 Service配置
```yaml
apiVersion: v1
kind: Service
metadata:
  name: real-time-api-service
spec:
  selector:
    app: real-time-api
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
```

#### 3.4 Ingress配置
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: real-time-api-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.realtimesystem.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: real-time-api-service
            port:
              number: 8080
```

## 环境配置

### 1. 开发环境配置
```yaml
# application-dev.yml
spring:
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

  # Redis配置
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 1000ms

# 日志配置
logging:
  level:
    com.lycorder: debug
    org.springframework: debug
```

### 2. 生产环境配置
```yaml
# application-prod.yml
spring:
  # 数据源配置
  datasource:
    url: jdbc:mysql://mysql-cluster:3306/real_time_api?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 100
      max-wait: 60000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

  # Redis集群配置
  redis:
    cluster:
      nodes:
        - redis1:6379
        - redis2:6379
        - redis3:6379
      max-redirects: 3
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 100
        max-idle: 50
        min-idle: 10
        max-wait: 1000ms

# 日志配置
logging:
  level:
    root: info
    com.lycorder: warn
  file:
    name: /app/logs/real-time-api.log
    max-size: 100MB
    max-history: 30
```

## 性能调优

### 1. JVM调优
```bash
# JVM启动参数
JAVA_OPTS="
-server
-Xms4g
-Xmx4g
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+ParallelRefProcEnabled
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:/app/logs/gc.log
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/app/logs/
-Dfile.encoding=UTF-8
-Djava.awt.headless=true
-Djava.net.preferIPv4Stack=true
"
```

### 2. 数据库调优
```sql
-- MySQL配置优化
SET GLOBAL innodb_buffer_pool_size = 8589934592; -- 8GB
SET GLOBAL innodb_log_file_size = 1073741824; -- 1GB
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL sync_binlog = 0;
SET GLOBAL query_cache_size = 536870912; -- 512MB
SET GLOBAL query_cache_type = ON;

-- 索引优化
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_dept ON sys_user(dept_id);
CREATE INDEX idx_role_data_scope ON sys_role(data_scope);
CREATE INDEX idx_menu_parent ON sys_menu(parent_id);
```

### 3. Redis调优
```yaml
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
tcp-keepalive 60
timeout 300
tcp-backlog 511
save 900 1
save 300 10
save 60 10000
rdbcompression yes
rdbchecksum yes
```

## 安全加固

### 1. 系统安全
```bash
# 创建专用用户
useradd -r -s /bin/false realtime
chown -R realtime:realtime /app/real-time-api

# 防火墙配置
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload

# SELinux配置（如启用）
setsebool -P httpd_can_network_connect 1
```

### 2. 数据库安全
```sql
-- 创建专用账号
CREATE USER 'api_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON real_time_api.* TO 'api_user'@'localhost';
FLUSH PRIVILEGES;

-- 启用SSL
SET GLOBAL require_secure_transport = ON;
```

### 3. 应用安全
```yaml
# 安全配置
security:
  # 密码策略
  password:
    min-length: 8
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special: true
    expire-days: 90

  # 登录限制
  login:
    max-attempts: 5
    lock-duration: 3600
    captcha-enabled: true

  # JWT配置
  jwt:
    secret: ${JWT_SECRET}
    expiration: 7200
    refresh-expiration: 604800
```

## 监控告警

### 1. 应用监控
```yaml
# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 日志监控
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="ELASTICSEARCH" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
        <url>http://elasticsearch:9200</url>
        <index>realtime-api-logs</index>
        <type>logs</type>
        <loggerName>realtime-api</loggerName>
    </appender>

    <root level="INFO">
        <appender-ref ref="ELASTICSEARCH"/>
    </root>
</configuration>
```

### 3. 告警规则
```yaml
# Prometheus告警规则
groups:
- name: real-time-api
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High error rate detected"

  - alert: HighMemoryUsage
    expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Memory usage is above 90%"
```

## 备份恢复

### 1. 数据库备份
```bash
#!/bin/bash
# 数据库备份脚本
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
DB_NAME="real_time_api"
DB_USER="backup_user"
DB_PASS="backup_password"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
mysqldump -u$DB_USER -p$DB_PASS --single-transaction --routines --triggers $DB_NAME | gzip > $BACKUP_DIR/${DB_NAME}_${DATE}.sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "${DB_NAME}_*.sql.gz" -mtime +7 -delete

# 同步到远程存储
rsync -avz $BACKUP_DIR/ backup@remote-server:/backup/mysql/
```

### 2. 文件备份
```bash
#!/bin/bash
# 文件备份脚本
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/files"
SOURCE_DIR="/app/upload"

# 创建备份
tar -czf $BACKUP_DIR/files_${DATE}.tar.gz -C $SOURCE_DIR .

# 同步到云存储
aws s3 sync $BACKUP_DIR s3://your-backup-bucket/files/
```

## 常见问题

### 1. 内存溢出
```bash
# 查看内存使用
jmap -heap <pid>

# 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>

# 分析工具
jvisualvm heap.hprof
```

### 2. 数据库连接池耗尽
```yaml
# 增加连接池大小
spring:
  datasource:
    druid:
      max-active: 200
      max-wait: 60000
      test-on-borrow: true
      validation-query: SELECT 1
```

### 3. Redis连接超时
```yaml
# 优化Redis配置
spring:
  redis:
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 200
        max-idle: 100
        min-idle: 20
```

## 性能压测

### 1. 使用JMeter
```bash
# 安装JMeter
wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.6.2.zip
unzip apache-jmeter-5.6.2.zip

# 运行压测
./jmeter -n -t test-plan.jmx -l result.jtl
```

### 2. 使用wrk
```bash
# 安装wrk
git clone https://github.com/wg/wrk.git
cd wrk
make

# 压测命令
./wrk -t12 -c400 -d30s http://localhost:8080/api/system/user/page
```

## 更新维护

### 1. 版本更新流程
```bash
#!/bin/bash
# 更新脚本
BACKUP_DIR="/backup/$(date +%Y%m%d_%H%M%S)"
APP_DIR="/app/real-time-api"

# 1. 备份当前版本
mkdir -p $BACKUP_DIR
cp $APP_DIR/*.jar $BACKUP_DIR/
cp $APP_DIR/config $BACKUP_DIR/ -r

# 2. 下载新版本
wget -O $APP_DIR/real-time-api-new.jar https://your-repo/releases/real-time-api-1.1.0.jar

# 3. 停止服务
$APP_DIR/stop.sh

# 4. 更新应用
mv $APP_DIR/real-time-api.jar $BACKUP_DIR/
mv $APP_DIR/real-time-api-new.jar $APP_DIR/real-time-api.jar

# 5. 启动服务
$APP_DIR/start.sh

# 6. 验证服务
sleep 30
curl -f http://localhost:8080/actuator/health

# 7. 回滚（如果需要）
if [ $? -ne 0 ]; then
  echo "服务启动失败，开始回滚..."
  $APP_DIR/stop.sh
  mv $BACKUP_DIR/real-time-api.jar $APP_DIR/
  $APP_DIR/start.sh
fi
```

### 2. 数据库迁移
```bash
# 使用Flyway
mvn flyway:migrate -Dflyway.configFile=flyway.conf

# 使用Liquibase
mvn liquibase:update -Dliquibase.url=jdbc:mysql://localhost:3306/real_time_api
```

## 安全更新

### 1. 依赖升级
```xml
<!-- 使用Maven插件检查依赖更新 -->
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>versions-maven-plugin</artifactId>
    <version>2.16.0</version>
    <configuration>
        <generateBackupPoms>false</generateBackupPoms>
    </configuration>
</plugin>
```

```bash
# 检查依赖更新
mvn versions:display-dependency-updates

# 更新依赖
mvn versions:use-latest-versions
```

### 2. 安全扫描
```bash
# OWASP依赖检查
mvn dependency-check:check

# SonarQube扫描
mvn sonar:sonar -Dsonar.host.url=http://sonar-server:9000
```

## 总结

本文档涵盖了Real-Time-Api项目的完整部署流程，包括：
- 开发环境搭建
- 生产环境部署（传统/Docker/K8s）
- 性能调优
- 安全加固
- 监控告警
- 备份恢复
- 更新维护

请根据实际需求选择合适的部署方案，并定期进行系统维护和更新。