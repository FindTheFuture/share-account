package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.request.CreatePaymentOrderRequest;

import java.util.List;

public interface PaymentRecordService {

    /**
     * 根据ID查询支付记录
     */
    PaymentRecord findById(Long id);

    /**
     * 根据商户订单号查询支付记录
     */
    PaymentRecord findByOutTradeNo(String outTradeNo);

    /**
     * 根据微信支付订单号查询支付记录
     */
    PaymentRecord findByTransactionId(String transactionId);

    /**
     * 查询用户的所有支付记录
     */
    List<PaymentRecord> findByUserId(Long userId);

    /**
     * 查询用户指定状态的支付记录
     */
    List<PaymentRecord> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 创建支付记录
     */
    PaymentRecord createPaymentRecord(CreatePaymentOrderRequest request);

    /**
     * 更新支付订单号
     */
    boolean updatePaymentOutTradeNo(PaymentRecord paymentRecord);

    /**
     * 更新支付状态
     */
    boolean updatePaymentStatus(PaymentRecord paymentRecord);

    /**
     * 更新退款状态
     */
    boolean updateRefundStatus(PaymentRecord paymentRecord);

    /**
     * 统计用户成功支付的次数
     */
    int countSuccessfulPaymentsByUserId(Long userId);

    /**
     * 获取用户支付总额
     */
    Double getTotalPaymentAmountByUserId(Long userId);
}