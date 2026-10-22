package com.lycorder.common.enums;


import com.lycorder.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 环境枚举
 * @author LYCoder
 */
@Getter
public enum EnvEnum implements IBaseEnum<String> {

    DEV("dev", "开发环境"),
    PROD("prod", "生产环境");

    private final String value;

    private final String label;

    EnvEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
