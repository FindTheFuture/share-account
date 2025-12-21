package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.UserMessageSubscription;
import cn.lizongyi.shareaccount.enums.SendMessageKeyEnum;

import java.util.List;
import java.util.Map;

/**
 * 模板消息服务接口
 */
public interface TemplateMessageService {
    /**
     * 获取用户订阅状态
     */
    UserMessageSubscription getUserSubscriptionStatus(Long userId, String templateId);
    
    /**
     * 更新授权状态
     */
    void updateAuthorizationStatus(Long userId, String templateId, boolean isAuthorized);
    
    /**
     * 设置不再提醒
     */
    void setNeverRemind(Long userId, String templateId, boolean neverRemind);
    
    /**
     * 检查是否可以发送消息给用户
     */
    boolean canSendMessageToUser(Long userId, String templateId);
    
    /**
     * 批量发送模板消息
     */
    void batchSendTemplateMessage(String templateId, List<Long> userIds, Map<String, Object> data);
    
    /**
     * 获取用户所有订阅信息
     */
    List<UserMessageSubscription> getUserSubscriptions(Long userId);
    
    /**
     * 发送日报消息
     */
    void sendDailyReport(Long userId, Map<String, Object> reportData);
    
    /**
     * 发送小时报消息
     */
    void sendHourlyReport(Long userId, Map<String, Object> reportData);
}