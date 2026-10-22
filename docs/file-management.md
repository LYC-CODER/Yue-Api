# 文件管理模块使用指南

## 概述

文件管理模块提供了统一的文件上传、下载、存储和管理功能。支持多种存储方式（本地存储、MinIO对象存储），具备文件预览、断点续传、批量操作等特性。

## 架构设计

### 模块结构
```
com.lycorder.platform.file
├── controller      # 文件控制器
├── dto            # 数据传输对象
├── entity         # 实体类
├── enums          # 枚举类
├── mapper         # 数据访问层
├── service        # 业务逻辑层
├── vo             # 视图对象
└── config         # 配置文件
```

### 存储策略
支持两种存储策略：
1. **本地存储**：文件存储在服务器本地磁盘
2. **MinIO存储**：文件存储在MinIO对象存储服务

## 核心功能

### 1. 文件上传

#### 1.1 单文件上传
```java
@PostMapping("/upload")
public Result<FileVO> uploadFile(@RequestParam("file") MultipartFile file) {
    FileVO fileVO = fileService.uploadFile(file);
    return Result.success(fileVO);
}
```

#### 1.2 多文件上传
```java
@PostMapping("/upload/batch")
public Result<List<FileVO>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
    List<FileVO> fileList = fileService.uploadFiles(Arrays.asList(files));
    return Result.success(fileList);
}
```

#### 1.3 分片上传（断点续传）
```java
@PostMapping("/upload/chunk")
public Result<Void> uploadChunk(@RequestBody UploadChunkDTO dto) {
    fileService.uploadChunk(dto);
    return Result.success();
}

@PostMapping("/upload/merge")
public Result<FileVO> mergeChunks(@RequestBody MergeChunksDTO dto) {
    FileVO fileVO = fileService.mergeChunks(dto);
    return Result.success(fileVO);
}
```

### 2. 文件下载

#### 2.1 普通下载
```java
@GetMapping("/download/{fileId}")
public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
    fileService.downloadFile(fileId, response);
}
```

#### 2.2 图片预览
```java
@GetMapping("/preview/{fileId}")
public void previewImage(@PathVariable Long fileId, HttpServletResponse response) {
    fileService.previewImage(fileId, response);
}
```

### 3. 文件管理

#### 3.1 文件信息
```java
public class FileInfo extends BaseEntity {
    private Long fileId;        // 文件ID
    private String fileName;    // 原文件名
    private String filePath;    // 存储路径
    private Long fileSize;      // 文件大小
    private String fileType;    // 文件类型
    private String mimeType;    // MIME类型
    private String md5;         // MD5值
    private String storageType; // 存储类型
    private Integer status;     // 状态
}
```

#### 3.2 文件列表查询
```java
@GetMapping("/page")
public Result<PageResult<FileVO>> getFilePage(@Valid FileQuery query) {
    PageResult<FileVO> page = fileService.getFilePage(query);
    return Result.success(page);
}
```

## 存储配置

### 1. 本地存储配置
```yaml
file:
  storage:
    type: local  # 存储类型
    local:
      path: /data/files  # 存储路径
      domain: http://localhost:8080  # 访问域名
```

### 2. MinIO存储配置
```yaml
file:
  storage:
    type: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket: files
      secure: false
```

### 3. 通用配置
```yaml
file:
  upload:
    max-size: 10MB  # 最大上传大小
    allow-types: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar  # 允许的文件类型
    forbid-extensions: exe,bat,sh,jsp,html,htm  # 禁止的文件扩展名
  download:
    speed-limit: 1024KB  # 下载速度限制
  preview:
    image-types: jpg,jpeg,png,gif,bmp,webp  # 支持预览的图片类型
```

## 高级功能

### 1. 文件唯一性校验
通过MD5值判断文件是否已存在，避免重复存储：
```java
private String getFileMd5(MultipartFile file) {
    return DigestUtils.md5DigestAsHex(file.getInputStream());
}

private FileInfo checkFileExists(String md5) {
    LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(FileInfo::getMd5, md5);
    return fileMapper.selectOne(wrapper);
}
```

