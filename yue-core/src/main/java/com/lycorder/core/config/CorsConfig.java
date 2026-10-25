package com.lycorder.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 资源共享配置
 * <p>
 * 注册全局跨域过滤器，统一为所有路径追加跨域响应头并处理预检请求。
 * 允许来源通过 {@code cors.allowed-origins} 配置，多个来源用英文逗号分隔。
 *
 * @author LYCoder
 */
@Configuration
public class CorsConfig {

    /**
     * 允许跨域的来源列表，默认仅允许本地前端开发地址
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOrigins;

    /**
     * 注册跨域过滤器。
     *
     * @return 跨域过滤器注册对象，执行顺序早于 Spring Security
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 使用 allowedOriginPatterns 而非 allowedOrigins，以兼容 allowCredentials=true
        // 从配置读取白名单，避免允许任意来源携带凭证
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        corsConfiguration.setAllowedOriginPatterns(origins);
        //2.允许任何请求头
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        //3.允许任何方法
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        //4.允许凭证
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        CorsFilter corsFilter = new CorsFilter(source);

        FilterRegistrationBean<CorsFilter> filterRegistrationBean=new FilterRegistrationBean<>(corsFilter);
        filterRegistrationBean.setOrder(-101);  // 小于 SpringSecurity Filter的 Order(-100) 即可

        return filterRegistrationBean;
    }
}