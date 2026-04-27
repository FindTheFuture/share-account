package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.dao.ChatMessageMapper;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.entity.ChatMessage;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.response.ChatMessageResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ChatService;
import cn.lizongyi.shareaccount.services.PictureService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private BaseHandler baseHandler;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean sendMessage(Long fromUserId, Long toUserId, Integer type, String content) {
        if (fromUserId == null || toUserId == null || type == null || content == null) {
            log.warn("发送消息参数不完整: fromUserId={}, toUserId={}, type={}", fromUserId, toUserId, type);
            return false;
        }

        ChatMessage message = new ChatMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setType(type);
        message.setContent(content);
        message.setIsRead(ChatMessage.UNREAD);
        message.setIsDeleted(ChatMessage.NOT_DELETED);
        message.setCreateTime(LocalDateTime.now());

        chatMessageMapper.insert(message);
        log.info("发送消息成功: fromUserId={}, toUserId={}, type={}", fromUserId, toUserId, type);
        return true;
    }

    @Override
    public List<ChatMessageResponse> getChatHistory(Long userId1, Long userId2, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<ChatMessage> messages = chatMessageMapper.findChatHistory(userId1, userId2, limit, threeMonthsAgo);
        if (CollectionUtils.isEmpty(messages)) {
            return new ArrayList<>();
        }

        Set<Long> userIds = new HashSet<>();
        for (ChatMessage msg : messages) {
            userIds.add(msg.getFromUserId());
            userIds.add(msg.getToUserId());
        }

        List<User> users = userMapper.findByIds(new ArrayList<>(userIds));
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage msg : messages) {
            ChatMessageResponse response = new ChatMessageResponse();
            response.setId(msg.getId());
            response.setFromUserId(msg.getFromUserId());
            response.setToUserId(msg.getToUserId());
            response.setType(msg.getType());
            response.setContent(msg.getContent());
            response.setIsRead(msg.getIsRead());
            response.setCreateTime(msg.getCreateTime() != null ? msg.getCreateTime().format(FORMATTER) : "");

            User fromUser = userMap.get(msg.getFromUserId());
            if (fromUser != null) {
                response.setFromUserName(fromUser.getNickName());
                response.setFromUserAvatar(getAvatarUrl(fromUser.getId()));
            }

            if (msg.getType() == ChatMessage.TYPE_IMAGE) {
                response.setImageUrl(getImageUrl(msg.getContent()));
            }

            responses.add(response);
        }

        return responses;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return chatMessageMapper.countUnreadByUserId(userId);
    }

    @Override
    public int getUnreadCountBetweenUsers(Long fromUserId, Long toUserId) {
        return chatMessageMapper.countUnreadBetweenUsers(fromUserId, toUserId);
    }

    @Override
    public boolean markAllRead(Long userId) {
        chatMessageMapper.markAllReadByUserId(userId);
        return true;
    }

    @Override
    public boolean markReadBetweenUsers(Long fromUserId, Long toUserId) {
        chatMessageMapper.markReadBetweenUsers(fromUserId, toUserId);
        return true;
    }

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageMapper.findById(messageId);
        if (message == null) {
            log.info("消息不存在: messageId={}", messageId);
            return false;
        }

        if (!message.getFromUserId().equals(userId)) {
            log.info("只能删除自己发送的消息: messageId={}, userId={}", messageId, userId);
            return false;
        }

        chatMessageMapper.markDeleted(messageId, userId);
        log.info("删除消息成功: messageId={}", messageId);
        return true;
    }

    @Override
    public boolean updateInviteStatus(Long messageId, Integer status, Long userId) {
        try {
            ChatMessage message = chatMessageMapper.findById(messageId);
            if (message == null) {
                log.warn("消息不存在: messageId={}", messageId);
                return false;
            }

            if (!message.getToUserId().equals(userId)) {
                log.warn("只有消息接收方才能处理邀请: messageId={}, userId={}", messageId, userId);
                return false;
            }

            if (message.getType() != ChatMessage.TYPE_LEDGER_CARD && message.getType() != ChatMessage.TYPE_BILL_CARD) {
                log.warn("不是卡片消息，无法处理邀请: messageId={}, type={}", messageId, message.getType());
                return false;
            }

            Map<String, Object> contentMap;
            try {
                contentMap = JSONObject.parseObject(message.getContent());
            } catch (Exception e) {
                log.error("解析消息内容失败: content={}", message.getContent(), e);
                return false;
            }

            Object currentStatus = contentMap.get("status");
            if (currentStatus != null && !currentStatus.equals(0)) {
                log.warn("邀请已处理，不能重复处理: messageId={}, status={}", messageId, currentStatus);
                return true;
            }

            contentMap.put("status", status);
            String newContent = JSONObject.toJSONString(contentMap);
            chatMessageMapper.updateContent(messageId, newContent);

            if (status == 1) {
                Long ledgerId = contentMap.get("ledgerId") != null ? ((Number) contentMap.get("ledgerId")).longValue() : null;
                Long billId = contentMap.get("billId") != null ? ((Number) contentMap.get("billId")).longValue() : null;
                Long fromUserId = message.getFromUserId();

                if (billId != null) {
                    if (ledgerId == null) {
                        Bill bill = billMapper.findById(billId);
                        if (bill != null) {
                            ledgerId = bill.getLedgerId();
                        }
                    }
                    if (ledgerId == null) {
                        log.warn("账单关联的账本ID不存在: billId={}", billId);
                        return false;
                    }
                    Member existing = memberMapper.findByFullKey(ledgerId, billId, fromUserId, userId, 1);
                    if (existing == null) {
                        Member member = new Member();
                        member.setLedgerId(ledgerId);
                        member.setBillId(billId);
                        member.setParentUserId(fromUserId);
                        member.setUserId(userId);
                        member.setStatus(1);
                        member.setPercentage(0);
                        member.setName("分享账单");
                        member.setCreateTime(LocalDateTime.now());
                        if (memberMapper.insert(member) > 0) {
                            log.info("账单分享-新增成员成功: ledgerId={}, billId={}, userId={}", ledgerId, billId, userId);
                        }
                    } else {
                        log.info("账单分享-成员已存在: ledgerId={}, billId={}, userId={}", ledgerId, billId, userId);
                    }
                } else if (ledgerId != null) {
                    Member existing = memberMapper.findPendingInvite(ledgerId, fromUserId, userId, 1);
                    if (existing == null) {
                        Member member = new Member();
                        member.setLedgerId(ledgerId);
                        member.setParentUserId(fromUserId);
                        member.setUserId(userId);
                        member.setStatus(1);
                        member.setPercentage(0);
                        member.setName("分享账本");
                        member.setCreateTime(LocalDateTime.now());
                        if (memberMapper.insert(member) > 0) {
                            log.info("账本分享-新增成员成功: ledgerId={}, userId={}", ledgerId, userId);
                        }
                    } else {
                        log.info("账本分享-成员已存在: ledgerId={}, userId={}", ledgerId, userId);
                    }
                }
            }

            log.info("更新邀请状态成功: messageId={}, status={}", messageId, status);
            return true;
        } catch (Exception e) {
            log.error("更新邀请状态失败: messageId={}, status={}", messageId, status, e);
            return false;
        }
    }

    private String getAvatarUrl(Long userId) {
        Picture picture = pictureService.findUserAvatarUrl(userId);
        if(picture == null){
            log.info("根据userId： {}  没有查询到图片", userId);
            return baseHandler.getDefaultUserAvatar(null);
        }
        baseHandler.fillPicPresignUrl(picture);
        return picture.getAddress();
    }

    private String getImageUrl(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            Long pictureId = Long.parseLong(content);
            Picture picture = pictureService.findById(pictureId);
            if (picture != null && picture.getStatus() != 1) {
                baseHandler.fillPicPresignUrl(picture);
                return picture.getAddress();
            }
        } catch (Exception e) {
            log.error("获取图片URL失败: content={}", content, e);
        }
        return null;
    }
}
