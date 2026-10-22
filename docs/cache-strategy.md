# 缓存策略说明

## 概述

Real-Time-Api项目采用多层缓存架构，结合Redis分布式缓存和Caffeine本地缓存，实现高性能、高可用的缓存系统。通过合理的缓存策略，有效降低数据库压力，提升系统响应速度。

## 缓存架构

### 1. 多层缓存结构
```
用户请求
    ↓
[多层缓存]
┌─────────────────┐
│   浏览器缓存     │  ← 静态资源、API响应
├─────────────────┤
│   CDN缓存       │  ← 静态文件、图片
├─────────────────┤
│   Nginx缓存     │  ← 反向代理缓存
├─────────────────┤
│   应用层缓存     │
│   ├─ Caffeine   │  ← 本地一级缓存
│   └─ Redis      │  ← 分布式二级缓存
├─────────────────┤
│   数据库         │  ← MySQL + 查询缓存
└─────────────────┘
```

### 2. 缓存组件选择

| 缓存类型 | 技术选型 | 适用场景 | 特点 |
|---------|---------|---------|------|
| 本地缓存 | Caffeine | 热点数据、频繁访问 | 速度快、无网络开销 |
| 分布式缓存 | Redis | 共享数据、会话存储 | 支持集群、数据持久化 |
| 本地锁 | synchronized | JVM级别锁 | 简单高效 |
| 分布式锁 | Redisson | 分布式环境锁 | 支持RedLock算法 |

## 缓存策略详解

### 1. 缓存更新策略

#### 1.1 Cache-Aside模式（旁路缓存）
```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String USER_CACHE_KEY = "user:";

    /**
     * 查询用户（Cache-Aside）
     */
    public User getUserById(Long userId) {
        String key = USER_CACHE_KEY + userId;

        // 1. 查询缓存
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) {
            return user;
        }

        // 2. 缓存未命中，查询数据库
        user = userMapper.selectById(userId);
        if (user != null) {
            // 3. 写入缓存
            redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
        }

        return user;
    }

    /**
     * 更新用户（先更新数据库，再删除缓存）
     */
    @Transactional
    public void updateUser(User user) {
        // 1. 更新数据库
        userMapper.updateById(user);

        // 2. 删除缓存
        String key = USER_CACHE_KEY + user.getUserId();
        redisTemplate.delete(key);

        // 3. 发送缓存更新消息
        eventPublisher.publishEvent(new CacheUpdateEvent(this, key));
    }
}
```

#### 1.2 Write-Through模式（直写缓存）
```java
@Service
public class DictService {

    /**
     * 更新字典（Write-Through）
     */
    @CachePut(value = "dict", key = "#dict.type")
    public Dict updateDict(Dict dict) {
        // 同时更新缓存和数据库
        dictMapper.updateById(dict);
        return dict;
    }
}
```

#### 1.3 Write-Behind模式（异步缓存写入）
```java
@Service
public class LogService {

    private final BlockingQueue<OperationLog> logQueue = new LinkedBlockingQueue<>(10000);

    /**
     * 异步写入日志
     */
    public void asyncSaveLog(OperationLog log) {
        logQueue.offer(log);
    }

    /**
     * 批量写入数据库（定时任务）
     */
    @Scheduled(fixedDelay = 5000)
    public void batchSaveLogs() {
        List<OperationLog> logs = new ArrayList<>();
        logQueue.drainTo(logs, 1000);

        if (!logs.isEmpty()) {
            logMapper.batchInsert(logs);
        }
    }
}
```

### 2. 缓存淘汰策略

#### 2.1 Caffeine缓存配置
```java
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine本地缓存配置
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            // 初始容量
            .initialCapacity(100)
            // 最大容量
            .maximumSize(1000)
            // 写入后过期时间
            .expireAfterWrite(10, TimeUnit.MINUTES)
            // 访问后过期时间
            .expireAfterAccess(30, TimeUnit.MINUTES)
            // 基于引用失效（软引用）
            .softValues()
            // 记录统计信息
            .recordStats()
            // 移除监听器
            .removalListener((key, value, cause) -> {
                log.info("Cache removed: key={}, value={}, cause={}", key, value, cause);
            })
        );
        return cacheManager;
    }
}
```

#### 2.2 Redis缓存淘汰策略
```yaml
spring:
  redis:
    # 缓存淘汰策略：allkeys-lru（移除最近最少使用的key）
    maxmemory-policy: allkeys-lru
    # 最大内存
    maxmemory: 1gb
    # 过期key的扫描频率
    hz: 10
```

### 3. 缓存预热策略

#### 3.1 启动时预热
```java
@Component
public class CacheWarmUpRunner implements CommandLineRunner {

    @Autowired
    private DictService dictService;

    @Autowired
    private ConfigService configService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始缓存预热...");

        // 预热字典数据
        dictService.warmUpDictCache();

        // 预热配置数据
        configService.warmUpConfigCache();

        // 预热用户权限数据
        permissionService.warmUpPermissionCache();

        log.info("缓存预热完成");
    }
}
```

