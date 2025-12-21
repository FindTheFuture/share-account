package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * 创建支付订单请求
 */
@Data
public class CreatePaymentOrderRequest {
    
    /**
     * 会员套餐ID
     */
    private Long packageId;
    
    /**
     * 支付类型（可选）
     */
    private Integer payType;
}