package cn.lizongyi.shareaccount.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DouyinPayConfig {

    private String appId;

    private String appSecret;

    private String token;

    private String notifyUrl;

    private String merchantId;

    private String signType = "RSA2";

    private String salt;
}
