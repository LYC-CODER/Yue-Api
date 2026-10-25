package com.lycorder.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下拉选项对象
 * <p>
 * 支持普通选项、带标签选项以及树形子选项三种形态。
 *
 * @author LYCoder
 */
@Schema(description ="下拉选项对象")
@Data
@NoArgsConstructor
public class Option<T> {

    /**
     * 构造基础选项。
     *
     * @param value 选项值
     * @param label 选项标签
     */
    public Option(T value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 构造树形选项。
     *
     * @param value    选项值
     * @param label    选项标签
     * @param children 子选项列表
     */
    public Option(T value, String label, List<Option<T>> children) {
        this.value = value;
        this.label = label;
        this.children= children;
    }

    /**
     * 构造带标签类型的选项。
     *
     * @param value 选项值
     * @param label 选项标签
     * @param tag   标签类型
     */
    public Option(T value, String label, String tag) {
        this.value = value;
        this.label = label;
        this.tag= tag;
    }


    @Schema(description="选项的值")
    private T value;

    @Schema(description="选项的标签")
    private String label;

    @Schema(description = "标签类型")
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private String tag;

    @Schema(description="子选项列表")
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<Option<T>> children;

}