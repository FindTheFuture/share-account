package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class WechatPayCallBackRequest {

    private String body;

        /*
            Wechatpay-Nonce
         */

    private String Nonce;
        /*
            Wechatpay-Serial
        */

    private String Serial;
        /*
            Wechatpay-Signature
        */

    private String Signature;
        /*
            Wechatpay-Signature-Type
        */

    private String SignatureType;
        /*
            Wechatpay-Timestamp
        */

    private String Timestamp;
}
