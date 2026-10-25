package com.lycorder.core.event;

import com.lycorder.common.enums.LogModuleEnum;
import lombok.Data;

/**
 * 操作日志事件
 * <p>
 * 由日志切面（yue-security）发布，由业务模块（yue-system）监听并持久化，
 * 用于解除框架层对业务层的依赖。
 * 事件仅承载请求上下文与执行结果数据，不包含任何持久化行为。
 * </p>
 *
 * @author LYCoder
 */
@Data
public class LogEvent {

    /**
     * 日志模块
     */
    private LogModuleEnum module;

    /**
     * 日志内容
     */
    private String content;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应内容
     */
    private String responseContent;

    /**
     * 方法名
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 创建人（用户ID）
     */
    private Long createBy;
}
