package com.lycorder.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 全局配置
 * <p>
 * 定义全局 ObjectMapper，并针对字符串编码做统一处理。
 */
@Configuration
public class JacksonConfig {

    /**
     * 创建全局 ObjectMapper。
     * <p>
     * 注册自定义 String 序列化器，修正按 ISO-8859-1 读取的字符串编码。
     *
     * @return 配置完成的 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册自定义序列化器处理可能的二进制数据
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new JsonSerializer<String>() {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    // 确保字符串正确编码
                    // 先按 ISO-8859-1 还原原始字节，再以 UTF-8 重建字符串，避免中文乱码
                    gen.writeString(new String(value.getBytes("ISO-8859-1"), "UTF-8"));
                } else {
                    gen.writeNull();
                }
            }
        });

        mapper.registerModule(module);
        return mapper;
    }
}