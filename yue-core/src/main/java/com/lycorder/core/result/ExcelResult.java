package com.lycorder.core.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导出响应结构体
 * <p>
 * 导入结束后由业务层回填有效/无效条数与错误明细，前端据此展示导入结果。
 *
 * @author LYCoder
 */
@Data
public class ExcelResult {

    /**
     * 响应码，来确定是否导入成功
     */
    private String code;

    /**
     * 有效条数
     */
    private Integer validCount;

    /**
     * 无效条数
     */
    private Integer invalidCount;

    /**
     * 错误提示信息
     */
    private List<String> messageList;

    /**
     * 构造导出结果，默认成功，统计值初始化为 0。
     */
    public ExcelResult() {
        this.code = ResultCode.SUCCESS.getCode();
        this.validCount = 0;
        this.invalidCount = 0;
        this.messageList = new ArrayList<>();
    }
}
