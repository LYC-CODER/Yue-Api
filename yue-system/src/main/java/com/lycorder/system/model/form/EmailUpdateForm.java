package com.lycorder.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改邮箱表单
 *
 * @author LYCoder
 */
@Schema(description = "修改邮箱表单")
@Data
public class EmailUpdateForm {

    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String password;
}
