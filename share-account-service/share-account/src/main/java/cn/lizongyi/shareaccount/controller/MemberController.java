package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.MemberResponse;
import cn.lizongyi.shareaccount.request.MemberRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.MemberService;
import cn.lizongyi.shareaccount.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BaseHandler baseHandler;

    /**
     * 根据ID获取成员信息
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getById(@PathVariable Long id) {
        log.info("根据ID获取成员信息请求: {}", id);
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }

            MemberResponse member = memberService.getMemberById(id);
            if (member == null) {
                return ResponseEntity.ok(ApiResponse.badRequest("未找到成员信息"));
            }

            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (Exception e) {
            log.error("获取成员信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取成员信息失败: " + e.getMessage()));
        }
    }

    /**
     * 根据账本ID获取成员列表
     */
    @GetMapping("/getByLedgerId/{ledgerId}")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getByLedgerId(@PathVariable Long ledgerId) {
        log.info("根据账本ID获取成员列表请求: {}", ledgerId);
        try {
            if (ledgerId == null || ledgerId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }

            List<MemberResponse> members = memberService.getMembersByLedgerId(ledgerId);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取成员列表失败: " + e.getMessage()));
        }
    }

    /**
     * 新增或更新成员
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Long>> saveMember(@RequestBody MemberRequest request) {
        log.info("新增或更新成员请求: {}", JacksonUtils.objToStr(request));
        try {
            // 获取当前登录用户ID
            Long currentUserId = baseHandler.getUserId();
            if (currentUserId == null || currentUserId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            // 调用service层方法处理保存逻辑
            Long memberId = memberService.saveMember(request);
            if (memberId != null && memberId > 0) {
                return ResponseEntity.ok(ApiResponse.success(memberId));
            } else {
                return ResponseEntity.ok(ApiResponse.error("成员保存失败"));
            }
        } catch (Exception e) {
            log.error("新增或更新成员失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("系统异常，请稍后重试"));
        }
    }

    /**
     * 接受邀请
     */
    @PostMapping("/acceptInvitation")
    public ResponseEntity<ApiResponse<Boolean>> acceptInvitation(@RequestParam Long id) {
        log.info("接受邀请请求: memberId={}", id);
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }

            // 获取当前登录用户ID
            Long currentUserId = baseHandler.getUserId();
            if (currentUserId == null || currentUserId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            boolean result = memberService.acceptInvitation(id, currentUserId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                return ResponseEntity.ok(ApiResponse.error("接受邀请失败"));
            }
        } catch (Exception e) {
            log.error("接受邀请失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("接受邀请失败: " + e.getMessage()));
        }
    }

    /**
     * 更新成员占比
     */
    @PostMapping("/updatePercentage")
    public ResponseEntity<ApiResponse<Boolean>> updatePercentage(@RequestParam Long id, @RequestParam Integer percentage) {
        log.info("更新成员占比请求: id={}, percentage={}", id, percentage);
        try {
            // 验证参数
            if (id == null || id <= 0 || percentage == null || percentage < 0 || percentage > 100) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确，占比必须在0-100之间"));
            }

            // 验证权限：只能更新自己或自己创建的成员信息
            MemberResponse member = memberService.getMemberById(id);
            if (member == null) {
                return ResponseEntity.ok(ApiResponse.badRequest("未找到成员信息"));
            }

            Long currentUserId = baseHandler.getUserId();
            if (!member.getParentUserId().equals(currentUserId)) {
                return ResponseEntity.ok(ApiResponse.error("无权限更新此成员占比"));
            }

            boolean success = memberService.updateMemberPercentage(id, percentage);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("更新成员占比失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("更新成员占比失败: " + e.getMessage()));
        }
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteMember(@PathVariable Long id) {
        log.info("删除成员请求: {}", id);
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }

            // 验证权限：只能删除自己创建的成员信息
            MemberResponse member = memberService.getMemberById(id);
            if (member == null) {
                return ResponseEntity.ok(ApiResponse.badRequest("未找到成员信息"));
            }

            Long currentUserId = baseHandler.getUserId();
            if (!member.getParentUserId().equals(currentUserId)) {
                return ResponseEntity.ok(ApiResponse.error("无权限删除此成员"));
            }

            boolean success = memberService.deleteMember(id);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            log.error("删除成员失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("删除成员失败: " + e.getMessage()));
        }
    }

    

    /**
     * 获取账本成员总占比
     */
    @GetMapping("/getTotalPercentageByLedgerId/{ledgerId}")
    public ResponseEntity<ApiResponse<Integer>> getTotalPercentageByLedgerId(@PathVariable Long ledgerId) {
        log.info("获取账本成员总占比请求: {}", ledgerId);
        try {
            if (ledgerId == null || ledgerId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("参数不正确"));
            }

            Integer totalPercentage = memberService.getTotalPercentageByLedgerId(ledgerId);
            return ResponseEntity.ok(ApiResponse.success(totalPercentage));
        } catch (Exception e) {
            log.error("获取账本成员总占比失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取账本成员总占比失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户的所有成员列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMemberList() {
        log.info("获取当前用户的成员列表请求");
        try {
            // 获取当前登录用户ID
            Long currentUserId = baseHandler.getUserId();
            if (currentUserId == null || currentUserId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            // 获取当前用户的所有成员列表
            List<MemberResponse> members = memberService.getMembersByParentUserId(currentUserId);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            log.error("获取成员列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取成员列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户的正常状态成员列表（status = 1）
     */
    @GetMapping("/getNormalByUser")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getNormalMembersByUser() {
        log.info("获取当前用户的正常状态成员列表请求");
        try {
            // 获取当前登录用户ID
            Long currentUserId = baseHandler.getUserId();
            if (currentUserId == null || currentUserId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            // 获取当前用户的正常状态成员列表
            List<MemberResponse> members = memberService.getNormalMembersByUserId();
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            log.error("获取正常状态成员列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("获取正常状态成员列表失败: " + e.getMessage()));
        }
    }
}