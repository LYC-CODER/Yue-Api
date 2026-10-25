package com.lycorder.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码表单。
 *
 * <p>密码通过请求体传递，避免明文出现在 URL、浏览器历史和代理访问日志中。</p>
 *
 * @author LYCoder
 */
@Schema(description = "重置用户密码表单")
@Data
public class PasswordResetForm {

    /**
     * 新密码（明文，由服务端加密后存储）
     */
    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为 6-20 位")
    private String password;
}
