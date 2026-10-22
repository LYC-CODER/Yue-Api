package com.lycorder.auth.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 验证码信息
 *
 * @author LYCoder
 */
@Schema(description = "验证码信息")
@Data
@Builder
public class CaptchaVO {

    @Schema(description = "验证码缓存 ID")
    private String captchaId;

    @Schema(description = "验证码图片Base64字符串")
    private String captchaBase64;

}
