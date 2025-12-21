package cn.lizongyi.shareaccount.response;

import lombok.Data;

/**
 * 支付响应类
 */
@Data
public class PaymentResponse {
    private Long paymentId;
    
    public PaymentResponse(Long paymentId) {
        this.paymentId = paymentId;
    }
}