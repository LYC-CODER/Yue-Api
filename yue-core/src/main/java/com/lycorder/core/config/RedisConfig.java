package com.lycorder.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 自动装配配置
 *
 * @author LYCoder
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义 RedisTemplate（JSON 序列化）
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 使用 String 作为 Key、JSON 作为 Value 的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key 使用字符串序列化，便于在 redis-cli 中直接查看
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // Value 使用 JSON 序列化，保留对象结构且跨语言可读
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 字符串 Redis 模板（纯文本序列化）
     * <p>
     * 用于读取 Spark Streaming 写入的纯文本统计数据
     * 所有 Key/Value 均使用 String 序列化，兼容 Jedis 直写的数据格式
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 纯文本序列化的 StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
