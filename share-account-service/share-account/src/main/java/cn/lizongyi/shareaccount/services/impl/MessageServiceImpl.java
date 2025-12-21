package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.MessageMapper;
import cn.lizongyi.shareaccount.dao.UserMessageMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.Message;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.UserMessage;
import cn.lizongyi.shareaccount.request.CreateMessageRequest;
import cn.lizongyi.shareaccount.response.MessageResponse;
import cn.lizongyi.shareaccount.services.MessageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 消息服务实现类
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private UserMessageMapper userMessageMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Long saveMessage(CreateMessageRequest request, Long operatorId) {
        Date now = new Date();
        
        // 如果有ID，则更新消息
        if (request.getId() != null) {
            // 更新消息
            Message message = messageMapper.selectById(request.getId());
            if (message == null) {
                throw new IllegalArgumentException("消息不存在");
            }
            
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setType(request.getType());
            message.setPriority(request.getPriority());
            message.setUpdatedAt(now);
            
            messageMapper.updateById(message);
            return request.getId();
        } else {
            // 创建新消息
            Message message = new Message();
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setType(request.getType());
            message.setPriority(request.getPriority());
            message.setStatus(0); // 0-正常
            message.setCreatedAt(now);
            message.setUpdatedAt(now);
            
            // 插入消息
            messageMapper.insert(message);
            Long messageId = message.getId();
            
            // 根据目标类型处理用户关联
            if ("all".equals(request.getTarget())) {
                // 获取所有用户并创建消息关联
                List<User> allUsers = userMapper.findAll();
                List<Long> allUserIds = new ArrayList<>();
                for (User user : allUsers) {
                    allUserIds.add(user.getId());
                }
                createUserMessageRelations(messageId, allUserIds);
            } else if ("specific".equals(request.getTarget()) && request.getUserIds() != null) {
                // 发送给指定用户
                createUserMessageRelations(messageId, request.getUserIds());
            }
            
            return messageId;
        }
    }

    /**
     * 创建消息与用户的关联关系
     * @param messageId 消息ID
     * @param userIds 用户ID列表
     */
    private void createUserMessageRelations(Long messageId, List<Long> userIds) {
        for (Long userId : userIds) {
            try {
                UserMessage userMessage = new UserMessage();
                userMessage.setMessageId(messageId);
                userMessage.setUserId(userId);
                userMessage.setIsRead(0); // 0-未读
                userMessage.setCreatedAt(new Date());
                userMessageMapper.insert(userMessage);
            } catch (Exception e) {
                log.error("创建用户消息关联失败", e);
            }
        }
    }

    @Override
    public MessageResponse getLatestMessage(Long userId) {
        // 调用mapper查询用户的最新消息
        return messageMapper.selectLatestByUserId(userId);
    }

    @Override
    public Map<String, Object> getMessageList(Long userId, Integer type, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<UserMessage> userMessages = userMessageMapper.selectByUserIdAndType(userId, type, offset, size);
        
        List<MessageResponse> responses = new ArrayList<>();
        for (UserMessage userMessage : userMessages) {
            Message message = messageMapper.selectById(userMessage.getMessageId());
            if (message != null) {
                MessageResponse response = new MessageResponse();
                BeanUtils.copyProperties(message, response);
                response.setIsRead(userMessage.getIsRead());
                response.setReadAt(userMessage.getReadAt());
                responses.add(response);
            }
        }
        
        // 统计总数并构建分页元数据
        int total = userMessageMapper.countByUserIdAndType(userId, type);
        int pages = (int) Math.ceil((double) total / size);
        Map<String, Object> result = new HashMap<>();
        result.put("list", responses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", pages);
        return result;
    }

    @Override
    public void markAsRead(Long userId, Long messageId) {
        userMessageMapper.updateReadStatus(userId, messageId);
    }

    @Override
    public void markAllAsRead(Long userId, Integer type) {
        if (type != null) {
            userMessageMapper.updateAllReadByUserIdAndType(userId, type);
        } else {
            userMessageMapper.updateAllReadByUserId(userId);
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        return userMessageMapper.countUnreadByUserId(userId);
    }

    @Override
    public MessageResponse getMessageDetail(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null) {
            MessageResponse response = new MessageResponse();
            BeanUtils.copyProperties(message, response);
            
            UserMessage userMessage = userMessageMapper.selectByUserIdAndMessageId(userId, messageId);
            if (userMessage != null) {
                response.setIsRead(userMessage.getIsRead());
                response.setReadAt(userMessage.getReadAt());
            }
            
            return response;
        }
        
        return null;
    }

    @Override
    public Map<String, Object> getAdvancedMessageList(Long userId, Integer page, Integer size) {
        // 使用PageHelper进行分页
        PageHelper.startPage(page, size);
        
        // 查询用户的系统消息，按消息类型、优先级、创建时间排序
        List<UserMessage> userMessages = userMessageMapper.selectAdvancedByUserId(userId);
        
        // 使用PageInfo获取分页信息
        PageInfo<UserMessage> pageInfo = new PageInfo<>(userMessages);
        
        List<MessageResponse> responses = new ArrayList<>();
        for (UserMessage userMessage : userMessages) {
            Message message = messageMapper.selectById(userMessage.getMessageId());
            if (message != null && message.getStatus() == 0) { // 只显示正常状态的消息
                MessageResponse response = new MessageResponse();
                BeanUtils.copyProperties(message, response);
                response.setIsRead(userMessage.getIsRead());
                response.setReadAt(userMessage.getReadAt());
                responses.add(response);
            }
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", responses);
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("size", pageInfo.getPageSize());
        result.put("pages", pageInfo.getPages());
        
        return result;
    }

    @Override
    public Boolean updateMessageStatus(Long userId, Long messageId, Integer status) {
        // 验证消息是否存在且与用户相关
        UserMessage userMessage = userMessageMapper.selectByUserIdAndMessageId(userId, messageId);
        if (userMessage == null) {
            throw new IllegalArgumentException("消息不存在或无权操作");
        }
        
        // 更新消息状态
        Message message = messageMapper.selectById(messageId);
        if (message != null) {
            message.setStatus(status);
            message.setUpdatedAt(new Date());
            return messageMapper.updateById(message) > 0;
        }
        
        return true;
    }
}