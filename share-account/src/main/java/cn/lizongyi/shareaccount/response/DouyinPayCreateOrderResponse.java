package cn.lizongyi.shareaccount.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DouyinPayCreateOrderResponse {

    private String orderId;

    private String tradeNo;

    private String paymentUrl;

    private String sign;

    private Long expireTime;
}
