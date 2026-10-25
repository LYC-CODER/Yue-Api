package com.lycorder.common.util;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.event.AnalysisEventListener;

import java.io.InputStream;

/**
 * Excel 工具类
 * <p>
 * 封装 EasyExcel 的读取入口，统一从首个 Sheet 开始解析。
 *
 *
 * @author LYCoder
 */
public class ExcelUtils {

    /**
     * 读取 Excel 首个 Sheet，并通过监听器逐行处理。
     *
     * @param is       Excel 文件输入流
     * @param clazz    行数据映射类型
     * @param listener 行数据监听器
     * @param <T>      行数据类型
     */
    public static <T> void importExcel(InputStream is, Class clazz, AnalysisEventListener<T> listener) {
        EasyExcel.read(is, clazz, listener).sheet().doRead();
    }
}
