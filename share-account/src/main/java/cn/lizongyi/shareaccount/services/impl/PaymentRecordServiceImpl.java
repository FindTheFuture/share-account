package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.PaymentRecordMapper;
import cn.lizongyi.shareaccount.response.MemberPackageResponse;
import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.enums.PayStatusEnum;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.MemberPackageService;
import cn.lizongyi.shareaccount.services.PaymentRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.lizongyi.shareaccount.request.CreatePaymentOrderRequest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PaymentRecordServiceImpl implements PaymentRecordService {

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private MemberPackageService memberPackageService;
    
    @Autowired
    private BaseHandler baseHandler;

    @Override
    public PaymentRecord findById(Long id) {
        log.info("查询支付记录，id: {}", id);
        return paymentRecordMapper.findById(id);
    }

    @Override
    public PaymentRecord findByOutTradeNo(String outTradeNo) {
        log.info("查询支付记录，商户订单号: {}", outTradeNo);
        return paymentRecordMapper.findByOutTradeNo(outTradeNo);
    }

    @Override
    public PaymentRecord findByTransactionId(String transactionId) {
        log.info("查询支付记录，微信支付订单号: {}", transactionId);
        return paymentRecordMapper.findByTransactionId(transactionId);
    }

    @Override
    public List<PaymentRecord> findByUserId(Long userId) {
        log.info("查询用户支付记录，userId: {}", userId);
        return paymentRecordMapper.findByUserId(userId);
    }

    @Override
    public List<PaymentRecord> findByUserIdAndStatus(Long userId, Integer status) {
        log.info("查询用户支付记录，userId: {}, status: {}", userId, status);
        return paymentRecordMapper.findByUserIdAndStatus(userId, status);
    }

    @Override
    public PaymentRecord createPaymentRecord(CreatePaymentOrderRequest request) {
        Long userId = baseHandler.getUserId();
        log.info("创建支付记录，userId: {}, packageId: {}", userId, request.getPackageId());

        MemberPackageResponse memberPackage = memberPackageService.findById(request.getPackageId());
        if (memberPackage == null) {
            log.error("会员套餐不存在，packageId: {}", request.getPackageId());
            return null;
        }
        
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setUserId(userId);
        paymentRecord.setMemberPackageId(request.getPackageId());
        paymentRecord.setAmount(memberPackage.getPrice());
        paymentRecord.setStatus(PayStatusEnum.PENDING.getId()); // 0-待支付
        paymentRecord.setPaymentType(0);
        paymentRecord.setPackageContentSnapshot(JSON.toJSONString(memberPackage));
        paymentRecord.setPointsEarned(memberPackage.getPoints());
        paymentRecord.setCreateTime(LocalDateTime.now());
        paymentRecord.setUpdateTime(LocalDateTime.now());

        int result = paymentRecordMapper.insert(paymentRecord);
        if (result > 0) {
            log.info("创建支付记录成功，id: {}", paymentRecord.getId());
            return paymentRecord;
        }
        log.error("创建支付记录失败");
        return null;
    }

    @Override
    public boolean updatePaymentOutTradeNo(PaymentRecord paymentRecord) {
        log.info("更新支付订单号，id: {}, outTradeNo: {}", paymentRecord.getId(), paymentRecord.getOutTradeNo());
        int result = paymentRecordMapper.updatePaymentOutTradeNo(paymentRecord);
        return result > 0;
    }

    @Override
    public boolean updatePaymentStatus(PaymentRecord paymentRecord) {
        log.info("更新支付状态，id: {}, status: {}", paymentRecord.getId(), paymentRecord.getStatus());
        int result = paymentRecordMapper.updatePaymentStatus(paymentRecord);
        return result > 0;
    }

    @Override
    public boolean updateRefundStatus(PaymentRecord paymentRecord) {
        log.info("更新退款状态，id: {}, status: {}", paymentRecord.getId(), paymentRecord.getStatus());
        int result = paymentRecordMapper.updateRefundStatus(paymentRecord);
        return result > 0;
    }

    @Override
    public int countSuccessfulPaymentsByUserId(Long userId) {
        log.info("统计用户成功支付次数，userId: {}", userId);
        return paymentRecordMapper.countSuccessfulPaymentsByUserId(userId);
    }

    @Override
    public Double getTotalPaymentAmountByUserId(Long userId) {
        log.info("获取用户支付总额，userId: {}", userId);
        return paymentRecordMapper.getTotalPaymentAmountByUserId(userId);
    }


}