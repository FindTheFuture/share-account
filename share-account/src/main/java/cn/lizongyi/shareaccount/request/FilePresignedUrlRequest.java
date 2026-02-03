package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-14
 * @description
 */
@Data
public class FilePresignedUrlRequest {

    private Long id;
    private String fileName;
    private Integer fileType;
    private Integer expirationTime;
}
