package com.lycorder.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置表单。
 *
 * @author LYCoder
 */
@Schema(description = "系统配置")
@Data
public class ConfigForm {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "配置名称", example = "系统名称")
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 50, message = "配置名称长度不能超过50个字符")
    private String configName;

    @Schema(description = "配置key", example = "system.name")
    @NotBlank(message = "配置key不能为空")
    @Size(max = 50, message = "配置key长度不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "配置key只能包含字母、数字、点、下划线和短横线")
    private String configKey;

    @Schema(description = "配置值", example = "Yue")
    @NotBlank(message = "配置值不能为空")
    @Size(max = 100, message = "配置值长度不能超过100个字符")
    private String configValue;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
