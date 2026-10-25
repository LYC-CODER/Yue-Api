package com.lycorder.security.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;

import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.common.util.IPUtils;
import com.lycorder.core.event.LogEvent;
import com.lycorder.security.util.SecurityUtils;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 日志切面
 * <p>
 * 采集操作日志并发布 {@link LogEvent}，由业务模块（yue-system）监听落库，
 * 不再直接依赖 system 模块的实体与 Service。
 * </p>
 *
 * @author LYCoder
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /**
     * 日志中的敏感字段，序列化后统一替换为 ******，避免密码/令牌落库
     */
    private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern.compile(
            "(\"(?:password|oldPassword|newPassword|confirmPassword|token|accessToken|refreshToken|secret|secretKey)\"\\s*:\\s*\")([^\"]*)(\")",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 事件发布器，将日志事件交给业务模块的监听器持久化
     */
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 当前请求对象，用于采集请求 URI、IP、User-Agent 等信息
     */
    private final HttpServletRequest request;
    /**
     * 缓存管理器，用于缓存 User-Agent 解析结果
     */
    private final CacheManager cacheManager;

    /**
     * 切点
     */
    @Pointcut("@annotation(com.lycorder.common.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 环绕目标方法执行，采集执行时长、结果或异常并发布操作日志。
     *
     * @param joinPoint     切点
     * @param logAnnotation 目标方法上的日志注解
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法抛出的异常，原样向上传递
     */
    @Around("logPointcut() && @annotation(logAnnotation)")
    public Object doAround(ProceedingJoinPoint joinPoint, com.lycorder.common.annotation.Log logAnnotation) throws Throwable {
        // 在方法执行前获取用户ID，避免在方法执行过程中清除上下文导致获取不到用户ID
        Long userId = SecurityUtils.getUserId();

        TimeInterval timer = DateUtil.timer();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = timer.interval(); // 执行时长
            // 无论业务方法成功还是失败，都发布日志事件；异常仍会继续向上抛出
            this.publishLog(joinPoint, exception, result, logAnnotation, executionTime, userId);
        }
        return result;
    }


    /**
     * 发布日志事件
     *
     * @param joinPoint     切点
     * @param e             异常
     * @param jsonResult    响应结果
     * @param logAnnotation 日志注解
     * @param executionTime 方法执行耗时（毫秒）
     * @param userId        用户ID
     */
    private void publishLog(final JoinPoint joinPoint, final Exception e, Object jsonResult, com.lycorder.common.annotation.Log logAnnotation, long executionTime, Long userId) {
        String requestURI = request.getRequestURI();
        // 创建日志事件
        LogEvent log = new LogEvent();
        log.setExecutionTime(executionTime);
        // 无日志注解但存在异常时，按系统异常记录堆栈信息
        if (logAnnotation == null && e != null) {
            log.setModule(LogModuleEnum.EXCEPTION);
            log.setContent("系统发生异常");
            this.setRequestParameters(joinPoint, log);
            log.setResponseContent(JSONUtil.toJsonStr(e.getStackTrace()));
        } else {
            log.setModule(logAnnotation.module());
            log.setContent(logAnnotation.value());
            // 请求参数
            if (logAnnotation.params()) {
                this.setRequestParameters(joinPoint, log);
            }
            // 响应结果
            if (logAnnotation.result() && jsonResult != null) {
                log.setResponseContent(maskSensitive(JSONUtil.toJsonStr(jsonResult)));
            }
        }
        log.setRequestUri(requestURI);
        log.setCreateBy(userId);
        String ipAddr = IPUtils.getIpAddr(request);
        if (StrUtil.isNotBlank(ipAddr)) {
            log.setIp(ipAddr);
            String region = IPUtils.getRegion(ipAddr);
            // 中国|0|四川省|成都市|电信 解析省和市
            if (StrUtil.isNotBlank(region)) {
                String[] regionArray = region.split("\\|");
                if (regionArray.length > 2) {
                    log.setProvince(regionArray[2]);
                    log.setCity(regionArray[3]);
                }
            }
        }


        // 获取浏览器和终端系统信息
        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = resolveUserAgent(userAgentString);
        if (Objects.nonNull(userAgent)) {
            // 系统信息
            log.setOs(userAgent.getOs().getName());
            // 浏览器信息
            log.setBrowser(userAgent.getBrowser().getName());
            log.setBrowserVersion(userAgent.getBrowser().getVersion(userAgentString));
        }
        //获取方法名
        String methodName = joinPoint.getSignature().getName();
        log.setMethod(methodName);
        // 发布日志事件，由 yue-system 的监听器持久化到数据库
        eventPublisher.publishEvent(log);
    }

    /**
     * 设置请求参数到日志事件中
     *
     * @param joinPoint 切点
     * @param log       操作日志事件
     */
    private void setRequestParameters(JoinPoint joinPoint, LogEvent log) {
        String requestMethod = request.getMethod();
        log.setRequestMethod(requestMethod);
        if (HttpMethod.GET.name().equalsIgnoreCase(requestMethod) || HttpMethod.PUT.name().equalsIgnoreCase(requestMethod) || HttpMethod.POST.name().equalsIgnoreCase(requestMethod)) {
            String params = convertArgumentsToString(joinPoint.getArgs());
            log.setRequestParams(StrUtil.sub(maskSensitive(params), 0, 65535));
        } else {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Map<?, ?> paramsMap = (Map<?, ?>) attributes.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                log.setRequestParams(StrUtil.sub(maskSensitive(paramsMap.toString()), 0, 65535));
            } else {
                log.setRequestParams("");
            }
        }
    }

    /**
     * 对 JSON 字符串中的密码、Token 等敏感字段做脱敏。
     *
     * @param content 原始内容
     * @return 脱敏后的内容
     */
    private String maskSensitive(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return SENSITIVE_FIELD_PATTERN.matcher(content).replaceAll("$1******$3");
    }

    /**
     * 将参数数组转换为字符串
     *
     * @param paramsArray 参数数组
     * @return 参数字符串
     */
    private String convertArgumentsToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null) {
            for (Object param : paramsArray) {
                if (!shouldFilterObject(param)) {
                    // 如果是基本类型或者枚举类型，直接添加到参数字符串中
                    if(param.getClass().isPrimitive() || param.getClass().isEnum()) {
                        params.append(param).append(" ");
                    } else {
                        params.append(JSONUtil.toJsonStr(param)).append(" ");
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param obj 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    private boolean shouldFilterObject(Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) obj;
            return collection.stream().anyMatch(item -> item instanceof MultipartFile);
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) obj;
            return map.values().stream().anyMatch(value -> value instanceof MultipartFile);
        }
        return obj instanceof MultipartFile || obj instanceof HttpServletRequest || obj instanceof HttpServletResponse;
    }


    /**
     * 解析UserAgent
     *
     * @param userAgentString UserAgent字符串
     * @return UserAgent
     */
    public UserAgent resolveUserAgent(String userAgentString) {
        if (StrUtil.isBlank(userAgentString)) {
            return null;
        }
        // 给userAgentStringMD5加密一次防止过长
        String userAgentStringMD5 = DigestUtil.md5Hex(userAgentString);
        //判断是否命中缓存
        UserAgent userAgent = Objects.requireNonNull(cacheManager.getCache("userAgent")).get(userAgentStringMD5, UserAgent.class);
        if (userAgent != null) {
            return userAgent;
        }
        userAgent = UserAgentUtil.parse(userAgentString);
        Objects.requireNonNull(cacheManager.getCache("userAgent")).put(userAgentStringMD5, userAgent);
        return userAgent;
    }

}
