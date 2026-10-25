package com.lycorder.auth.service;


import com.lycorder.auth.model.vo.CaptchaVO;
import com.lycorder.security.model.AuthenticationToken;

/**
 * 认证服务接口。
 * <p>
 * 定义图形验证码生成、用户登录、退出登录及刷新令牌等认证相关能力。
 *
 * @author LYCoder
 */
public interface AuthService {
    /**
     * 获取验证码
     *
     * @return 验证码
     */
    CaptchaVO getCaptcha();

    /**
     * 用户登录并签发认证令牌。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 认证令牌响应
     */
    AuthenticationToken login(String username, String password);

    /**
     * 退出登录并注销当前访问令牌。
     */
    void logout();

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌响应
     */
    AuthenticationToken refreshToken(String refreshToken);
}
