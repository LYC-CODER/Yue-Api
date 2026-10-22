package com.lycorder.common.util;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.event.AnalysisEventListener;

import java.io.InputStream;

/**
 * Excel 工具类
 *
 *
 * @author LYCoder
 */
public class ExcelUtils {

    public static <T> void importExcel(InputStream is, Class clazz, AnalysisEventListener<T> listener) {
        EasyExcel.read(is, clazz, listener).sheet().doRead();
    }
}
