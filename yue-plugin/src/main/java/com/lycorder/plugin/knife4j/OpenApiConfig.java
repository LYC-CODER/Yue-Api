package com.lycorder.plugin.knife4j;

import cn.hutool.core.util.ArrayUtil;

import com.lycorder.common.config.property.SecurityProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;

import java.util.stream.Stream;

/**
 * OpenAPI 接口文档配置类
 * 负责配置 SpringDoc/OpenAPI 的 Bean，生成 API 接口文档
 * 包括 API 基本信息、鉴权配置以及全局自定义扩展
 *
 * @author LYCoder
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OpenApiConfig {

    /**
     * 环境配置，用于读取 project.version 等属性
     */
    private final Environment environment;

    /**
     * 安全配置，提供免认证放行路径
     */
    private final SecurityProperties securityProperties;

    /**
     * 配置 OpenAPI Bean，生成接口文档的基本信息
     * 包含 API 标题、描述、版本、许可证、联系人信息以及全局 JWT 鉴权方案
     * 
     * @return OpenAPI 对象，包含完整的 API 文档基础信息和安全认证配置
     */
    @Bean
    public OpenAPI openApi() {

        String appVersion = environment.getProperty("project.version", "1.0.0");

        return new OpenAPI()
                .info(new Info()
                        .title("管理系统 API 文档")
                        .description("本文档涵盖管理系统的所有 API 接口，包括登录认证、用户管理、角色管理、部门管理等功能模块，提供详细的接口说明和使用指南。")
                        .version(appVersion)
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")
                        )
                        .contact(new Contact()
                                .name("LYCorder")
                                .email("2917905862@qq.com")
                                .url("https://github.com/LYC-CODER")
                        )
                )
                // 配置全局鉴权参数-Authorize
                .components(new Components()
                        .addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                                new SecurityScheme()
                                        .name(HttpHeaders.AUTHORIZATION)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .scheme("Bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }


    /**
     * 全局自定义 OpenAPI 扩展器，为所有 API 端点添加统一的安全认证配置
     * 根据 SecurityProperties 中配置的忽略 URL 列表，自动跳过无需认证的接口
     * 其余接口均会自动添加 Authorization 请求头参数
     * 
     * @return GlobalOpenApiCustomizer 对象，用于对所有 API 端点进行全局自定义处理
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 全局添加 Authorization
            // 未扫描到任何接口路径时直接跳过
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {

                    // 忽略认证的请求无需携带 Authorization
                    String[] ignoreUrls = securityProperties.getIgnoreUrls();
                    if (ArrayUtil.isNotEmpty(ignoreUrls)) {
                        // Ant 匹配忽略的路径，不添加 Authorization
                        AntPathMatcher antPathMatcher = new AntPathMatcher();
                        if (Stream.of(ignoreUrls).anyMatch(ignoreUrl -> antPathMatcher.match(ignoreUrl, path))) {
                            return;
                        }
                    }

                    // 其他接口统一添加 Authorization
                    pathItem.readOperations()
                            .forEach(operation ->
                                    operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                            );
                });
            }
        };
    }

}
