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
 * @description: TODO
 * @date: 2026/3/6 16:46
 * @version: 1.0
 */
@Tag(name = "1.认证中心")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        AuthenticationToken authenticationToken = authService.login(username, password);
        return Result.success(authenticationToken);
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = authService.getCaptcha();
        return Result.success(captcha);
    }
    @Operation(summary = "退出登录")
    @DeleteMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public Result<?> refreshToken(
            @Parameter(description = "刷新令牌", example = "xxx.xxx.xxx") @RequestParam String refreshToken
    ) {
        AuthenticationToken authenticationToken = authService.refreshToken(refreshToken);
        return Result.success(authenticationToken);
    }
}