### 2. 图片处理
支持图片压缩、缩略图生成：
```java
public void generateThumbnail(String filePath, String thumbnailPath) {
    Thumbnails.of(filePath)
        .size(200, 200)
        .toFile(thumbnailPath);
}
```

### 3. 视频处理
支持视频转码、截图：
```java
public void captureVideoFrame(String videoPath, String framePath) {
    FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
    grabber.start();
    Frame frame = grabber.grabImage();
    // 保存帧图片
    grabber.stop();
}
```

### 4. 文档预览
支持Office文档在线预览：
```java
@GetMapping("/preview/doc/{fileId}")
public Result<String> previewDocument(@PathVariable Long fileId) {
    String previewUrl = fileService.getDocumentPreviewUrl(fileId);
    return Result.success(previewUrl);
}
```

## 断点续传实现

### 1. 前端实现
```javascript
// 计算文件MD5
async function calculateFileMd5(file) {
    const spark = new SparkMD5.ArrayBuffer();
    const chunks = Math.ceil(file.size / chunkSize);

    for (let i = 0; i < chunks; i++) {
        const start = i * chunkSize;
        const end = Math.min(start + chunkSize, file.size);
        const chunk = file.slice(start, end);
        const buffer = await chunk.arrayBuffer();
        spark.append(buffer);
    }

    return spark.end();
}

// 上传分片
async function uploadChunk(chunk, index, md5) {
    const formData = new FormData();
    formData.append('chunk', chunk);
    formData.append('index', index);
    formData.append('md5', md5);

    return await fetch('/api/file/upload/chunk', {
        method: 'POST',
        body: formData
    });
}
```

### 2. 后端实现
```java
@Service
public class FileChunkService {

    public void uploadChunk(UploadChunkDTO dto) {
        // 保存分片文件
        String chunkPath = getChunkPath(dto.getMd5(), dto.getIndex());
        File chunkFile = new File(chunkPath);
        FileUtils.writeByteArrayToFile(chunkFile, dto.getChunk().getBytes());

        // 记录已上传分片
        redisTemplate.opsForSet().add("uploaded:" + dto.getMd5(), dto.getIndex());
    }

    public FileVO mergeChunks(MergeChunksDTO dto) {
        // 检查所有分片是否上传完成
        Set<Integer> uploadedChunks = redisTemplate.opsForSet()
            .members("uploaded:" + dto.getMd5());

        if (uploadedChunks.size() != dto.getTotalChunks()) {
            throw new BizException("分片上传不完整");
        }

        // 合并文件
        String filePath = mergeChunkFiles(dto.getMd5(), dto.getTotalChunks());

        // 保存文件信息
        FileInfo fileInfo = saveFileInfo(dto, filePath);

        // 清理分片文件和缓存
        cleanChunks(dto.getMd5());

        return convertToVO(fileInfo);
    }
}
```

## 存储策略接口

### 1. 存储策略接口定义
```java
public interface FileStorageStrategy {

    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String path) throws IOException;

    /**
     * 下载文件
     */
    void downloadFile(String path, HttpServletResponse response) throws IOException;

    /**
     * 删除文件
     */
    void deleteFile(String path) throws IOException;

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String path);

    /**
     * 检查文件是否存在
     */
    boolean exists(String path);
}
```

### 2. 本地存储实现
```java
@Component("localStorage")
public class LocalStorageStrategy implements FileStorageStrategy {

    @Value("${file.storage.local.path}")
    private String storagePath;

    @Value("${file.storage.local.domain}")
    private String domain;

    @Override
    public String uploadFile(MultipartFile file, String path) throws IOException {
        File dest = new File(storagePath + path);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);
        return path;
    }

    @Override
    public void downloadFile(String path, HttpServletResponse response) throws IOException {
        File file = new File(storagePath + path);
        if (!file.exists()) {
            throw new BizException("文件不存在");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" +
            URLEncoder.encode(file.getName(), "UTF-8"));

        FileUtils.copyFile(file, response.getOutputStream());
    }
}
```

