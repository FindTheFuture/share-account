package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-13
 * @description
 */

public interface FileService {

    FileUploadResponse uploadFile(MultipartFile file, int fileType, int pathType, String path, Long objectId) throws IOException;
    boolean delete(String path, String fileName);

    /**
     * 上传文件到COS
     * @param localFilePath 本地文件路径
     * @param cosPath COS存储路径
     * @return COS文件访问URL
     */
    String uploadFile(String localFilePath, String cosPath);
    
    /**
     * 删除COS文件
     * @param cosPath COS文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String cosPath);

    /**
     *  生成文件临时URL
     * @param filePath 文件路径，例如path/to/your/image.png
     * @param expireMinutes 多久过期：分钟
     */
    URL generatePresignedUrl(String filePath, int expireMinutes);
}
