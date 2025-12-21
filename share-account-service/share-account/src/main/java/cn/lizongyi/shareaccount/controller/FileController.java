package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.FilePresignedUrlRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.FileUploadResponse;
import cn.lizongyi.shareaccount.services.FileService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.enums.PictureTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-13
 * @description
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private BaseHandler baseHandler;

    @PostMapping("/fileUpload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileType") int fileType,
                                                                      @RequestParam("pathType") int pathType, @RequestParam("path") String path,
                                                                      @RequestParam("objectId") Long objectId) {
        log.info("上传图片请求参数 fileType={}, pathType={}, path={} objectId={}", fileType, pathType, path, objectId);
        if (file.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.badRequest("文件不存在"));
        }

        // 游客限制：游客不可上传用户头像
        try {
            if (fileType == PictureTypeEnum.AVATAR_USER.getId() && baseHandler.isGuestUser(objectId)) {
                return ResponseEntity.ok(ApiResponse.badRequest("游客不可上传头像，请升级为正式用户"));
            }
        } catch (Exception e) {
            // 安全兜底，避免阻塞其他类型上传
            log.warn("头像上传游客检查失败: {}", e.getMessage());
        }

        try {
            FileUploadResponse response = fileService.uploadFile(file, fileType, pathType, path, objectId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e){
            return ResponseEntity.ok(ApiResponse.badRequest("上传文件失败"));
        }
    }


    /**
     * 生成COS 文件临时URL
     */
    @PostMapping("/generatePresignedUrl")
    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(@RequestBody FilePresignedUrlRequest filePresignedUrlRequest) {
        Long id = filePresignedUrlRequest.getId();
        Integer fileType = filePresignedUrlRequest.getFileType();
        Integer expirationTime = filePresignedUrlRequest.getExpirationTime();
        String fileName = filePresignedUrlRequest.getFileName();


        if (fileType == 0 || id == null || id <=0L || expirationTime == null || expirationTime <=0 || StringUtils.isEmpty(fileName)) {
            return ResponseEntity.ok(ApiResponse.badRequest("获取cos临时路径失败"));
        }

        // 根据type获取文件路径
        /*String path = CosFilePathEnum.fromId(fileType).getName() +  id + "/" + fileName;

        try {
            URL url = fileService.generatePresignedUrl(path, expirationTime);
            return ResponseEntity.ok(ApiResponse.success(url.toString()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.badRequest("生成临时URL失败"));
        }*/
        return null;
    }



}
