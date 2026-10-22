package com.lycorder.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改手机表单
 *
 * @author LYCoder
 */
@Schema(description = "修改手机表单")
@Data
public class MobileUpdateForm {

    @Schema(description = "手机号码")
    @NotBlank(message = "手机号码不能为空")
    private String mobile;

    @Schema(description = "当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String password;


}
