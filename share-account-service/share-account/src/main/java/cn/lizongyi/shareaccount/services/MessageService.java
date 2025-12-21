package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.request.CreateMessageRequest;
import cn.lizongyi.shareaccount.response.MessageResponse;
import java.util.Map;

import java.util.List;

public interface MessageService {
    /**
     * 保存消息（创建或更新）
     * @param request 消息请求参数
     * @param operatorId 操作人ID
     * @return 保存的消息ID
     */
    Long saveMessage(CreateMessageRequest request, Long operatorId);
    // 获取最新消息（用于通告栏）
    MessageResponse getLatestMessage(Long userId);
    // 获取消息列表（返回分页元数据）
    Map<String, Object> getMessageList(Long userId, Integer type, Integer page, Integer size);
    // 标记消息已读
    void markAsRead(Long userId, Long messageId);
    // 标记所有消息已读
    void markAllAsRead(Long userId, Integer type);
    // 获取未读消息数
    int getUnreadCount(Long userId);
    // 获取消息详情
    MessageResponse getMessageDetail(Long userId, Long messageId);
    // 获取高级消息列表（按消息类型、优先级、创建时间排序）
    Map<String, Object> getAdvancedMessageList(Long userId, Integer pageNum, Integer pageSize);
    // 更新消息状态
    Boolean updateMessageStatus(Long userId, Long messageId, Integer status);
}