#### 3.2 定时预热
```java
@Service
public class CacheWarmUpService {

    /**
     * 每天凌晨预热热点数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void warmUpHotData() {
        // 获取昨天的热点数据
        List<String> hotKeys = statisticsService.getYesterdayHotKeys();

        // 重新加载到缓存
        for (String key : hotKeys) {
            cacheReloadService.reload(key);
        }
    }
}
```

## 分布式缓存方案

### 1. Redis集群配置
```yaml
spring:
  redis:
    cluster:
      nodes:
        - 192.168.1.101:6379
        - 192.168.1.102:6379
        - 192.168.1.103:6379
        - 192.168.1.104:6379
        - 192.168.1.105:6379
        - 192.168.1.106:6379
      max-redirects: 3
    password: your-password
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 100
        max-idle: 50
        min-idle: 10
        max-wait: 1000ms
```

### 2. 缓存序列化
```java
@Configuration
public class RedisConfig {

    /**
     * Redis序列化配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用Jackson2JsonRedisSerializer进行序列化
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        // 设置key和value的序列化规则
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### 3. 多级缓存实现
```java
@Component
public class MultiLevelCache {

    @Autowired
    private CacheManager caffeineCacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存（先查本地缓存，再查Redis）
     */
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        // 1. 查询本地缓存
        Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            Cache.ValueWrapper wrapper = caffeineCache.get(key);
            if (wrapper != null) {
                return clazz.cast(wrapper.get());
            }
        }

        // 2. 查询Redis缓存
        String redisKey = cacheName + ":" + key;
        T value = (T) redisTemplate.opsForValue().get(redisKey);

        // 3. 写入本地缓存
        if (value != null && caffeineCache != null) {
            caffeineCache.put(key, value);
        }

        return value;
    }

    /**
     * 更新缓存（更新Redis和本地缓存）
     */
    public void put(String cacheName, String key, Object value) {
        // 1. 更新Redis缓存
        String redisKey = cacheName + ":" + key;
        redisTemplate.opsForValue().set(redisKey, value, 30, TimeUnit.MINUTES);

        // 2. 更新本地缓存
        Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            caffeineCache.put(key, value);
        }
    }

    /**
     * 删除缓存（删除Redis和本地缓存）
     */
    public void evict(String cacheName, String key) {
        // 1. 删除Redis缓存
        String redisKey = cacheName + ":" + key;
        redisTemplate.delete(redisKey);

        // 2. 删除本地缓存
        Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            caffeineCache.evict(key);
        }
    }
}
```

## 缓存一致性方案

### 1. 基于消息队列的缓存同步
```java
/**
 * 缓存更新事件监听器
 */
@Component
public class CacheSyncListener {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleCacheUpdate(CacheUpdateEvent event) {
        // 发送缓存更新消息
        CacheMessage message = new CacheMessage();
        message.setCacheName(event.getCacheName());
        message.setKey(event.getKey());
        message.setOperation(event.getOperation());

        redisTemplate.convertAndSend("cache:sync", message);
    }
}

/**
 * 缓存同步消息处理器
 */
@Component
public class CacheSyncReceiver {

    @Autowired
    private CacheManager cacheManager;

    @RedisListener(topics = "cache:sync")
    public void handleCacheSync(CacheMessage message) {
        Cache cache = cacheManager.getCache(message.getCacheName());
        if (cache != null) {
            switch (message.getOperation()) {
                case EVICT:
                    cache.evict(message.getKey());
                    break;
                case CLEAR:
                    cache.clear();
                    break;
            }
        }
    }
}
```

### 2. 基于数据库binlog的缓存同步
```java
/**
 * Canal监听器，监听数据库变更
 */
@Component
public class DatabaseChangeListener {

    @Autowired
    private CacheManager cacheManager;

