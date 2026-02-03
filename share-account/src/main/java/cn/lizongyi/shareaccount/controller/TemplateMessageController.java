package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.UserMessageSubscription;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.TemplateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.enums.SendMessageKeyEnum;
import cn.lizongyi.shareaccount.scheduler.DailyReportScheduler;
import cn.lizongyi.shareaccount.scheduler.HourlyReportScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 模板消息控制器
 */
@RestController
@RequestMapping("/api/template-message")
public class TemplateMessageController {

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private BaseHandler baseHandler;
    
    @Autowired
    private DailyReportScheduler dailyReportScheduler;
    
    @Autowired
    private HourlyReportScheduler hourlyReportScheduler;
    
    /**
     * 获取当前用户的模板消息授权状态
     */
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkAuthorization(@RequestParam String templateId) {
        Long userId = baseHandler.getUserId();
        UserMessageSubscription subscription = templateMessageService.getUserSubscriptionStatus(userId, templateId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("authorizeCount", subscription.getAuthorizeCount());
        result.put("neverRemind", subscription.getNeverRemind());
        result.put("authorizedAt", subscription.getAuthorizedAt());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 更新模板消息授权状态
     */
    @PostMapping("/update-authorization")
    public ApiResponse<Void> updateAuthorizationStatus(@RequestBody Map<String, Object> requestBody) {
        Long userId = baseHandler.getUserId();
        String templateId = (String) requestBody.get("templateId");
        Boolean isAuthorized = (Boolean) requestBody.get("isAuthorized");
        
        if (templateId == null || isAuthorized == null) {
            return ApiResponse.badRequest("缺少必要参数");
        }
        
        templateMessageService.updateAuthorizationStatus(userId, templateId, isAuthorized);
        return ApiResponse.success();
    }
    
    /**
     * 设置不再提醒
     */
    @PostMapping("/set-never-remind")
    public ApiResponse<Void> setNeverRemind(@RequestBody Map<String, Object> requestBody) {
        Long userId = baseHandler.getUserId();
        String templateId = (String) requestBody.get("templateId");
        Boolean neverRemind = (Boolean) requestBody.get("neverRemind");
        
        if (templateId == null || neverRemind == null) {
            return ApiResponse.badRequest("缺少必要参数");
        }
        
        templateMessageService.setNeverRemind(userId, templateId, neverRemind);
        return ApiResponse.success();
    }
    
    /**
     * 获取当前用户的所有订阅信息
     */
    @GetMapping("/subscriptions")
    public ApiResponse<List<UserMessageSubscription>> getUserSubscriptions() {
        Long userId = baseHandler.getUserId();
        List<UserMessageSubscription> subscriptions = templateMessageService.getUserSubscriptions(userId);
        return ApiResponse.success(subscriptions);
    }
    
    /**
     * 获取模板消息配置
     * 提供日报和小时报的模板ID配置
     */
    @GetMapping("/configs")
    public ApiResponse<Map<String, String>> getTemplateConfigs() {
        Map<String, String> templateConfigs = new HashMap<>();

        templateConfigs.put("dailyReport", SendMessageKeyEnum.DAILY_REPORT_REMINDER.getTemplateId());  // 日报模板ID
        templateConfigs.put("hourlyReport", SendMessageKeyEnum.HOURLY_REPORT_REMINDER.getTemplateId());  // 小时报模板ID
        
        return ApiResponse.success(templateConfigs);
    }
    
    /**
     * 测试发送日报消息
     */
    @PostMapping("/send-test-daily")
    public ApiResponse<String> sendTestDailyReport() {
        dailyReportScheduler.generateDailyReports();
        return ApiResponse.success("测试日报发送成功，请检查微信消息");
    }
    
    /**
     * 测试发送小时报消息
     */
    @PostMapping("/send-test-hourly")
    public ApiResponse<String> sendTestHourlyReport() {
        hourlyReportScheduler.generateHourlyReports();
        return ApiResponse.success("测试小时报发送成功，请检查微信消息");
    }
}