package com.lycorder.auth.config;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;

import com.lycorder.common.config.property.CaptchaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

/**
 * 验证码自动装配配置。
 * <p>
 * 依据验证码配置项装配字符生成器与字体，供验证码服务生成图片时使用。
 *
 * @author LYCoder
 */
@Configuration
public class CaptchaConfig {

    @Autowired
    private CaptchaProperties captchaProperties;

    /**
     * 创建验证码文字生成器 Bean
     * <p>
     * 根据配置文件中的验证码类型（math 或 random）创建对应的生成器实例
     * math 类型生成算术表达式验证码，random 类型生成随机字符验证码
     * </p>
     *
     * @return CodeGenerator 验证码文字生成器实例
     * @throws IllegalArgumentException 当验证码类型既不是 math 也不是 random 时抛出
     */
    @Bean
    public CodeGenerator codeGenerator() {
        String codeType = captchaProperties.getCode().getType();
        int codeLength = captchaProperties.getCode().getLength();
        // 按配置选择算术表达式或随机字符生成器，未知类型直接拒绝启动
        if ("math".equalsIgnoreCase(codeType)) {
            return new MathGenerator(codeLength, false);
        } else if ("random".equalsIgnoreCase(codeType)) {
            return new RandomGenerator(codeLength);
        } else {
            throw new IllegalArgumentException("Invalid captcha codegen type: " + codeType);
        }
    }

    /**
     * 创建验证码字体 Bean
     * <p>
     * 根据配置文件中的字体名称、大小和样式创建 Font 对象
     * 用于设置验证码图片中文本的显示样式
     * </p>
     *
     * @return Font 验证码文本使用的字体对象
     */
    @Bean
    public Font captchaFont() {
        String fontName = captchaProperties.getFont().getName();
        int fontSize = captchaProperties.getFont().getSize();
        int fontWight = captchaProperties.getFont().getWeight();
        // 字体名称、样式和字号均来自外部配置
        return new Font(fontName, fontWight, fontSize);
    }


}
