package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.enums.PayStatusEnum;
import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.request.WechatPayCallBackRequest;
import cn.lizongyi.shareaccount.services.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.cipher.PrivacyEncryptor;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.SceneInfo;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import cn.lizongyi.shareaccount.entity.UserMember;
import cn.lizongyi.shareaccount.enums.UserMemberStatusEnum;
import cn.lizongyi.shareaccount.response.MemberPackageResponse;

@Slf4j
@Service
public class WeChatPayServiceImplV3 implements WeChatPayServiceV3 {

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private UserIpService userIpService;

    @Autowired
    private Config config;

    @Resource
    private JsapiServiceExtension jsapiServiceExtension;

    @Resource
    private NotificationParser notificationParser;

    @Resource
    private RefundService refundService;
    
    @Autowired
    private PaymentRecordService paymentRecordService;
    
    @Autowired
    private MemberPackageService memberPackageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMemberService userMemberService;
    
    private static final ObjectMapper jacksonUtils = new ObjectMapper();

    @Override
    public PrepayWithRequestPaymentResponse createPrepayId(Long paymentId) throws Exception {
        log.info("开始创建预支付订单ID，paymentId={}", paymentId);
        if (paymentId == null) {
            log.info("传入的支付记录ID为空");
            return null;
        }

        // 获取支付记录信息
        PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
        if (paymentRecord == null) {
            log.info("找不到对应的支付记录，paymentId={}", paymentId);
            return null;
        }

        User user = userService.findById(paymentRecord.getUserId());
        if (user == null) {
            log.info("找不到对应的用户，userId={}", paymentRecord.getUserId());
            return null;
        }

        boolean isGuest = baseHandler.isGuestUser(paymentRecord.getUserId());
        if (isGuest) {
            log.info("游客模式不允许创建预支付订单");
            return null;
        }

        // 获取会员套餐信息
        MemberPackageResponse memberPackage = memberPackageService.findById(paymentRecord.getMemberPackageId());
        if (memberPackage == null) {
            log.info("找不到对应的会员套餐，me                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       mberPackageId={}", paymentRecord.getMemberPackageId());
            return null;
        }

        String outTradeNo = generateOutTradeNo(paymentId);
        if (outTradeNo == null) {
            log.info("生成微信支付订单号失败，paymentId={}", paymentId);
            return null;
        }

        paymentRecord.setOutTradeNo(outTradeNo);
        boolean isSuccess = paymentRecordService.updatePaymentOutTradeNo(paymentRecord);
        if (!isSuccess) {
            log.info("更新支付记录 outTradeNo 失败，paymentId={}, outTradeNo={}", paymentId, outTradeNo);
            return null;
        }

        PrepayRequest request = new PrepayRequest();
        request.setAppid(baseHandler.getWechatV3Appid());
        request.setMchid(baseHandler.getWechatV3MchId());
        request.setDescription("清甜生活圈子共享账单-订阅会员：" + memberPackage.getName());
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(baseHandler.getWechatV3NotifyUrl());

        // 设置金额 - 微信支付API要求金额单位为分
        Amount amount = new Amount();
        // 将元转换为分，避免精度问题 - 使用BigDecimal的multiply方法
        amount.setTotal(paymentRecord.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        // 设置场景信息
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(userIpService.getUserIp());
        request.setSceneInfo(sceneInfo);

        // 设置支付者信息
        Payer payer = new Payer();
        payer.setOpenid(user.getOpenid());
        request.setPayer(payer);

        log.info("准备发送预支付请求：{}", request);

        PrepayWithRequestPaymentResponse result;
        try {
            result = jsapiServiceExtension.prepayWithRequestPayment(request);
        } catch (ServiceException e) {
            log.error("微信下单服务状态错误，错误信息：{}", e.getErrorMessage());
            throw new RuntimeException("微信下单服务状态错误", e);
        }
        log.info("prepayWithRequestPayment end");
        return result;
    }

    @Override
    public Boolean queryOrderStatus(Long paymentId) throws Exception {
        log.info("查询支付记录状态，paymentId={}", paymentId);
        if (paymentId == null) {
            log.info("传入的支付记录ID为空");
            return false;
        }
        PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
        if (paymentRecord == null) {
            log.info("找不到对应的支付记录，paymentId={}", paymentId);
            return false;
        }

        if (paymentRecord.getStatus() != PayStatusEnum.PENDING.getId()) {
            log.info("支付记录已不是待支付状态，当前状态为：{}", paymentRecord.getStatus());
            return false;
        }

        if (paymentRecord.getStatus() == PayStatusEnum.SUCCESS.getId()) {
            log.info("支付记录已经支付成功，无需再次查询");
            return true;
        }

        try {
            Transaction transaction = queryStatus(paymentRecord.getOutTradeNo());
            // 处理支付状态
            return dealPaymentStatus(transaction, paymentRecord);
        } catch (Exception e) {
            log.error("查询支付状态时发生异常", e);
            throw e;
        }
    }


    @Override
    public Boolean processNotify(HttpServletRequest request) throws Exception {
        log.info("开始处理微信回调通知--------");

        try {
            // 读取请求体、签名和其他必要的头部信息
            String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

            WechatPayCallBackRequest callBackRequest = new WechatPayCallBackRequest();
            callBackRequest.setBody(body);
            callBackRequest.setNonce(request.getHeader("Wechatpay-Nonce"));
            callBackRequest.setSerial(request.getHeader("Wechatpay-Serial"));
            callBackRequest.setSignature(request.getHeader("Wechatpay-Signature"));
            callBackRequest.setSignatureType(request.getHeader("Wechatpay-Signature-Type"));
            callBackRequest.setTimestamp(request.getHeader("Wechatpay-Timestamp"));
            log.info("验签参数{}", jacksonUtils.writeValueAsString(callBackRequest));
            
            // 验签并解析回调数据
            Transaction transaction = analysisSign(callBackRequest, Transaction.class);
            log.info("验签成功！- 回调结果：{}", jacksonUtils.writeValueAsString(transaction));
            
            // 查询最新的交易状态
            transaction = queryStatus(transaction.getOutTradeNo());
            
            // 根据outTradeNo查找支付记录
            PaymentRecord paymentRecord = paymentRecordService.findByOutTradeNo(transaction.getOutTradeNo());
            if (paymentRecord == null) {
                log.info("找不到对应的支付记录，outTradeNo={}", transaction.getOutTradeNo());
                return false;
            }

            // 处理支付状态
            return dealPaymentStatus(transaction, paymentRecord);
        } catch (IOException e) {
            log.error("读取请求体失败", e);
            throw new RuntimeException("读取请求体失败", e);
        } catch (Exception e) {
            log.error("微信回调通知业务处理错误", e);
            throw e;
        }
    }



    @Override
    public Boolean refund(Long paymentId) throws Exception {
        PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
        if (paymentRecord == null) {
            log.info("找不到对应的支付记录，paymentId={}", paymentId);
            return false;
        }
        
        if (paymentRecord.getStatus() != PayStatusEnum.SUCCESS.getId()) {
            log.info("支付记录不是支付成功状态，不用退款，paymentId={}", paymentId);
            return false;
        }

        try {
            // 构建退款请求对象
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(paymentRecord.getOutTradeNo());
            request.setOutRefundNo(generateRefundOutTradeNo(paymentId));
            
            // 设置退款金额 - 微信支付API要求金额单位为分
            com.wechat.pay.java.service.refund.model.AmountReq amount = new com.wechat.pay.java.service.refund.model.AmountReq();
            // 将元转换为分，避免精度问题 - 使用BigDecimal的multiply方法
            amount.setTotal(paymentRecord.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
            amount.setRefund(paymentRecord.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
            amount.setCurrency("CNY");
            request.setAmount(amount);

            int maxRetries = 3; // 最大重试次数
            boolean success = false;
            Refund refund = null;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                log.info("向微信发起退款申请，请求参数={}", jacksonUtils.writeValueAsString(request));
                // 请求API申请退款
                refund = refundService.create(request);
                log.info("退款结果，refundResult={}", jacksonUtils.writeValueAsString(refund));
                success = refund != null && "SUCCESS".equals(refund.getStatus().name());

                if (success) {
                    break; // 如果成功了，跳出循环
                } else {
                    if (attempt < maxRetries) {
                        log.warn("第 {} 次退款尝试失败，准备进行第 {} 次重试.........", attempt, attempt + 1);
                        Thread.sleep(1500); // 等待时间
                    } else {
                        log.error("所有退款尝试均失败。");
                    }
                }
            }

            if (success) {
                log.info("退款成功，开始更新支付记录状态");
                paymentRecord.setRefundTime(LocalDateTime.now());
                paymentRecord.setUpdateTime(LocalDateTime.now());
                paymentRecord.setStatus(PayStatusEnum.REFUNDED.getId()); // 退款成功
                Boolean refundSuccess = paymentRecordService.updateRefundStatus(paymentRecord);
                log.info("支付记录状态更新为已退款 结果：{}", refundSuccess);
                
                return refundSuccess;
            } else {
                log.error("退款最终失败。");
                return false;
            }

        } catch (Exception e) {
            log.error("退款处理失败", e);
            throw e;
        }
    }

    public <T> T analysisSign(WechatPayCallBackRequest wechatPayCallBackRequest, Class<T> clazz) {
        log.info("analysisSign方法");
        PrivacyEncryptor privacyEncryptor = config.createEncryptor();
        String weChatPayCertificateSerialNumber = privacyEncryptor.getWechatpaySerial();
        if (!wechatPayCallBackRequest.getSerial().equals(weChatPayCertificateSerialNumber)) {
            log.error("证书不一致");
            throw new RuntimeException("证书不一致");
        }
        // 构造 RequestParam
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatPayCallBackRequest.getSerial())
                .nonce(wechatPayCallBackRequest.getNonce())
                .signType(wechatPayCallBackRequest.getSignatureType())
                .signature(wechatPayCallBackRequest.getSignature())
                .timestamp(wechatPayCallBackRequest.getTimestamp())
                .body(wechatPayCallBackRequest.getBody())
                .build();
        // 以支付通知回调为例，验签、解密并转换成 Transaction
        return notificationParser.parse(requestParam, clazz);
    }


    public Transaction queryStatus(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(baseHandler.getWechatV3MchId());
        request.setOutTradeNo(outTradeNo);
        try {
            return jsapiServiceExtension.queryOrderByOutTradeNo(request);
        } catch (ServiceException e) {
            log.error("订单查询失败，返回码：{},返回信息：{}", e.getErrorCode(), e.getErrorMessage());
            throw new RuntimeException("订单查询失败", e);
        }
    }



    /**
     * 处理支付记录状态
     */
    private Boolean dealPaymentStatus(Transaction transaction, PaymentRecord paymentRecord){
        // 校验交易状态
        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            if(paymentRecord.getStatus() == PayStatusEnum.SUCCESS.getId()){
                log.info("支付记录状态已经是已支付状态，不需要修改");
                return true;
            }
            
            // 更新支付记录状态为已支付
            paymentRecord.setStatus(PayStatusEnum.SUCCESS.getId());
            paymentRecord.setTransactionId(transaction.getTransactionId());
            paymentRecord.setPayTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            Boolean paid = paymentRecordService.updatePaymentStatus(paymentRecord);
            log.info("支付记录状态更新结果：{}", paid);
            
            if (paid) {
                // 支付成功后，激活会员权益
                activateMemberBenefits(paymentRecord);
            }
            return paid;

        } else if(Transaction.TradeStateEnum.REFUND.equals(transaction.getTradeState())) {
            if(paymentRecord.getStatus() == PayStatusEnum.REFUNDED.getId()){ // 已退款状态
                log.info("支付记录状态已经是已退款状态，不需要修改");
                return true;
            }
            paymentRecord.setRefundTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentRecord.setStatus(PayStatusEnum.REFUNDED.getId()); // 退款成功
            Boolean refundSuccess = paymentRecordService.updateRefundStatus(paymentRecord);
            
            // 更新用户会员记录状态为已退款
            if (refundSuccess) {
                try {
                    boolean updateSuccess = userMemberService.updateStatusByPaymentRecordId(paymentRecord.getId(), UserMemberStatusEnum.REFUNDED.getCode());
                    if (updateSuccess) {
                        log.info("退款成功，已更新用户会员记录状态，paymentRecordId={}", paymentRecord.getId());
                    }
                } catch (Exception e) {
                    log.error("更新用户会员记录状态失败，paymentRecordId={}", paymentRecord.getId(), e);
                }
            }
            log.info("支付记录状态更新为已退款 结果：{}", refundSuccess);
            return refundSuccess;
        } else {
            log.info("查询支付状态 tradeState={}", transaction.getTradeState());
            return false;
        }
    }
    
