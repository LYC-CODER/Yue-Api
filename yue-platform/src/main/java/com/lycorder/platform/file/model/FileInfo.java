package com.lycorder.platform.file.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 文件信息对象。
 * <p>
 * 作为文件上传接口的返回体，包含文件原始名称和可访问地址。
 * </p>
 *
 * @author LYCoder
 */
@Schema(description = "文件对象")
@Data
public class FileInfo {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件URL")
    private String url;

}
