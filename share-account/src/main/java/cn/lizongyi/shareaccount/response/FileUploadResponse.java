package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-14
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {

    private Long id;
    private String fileName;
    private String path;
    private String address;

    public static FileUploadResponse createResponse(Long id, String fileName, String path, String address) {
        return new FileUploadResponse(id, fileName, path, address);
    }
}
