package com.lycorder.common.validator;

import com.lycorder.common.annotation.ValidField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * 字段校验器
 * <p>
 * 对应 {@link ValidField} 注解，校验字符串取值是否在注解声明的白名单内；
 * 值为 null 时视为通过，必填约束交由 @NotNull 等注解处理。
 *
 * @author LYCoder
 */
public class FieldValidator implements ConstraintValidator<ValidField, String> {

    private String[] allowedValues;

    /**
     * 初始化允许的字段值白名单。
     *
     * @param constraintAnnotation 字段上的 ValidField 注解实例
     */
    @Override
    public void initialize(ValidField constraintAnnotation) {
        // 初始化允许的值列表
        this.allowedValues = constraintAnnotation.allowedValues();
    }

    /**
     * 校验字段值是否在白名单内。
     *
     * @param value   待校验的字段值
     * @param context 校验上下文
     * @return true 表示值为 null 或命中白名单
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 如果字段允许为空，可以返回 true
        }
        // 检查值是否在允许列表中
        return Arrays.asList(allowedValues).contains(value);
    }
}