    /**
     * 激活会员权益
     */
    private void activateMemberBenefits(PaymentRecord paymentRecord) {
        try {
            // 根据支付记录激活会员权益
            Long userId = paymentRecord.getUserId();
            Long memberPackageId = paymentRecord.getMemberPackageId();
            
            // 获取会员套餐信息
            MemberPackageResponse memberPackage = memberPackageService.findById(memberPackageId);
            if (memberPackage == null) {
                log.error("找不到对应的会员套餐，memberPackageId={}", memberPackageId);
                return;
            }
            
            // 创建用户会员记录
            UserMember userMember = new UserMember();
            userMember.setUserId(userId);
            userMember.setPackageId(memberPackageId);
            userMember.setPackageName(memberPackage.getName());
            userMember.setPrice(memberPackage.getPrice());
            userMember.setPackageType(memberPackage.getType());
            userMember.setPaymentRecordId(paymentRecord.getId());
            userMember.setAiCount(memberPackage.getAiCount());
            userMember.setAiUsedCount(0);
            userMember.setPdfCount(memberPackage.getPdfCount());
            userMember.setPdfUsedCount(0);
            userMember.setStartTime(LocalDateTime.now());
            userMember.setEndTime(LocalDateTime.now().plusDays(memberPackage.getDurationDays()));
            userMember.setStatus(UserMemberStatusEnum.NORMAL.getCode()); // 正常状态
            
            // 保存用户会员记录
            userMemberService.createUserMember(userMember);
            log.info("创建用户会员记录成功，userId={}, userMemberId={}", userId, userMember.getId());
            
            // 更新用户状态为付费会员
            userService.activateMember(userId);
        } catch (Exception e) {
            log.error("激活会员权益失败，paymentRecordId={}", paymentRecord.getId(), e);
        }
    }
    

    /**
     * 生成微信订单号
     */
    private String generateOutTradeNo(Long paymentId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "PAY_" + paymentId + "_" + sdf.format(new Date());
    }


    /**
     * 生成退款单号
     */
    private String generateRefundOutTradeNo(Long paymentId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "REFUND_" + paymentId + "_" + sdf.format(new Date());
    }
    


}