package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.ChatHistoryRequest;
import cn.lizongyi.shareaccount.request.SendMessageRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.ChatMessageResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private BaseHandler baseHandler;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Boolean>> sendMessage(@RequestBody SendMessageRequest request) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        if (request.getToUserId() == null || request.getType() == null || request.getContent() == null) {
            return ResponseEntity.ok(ApiResponse.error("参数不完整"));
        }

        boolean success = chatService.sendMessage(userId, request.getToUserId(), request.getType(), request.getContent());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("发送失败"));
        }
    }

    @PostMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(@RequestBody ChatHistoryRequest request) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        if (request.getUserId() == null) {
            return ResponseEntity.ok(ApiResponse.error("用户ID不能为空"));
        }

        List<ChatMessageResponse> history = chatService.getChatHistory(userId, request.getUserId(), request.getLimit());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/unreadCount")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount() {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        int count = chatService.getUnreadCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/unreadCount/{toUserId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCountWithUser(@PathVariable Long toUserId) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        int count = chatService.getUnreadCountBetweenUsers(toUserId, userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/markRead/{fromUserId}")
    public ResponseEntity<ApiResponse<Boolean>> markRead(@PathVariable Long fromUserId) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        boolean success = chatService.markReadBetweenUsers(fromUserId, userId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    @PostMapping("/delete/{messageId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteMessage(@PathVariable Long messageId) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        boolean success = chatService.deleteMessage(messageId, userId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @PostMapping("/updateInviteStatus/{messageId}")
    public ResponseEntity<ApiResponse<Boolean>> updateInviteStatus(@PathVariable Long messageId, @RequestBody Map<String, Integer> body) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        Integer status = body != null ? body.get("status") : null;
        if (status == null || (status != 1 && status != 2)) {
            return ResponseEntity.ok(ApiResponse.error("状态参数不正确"));
        }

        boolean success = chatService.updateInviteStatus(messageId, status, userId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("处理失败"));
        }
    }

}
