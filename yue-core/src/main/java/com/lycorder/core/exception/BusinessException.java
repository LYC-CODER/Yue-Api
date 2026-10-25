package com.lycorder.core.exception;

import com.lycorder.core.result.IResultCode;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;

/**
 * 自定义业务异常
 * <p>
 * 用于在业务逻辑中主动中断流程并携带统一响应码，由全局异常处理器转换为统一响应结构。
 *
 * @author LYCoder
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 关联的响应码，供全局异常处理器决定返回的业务码
     */
    public IResultCode resultCode;

    /**
     * 使用响应码的默认提示构造异常。
     *
     * @param errorCode 响应码
     */
    public BusinessException(IResultCode errorCode) {
        super(errorCode.getMsg());
        this.resultCode = errorCode;
    }


    /**
     * 使用响应码和自定义提示构造异常。
     *
     * @param errorCode 响应码
     * @param message   自定义异常提示
     */
    public BusinessException(IResultCode errorCode,String message) {
        super(message);
        this.resultCode = errorCode;
    }


    /**
     * 使用提示信息和原始异常构造异常（不关联响应码）。
     *
     * @param message 异常提示
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原始异常构造异常。
     *
     * @param cause 原始异常
     */
    public BusinessException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用 SLF4J 占位符格式化提示信息后构造异常。
     *
     * @param message 消息模板，支持 {} 占位符
     * @param args    占位符参数
     */
    public BusinessException(String message, Object... args) {
        super(formatMessage(message, args));
    }

    /**
     * 使用 SLF4J 消息格式化器填充占位符。
     *
     * @param message 消息模板
     * @param args    占位符参数
     * @return 格式化后的消息
     */
    private static String formatMessage(String message, Object... args) {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }
}
