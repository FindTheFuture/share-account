package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.ShareConfigResponse;
import cn.lizongyi.shareaccount.response.ThemeConfigResponse;
import cn.lizongyi.shareaccount.services.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/theme")
    public ResponseEntity<ApiResponse<ThemeConfigResponse>> getThemeConfig() {
        try {
            ThemeConfigResponse response = configService.getThemeConfig();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取主题配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.badRequest("获取主题配置失败"));
        }
    }

    @GetMapping("/share")
    public ResponseEntity<ApiResponse<ShareConfigResponse>> getShareConfig() {
        try {
            ShareConfigResponse response = configService.getShareConfig();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取分享配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.badRequest("获取分享配置失败"));
        }
    }
}