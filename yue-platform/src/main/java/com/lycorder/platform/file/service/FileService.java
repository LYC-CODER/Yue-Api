package com.lycorder.platform.file.service;

import com.lycorder.platform.file.model.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务接口层。
 * <p>
 * 定义文件上传、删除的统一能力，具体实现由配置项 oss.type 决定：
 * local 对应本地磁盘存储，minio 对应 MinIO 对象存储。
 * </p>
 *
 * @author LYCoder
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 表单文件对象
     * @return 文件信息
     */
    FileInfo uploadFile(MultipartFile file);

    /**
     * 删除文件
     *
     * @param filePath 文件完整URL
     * @return 删除结果
     */
    boolean deleteFile(String filePath);


}
