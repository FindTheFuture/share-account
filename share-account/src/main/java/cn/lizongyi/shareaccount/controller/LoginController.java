package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.LoginRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        if(request == null || !StringUtils.hasText(request.getCode())) {
            log.error("微信登录失败 没有code");
            return ResponseEntity.ok(ApiResponse.badRequest("登录失败，请稍后再试"));
        }
        try {
            Map<String, Object> loginResult = loginService.wechatLogin(request.getCode());
            return ResponseEntity.ok(ApiResponse.success(loginResult));
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("登录失败，请稍后再试"));
        }
    }


    @PostMapping("/refreshToken")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestBody LoginRequest request) {
        if(request == null || !StringUtils.hasText(request.getRefreshToken())) {
            log.error("微信刷新token失败 没有refreshToken");
            return ResponseEntity.ok(ApiResponse.badRequest("操作失败，请稍后再试"));
        }

        try {
            Map<String, Object> refreshResult = loginService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(refreshResult));
        } catch (Exception e) {
            log.error("刷新token失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest("刷新token失败，请稍后再试"));
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> guestLogin() {
        try {
            Map<String, Object> loginResult = loginService.guestLogin();
            return ResponseEntity.ok(ApiResponse.success(loginResult));
        } catch (Exception e) {
            log.error("游客登录失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upgrade(@RequestBody LoginRequest request) {
        if (request == null || !org.springframework.util.StringUtils.hasText(request.getCode())) {
            log.error("游客升级失败 没有code");
            return ResponseEntity.ok(ApiResponse.badRequest("升级失败，请稍后再试"));
        }
        try {
            Map<String, Object> result = loginService.upgradeGuest(request.getCode());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("游客升级失败", e);
            return ResponseEntity.ok(ApiResponse.badRequest(e.getMessage()));
        }
    }
}