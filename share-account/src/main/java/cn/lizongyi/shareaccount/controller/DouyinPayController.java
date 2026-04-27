package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.PaymentRecord;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.request.CreatePaymentOrderRequest;
import cn.lizongyi.shareaccount.request.DouyinPayCreateOrderRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.DouyinPayCreateOrderResponse;
import cn.lizongyi.shareaccount.response.PaymentResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.DouyinPayService;
import cn.lizongyi.shareaccount.services.PaymentRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/douyinPayment")
@Slf4j
public class DouyinPayController {

    @Autowired
    private DouyinPayService douyinPayService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private BaseHandler baseHandler;

    @PostMapping("/createPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@RequestBody CreatePaymentOrderRequest request) {
        log.info("创建抖音支付记录请求: {}", request);
        try {
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            if (request.getPackageId() == null || request.getPackageId() <= 0) {
                return ResponseEntity.ok(ApiResponse.error("套餐ID不能为空"));
            }

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
    public ResponseEntity<ApiResponse<DouyinPayCreateOrderResponse>> getPayInfo(@PathVariable Long paymentId) {
        log.info("获取抖音支付信息 支付记录id:{}", paymentId);
        try {
            PaymentRecord paymentRecord = paymentRecordService.findById(paymentId);
            if (paymentRecord == null) {
                return ResponseEntity.ok(ApiResponse.error("支付记录不存在"));
            }

            User user = userService.findById(paymentRecord.getUserId());
            if (user == null) {
                return ResponseEntity.ok(ApiResponse.error("用户不存在"));
            }

            DouyinPayCreateOrderRequest orderRequest = new DouyinPayCreateOrderRequest();
            orderRequest.setPaymentId(paymentId);
            orderRequest.setUserId(paymentRecord.getUserId());
            orderRequest.setOpenid(user.getOpenid());

            DouyinPayCreateOrderResponse response = douyinPayService.createOrder(orderRequest);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("创建抖音预支付订单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建预支付订单失败，请稍后再试"));
        }
    }

    @GetMapping("/checkstatus/{paymentId}")
    public ResponseEntity<ApiResponse<Boolean>> checkStatus(@PathVariable Long paymentId) {
        log.info("检查抖音订单支付状态id:{}", paymentId);
        try {
            Boolean success = douyinPayService.queryOrderStatus(paymentId);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询订单状态失败，请稍后再试"));
        }
    }

    @GetMapping("/refund/{paymentId}")
    public ResponseEntity<ApiResponse<Boolean>> refund(@PathVariable Long paymentId) {
        log.info("抖音订单退款id:{}", paymentId);
        try {
            boolean success = douyinPayService.refund(paymentId);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("退款失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("退款失败，请稍后再试"));
        }
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(HttpServletRequest request) {
        log.info("收到抖音支付回调通知");
        try {
            String notifyData = request.getParameter("data");
            if (notifyData == null || notifyData.isEmpty()) {
                return ResponseEntity.ok("fail");
            }

            var result = douyinPayService.processNotify(notifyData);
            if (result.getSuccess()) {
                return ResponseEntity.ok("success");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
            }
        } catch (Exception e) {
            log.error("处理抖音支付回调失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

    @Autowired
    private cn.lizongyi.shareaccount.services.UserService userService;
}
