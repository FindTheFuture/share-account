package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.constants.Constants;
import cn.lizongyi.shareaccount.enums.PayStatusEnum;
import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.request.DouyinPayConfig;
import cn.lizongyi.shareaccount.request.DouyinPayCreateOrderRequest;
import cn.lizongyi.shareaccount.response.DouyinPayCreateOrderResponse;
import cn.lizongyi.shareaccount.response.DouyinPayNotifyResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.DouyinPayService;
import cn.lizongyi.shareaccount.services.PaymentRecordService;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.services.MemberPackageService;
import cn.lizongyi.shareaccount.services.UserService;
import cn.lizongyi.shareaccount.services.UserMemberService;
import cn.lizongyi.shareaccount.entity.UserMember;
import cn.lizongyi.shareaccount.enums.UserMemberStatusEnum;
import cn.lizongyi.shareaccount.response.MemberPackageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DouyinPayServiceImpl implements DouyinPayService {

    private static final String DOUYIN_PAY_API_HOST = "https://pay.douyin.com";

    private static final String DOUYIN_PAY_PUBLIC_KEY_PATH = "douyin/douyin_pay_public_key.pem";

    private static final String DOUYIN_MERCHANT_PRIVATE_KEY_PATH = "douyin/merchant_private_key.pem";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMemberService userMemberService;

    @Autowired
    private MemberPackageService memberPackageService;

    @Autowired
    private RestTemplate douyinRestTemplate;

    @Override
    public DouyinPayCreateOrderResponse createOrder(DouyinPayCreateOrderRequest request) throws Exception {
        log.info("========== 抖音支付 - 创建订单开始 ==========");
        log.info("paymentId: {}", request.getPaymentId());

        DouyinPayConfig config = baseHandler.getDouyinPayConfigBean();
        if (config == null) {
            log.error("抖音支付配置未设置，请检查 config 表中 key={} 的配置", Constants.DOUYIN_PAY_CONFIG);
            throw new RuntimeException("抖音支付配置未设置");
        }

        log.info("抖音支付配置加载成功:");
        log.info("  - appId: {}", config.getAppId());
        log.info("  - merchantId: {}", config.getMerchantId());
        log.info("  - notifyUrl: {}", config.getNotifyUrl());
        log.info("  - privateKeyPath: {}", DOUYIN_MERCHANT_PRIVATE_KEY_PATH);

        log.info("========== 创建支付记录 ==========");

        PaymentRecord paymentRecord = paymentRecordService.findById(request.getPaymentId());
        if (paymentRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }

        String outTradeNo = generateOutTradeNo(request.getPaymentId());
        paymentRecord.setOutTradeNo(outTradeNo);
        paymentRecord.setUpdateTime(LocalDateTime.now());
        paymentRecordService.updatePaymentOutTradeNo(paymentRecord);

        User user = userService.findById(paymentRecord.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        MemberPackageResponse memberPackage = memberPackageService.findById(paymentRecord.getMemberPackageId());
        if (memberPackage == null) {
            throw new RuntimeException("会员套餐不存在");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", config.getAppId());
        params.put("app_secret", config.getAppSecret());
        params.put("out_order_no", outTradeNo);
        params.put("total_amount", String.valueOf(paymentRecord.getAmount().multiply(BigDecimal.valueOf(100)).intValue()));
        params.put("subject", memberPackage.getName());
        params.put("body", memberPackage.getName() + "订阅服务");
        params.put("valid_time", "1800");
        params.put("notify_url", config.getNotifyUrl());

        if (request.getOpenid() != null && !request.getOpenid().isEmpty()) {
            params.put("openid", request.getOpenid());
        }

        log.info("========== 生成签名 ==========");
        log.info("签名参数: {}", params);

        String privateKey = loadPrivateKeyFromFile(DOUYIN_MERCHANT_PRIVATE_KEY_PATH);
        String sign = generateSign(params, privateKey);
        params.put("sign", sign);
        params.put("sign_type", config.getSignType());
        log.info("签名生成完成，sign: {}", sign);

        log.info("========== 发送支付请求 ==========");
        log.info("请求地址: {}{}", DOUYIN_PAY_API_HOST, "/api/trade/create");
        log.info("请求参数: {}", params);

        String response = sendPostRequest("/api/trade/create", params);
        log.info("抖音支付下单响应: {}", response);

        return parseCreateOrderResponse(response, outTradeNo);
    }

    @Override
    public Boolean queryOrderStatus(Long paymentId) throws Exception {
        log.info("查询抖音支付订单状态，paymentId={}", paymentId);

        PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
        if (paymentRecord == null) {
            log.info("支付记录不存在，paymentId={}", paymentId);
            return false;
        }

        if (paymentRecord.getStatus() != PayStatusEnum.PENDING.getId()) {
            log.info("支付记录已不是待支付状态，当前状态: {}", paymentRecord.getStatus());
            return paymentRecord.getStatus() == PayStatusEnum.SUCCESS.getId();
        }

        DouyinPayConfig config = baseHandler.getDouyinPayConfigBean();
        if (config == null) {
            throw new RuntimeException("抖音支付配置未设置");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", config.getAppId());
        params.put("app_secret", config.getAppSecret());
        params.put("out_order_no", paymentRecord.getOutTradeNo());

        String privateKey = loadPrivateKeyFromFile(DOUYIN_MERCHANT_PRIVATE_KEY_PATH);
        String sign = generateSign(params, privateKey);
        params.put("sign", sign);
        params.put("sign_type", config.getSignType());

        String response = sendPostRequest("/api/trade/query", params);
        log.info("抖音支付查询响应: {}", response);

        return dealQueryOrderStatus(response, paymentRecord);
    }

    @Override
    public Boolean refund(Long paymentId) throws Exception {
        log.info("抖音支付退款开始，paymentId={}", paymentId);

        PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
        if (paymentRecord == null) {
            log.info("支付记录不存在，paymentId={}", paymentId);
            return false;
        }

        if (paymentRecord.getStatus() != PayStatusEnum.SUCCESS.getId()) {
            log.info("支付记录不是支付成功状态，不用退款，paymentId={}", paymentId);
            return false;
        }

        DouyinPayConfig config = baseHandler.getDouyinPayConfigBean();
        if (config == null) {
            throw new RuntimeException("抖音支付配置未设置");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", config.getAppId());
        params.put("app_secret", config.getAppSecret());
        params.put("out_order_no", paymentRecord.getOutTradeNo());
        params.put("out_refund_no", generateRefundOutTradeNo(paymentId));
        params.put("refund_amount", String.valueOf(paymentRecord.getAmount().multiply(BigDecimal.valueOf(100)).intValue()));
        params.put("notify_url", config.getNotifyUrl());

        String privateKey = loadPrivateKeyFromFile(DOUYIN_MERCHANT_PRIVATE_KEY_PATH);
        String sign = generateSign(params, privateKey);
        params.put("sign", sign);
        params.put("sign_type", config.getSignType());

        String response = sendPostRequest("/api/trade/refund", params);
        log.info("抖音支付退款响应: {}", response);

        return dealRefundStatus(response, paymentRecord);
    }

    @Override
    public DouyinPayNotifyResponse processNotify(String notifyData) throws Exception {
        log.info("========== 处理抖音支付回调通知 ==========");
        log.info("回调数据: {}", notifyData);

        DouyinPayConfig config = baseHandler.getDouyinPayConfigBean();
        if (config == null) {
            throw new RuntimeException("抖音支付配置未设置");
        }

        JsonNode jsonNode = objectMapper.readTree(notifyData);
        JsonNode dataNode = jsonNode.get("data");
        if (dataNode == null) {
            log.error("无效的回调数据");
            return new DouyinPayNotifyResponse(false, "无效的回调数据", null, null, null);
        }

        String orderId = dataNode.has("order_id") ? dataNode.get("order_id").asText() : null;
        String outOrderNo = dataNode.has("out_order_no") ? dataNode.get("out_order_no").asText() : null;
        String tradeNo = dataNode.has("trade_no") ? dataNode.get("trade_no").asText() : null;
        String tradeStatus = dataNode.has("trade_status") ? dataNode.get("trade_status").asText() : null;
        String sign = dataNode.has("sign") ? dataNode.get("sign").asText() : null;

        log.info("回调解析结果:");
        log.info("  - orderId: {}", orderId);
        log.info("  - outOrderNo: {}", outOrderNo);
        log.info("  - tradeNo: {}", tradeNo);
        log.info("  - tradeStatus: {}", tradeStatus);
        log.info("  - sign: {}", sign);

        if (outOrderNo == null) {
            log.error("缺少订单号");
            return new DouyinPayNotifyResponse(false, "缺少订单号", null, null, null);
        }

        PaymentRecord paymentRecord = paymentRecordService.findByOutTradeNo(outOrderNo);
        if (paymentRecord == null) {
            log.info("找不到对应的支付记录，outOrderNo={}", outOrderNo);
            return new DouyinPayNotifyResponse(false, "支付记录不存在", null, null, null);
        }

        log.info("========== 验证回调签名 ==========");
        String publicKey = loadPublicKeyFromFile(DOUYIN_PAY_PUBLIC_KEY_PATH);
        boolean signVerified = verifySign(dataNode, sign, publicKey);
        log.info("签名验证结果: {}", signVerified);

        if (!signVerified) {
            log.error("回调签名验证失败");
            return new DouyinPayNotifyResponse(false, "签名验证失败", orderId, tradeNo, tradeStatus);
        }

        log.info("签名验证通过");

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            paymentRecord.setStatus(PayStatusEnum.SUCCESS.getId());
            paymentRecord.setTransactionId(tradeNo);
            paymentRecord.setPayTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentRecordService.updatePaymentStatus(paymentRecord);

            activateMemberBenefits(paymentRecord);

            log.info("支付成功处理完成");
            return new DouyinPayNotifyResponse(true, "处理成功", orderId, tradeNo, tradeStatus);
        } else if ("TRADE_REFUND".equals(tradeStatus)) {
            paymentRecord.setRefundTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentRecord.setStatus(PayStatusEnum.REFUNDED.getId());
            paymentRecordService.updateRefundStatus(paymentRecord);

            userMemberService.updateStatusByPaymentRecordId(paymentRecord.getId(), UserMemberStatusEnum.REFUNDED.getCode());

            log.info("退款处理完成");
            return new DouyinPayNotifyResponse(true, "退款处理成功", orderId, tradeNo, tradeStatus);
        }

        log.info("收到其他状态回调，tradeStatus={}", tradeStatus);
        return new DouyinPayNotifyResponse(true, "已接收", orderId, tradeNo, tradeStatus);
    }

    private DouyinPayCreateOrderResponse parseCreateOrderResponse(String response, String outTradeNo) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode dataNode = jsonNode.get("data");

        DouyinPayCreateOrderResponse result = new DouyinPayCreateOrderResponse();
        result.setOrderId(outTradeNo);

        if (dataNode != null) {
            result.setTradeNo(dataNode.has("trade_no") ? dataNode.get("trade_no").asText() : null);
            result.setPaymentUrl(dataNode.has("payment_url") ? dataNode.get("payment_url").asText() : null);
            if (dataNode.has("expire_time")) {
                result.setExpireTime(dataNode.get("expire_time").asLong());
            }
        }

        return result;
    }

    private Boolean dealQueryOrderStatus(String response, PaymentRecord paymentRecord) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode dataNode = jsonNode.get("data");

        if (dataNode == null) {
            return false;
        }

        String tradeStatus = dataNode.has("trade_status") ? dataNode.get("trade_status").asText() : null;

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            String tradeNo = dataNode.has("trade_no") ? dataNode.get("trade_no").asText() : null;

            paymentRecord.setStatus(PayStatusEnum.SUCCESS.getId());
            paymentRecord.setTransactionId(tradeNo);
            paymentRecord.setPayTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentRecordService.updatePaymentStatus(paymentRecord);

            activateMemberBenefits(paymentRecord);

            return true;
        }

        return false;
    }

    private Boolean dealRefundStatus(String response, PaymentRecord paymentRecord) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode dataNode = jsonNode.get("data");

        if (dataNode == null) {
            return false;
        }

        String refundStatus = dataNode.has("refund_status") ? dataNode.get("refund_status").asText() : null;

        if ("TRADE_REFUND".equals(refundStatus)) {
            paymentRecord.setRefundTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            paymentRecord.setStatus(PayStatusEnum.REFUNDED.getId());
            paymentRecordService.updateRefundStatus(paymentRecord);

            userMemberService.updateStatusByPaymentRecordId(paymentRecord.getId(), UserMemberStatusEnum.REFUNDED.getCode());

            return true;
        }

        return false;
    }

    private void activateMemberBenefits(PaymentRecord paymentRecord) {
        try {
            Long userId = paymentRecord.getUserId();
            Long memberPackageId = paymentRecord.getMemberPackageId();

            MemberPackageResponse memberPackage = memberPackageService.findById(memberPackageId);
            if (memberPackage == null) {
                log.error("找不到对应的会员套餐，memberPackageId={}", memberPackageId);
                return;
            }

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
            userMember.setStatus(UserMemberStatusEnum.NORMAL.getCode());

            userMemberService.createUserMember(userMember);
            log.info("创建用户会员记录成功，userId={}, userMemberId={}", userId, userMember.getId());

            userService.activateMember(userId);
        } catch (Exception e) {
            log.error("激活会员权益失败，paymentRecordId={}", paymentRecord.getId(), e);
        }
    }

    private String generateOutTradeNo(Long paymentId) {
        return "DY_" + paymentId + "_" + System.currentTimeMillis();
    }

    private String generateRefundOutTradeNo(Long paymentId) {
        return "REFUND_" + paymentId + "_" + System.currentTimeMillis();
    }

    private String generateSign(Map<String, String> params, String privateKey) throws Exception {
        StringBuilder signContent = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (signContent.length() > 0) {
                signContent.append("&");
            }
            signContent.append(entry.getKey()).append("=").append(entry.getValue());
        }

        log.info("签名内容: {}", signContent.toString());

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(loadPrivateKey(privateKey));
        signature.update(signContent.toString().getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private boolean verifySign(JsonNode dataNode, String sign, String publicKey) throws Exception {
        if (sign == null || sign.isEmpty()) {
            log.warn("签名为空，跳过验证");
            return true;
        }

        Set<String> signFields = new TreeSet<>();
        signFields.add("order_id");
        signFields.add("out_order_no");
        signFields.add("trade_no");
        signFields.add("trade_status");
        signFields.add("total_amount");
        signFields.add("refund_amount");

        StringBuilder signContent = new StringBuilder();
        for (String field : signFields) {
            if (dataNode.has(field)) {
                if (signContent.length() > 0) {
                    signContent.append("&");
                }
                signContent.append(field).append("=").append(dataNode.get(field).asText());
            }
        }

        log.info("验签内容: {}", signContent.toString());

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(loadPublicKey(publicKey));
        signature.update(signContent.toString().getBytes(StandardCharsets.UTF_8));

        return signature.verify(Base64.getDecoder().decode(sign));
    }

    private PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        String filteredKey = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(filteredKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                IOUtils.toInputStream(publicKeyStr, StandardCharsets.UTF_8)
        );
        return certificate.getPublicKey();
    }

    private String loadPrivateKeyFromFile(String privateKeyPath) throws Exception {
        log.info("从文件加载商家私钥，路径: {}", privateKeyPath);
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(privateKeyPath);
            if (!resource.exists()) {
                log.error("商家私钥文件不存在: {}", privateKeyPath);
                throw new RuntimeException("商家私钥文件不存在: " + privateKeyPath);
            }
            String privateKey = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("商家私钥文件加载成功");
            return privateKey;
        } catch (Exception e) {
            log.error("加载商家私钥文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("加载商家私钥文件失败: " + e.getMessage(), e);
        }
    }

    private String loadPublicKeyFromFile(String publicKeyPath) throws Exception {
        log.info("从文件加载抖音支付公钥，路径: {}", publicKeyPath);
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(publicKeyPath);
            if (!resource.exists()) {
                log.error("抖音支付公钥文件不存在: {}", publicKeyPath);
                throw new RuntimeException("抖音支付公钥文件不存在: " + publicKeyPath);
            }
            String publicKey = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("抖音支付公钥文件加载成功");
            return publicKey;
        } catch (Exception e) {
            log.error("加载抖音支付公钥文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("加载抖音支付公钥文件失败: " + e.getMessage(), e);
        }
    }

    private String sendPostRequest(String path, Map<String, String> params) throws Exception {
        log.info("========== 发送HTTP请求 ==========");
        log.info("请求路径: {}", path);
        log.info("请求参数: {}", params);

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(params);

        try {
            String response = douyinRestTemplate.postForObject(DOUYIN_PAY_API_HOST + path, multiValueMap, String.class);
            log.info("HTTP请求响应: {}", response);
            return response;
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}
