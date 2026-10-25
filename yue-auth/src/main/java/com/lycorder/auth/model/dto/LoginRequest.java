package com.lycorder.auth.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数。
 * <p>
 * 承载用户名、密码以及图形验证码校验所需的验证码标识和验证码文本。
 *
 * @author LYCoder
 */
@Schema(description = "登录请求参数")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码缓存ID", example = "captcha_id_123")
    private String captchaId;

    @Schema(description = "验证码", example = "1234")
    private String captchaCode;
}