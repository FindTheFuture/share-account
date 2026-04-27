package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.SendSmsRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.SmsService;
import cn.lizongyi.shareaccount.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
@Slf4j
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private IpUtil ipUtil;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendSms(@Valid @RequestBody SendSmsRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = ipUtil.getIpAddr(httpRequest);
            smsService.sendSmsCode(request.getPhone(), ipAddress);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            log.error("发送短信失败: phone={}, error={}", request.getPhone(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }
}