### 3. MinIO存储实现
```java
@Component("minioStorage")
public class MinioStorageStrategy implements FileStorageStrategy {

    @Autowired
    private MinioClient minioClient;

    @Value("${file.storage.minio.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file, String path) throws IOException {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return path;
        } catch (Exception e) {
            throw new BizException("文件上传失败", e);
        }
    }

    @Override
    public void downloadFile(String path, HttpServletResponse response) throws IOException {
        try {
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build()
            );
            IOUtils.copy(stream, response.getOutputStream());
        } catch (Exception e) {
            throw new BizException("文件下载失败", e);
        }
    }
}
```

## 文件安全控制

### 1. 文件类型检查
```java
public boolean isAllowedFileType(String fileName) {
    String extension = getFileExtension(fileName);
    String[] allowTypes = allowTypesConfig.split(",");
    String[] forbidTypes = forbidExtensionsConfig.split(",");

    // 检查禁止类型
    for (String forbid : forbidTypes) {
        if (forbid.equalsIgnoreCase(extension)) {
            return false;
        }
    }

    // 检查允许类型
    for (String allow : allowTypes) {
        if (allow.equalsIgnoreCase(extension)) {
            return true;
        }
    }

    return false;
}
```

### 2. 文件大小限制
```java
public void checkFileSize(long size) {
    long maxSize = maxSizeConfig * 1024 * 1024; // MB转字节
    if (size > maxSize) {
        throw new BizException("文件大小超过限制");
    }
}
```

### 3. 文件访问权限
```java
@PreAuthorize("hasPermission(#fileId, 'file:download')")
@GetMapping("/download/{fileId}")
public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
    // 下载文件
}
```

## 性能优化

### 1. 文件上传优化
- 使用NIO进行文件传输
- 大文件分片并行上传
- 文件MD5缓存避免重复上传

### 2. 文件下载优化
- 支持Range请求实现断点续传
- 使用缓冲流提高传输效率
- 对大文件进行压缩传输

### 3. 存储优化
- 定期清理临时文件
- 冷热数据分离存储
- 文件压缩归档

## 监控与统计

### 1. 文件统计信息
```java
@GetMapping("/statistics")
public Result<FileStatisticsVO> getFileStatistics() {
    FileStatisticsVO statistics = fileService.getFileStatistics();
    return Result.success(statistics);
}
```

统计内容包括：
- 总文件数
- 总存储大小
- 各类型文件分布
- 上传下载趋势

### 2. 操作日志
记录文件操作日志：
- 上传日志
- 下载日志
- 删除日志
- 预览日志

## 常见问题

### Q1: 大文件上传失败怎么办？
A: 检查Nginx/服务器上传大小限制，调整配置：
```nginx
client_max_body_size 100M;
```

### Q2: 文件预览不支持某些格式？
A: 需要添加对应的MIME类型配置，或集成第三方预览服务。

### Q3: 存储空间不足如何处理？
A: 实现存储空间监控，超出阈值时发送告警，并清理过期文件。

### Q4: 文件上传后访问404？
A: 检查文件访问路径配置，确保域名和路径正确。

## 最佳实践

1. **文件命名**: 使用UUID重命名文件，避免中文和特殊字符
2. **目录结构**: 按日期分目录存储，避免单目录文件过多
3. **备份策略**: 定期备份重要文件，建议异地备份
4. **监控告警**: 监控文件上传下载成功率，及时发现问题
5. **CDN加速**: 对静态文件使用CDN加速访问

## 更新计划

- [ ] 添加阿里云OSS存储支持
- [ ] 实现文件加密存储
- [ ] 支持音视频在线播放
- [ ] 添加文件版本管理
- [ ] 实现文件分享功能
- [ ] 支持Office文档在线编辑
- [ ] 添加文件标签管理
- [ ] 实现智能分类功能