    @CanalEventListener
    public void onUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        // 解析binlog，获取变更的数据
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if ("user_name".equals(column.getName())) {
                // 用户名称变更，清除相关缓存
                String cacheKey = "user:" + getUserId(rowData);
                Cache userCache = cacheManager.getCache("user");
                if (userCache != null) {
                    userCache.evict(cacheKey);
                }
            }
        }
    }
}
```

## 缓存监控

### 1. 缓存指标收集
```java
@Component
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * 记录缓存命中率
     */
    public void recordCacheHit(String cacheName, boolean hit) {
        meterRegistry.counter("cache.hit.rate",
            "cache", cacheName,
            "hit", String.valueOf(hit)
        ).increment();
    }

    /**
     * 记录缓存操作耗时
     */
    public void recordCacheOperation(String operation, long duration) {
        meterRegistry.timer("cache.operation.duration", "operation", operation)
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

### 2. 缓存监控面板
```yaml
# Grafana监控面板配置
{
  "dashboard": {
    "title": "Cache Monitoring",
    "panels": [
      {
        "title": "Cache Hit Rate",
        "targets": [
          {
            "expr": "rate(cache_hit_total[5m])",
            "legendFormat": "{{cache}} - {{hit}}"
          }
        ]
      },
      {
        "title": "Cache Size",
        "targets": [
          {
            "expr": "cache_size_bytes",
            "legendFormat": "{{cache}}"
          }
        ]
      }
    ]
  }
}
```

## 缓存穿透、击穿、雪崩解决方案

### 1. 缓存穿透防护
```java
/**
 * 布隆过滤器防止缓存穿透
 */
@Component
public class BloomFilterCache {

    private BloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        // 初始化布隆过滤器，预计元素数量100万，误判率0.01%
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.0001);

        // 加载所有有效key
        List<String> allKeys = loadAllValidKeys();
        allKeys.forEach(bloomFilter::put);
    }

    /**
     * 查询数据（带布隆过滤器）
     */
    public <T> T get(String key, Supplier<T> dbFallback) {
        // 布隆过滤器判断key是否存在
        if (!bloomFilter.mightContain(key)) {
            return null;
        }

        // 查询缓存
        T value = getFromCache(key);
        if (value != null) {
            return value;
        }

        // 查询数据库
        value = dbFallback.get();
        if (value != null) {
            putToCache(key, value);
        } else {
            // 空值也缓存，防止重复查询
            putToCache(key, NULL_VALUE, 5, TimeUnit.MINUTES);
        }

        return value;
    }
}
```

### 2. 缓存击穿防护
```java
/**
 * 分布式锁防止缓存击穿
 */
@Component
public class CacheBreakerProtection {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取数据（带互斥锁）
     */
    public <T> T getWithMutexLock(String key, Supplier<T> dbFallback) {
        // 查询缓存
        T value = getFromCache(key);
        if (value != null) {
            return value;
        }

        // 获取分布式锁
        RLock lock = redissonClient.getLock("lock:" + key);

        try {
            // 尝试获取锁，最多等待100ms
            if (lock.tryLock(100, 10, TimeUnit.SECONDS)) {
                // 双重检查
                value = getFromCache(key);
                if (value != null) {
                    return value;
                }

                // 查询数据库
                value = dbFallback.get();
                if (value != null) {
                    putToCache(key, value, 30, TimeUnit.MINUTES);
                }

                return value;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 获取锁失败，直接查询数据库
        return dbFallback.get();
    }
}
```

### 3. 缓存雪崩防护
```java
/**
 * 随机过期时间防止缓存雪崩
 */
@Component
public class CacheAvalancheProtection {

    private static final Random RANDOM = new Random();

    /**
     * 设置随机过期时间
     */
    public void setWithRandomExpire(String key, Object value, int baseExpire) {
        // 添加随机偏移，范围：±10%
        int randomOffset = RANDOM.nextInt(baseExpire / 5) - baseExpire / 10;
        int actualExpire = baseExpire + randomOffset;

        redisTemplate.opsForValue().set(key, value, actualExpire, TimeUnit.MINUTES);
    }

    /**
     * 分批更新缓存
     */
    public void refreshCacheInBatch(List<String> keys) {
        int batchSize = 100;
        int delaySeconds = 0;

        for (int i = 0; i < keys.size(); i += batchSize) {
            List<String> batch = keys.subList(i, Math.min(i + batchSize, keys.size()));

            // 延迟执行，分散数据库压力
            scheduler.schedule(() -> {
                refreshBatch(batch);
            }, delaySeconds, TimeUnit.SECONDS);

            delaySeconds += 5; // 每批延迟5秒
        }
    }
}
```

## 最佳实践

1. **缓存粒度控制**：根据业务场景选择合适的缓存粒度
2. **缓存预热**：系统启动时加载热点数据
3. **监控告警**：实时监控缓存命中率和性能指标
4. **容灾降级**：缓存失效时提供降级方案
5. **容量规划**：合理评估缓存容量，避免内存溢出

## 性能调优

### 1. JVM参数优化
```bash
# 堆内存设置
-Xms4g -Xmx4g

# GC调优
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200

# 堆外内存（Netty、Redis客户端使用）
-XX:MaxDirectMemorySize=1g
```

### 2. Redis性能优化
```yaml
# redis.conf
maxmemory: 2gb
maxmemory-policy: allkeys-lru
tcp-keepalive: 60
timeout: 300
```

### 3. 连接池配置
```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 100
        max-idle: 50
        min-idle: 10
        max-wait: 1000ms
        time-between-eviction-runs: 30s
```

## 更新计划

- [ ] 实现缓存自动扩缩容
- [ ] 添加缓存智能预热
- [ ] 支持缓存版本管理
- [ ] 实现缓存故障自愈
- [ ] 添加缓存成本优化
- [ ] 支持多数据中心缓存同步
- [ ] 实现缓存AI调优