package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    boolean sendMessage(Long fromUserId, Long toUserId, Integer type, String content);

    List<ChatMessageResponse> getChatHistory(Long userId1, Long userId2, Integer limit);

    int getUnreadCount(Long userId);

    int getUnreadCountBetweenUsers(Long fromUserId, Long toUserId);

    boolean markAllRead(Long userId);

    boolean markReadBetweenUsers(Long fromUserId, Long toUserId);

    boolean deleteMessage(Long messageId, Long userId);

    boolean updateInviteStatus(Long messageId, Integer status, Long userId);
}
