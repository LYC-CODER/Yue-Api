package com.lycorder.platform.file.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;


import com.lycorder.platform.file.model.FileInfo;
import com.lycorder.platform.file.service.FileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * 本地磁盘存储服务类。
 * <p>
 * 当 oss.type=local 时生效，将文件写入 oss.local.storage-path 指定的根目录，
 * 按日期子目录存放，并返回相对于根目录的访问路径。
 * </p>
 *
 * @author Theo
 * @since 2024-12-09 17:11
 */
@Data
@Slf4j
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "local")
@ConfigurationProperties(prefix = "oss.local")
@RequiredArgsConstructor
public class LocalFileService implements FileService {

    /** 本地文件存储根目录 */
    @Value("${oss.local.storage-path}")
    private String storagePath;

    /**
     * 上传文件方法。
     * <p>
     * 使用 UUID 重命名文件并按日期目录保存，返回的 URL 为相对路径。
     * </p>
     *
     * @param file 表单文件对象
     * @return 文件信息
     * @throws RuntimeException 文件读取或写入失败时抛出
     */
    @Override
    public FileInfo uploadFile(MultipartFile file) {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        // 使用 UUID 重命名，避免原始文件名冲突和路径穿越
        String fileName = IdUtil.simpleUUID()+ "." + suffix;;
        // 按日期建立子目录，避免单目录下文件过多
        String folder = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        // 确保根目录以分隔符结尾，避免拼接后的路径缺少分隔符
        String filePrefix = storagePath.endsWith(File.separator) ? storagePath : storagePath + File.separator;
        //  try-with-resource 语法糖自动释放流
        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件
            FileUtil.writeFromStream(inputStream, filePrefix + folder + File.separator + fileName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
        // 获取文件访问路径，因为这里是本地存储，所以直接返回文件的相对路径，需要前端自行处理访问前缀
        String fileUrl = File.separator + folder + File.separator + fileName;
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(originalFilename);
        fileInfo.setUrl(fileUrl);
        return fileInfo;
    }


    /**
     * 删除文件。
     *
     * @param filePath 文件完整URL
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        //判断文件是否为空
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            // 只允许删除存储根目录内的文件，防止 ../../ 路径穿越
            Path root = Paths.get(storagePath).toAbsolutePath().normalize();
            String relativePath = filePath.replace('\\', '/').replaceAll("^/+", "");
            Path target = root.resolve(relativePath).normalize();

            // 目标必须真实存在，且解析软链接后仍位于存储根目录内
            if (!target.startsWith(root) || !Files.exists(target)) {
                return false;
            }
            Path realRoot = root.toRealPath();
            Path realTarget = target.toRealPath();
            if (!realTarget.startsWith(realRoot) || Files.isDirectory(realTarget)) {
                return false;
            }
            return Files.deleteIfExists(realTarget);
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", filePath, e);
            return false;
        }
    }
}
