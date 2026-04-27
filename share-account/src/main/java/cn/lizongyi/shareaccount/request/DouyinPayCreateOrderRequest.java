package cn.lizongyi.shareaccount.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class DouyinPayCreateOrderRequest {

    private Long paymentId;

    private Long userId;

    private String orderId;

    private BigDecimal amount;

    private String subject;

    private String body;

    private String notifyUrl;

    private String openid;
}
