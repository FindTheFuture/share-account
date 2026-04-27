package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.AddContactRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.ContactResponse;
import cn.lizongyi.shareaccount.response.FriendRequestResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contact")
@Slf4j
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private BaseHandler baseHandler;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getContactList() {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        List<ContactResponse> list = contactService.getContactList(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests() {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        List<FriendRequestResponse> list = contactService.getPendingRequests(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/requestCount")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getRequestCount() {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        int count = contactService.getPendingRequestCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Boolean>> addContact(@RequestBody AddContactRequest request) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("手机号不能为空"));
        }

        ContactService.AddContactResult result = contactService.addContact(userId, request.getPhone().trim());
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error(result.getErrorMessage()));
        }
    }

    @PostMapping("/accept/{contactId}")
    public ResponseEntity<ApiResponse<Boolean>> acceptContact(@PathVariable Long contactId) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        boolean success = contactService.acceptContact(contactId, userId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("同意失败"));
        }
    }

    @PostMapping("/reject/{contactId}")
    public ResponseEntity<ApiResponse<Boolean>> rejectContact(@PathVariable Long contactId) {
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }

        boolean success = contactService.rejectContact(contactId, userId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("拒绝失败"));
        }
    }
}
