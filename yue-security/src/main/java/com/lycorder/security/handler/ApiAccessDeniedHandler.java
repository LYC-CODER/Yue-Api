package com.lycorder.security.handler;



import com.lycorder.core.result.ResultCode;
import com.lycorder.core.result.WebResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 无权限访问处理器
 * @author LYCoder
 */
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        WebResponseWriter.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
    }
}
