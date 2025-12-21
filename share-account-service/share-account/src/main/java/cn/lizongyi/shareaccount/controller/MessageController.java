package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateMessageRequest;
import cn.lizongyi.shareaccount.response.MessageResponse;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.lizongyi.shareaccount.services.BaseHandler;

import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private BaseHandler baseHandler;
    
    // 保存消息（创建或更新，管理员接口）
    @PostMapping
    public ApiResponse<Long> saveMessage(@RequestBody CreateMessageRequest request) {
        Long operatorId = getCurrentUserId();
        Long messageId = messageService.saveMessage(request, operatorId);
        return ApiResponse.success(messageId);
    }

    // 获取最新消息（用于通告栏）
    @GetMapping("/latest")
    public ApiResponse<MessageResponse> getLatestMessage() {
        Long userId = getCurrentUserId();
        MessageResponse message = messageService.getLatestMessage(userId);
        return ApiResponse.success(message);
    }

    // 获取消息列表（返回分页元数据）
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getMessageList(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = messageService.getMessageList(userId, type, page, size);
        return ApiResponse.success(result);
    }
    
    // 获取高级消息列表（按消息类型、优先级、创建时间排序）
    @GetMapping("/advancedList")
    public ApiResponse<Map<String, Object>> getAdvancedMessageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = messageService.getAdvancedMessageList(userId, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    // 更新消息状态
    @PutMapping("/updateStatus")
    public ApiResponse<Void> updateMessageStatus(@RequestBody Map<String, Object> requestBody) {
        Long userId = getCurrentUserId();
        
        // 参数校验
        if (requestBody == null || !requestBody.containsKey("id") || !requestBody.containsKey("status")) {
            return ApiResponse.badRequest("缺少必需参数：id和status");
        }
        
        try {
            // 从请求体中获取id和status参数
            Long id = Long.parseLong(requestBody.get("id").toString());
            Integer status = Integer.parseInt(requestBody.get("status").toString());
            
            // 校验参数有效性
            if (id <= 0 || status == null) {
                return ApiResponse.badRequest("参数无效：id必须为正数，status不能为空");
            }
            
            Boolean success = messageService.updateMessageStatus(userId, id, status);
            if (success) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error("更新消息状态失败");
            }
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("参数格式错误：id必须为数字，status必须为整数");
        } catch (Exception e) {
            return ApiResponse.error("更新消息状态失败：" + e.getMessage());
        }
    }

    // 标记消息已读
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable("id") Long messageId) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(userId, messageId);
        return ApiResponse.success();
    }

    // 标记所有消息已读
    @PostMapping("/all/read")
    public ApiResponse<Void> markAllAsRead(@RequestBody(required = false) Map<String, Object> requestBody) {
        Long userId = getCurrentUserId();
        Integer type = null;
        if (requestBody != null && requestBody.containsKey("type")) {
            Object typeObj = requestBody.get("type");
            if (typeObj != null) {
                type = Integer.parseInt(typeObj.toString());
            }
        }
        messageService.markAllAsRead(userId, type);
        return ApiResponse.success();
    }

    // 获取未读消息数
    @GetMapping("/unread/count")
    public ApiResponse<Integer> getUnreadCount() {
        Long userId = getCurrentUserId();
        int count = messageService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

    // 获取消息详情
    @GetMapping("/{id}")
    public ApiResponse<MessageResponse> getMessageDetail(@PathVariable("id") Long messageId) {
        Long userId = getCurrentUserId();
        MessageResponse message = messageService.getMessageDetail(userId, messageId);
        // 自动标记为已读
        if (message != null && message.getIsRead() == 0) {
            messageService.markAsRead(userId, messageId);
            message.setIsRead(1);
        }
        return ApiResponse.success(message);
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        return baseHandler.getUserId();
    }
}