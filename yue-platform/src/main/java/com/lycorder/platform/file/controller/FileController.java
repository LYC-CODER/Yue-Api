package com.lycorder.platform.file.controller;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.core.result.Result;
import com.lycorder.platform.file.model.FileInfo;
import com.lycorder.platform.file.service.FileService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * 文件控制层。
 * <p>
 * 对外提供文件上传和删除接口，具体存储方式由 {@link FileService} 的实现决定。
 * </p>
 *
 * @author LYCoder
 */
@Tag(name = "7.文件接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    /** 单文件大小上限：20MB */
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    /** 允许上传的扩展名白名单，禁止 HTML/SVG/JS 等可执行内容 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "zip", "rar", "7z"
    );

    /** 文件存储服务，运行时按 oss.type 注入本地或 MinIO 实现 */
    private final FileService fileService;

    /**
     * 上传文件。
     *
     * @param file 表单文件对象
     * @return 上传成功后的文件信息
     */
    @PostMapping
    @Operation(summary = "文件上传")
    public Result<FileInfo> uploadFile(
            @Parameter(
                    name = "file",
                    description = "表单文件对象",
                    required = true,
                    in = ParameterIn.DEFAULT,
                    schema = @Schema(name = "file", format = "binary")
            )
            @RequestPart(value = "file") MultipartFile file
    ) {
        validateFile(file);
        // 由具体存储实现完成上传，并返回文件名称与访问地址
        FileInfo fileInfo = fileService.uploadFile(file);
        return Result.success(fileInfo);
    }

    /**
     * 删除文件。
     *
     * @param filePath 文件路径
     * @return 删除结果
     */
    @DeleteMapping
    @Operation(summary = "文件删除")
    @PreAuthorize("@ss.hasPerm('sys:file:delete') or @ss.hasRole('ADMIN')")
    @SneakyThrows
    public Result<?> deleteFile(
            @Parameter(description = "文件路径") @RequestParam String filePath
    ) {
        boolean result = fileService.deleteFile(filePath);
        // 统一按删除结果转换为成功或失败响应
        return Result.judge(result);
    }

    /**
     * 校验上传文件的大小和扩展名。
     *
     * @param file 上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 20MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = FileUtil.getSuffix(originalFilename);
        if (StrUtil.isBlank(suffix)
                || !ALLOWED_EXTENSIONS.contains(suffix.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("不支持的文件类型");
        }
    }
}
