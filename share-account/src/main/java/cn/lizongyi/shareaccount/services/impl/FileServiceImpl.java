package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.response.FileUploadResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.FileService;
import cn.lizongyi.shareaccount.services.PictureService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.coobird.thumbnailator.Thumbnails;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-13
 * @description
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private COSClient cosClient;

    @Autowired
    private BaseHandler baseHandler;

    private String getBucket() {
        return baseHandler.getCosBucket();
    }

    private String getUrl() {
        return baseHandler.getCosUrl();
    }

    // 如果要修改这个，需要将application.yml 里 max-file-size 一起修改
    private Integer pictureMaxSize = 20;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, int fileType, int pathType, String path, Long objectId) throws IOException {

        // 文件大小限制（20MB）
        long maxSize = pictureMaxSize * 1024 * 1024; // 20 MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("照片不能大于20M");
        }

        // 文件类型限制
        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/avif");
        if (!allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("只能上传图片");
        }

        // 根据内容类型设置文件扩展名
        String fileExtension;
        switch (contentType) {
            case "image/jpg":
            case "image/jpeg":
                fileExtension = ".jpeg";
                break;
            case "image/png":
                fileExtension = ".png";
                break;
            case "image/avif":
                fileExtension = ".jpeg";
                break;
            default:
                throw new IllegalArgumentException("不支持的图片格式");
        }

        // 定义是否需要压缩
        Integer compressPicSize = 1024 * 1024; // 1MB in bytes
        Integer aiCompressSize = 500 * 1024; // 0.5MB for AI recognition
        boolean needCompress = file.getSize() > compressPicSize;
        boolean needAiCompress = file.getSize() > aiCompressSize; // 大于0.5MB的图片需要压缩以减少AI token消耗
        byte[] imageData = null;

        try {
            if (needAiCompress && !fileExtension.equalsIgnoreCase(".avif")) {
                log.info("图片大小: {}MB，执行AI优化压缩", file.getSize() / 1024.0 / 1024.0);
                
                // 三级压缩策略
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    // 第一级：只压缩质量
                    Thumbnails.of(new ByteArrayInputStream(file.getBytes()))
                              .outputQuality(0.8) // 初始质量设置为80%
                              .scale(1f) // 不改变分辨率
                              .toOutputStream(os);
                    imageData = os.toByteArray();
                    log.info("第一级压缩后大小: {}KB", imageData.length / 1024);

                    // 第二级：如果仍然大于0.5MB，缩小分辨率
                    if (imageData.length > aiCompressSize) {
                        os.reset();
                        Thumbnails.of(new ByteArrayInputStream(file.getBytes()))
                                  .size(1200, 1200) // 适当缩小分辨率
                                  .outputQuality(0.7)
                                  .toOutputStream(os);
                        imageData = os.toByteArray();
                        log.info("第二级压缩后大小: {}KB", imageData.length / 1024);
                    }
                    
                    // 第三级：如果仍然大于0.5MB，进一步缩小分辨率
                    if (imageData.length > aiCompressSize) {
                        os.reset();
                        Thumbnails.of(new ByteArrayInputStream(file.getBytes()))
                                  .size(800, 800) // 进一步缩小分辨率
                                  .outputQuality(0.6)
                                  .toOutputStream(os);
                        imageData = os.toByteArray();
                        log.info("第三级压缩后大小: {}KB", imageData.length / 1024);
                    }
                }
            } else if (needCompress && !fileExtension.equalsIgnoreCase(".avif")) {
                log.info("图片大小: {}MB，执行常规压缩", file.getSize() / 1024.0 / 1024.0);
                // 常规压缩逻辑
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    Thumbnails.of(new ByteArrayInputStream(file.getBytes()))
                              .outputQuality(0.9)
                              .scale(1f)
                              .toOutputStream(os);
                    imageData = os.toByteArray();
                }
            } else {
                // 小于等于阈值的图片直接使用原图数据
                imageData = file.getBytes();
            }
        } catch (IOException e) {
            log.error("图片压缩失败", e);
            // 压缩失败时使用原图
            imageData = file.getBytes();
        }

        // 生成唯一的文件名
        String fileName = UUID.randomUUID().toString() + fileExtension;

        String address = getUrl() + path + fileName;

        // 设置对象元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(imageData.length);

        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);

        // 创建上传请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(getBucket(), path + fileName, bis, metadata);

        // 设置对象的 ACL 为公有读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

        // 执行上传
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        // 检查上传是否成功
        if (putObjectResult != null && putObjectResult.getETag() != null) {
            // 保存图片记录
            Picture picture = new Picture();
            picture.setAddress(address);
            picture.setType(fileType);
            picture.setPath(path);
            picture.setStatus(0);
            picture.setObjectId(objectId);
            picture.setName(fileName);
            picture.setCreateTime(LocalDateTime.now());

            boolean result = pictureService.save(picture);
            if (result) {
                // 为文件申请临时URL
                URL presignedUrl = generatePresignedUrl(path + fileName, 30);

                return FileUploadResponse.createResponse(picture.getId(), fileName, path, presignedUrl.toString());
            } else {
                log.error("新增图片记录失败");
                return null;
            }
        } else {
            log.error("新增图片到对象存储失败");
            return null;
        }
    }


    @Override
    public boolean delete(String path, String fileName){
        try {
            // 删除 COS 上的文件
            cosClient.deleteObject(getBucket(), path + fileName);
        } catch (Exception e){
            log.error("删除文件失败", e);
            return false;
        }

        return true;
    }

    /**
     * 生成文件临时URL
     *
     * @param filePath      文件路径，例如path/to/your/image.png
     * @param expireMinutes 多久过期：分钟
     */
    @Override
    public URL generatePresignedUrl(String filePath, int expireMinutes) {
        try {
            // 设置 URL 过期时间为当前时间加上指定分钟数
            Date expiration = new Date(System.currentTimeMillis() + (expireMinutes * 60 * 1000));

            // 创建预签名 URL 请求
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(getBucket(), filePath);
            request.setMethod(com.qcloud.cos.http.HttpMethodName.GET); // 设置 HTTP 方法为 GET
            request.setExpiration(expiration);

            // 生成预签名 URL
            URL url = cosClient.generatePresignedUrl(request);
            return url;
        } catch (Exception e) {
            log.error("生成临时URL失败", e);
            throw new RuntimeException("生成临时URL失败", e);
        }
    }
    
    /**
     * 上传本地文件到COS
     * @param localFilePath 本地文件路径
     * @param cosPath COS存储路径
     * @return 文件访问URL
     */
    @Override
    public String uploadFile(String localFilePath, String cosPath) {
        try {
            // 上传文件
            java.io.File localFile = new java.io.File(localFilePath);
            PutObjectRequest putObjectRequest = new PutObjectRequest(getBucket(), cosPath, localFile);
            
            // 设置存储类型为标准存储
            ObjectMetadata objectMetadata = new ObjectMetadata();
            putObjectRequest.setMetadata(objectMetadata);

            // 设置对象的 ACL 为公有读
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("COS上传成功: {}, ETag: {}", cosPath, putObjectResult.getETag());
            
            // 生成访问URL
            return getUrl() + cosPath;
            
        } catch (Exception e) {
            log.error("COS上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    /**
     * 删除COS上的文件
     * @param cosPath COS文件路径
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String cosPath) {
        try {
            // 删除文件
            cosClient.deleteObject(getBucket(), cosPath);
            log.info("COS文件删除成功: {}", cosPath);
            return true;
            
        } catch (Exception e) {
            log.error("COS文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
