package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.request.CreatePaymentOrderRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.PaymentResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.PaymentRecordService;
import cn.lizongyi.shareaccount.services.WeChatPayServiceV3;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/v3")
@Slf4j
public class PayControllerV3 {

    @Autowired
    private WeChatPayServiceV3 weChatPayServiceV3;
    
    @Autowired
    private PaymentRecordService paymentRecordService;
    
    @Autowired
    private BaseHandler baseHandler;

    /**
     * 创建支付记录
     */
    @PostMapping("/createPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@RequestBody CreatePaymentOrderRequest request) {
        log.info("创建支付记录请求: {}", request);
        try {
            // 获取当前用户
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }
            
            // 验证套餐ID
            if (request.getPackageId() == null || request.getPackageId() <= 0) {
                return ResponseEntity.ok(ApiResponse.error("套餐ID不能为空"));
            }
            
            // 保存支付记录
            PaymentRecord paymentRecord = paymentRecordService.createPaymentRecord(request);
            if (paymentRecord.getId() == null) {
                return ResponseEntity.ok(ApiResponse.error("创建支付记录失败"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(new PaymentResponse(paymentRecord.getId())));
        } catch (Exception e) {
            log.error("创建支付记录异常: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("创建支付记录失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/payinfo/{paymentId}")
    public ResponseEntity<ApiResponse<Object>> getPayInfo(@PathVariable(value = "paymentId") Long paymentId) {
        log.info("获取支付信息 支付记录id:{}", paymentId);
        try {
            PrepayWithRequestPaymentResponse payParams = weChatPayServiceV3.createPrepayId(paymentId);
            return ResponseEntity.ok(ApiResponse.success(payParams));
        } catch (Exception e) {
            log.error("创建预支付订单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("创建预支付订单失败，请稍后再试"));
        }
    }

    @GetMapping("/checkstatus/{paymentId}")
    public ResponseEntity<ApiResponse<Object>> checkStatus(@PathVariable(value = "paymentId") Long paymentId) {
        log.info("检查订单支付状态id:{}", paymentId);
        try {
            Boolean success = weChatPayServiceV3.queryOrderStatus(paymentId);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error("查询订单状态失败，请稍后再试"));
        }
    }

    @GetMapping("/refund/{paymentId}")
    public ResponseEntity<ApiResponse<Boolean>> refund(@PathVariable Long paymentId) {
        log.info("订单退款id:{}", paymentId);

        try {
            boolean success = weChatPayServiceV3.refund(paymentId);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("退款失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error("退款失败，请稍后再试"));
        }
    }

    /**
     * 支付结果通知接口
     */
    @PostMapping("/notify")
    public ResponseEntity<String> notify(HttpServletRequest request) throws Exception {
        // 验证签名并处理业务逻辑
        boolean isValid = weChatPayServiceV3.processNotify(request);

        if (isValid) {
            // 返回给微信服务器确认已接收到通知
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }
    
    
    
    
}