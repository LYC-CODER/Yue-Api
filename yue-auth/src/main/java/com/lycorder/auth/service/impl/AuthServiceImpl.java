package com.lycorder.auth.service.impl;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lycorder.auth.model.vo.CaptchaVO;
import com.lycorder.auth.service.AuthService;
import com.lycorder.common.config.property.CaptchaProperties;
import com.lycorder.common.constant.RedisConstants;
import com.lycorder.common.enums.CaptchaTypeEnum;

import com.lycorder.security.model.AuthenticationToken;
import com.lycorder.security.token.TokenManager;

import com.lycorder.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: Real-Time-Api
 * @package: com.lycorder.service.impl
 * @className: AuthServiceImpl
 * @author: LYCorder
 * @description: 认证服务实现，负责图形验证码生成、登录认证、退出登录及令牌刷新
 * @date: 2026/3/6 17:16
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CaptchaProperties captchaProperties;
    private final CodeGenerator codeGenerator;
    private final Font captchaFont;
    private final TokenManager tokenManager;
    // 认证管理器 - 用于执行认证
    private final AuthenticationManager authenticationManager;


    /**
     * 生成图形验证码，并将验证码文本按标识缓存至 Redis 供登录时校验。
     *
     * @return 验证码缓存标识与图片 Base64 数据
     * @throws IllegalArgumentException 当配置的验证码类型不受支持时抛出
     */
    @Override
    public CaptchaVO getCaptcha() {
        // 读取验证码样式、尺寸、干扰线数量及字符长度等配置
        String captchaType = captchaProperties.getType();
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        int interfereCount = captchaProperties.getInterfereCount();
        int codeLength = captchaProperties.getCode().getLength();

        AbstractCaptcha captcha;
        // 根据配置的验证码类型创建对应的 Hutool 验证码实例
        if (CaptchaTypeEnum.CIRCLE.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createCircleCaptcha(width, height, codeLength, interfereCount);
        } else if (CaptchaTypeEnum.GIF.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createGifCaptcha(width, height, codeLength);
        } else if (CaptchaTypeEnum.LINE.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createLineCaptcha(width, height, codeLength, interfereCount);
        } else if (CaptchaTypeEnum.SHEAR.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createShearCaptcha(width, height, codeLength, interfereCount);
        } else {
            throw new IllegalArgumentException("Invalid captcha type: " + captchaType);
        }
        captcha.setGenerator(codeGenerator);
        captcha.setTextAlpha(captchaProperties.getTextAlpha());
        captcha.setFont(captchaFont);

        String captchaCode = captcha.getCode();
        String imageBase64Data = captcha.getImageBase64Data();

        // 验证码文本缓存至Redis，用于登录校验
        // 生成一次性验证码标识，作为 Redis 缓存键的一部分返回给前端
        String captchaId = IdUtil.fastSimpleUUID();
        redisTemplate.opsForValue().set(
                StrUtil.format(RedisConstants.Captcha.IMAGE_CODE, captchaId),
                captchaCode,
                captchaProperties.getExpireSeconds(),
                TimeUnit.SECONDS
        );

        return CaptchaVO.builder()
                .captchaId(captchaId)
                .captchaBase64(imageBase64Data)
                .build();
    }

    /**
     * 校验用户名和密码并签发认证令牌。
     *
     * @param username 用户名，处理前会去除首尾空白
     * @param password 明文密码
     * @return 认证令牌响应
     * @throws org.springframework.security.core.AuthenticationException 认证失败时抛出
     */
    @Override
    public AuthenticationToken login(String username, String password) {
        // 构造用户名密码认证请求，交由 Spring Security 的认证管理器校验
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username.trim(), password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 认证成功后签发令牌，并写入安全上下文供后续业务读取当前登录用户
        AuthenticationToken authenticationTokenResponse =
                tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authenticationTokenResponse;
    }

    /**
     * 注销当前访问令牌并清空安全上下文。
     */
    @Override
    public void logout() {
        String token = SecurityUtils.getAccessToken();
        // 仅当请求携带非空令牌时才执行注销，避免无意义的上下文清理
        if (StrUtil.isNotBlank(token)) {
            tokenManager.invalidateToken(token);
            // 清除Security上下文
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌响应
     * @throws com.lycorder.core.exception.BusinessException 刷新令牌无效或已过期时抛出
     */
    @Override
    public AuthenticationToken refreshToken(String refreshToken) {
        return tokenManager.refreshToken(refreshToken);
    }
}
