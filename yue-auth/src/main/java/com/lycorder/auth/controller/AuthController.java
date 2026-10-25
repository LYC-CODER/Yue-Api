package com.lycorder.auth.controller;

import com.lycorder.auth.model.dto.LoginRequest;
import com.lycorder.auth.model.vo.CaptchaVO;
import com.lycorder.auth.service.AuthService;
import com.lycorder.core.result.Result;
import com.lycorder.security.model.AuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @projectName: Real-Time-Api
 * @package: com.lycorder.controller.auth
 * @className: Auth
 * @author: LYCorder
 * @description: 认证中心控制器，提供登录、验证码获取、退出登录和令牌刷新接口
 * @date: 2026/3/6 16:46
 * @version: 1.0
 */
@Tag(name = "1.认证中心")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录。
     * <p>
     * 校验用户名、密码及图形验证码（由安全过滤器前置完成），认证通过后签发访问令牌与刷新令牌。
     *
     * @param request 登录请求参数，包含用户名、密码及验证码信息
     * @return 统一响应结果，数据部分为认证令牌
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        AuthenticationToken authenticationToken = authService.login(username, password);
        return Result.success(authenticationToken);
    }

    /**
     * 获取图形验证码。
     *
     * @return 统一响应结果，数据部分包含验证码缓存标识和图片 Base64 数据
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = authService.getCaptcha();
        return Result.success(captcha);
    }
    /**
     * 退出当前登录会话。
     * <p>
     * 将当前访问令牌置为失效，并清理安全上下文。
     */
    @Operation(summary = "退出登录")
    @DeleteMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 统一响应结果，数据部分为新的认证令牌
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public Result<?> refreshToken(
            @Parameter(description = "刷新令牌", example = "xxx.xxx.xxx") @RequestParam String refreshToken
    ) {
        AuthenticationToken authenticationToken = authService.refreshToken(refreshToken);
        return Result.success(authenticationToken);
    }
}
