package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-06
 * @description
 */
@Data
public class LoginRequest {
    private String code;
    private String refreshToken;
}
