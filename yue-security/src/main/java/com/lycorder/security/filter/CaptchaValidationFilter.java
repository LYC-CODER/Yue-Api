package com.lycorder.security.filter;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;


import com.lycorder.common.constant.RedisConstants;
import com.lycorder.common.constant.SecurityConstants;
import com.lycorder.common.util.IPUtils;
import com.lycorder.core.result.ResultCode;
import com.lycorder.core.result.WebResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 图形验证码校验过滤器。
 * <p>
 * 仅拦截 POST 登录请求，从 JSON 请求体读取验证码标识与验证码文本，并与 Redis 中的缓存值比对；
 * 校验通过后立即删除缓存并继续过滤器链，校验失败则直接写出统一错误响应。
 *
 * @author LYCoder
 */
public class CaptchaValidationFilter extends OncePerRequestFilter {

    /**
     * 登录接口请求匹配器，仅匹配 POST 登录路径
     */
    private static final RequestMatcher LOGIN_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, SecurityConstants.LOGIN_PATH);

    /**
     * 请求体中的验证码文本字段名
     */
    public static final String CAPTCHA_CODE_PARAM_NAME = "captchaCode";
    /**
     * 请求体中的验证码标识字段名
     */
    public static final String CAPTCHA_ID_PARAM_NAME = "captchaId";

    /**
     * 同一 IP 在统计窗口内允许的最大失败次数
     */
    private static final int MAX_ATTEMPTS = 10;

    /**
     * 验证码失败次数统计窗口（5 分钟）
     */
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(5);

    /**
     * Redis 客户端，用于读取和删除验证码缓存
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 验证码字符生成器，用于校验验证码文本
     */
    private final CodeGenerator codeGenerator;

    /**
     * 创建图形验证码校验过滤器。
     *
     * @param redisTemplate Redis 客户端
     * @param codeGenerator 验证码字符生成器
     */
    public CaptchaValidationFilter(RedisTemplate<String, Object> redisTemplate, CodeGenerator codeGenerator) {
        this.redisTemplate = redisTemplate;
        this.codeGenerator = codeGenerator;
    }

    /**
     * 对登录请求执行图形验证码校验，非登录请求直接放行。
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param chain    过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      读写请求或响应失败时抛出
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 非登录接口直接放行
        if (!LOGIN_PATH_REQUEST_MATCHER.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        // 按客户端 IP 统计验证码失败次数，防止无限重试
        String attemptKey = StrUtil.format(
                RedisConstants.Captcha.ATTEMPT_COUNT,
                IPUtils.getIpAddr(request)
        );
        if (isAttemptLimitExceeded(attemptKey)) {
            WebResponseWriter.writeError(response, ResultCode.USER_VERIFICATION_CODE_ATTEMPT_LIMIT_EXCEEDED);
            return;
        }

        // 仅支持 JSON 登录
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            recordFailure(attemptKey);
            WebResponseWriter.writeError(response, ResultCode.USER_VERIFICATION_CODE_ERROR);
            return;
        }

        // 包装请求，确保下游还能读取 body
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);

        byte[] bodyBytes = StreamUtils.copyToByteArray(requestWrapper.getInputStream());
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String captchaCode = null;
        String captchaId = null;

        // 从 JSON 请求体中提取验证码文本和缓存标识
        if (StrUtil.isNotBlank(body)) {
            JSONObject jsonObject = JSONUtil.parseObj(body);
            captchaCode = jsonObject.getStr(CAPTCHA_CODE_PARAM_NAME);
            captchaId = jsonObject.getStr(CAPTCHA_ID_PARAM_NAME);
        }

        // 验证码字段缺失时不进入认证流程
        if (StrUtil.isBlank(captchaCode) || StrUtil.isBlank(captchaId)) {
            recordFailure(attemptKey);
            WebResponseWriter.writeError(response, ResultCode.USER_VERIFICATION_CODE_ERROR);
            return;
        }

        // 按验证码标识定位缓存，缓存不存在通常表示已过期或已被使用
        String cacheKey = StrUtil.format(RedisConstants.Captcha.IMAGE_CODE, captchaId);
        String cacheVerifyCode = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cacheVerifyCode == null) {
            recordFailure(attemptKey);
            WebResponseWriter.writeError(response, ResultCode.USER_VERIFICATION_CODE_EXPIRED);
            return;
        }

        if (codeGenerator.verify(cacheVerifyCode, captchaCode)) {
            // 验证码一次性使用，校验通过后立即删除，防止重放
            redisTemplate.delete(cacheKey);
            // 登录成功后清空该 IP 的失败计数
            redisTemplate.delete(attemptKey);
            HttpServletRequest repeatableRequest = new RepeatableReadRequestWrapper(requestWrapper, bodyBytes);
            chain.doFilter(repeatableRequest, response);
        } else {
            recordFailure(attemptKey);
            WebResponseWriter.writeError(response, ResultCode.USER_VERIFICATION_CODE_ERROR);
        }
    }

    /**
     * 判断指定 IP 的验证码失败次数是否已达到上限。
     *
     * @param attemptKey 失败次数缓存 key
     * @return true 表示已超限
     */
    private boolean isAttemptLimitExceeded(String attemptKey) {
        Object value = redisTemplate.opsForValue().get(attemptKey);
        if (value instanceof Number number) {
            return number.intValue() >= MAX_ATTEMPTS;
        }
        return false;
    }

    /**
     * 记录一次验证码失败，并设置统计窗口过期时间。
     *
     * @param attemptKey 失败次数缓存 key
     */
    private void recordFailure(String attemptKey) {
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptKey, ATTEMPT_WINDOW);
        }
    }

    /**
     * Simple wrapper to allow repeated reads of the request body after we've parsed it here.
     */
    private static class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        RepeatableReadRequestWrapper(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = cachedBody != null ? cachedBody : new byte[0];
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                    // no-op
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return cachedBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return cachedBody.length;
        }
    }
}


