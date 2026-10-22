package com.lycorder.auth.service;


import com.lycorder.auth.model.vo.CaptchaVO;
import com.lycorder.security.model.AuthenticationToken;

/**
 * @author LYCoder
 */
public interface AuthService {
    /**
     * 获取验证码
     *
     * @return 验证码
     */
    CaptchaVO getCaptcha();

    AuthenticationToken login(String username, String password);

    void logout();

    AuthenticationToken refreshToken(String refreshToken);
}
