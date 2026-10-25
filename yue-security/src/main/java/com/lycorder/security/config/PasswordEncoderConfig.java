package com.lycorder.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置。
 * <p>
 * 注册 BCrypt 密码编码器，供认证与用户密码变更场景统一使用。
 *
 * @author LYCoder
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 创建 BCrypt 密码编码器 Bean。
     *
     * @return 使用 BCrypt 算法的密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 内置随机盐，相同明文每次编码结果不同
        return new BCryptPasswordEncoder();
    }
}
