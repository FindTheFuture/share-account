package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.UserMemberService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.lizongyi.shareaccount.response.UserMemberQueryResponse;
import cn.lizongyi.shareaccount.response.UserMemberResponse;


/**
 * 用户会员控制器
 */
@RestController
@RequestMapping("/api/user/member")
@Slf4j
public class UserMemberController {

    @Autowired
    private UserMemberService userMemberService;

    @Autowired
    private BaseHandler baseHandler;

 

    /**
     * 分页查询用户会员记录
     * @return 查询结果，包含会员列表、分页信息、AI识别剩余次数和PDF导出剩余次数
     */
    @GetMapping("/queryUserMembers")
    public ResponseEntity<ApiResponse<UserMemberQueryResponse>> queryUserMembers(@RequestParam(defaultValue = "1") Integer currentPage, 
                                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                                              @RequestParam(required = false) Integer status) {
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 参数验证
            if (currentPage < 1 || pageSize < 1) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }
            
            // 调用service层进行分页查询
            PageInfo<UserMemberResponse> pageInfo = userMemberService.queryUserMembersWithPage(userId, currentPage, pageSize, status);
            
            // 获取AI识别剩余次数
            Integer remainingAiCount = userMemberService.getRemainingAiCount(userId);
            
            // 获取PDF导出剩余次数
            Integer remainingPdfCount = userMemberService.getRemainingPdfCount(userId);
            
            // 构建完整响应数据
            UserMemberQueryResponse response = new UserMemberQueryResponse(pageInfo, remainingAiCount, remainingPdfCount);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("查询用户会员记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 获取AI识别剩余次数
     * @return AI识别剩余次数
     */
    @GetMapping("/getRemainingAiCount")
    public ResponseEntity<ApiResponse<Integer>> getRemainingAiCount() {
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 获取AI识别剩余次数
            Integer remainingAiCount = userMemberService.getRemainingAiCount(userId);
            
            return ResponseEntity.ok(ApiResponse.success(remainingAiCount));
        } catch (Exception e) {
            log.error("获取AI识别剩余次数失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }


    /**
     * 获取AI识别剩余次数
     * @return AI识别剩余次数
     */
    @GetMapping("/getRemainingPdfCount")
    public ResponseEntity<ApiResponse<Integer>> getRemainingPdfCount() {
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 获取PDF导出剩余次数
            Integer remainingPdfCount = userMemberService.getRemainingPdfCount(userId);
            
            return ResponseEntity.ok(ApiResponse.success(remainingPdfCount));
        } catch (Exception e) {
            log.error("获取PDF导出剩余次数失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }
}