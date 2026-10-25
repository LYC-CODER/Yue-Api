package com.lycorder.security.token;



import com.lycorder.security.model.AuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Token 管理器。
 * <p>
 * 定义令牌生成、解析、校验、刷新与失效能力；具体实现由 {@code security.session.type}
 * 配置选择 JWT 或 Redis Token 模式。
 *
 * @author LYCoder
 */
public interface TokenManager {

    /**
     * 生成认证 Token
     *
     * @param authentication 用户认证信息
     * @return 认证 Token 响应
     */
    AuthenticationToken generateToken(Authentication authentication);

    /**
     * 解析 Token 获取认证信息
     *
     * @param token  Token
     * @return 用户认证信息
     */
    Authentication parseToken(String token);

    /**
     * 校验 Token 是否有效
     *
     * @param token 待校验的访问令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 校验 刷新 Token 是否有效
     *
     * @param refreshToken 待校验的刷新令牌
     * @return 是否有效
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     *  刷新 Token
     *
     * @param token 刷新令牌
     * @return 认证 Token 响应
     */
    AuthenticationToken refreshToken(String token);

    /**
     * 使单个 Token 失效。
     * <p>
     * 默认空实现，支持黑名单或会话存储的实现应覆盖此方法。
     *
     * @param token 待失效的 Token
     */
    default void invalidateToken(String token) {
        // 默认实现可以是空的，或者抛出不支持的操作异常
        // throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * 使指定用户的所有会话失效
     *
     * @param userId 用户ID
     */
    default void invalidateUserSessions(Long userId) {
        // 默认空实现，由具体 TokenManager 决定是否支持按用户下线
    }

}
