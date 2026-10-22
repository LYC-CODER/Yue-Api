package com.lycorder.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

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