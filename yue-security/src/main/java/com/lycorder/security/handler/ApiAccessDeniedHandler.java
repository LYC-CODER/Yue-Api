package com.lycorder.security.handler;



import com.lycorder.core.result.ResultCode;
import com.lycorder.core.result.WebResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 无权限访问处理器。
 * <p>
 * 当已认证用户访问其权限不足的资源时，统一写出无权限错误响应。
 *
 * @author LYCoder
 */
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 处理权限不足异常。
     *
     * @param request               当前 HTTP 请求
     * @param response              当前 HTTP 响应
     * @param accessDeniedException 权限拒绝异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        // 统一返回无权限错误码，不向客户端暴露具体授权细节
        WebResponseWriter.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
    }
}
