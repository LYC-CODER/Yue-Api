package com.lycorder.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置。
 *
 * <p>操作日志落库属于旁路逻辑，不应阻塞业务请求。这里为日志监听器提供独立线程池，
 * 队列满时使用调用方线程执行，避免日志事件被静默丢弃。</p>
 *
 * @author LYCoder
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志线程池。
     *
     * @return 日志异步执行器
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
