package com.lycorder.security.config;

import cn.hutool.core.util.ArrayUtil;
import com.lycorder.common.config.property.SecurityProperties;
import com.lycorder.security.filter.TokenAuthenticationFilter;
import com.lycorder.security.handler.ApiAccessDeniedHandler;
import com.lycorder.security.handler.ApiAuthenticationEntryPoint;
import com.lycorder.security.token.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 * <p>
 * 仅依赖 {@link UserDetailsService} 接口，具体实现（SysUserDetailsService）由 yue-system 提供，
 * 解除安全框架层对业务层的依赖。
 * </p>
 *
 * @author LYCoder
 */
@Configuration
@EnableWebSecurity  // 启用 Spring Security 的 Web 安全功能，允许配置安全过滤链
@EnableMethodSecurity // 启用方法级别的安全控制（如 @PreAuthorize 等）
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;

    /**
     * 用户认证信息加载服务，实现类由 yue-system 模块提供
     */
    private final UserDetailsService userDetailsService;

    private final SecurityProperties securityProperties;
    private final TokenManager tokenManager;


    /**
     * 配置安全过滤链，用于定义哪些请求需要认证或授权
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 配置认证与授权规则
        return http
                .authorizeHttpRequests(requestMatcherRegistry -> {
                            String[] ignoreUrls = securityProperties.getIgnoreUrls();
                            if (ArrayUtil.isNotEmpty(ignoreUrls)) {
                                requestMatcherRegistry.requestMatchers(ignoreUrls).permitAll();
                            }
                            requestMatcherRegistry.anyRequest().authenticated();
                        }
                )
                // 异常处理
                .exceptionHandling(configurer -> {
                    configurer
                            .authenticationEntryPoint(new ApiAuthenticationEntryPoint()) // 未认证处理器
                            .accessDeniedHandler(new ApiAccessDeniedHandler()); // 无权限访问处理器
                })
                // 使用无状态认证，禁用 Session 管理（前后端分离 + JWT）
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 禁用 CSRF 防护（前后端分离通过 Token 验证，不需要 CSRF）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用默认的表单登录功能
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic 认证（统一使用 JWT 认证）
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用 X-Frame-Options 响应头，允许页面被嵌套到 iframe 中
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 验证和解析过滤器
                .addFilterBefore(new TokenAuthenticationFilter(tokenManager), UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    /**
     * 配置Web安全自定义器，以忽略特定请求路径的安全性检查。
     * <p>
     * 该配置用于指定哪些请求路径不经过Spring Security过滤器链。通常用于静态资源文件。
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            String[] unsecuredUrls = securityProperties.getUnsecuredUrls();
            if (ArrayUtil.isNotEmpty(unsecuredUrls)) {
                web.ignoring().requestMatchers(unsecuredUrls);
            }
        };
    }

    /**
     * 默认密码认证的 Provider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider
    ) {
        return new ProviderManager(
                daoAuthenticationProvider
        );
    }
}
