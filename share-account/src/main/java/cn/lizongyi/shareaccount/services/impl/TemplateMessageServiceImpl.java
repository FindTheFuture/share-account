package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.dao.UserMessageSubscriptionMapper;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.UserMessageSubscription;
import cn.lizongyi.shareaccount.enums.SendMessageKeyEnum;
import cn.lizongyi.shareaccount.services.TemplateMessageService;
import cn.lizongyi.shareaccount.services.WeChatMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 模板消息服务实现类
 */
@Service
@Slf4j
public class TemplateMessageServiceImpl implements TemplateMessageService {

    @Autowired
    private UserMessageSubscriptionMapper subscriptionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WeChatMessageSender messageSender;

    @Override
    public UserMessageSubscription getUserSubscriptionStatus(Long userId, String templateId) {
        Optional<UserMessageSubscription> subscriptionOpt = subscriptionMapper.selectByUserIdAndTemplateId(userId, templateId);
        
        if (subscriptionOpt.isPresent()) {
            return subscriptionOpt.get();
        }
        
        // 如果不存在，创建默认记录
        UserMessageSubscription subscription = new UserMessageSubscription();
        subscription.setUserId(userId);
        subscription.setTemplateId(templateId);
        
        // 根据模板ID查找对应的枚举ID
        for (SendMessageKeyEnum enumValue : SendMessageKeyEnum.values()) {
            if (enumValue.getTemplateId().equals(templateId)) {
                subscription.setTemplateKeyId(enumValue.getId());
                break;
            }
        }
        
        subscription.setAuthorizeCount(0); // 默认授权次数为0
        subscription.setNeverRemind(false);
        subscription.setCreatedAt(new Date());
        subscription.setUpdatedAt(new Date());
        
        subscriptionMapper.insert(subscription);
        return subscription;
    }

    @Override
    public void updateAuthorizationStatus(Long userId, String templateId, boolean isAuthorized) {
        // 不记录日志，符合需求
        if (isAuthorized) {
            // 授权成功，增加授权次数
            Date authorizedAt = new Date();
            subscriptionMapper.updateAuthorizationStatus(userId, templateId, authorizedAt);
        }
    }

    @Override
    public void setNeverRemind(Long userId, String templateId, boolean neverRemind) {
        subscriptionMapper.updateNeverRemind(userId, templateId, neverRemind);
    }

    @Override
    public boolean canSendMessageToUser(Long userId, String templateId) {
        UserMessageSubscription subscription = getUserSubscriptionStatus(userId, templateId);
        // 如果设置了不再提醒，直接返回false
        if (subscription.getNeverRemind()) {
            return false;
        }
        // 检查授权次数是否大于0
        Integer count = subscription.getAuthorizeCount();
        return count != null && count > 0;
    }

    @Override
    public void batchSendTemplateMessage(String templateId, List<Long> userIds, Map<String, Object> data) {
        for (Long userId : userIds) {
            try {
                // 检查是否可以发送
                if (canSendMessageToUser(userId, templateId)) {
                    User user = userMapper.findById(userId);
                    if (user != null && user.getOpenid() != null) {
                        // 查找对应的枚举
                        for (SendMessageKeyEnum enumValue : SendMessageKeyEnum.values()) {
                            if (enumValue.getTemplateId().equals(templateId)) {
                                // 发送消息
                                messageSender.sendTemplateMessage(user, enumValue, data);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 授权失败后不做错误处理，不记录日志
            }
        }
    }

    @Override
    public List<UserMessageSubscription> getUserSubscriptions(Long userId) {
        return subscriptionMapper.selectByUserId(userId);
    }

    @Override
    public void sendDailyReport(Long userId, Map<String, Object> reportData) {
        try {
            String templateId = SendMessageKeyEnum.DAILY_REPORT_REMINDER.getTemplateId();
            // 检查是否可以发送
            if (canSendMessageToUser(userId, templateId)) {
                User user = userMapper.findById(userId);
                if (user != null && user.getOpenid() != null) {
                    // 发送消息
                    messageSender.sendTemplateMessage(user, SendMessageKeyEnum.DAILY_REPORT_REMINDER, reportData);
                }
            } else {
                log.warn("用户 {} 未授权发送 日报 模板消息 {}，模版消息暂不发送", userId, templateId);
            }
        } catch (Exception e) {
            // 授权失败后不做错误处理，不记录日志
        }
    }

    @Override
    public void sendHourlyReport(Long userId, Map<String, Object> reportData) {
        try {
            String templateId = SendMessageKeyEnum.HOURLY_REPORT_REMINDER.getTemplateId();
            // 检查是否可以发送
            if (canSendMessageToUser(userId, templateId)) {
                User user = userMapper.findById(userId);
                if (user != null && user.getOpenid() != null) {
                    // 发送消息
                    messageSender.sendTemplateMessage(user, SendMessageKeyEnum.HOURLY_REPORT_REMINDER, reportData);
                }
            } else {
                log.warn("用户 {} 未授权发送 小时报 模板消息 {}，模版消息暂不发送", userId, templateId);
            }
        } catch (Exception e) {
            // 授权失败后不做错误处理，不记录日志
        }
    }
}