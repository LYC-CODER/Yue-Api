package com.lycorder.core.result;

/**
 * 响应码接口
 * <p>
 * 定义统一响应码的最小契约，枚举或自定义异常可通过实现该接口接入统一响应体系。
 *
 * @author LYCoder
 **/
public interface IResultCode {

    /**
     * 获取响应码。
     *
     * @return 5 位字符串响应码
     */
    String getCode();

    /**
     * 获取响应码对应的提示信息。
     *
     * @return 默认提示信息
     */
    String getMsg();

}
