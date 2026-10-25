package com.lycorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类。
 * <p>
 * 通过 {@link SpringApplication} 启动 Spring Boot 应用，触发自动配置和组件扫描。
 * </p>
 *
 * @author LYCoder
 */

@SpringBootApplication
public class ApiSystemApplication {
    /**
     * 应用入口。
     *
     * @param args 启动命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiSystemApplication.class, args);
    }
}