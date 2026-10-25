package com.lycorder.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * caffeine缓存配置
 * <p>
 * 基于 Caffeine 提供本地缓存管理器，缓存规格由配置文件中的 spec 决定。
 *
 * @author LYCoder
 */
@Slf4j
@Configuration
public class CaffeineConfig {

    /**
     * Caffeine 缓存规格，来源于 spring.cache.caffeine.spec
     */
    @Value("${spring.cache.caffeine.spec}")
    private String caffeineSpec;

    /**
     * 缓存管理器
     *
     * @return CacheManager 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        // 解析配置中的缓存规格，如 maximumSize、expireAfterWrite 等
        Caffeine<Object, Object> caffeineBuilder = Caffeine.from(caffeineSpec);
        caffeineCacheManager.setCaffeine(caffeineBuilder);
        return caffeineCacheManager;
    }
}

