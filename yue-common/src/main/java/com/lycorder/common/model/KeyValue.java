package com.lycorder.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 键值对
 * <p>
 * 用于字典项、选项列表等简单的键值数据传输场景。
 *
 * @author LYCoder
 */
@Schema(description = "键值对")
@Data
@NoArgsConstructor
public class KeyValue {

    /**
     * 构造键值对。
     *
     * @param key   键（选项值）
     * @param value 值（选项标签）
     */
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Schema(description = "选项的值")
    private String key;

    @Schema(description = "选项的标签")
    private String value;

}