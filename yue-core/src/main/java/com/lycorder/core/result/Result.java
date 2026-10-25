package com.lycorder.core.result;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构体
 * <p>
 * 统一封装业务码、提示信息与响应数据，前端可通过 code 区分成功与失败分支。
 *
 *
 * @author LYCoder
 **/
@Data
public class Result<T> implements Serializable {

    private String code;

    private T data;

    private String msg;

    /**
     * 构建成功响应（无数据）。
     *
     * @param <T> 响应数据类型
     * @return 成功响应，data 为 null
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构建成功响应。
     *
     * @param <T>  响应数据类型
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 构建系统错误响应。
     *
     * @param <T> 响应数据类型
     * @return 系统错误响应，提示信息取 {@link ResultCode#SYSTEM_ERROR} 的默认消息
     */
    public static <T> Result<T> failed() {
        return result(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), null);
    }

    /**
     * 构建系统错误响应（自定义提示）。
     *
     * @param <T> 响应数据类型
     * @param msg 错误提示信息
     * @return 系统错误响应
     */
    public static <T> Result<T> failed(String msg) {
        return result(ResultCode.SYSTEM_ERROR.getCode(), msg, null);
    }

    /**
     * 根据布尔结果构建成功/失败响应。
     *
     * @param <T>    响应数据类型
     * @param status 业务执行结果
     * @return status 为 true 时返回成功响应，否则返回系统错误响应
     */
    public static <T> Result<T> judge(boolean status) {
        if (status) {
            return success();
        } else {
            return failed();
        }
    }

    /**
     * 根据响应码构建失败响应。
     *
     * @param <T>        响应数据类型
     * @param resultCode 响应码
     * @return 失败响应
     */
    public static <T> Result<T> failed(IResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 根据响应码构建失败响应（自定义提示）。
     *
     * @param <T>        响应数据类型
     * @param resultCode 响应码
     * @param msg        错误提示，为空时回退到响应码自带的提示
     * @return 失败响应
     */
    public static <T> Result<T> failed(IResultCode resultCode, String msg) {
        // 自定义提示为空时使用响应码的默认消息
        return result(resultCode.getCode(), StrUtil.isNotBlank(msg) ? msg : resultCode.getMsg(), null);
    }

    /**
     * 根据响应码构建失败响应（携带数据）。
     *
     * @param <T>        响应数据类型
     * @param resultCode 响应码
     * @param data       随失败响应返回的数据（如校验明细）
     * @return 失败响应
     */
    public static <T> Result<T> failed(IResultCode resultCode, T data) {
        return result(resultCode.getCode(), resultCode.getMsg(), data);
    }

    /**
     * 根据响应码构建失败响应（自定义提示并携带数据）。
     *
     * @param <T>        响应数据类型
     * @param resultCode 响应码
     * @param msg        错误提示，为空时回退到响应码自带的提示
     * @param data       随失败响应返回的数据
     * @return 失败响应
     */
    public static <T> Result<T> failed(IResultCode resultCode, String msg, T data) {
        return result(resultCode.getCode(), StrUtil.isNotBlank(msg) ? msg : resultCode.getMsg(), data);
    }

    private static <T> Result<T> result(IResultCode resultCode, T data) {
        return result(resultCode.getCode(), resultCode.getMsg(), data);
    }

    private static <T> Result<T> result(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

}
