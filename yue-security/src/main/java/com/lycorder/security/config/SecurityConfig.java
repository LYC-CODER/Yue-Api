package com.lycorder.security.config;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.ArrayUtil;
import com.lycorder.common.config.property.SecurityProperties;
import com.lycorder.security.filter.CaptchaValidationFilter;
import com.lycorder.security.filter.TokenAuthenticationFilter;
import com.lycorder.security.handler.ApiAccessDeniedHandler;
import com.lycorder.security.handler.ApiAuthenticationEntryPoint;
import com.lycorder.security.token.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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

    /**
     * 安全相关配置项，包含放行路径、会话类型等
     */
    private final SecurityProperties securityProperties;
    /**
     * Token 管理器，供 Token 认证过滤器解析和校验令牌
     */
    private final TokenManager tokenManager;
    /**
     * Redis 客户端，供验证码校验过滤器读取验证码缓存
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 验证码字符生成器，用于校验用户提交的验证码文本
     */
    private final CodeGenerator codeGenerator;


    /**
     * 配置安全过滤链，定义放行路径、认证授权规则、无状态会话以及自定义过滤器顺序。
     *
     * @param http Spring Security 的 HTTP 安全构建器
     * @return 构建完成的安全过滤链
     * @throws Exception 构建过滤链失败时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 配置认证与授权规则
        return http
                .authorizeHttpRequests(requestMatcherRegistry -> {
                            // 加载配置中的免认证路径，未配置时不做放行
                            String[] ignoreUrls = securityProperties.getIgnoreUrls();
                            if (ArrayUtil.isNotEmpty(ignoreUrls)) {
                                requestMatcherRegistry.requestMatchers(ignoreUrls).permitAll();
                            }
                            // 除上述放行路径外，其余请求一律要求认证
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
                // 仅允许同源 iframe（Apidoc 通过前端同源 /doc.html 代理访问），避免点击劫持
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // 验证和解析 Token
                .addFilterBefore(new TokenAuthenticationFilter(tokenManager), UsernamePasswordAuthenticationFilter.class)
                // 校验登录接口的图形验证码（必须放在 Token 过滤器之前）
                .addFilterBefore(new CaptchaValidationFilter(redisTemplate, codeGenerator), TokenAuthenticationFilter.class)
                .build();

    }

    /**
     * 配置Web安全自定义器，以忽略特定请求路径的安全性检查。
     * <p>
     * 该配置用于指定哪些请求路径不经过Spring Security过滤器链。通常用于静态资源文件。
     *
     * @return Web 安全自定义器
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            // 这些路径完全绕过安全过滤器链，需谨慎配置
            String[] unsecuredUrls = securityProperties.getUnsecuredUrls();
            if (ArrayUtil.isNotEmpty(unsecuredUrls)) {
                web.ignoring().requestMatchers(unsecuredUrls);
            }
        };
    }

    /**
     * 创建基于数据库用户信息的密码认证 Provider。
     *
     * @return 配置了用户加载服务和密码编码器的认证 Provider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        // 指定密码编码器，认证时使用相同算法比对密文
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    /**
     * 创建认证管理器。
     *
     * @param daoAuthenticationProvider 数据库用户认证 Provider
     * @return 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider
    ) {
        // 当前仅启用数据库认证方式
        return new ProviderManager(
                daoAuthenticationProvider
        );
    }
}
