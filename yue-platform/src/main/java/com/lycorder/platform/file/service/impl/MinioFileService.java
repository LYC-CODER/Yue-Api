package com.lycorder.platform.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;


import com.lycorder.core.exception.BusinessException;
import com.lycorder.core.result.ResultCode;
import com.lycorder.platform.file.model.FileInfo;
import com.lycorder.platform.file.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * MinIO 对象存储服务类。
 * <p>
 * 当 oss.type=minio 时生效，使用 oss.minio.* 配置构建客户端；
 * 文件按日期目录存放，未配置 customDomain 时返回预签名地址，否则拼接自定义域名。
 * </p>
 *
 * @author LYCoder
 */
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "minio")
@ConfigurationProperties(prefix = "oss.minio")
@RequiredArgsConstructor
@Data
@Slf4j
public class MinioFileService implements FileService {

    /**
     * 服务Endpoint
     */
    private String endpoint;
    /**
     * 访问凭据
     */
    private String accessKey;
    /**
     * 凭据密钥
     */
    private String secretKey;
    /**
     * 存储桶名称
     */
    private String bucketName;
    /**
     * 自定义域名
     */
    private String customDomain;

    /** MinIO 客户端，在 {@link #init()} 中完成初始化 */
    private MinioClient minioClient;

    /**
     * 初始化 MinIO 客户端。
     * <p>
     * 依赖注入完成后执行；此处仅构建客户端，存储桶在首次上传时按需创建。
     * </p>
     */
    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        // 创建存储桶(存储桶不存在)
        // createBucketIfAbsent(bucketName);
    }


    /**
     * 上传文件。
     * <p>
     * 首次上传时会检查并创建存储桶；返回的地址可能是预签名 URL 或自定义域名地址。
     * </p>
     *
     * @param file 表单文件对象
     * @return 文件信息
     * @throws BusinessException 上传失败时抛出
     */
    @Override
    public FileInfo uploadFile(MultipartFile file) {

        // 创建存储桶(存储桶不存在)，如果有搭建好的minio服务，建议放在init方法中
        createBucketIfAbsent(bucketName);

        // 文件原生名称
        String originalFilename = file.getOriginalFilename();
        // 文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        // 按日期建立对象目录，避免同一目录下对象过多
        String dateFolder = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        // 使用 UUID 重命名，避免对象名冲突和路径穿越
        String fileName = IdUtil.simpleUUID() + "." + suffix;

        //  try-with-resource 语法糖自动释放流
        try (InputStream inputStream = file.getInputStream()) {
            // 文件上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(dateFolder + "/"+ fileName)
                    .contentType(file.getContentType())
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);

            // 返回文件路径
            String fileUrl;
            // 未配置自定义域名
            if (StrUtil.isBlank(customDomain)) {
                // 获取文件URL
                GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(dateFolder + "/"+ fileName)
                        .method(Method.GET)
                        .build();

                fileUrl = minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
                // 去掉预签名 URL 的查询参数，返回不含签名的对象地址
                fileUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            } else {
                // 配置自定义文件路径域名
                fileUrl = customDomain + "/"+ bucketName + "/"+ dateFolder + "/"+ fileName;
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(originalFilename);
            fileInfo.setUrl(fileUrl);
            return fileInfo;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ResultCode.UPLOAD_FILE_EXCEPTION, e.getMessage());
        }
    }


    /**
     * 删除文件。
     *
     * @param filePath 文件完整路径
     * @return 是否删除成功
     * @throws BusinessException 删除失败时抛出
     */
    @Override
    public boolean deleteFile(String filePath) {
        Assert.notBlank(filePath, "删除文件路径不能为空");
        try {
            String fileName;
            // 按 URL 前缀截取出对象名，filePath 需由本服务生成，否则截取位置可能不正确
            if (StrUtil.isNotBlank(customDomain)) {
                // https://oss.youlai.tech/default/20221120/test.jpg → 20221120/websocket.jpg
                fileName = filePath.substring(customDomain.length() + 1 + bucketName.length() + 1); // 两个/占了2个字符长度
            } else {
                // http://localhost:9000/default/20221120/test.jpg → 20221120/websocket.jpg
                fileName = filePath.substring(endpoint.length() + 1 + bucketName.length() + 1);
            }
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build();

            minioClient.removeObject(removeObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new BusinessException(ResultCode.DELETE_FILE_EXCEPTION, e.getMessage());
        }
    }


    /**
     * PUBLIC桶策略。
     * <p>
     * 如果不配置，则新建的存储桶默认是 PRIVATE，存储桶中的文件会拒绝访问（Access Denied）。
     * 该策略允许匿名读取对象，生产环境应按需收紧权限。
     * </p>
     *
     * @param bucketName 存储桶名称
     * @return 存储桶策略 JSON
     */
    private static String publicBucketPolicy(String bucketName) {
        // 仅开放匿名读取，写/删必须经过后端鉴权后使用 MinIO 客户端执行。
        // 这样既能让头像、附件 URL 直接访问，又不会把整个桶暴露为可写。
        return "{\"Version\":\"2012-10-17\","
                + "\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
    }

    /**
     * 确保存储桶存在，并将桶策略收敛为“匿名只读”。
     * <p>
     * 文件上传、删除仍然通过后端 MinIO 客户端完成；匿名访问只能读取对象，
     * 不能 Put/Delete，避免存储桶被外部直接写入或清空。
     * </p>
     *
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    private void createBucketIfAbsent(String bucketName) {
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        if (!minioClient.bucketExists(bucketExistsArgs)) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
            minioClient.makeBucket(makeBucketArgs);
        }

        // 每次启动后首次上传都会校准桶策略，已有桶也会从旧的“公共读写”收敛为只读
        SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs
                .builder()
                .bucket(bucketName)
                .config(publicBucketPolicy(bucketName))
                .build();
        minioClient.setBucketPolicy(setBucketPolicyArgs);
    }
}
