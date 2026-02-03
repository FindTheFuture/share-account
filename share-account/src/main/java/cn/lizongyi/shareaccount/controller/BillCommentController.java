package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateBillCommentRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.BillCommentResponse;
import cn.lizongyi.shareaccount.services.BillCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/bill")
public class BillCommentController {

    @Autowired
    private BillCommentService billCommentService;

    // 账单评论：按评论ID获取评论详情
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponse<BillCommentResponse.BillCommentResponseItem>> getCommentById(@PathVariable Long commentId) {
        if (commentId == null || commentId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        BillCommentResponse.BillCommentResponseItem item = billCommentService.getById(commentId);
        if (item == null) {
            return ResponseEntity.ok(ApiResponse.error("评论不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // 账单评论：按账单ID获取评论列表（支持分页）
    @GetMapping("/comments/{billId}")
    public ResponseEntity<ApiResponse<BillCommentResponse>> listComments(
            @PathVariable Long billId,
            @RequestParam(name = "pageNum", required = false) Integer pageNum,
            @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        if (billId == null || billId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        BillCommentResponse response = billCommentService.listByBillId(billId, pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 账单评论：创建评论
    @PostMapping("/comment/save")
    public ResponseEntity<ApiResponse<Boolean>> saveComment(@RequestBody CreateBillCommentRequest request) {
        if (request == null || request.getBillId() == null || request.getBillId() <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        try {
            Boolean ok = billCommentService.save(request);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.success(true));
            }
            return ResponseEntity.ok(ApiResponse.error("保存失败"));
        } catch (Exception e) {
            log.error("保存评论异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("保存失败"));
        }
    }

    // 账单评论：删除评论（作者或账单创建人）
    @PostMapping("/comment/delete/{commentId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(@PathVariable Long commentId) {
        if (commentId == null || commentId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        try {
            Boolean ok = billCommentService.deleteById(commentId);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.success(true));
            }
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        } catch (Exception e) {
            log.error("删除评论异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }
}