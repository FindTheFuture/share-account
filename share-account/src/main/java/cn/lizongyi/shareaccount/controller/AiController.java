package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.AiTask;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.AiService;
import cn.lizongyi.shareaccount.services.AiTaskService;
import cn.lizongyi.shareaccount.services.PictureService;
import cn.lizongyi.shareaccount.services.UserMemberService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.response.BillResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI控制器
 * @author lizongyi
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController extends BaseController {
    
    @Autowired
    @Qualifier("doubaoAiService")
    private AiService aiService;
    
    @Autowired
    private AiTaskService aiTaskService;
    
    @Autowired
    private PictureService pictureService;

    @Autowired
    private BaseHandler baseHandler;
    
    @Autowired
    private UserMemberService userMemberService;

    /**
     * 识别消费截图
     * @param pictureId 图片ID
     * @return 识别结果
     */
    @PostMapping("/recognize/bill/chat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recognizeBillChat(@RequestBody Map<String, String> requestBody) {
        String chat = requestBody.get("chat");
        if (chat == null) {
            return ResponseEntity.ok(ApiResponse.error("缺少必要参数chat"));
        }
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 检查AI识别剩余次数  // 暂时 聊天内容任务 不更新用户AI识别使用次数
            //Integer remainingAiCount = userMemberService.getRemainingAiCount(userId);
            //if (remainingAiCount <= 0) {
            //    return ResponseEntity.ok(ApiResponse.error("AI识别次数不足，请订阅会员获取更多次数"));
            //}
            
            // 创建AI任务
            AiTask task = aiTaskService.createTask(chat, 4, userId);
            
            try {
                // 更新任务状态为识别中
                aiTaskService.updateTaskStatus(task.getId(), 1, null, null);
                
                BillResponse billResponse = aiService.recognizeBillChat(chat, userId);
                
                // 更新任务状态为成功，同时更新结束时间
                if(billResponse != null){
                    aiTaskService.updateTaskStatus(task.getId(), 2, billResponse.toString(), null, LocalDateTime.now());
                }
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("bill", billResponse);
                result.put("taskId", task.getId());
                result.put("status", "success");
                
                return ResponseEntity.ok(ApiResponse.success(result));
            } catch (Exception e) {
                // 更新任务状态为失败，同时更新结束时间
                aiTaskService.updateTaskStatus(task.getId(), 3, null, e.getMessage(), LocalDateTime.now());
                log.error("AI识别失败", e);
                
                // 失败直接告知用户，让用户手动重试
                return ResponseEntity.ok(ApiResponse.error("识别失败，请点击重试按钮重新识别"));
            }
        } catch (Exception e) {
            log.error("处理AI识别请求失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }


    /**
     * 识别消费截图
     * @param pictureId 图片ID
     * @return 识别结果
     */
    @PostMapping("/recognize/bill/pic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recognizeBillScreenshot(@RequestBody Map<String, Long> requestBody) {
        Long pictureId = requestBody.get("pictureId");
        if (pictureId == null) {
            return ResponseEntity.ok(ApiResponse.error("缺少必要参数pictureId"));
        }
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 检查AI识别剩余次数
            Integer remainingAiCount = userMemberService.getRemainingAiCount(userId);
            if (remainingAiCount <= 0) {
                return ResponseEntity.ok(ApiResponse.error("AI识别次数不足，请订阅会员获取更多次数"));
            }
            
            // 查询图片信息
            Picture picture = pictureService.findById(pictureId);
            if (picture == null) {
                return ResponseEntity.ok(ApiResponse.error("图片不存在"));
            }
            
            // 创建AI任务
            AiTask task = aiTaskService.createTask(picture.getId().toString(), 0, userId);
            
            try {
                // 更新任务状态为识别中
                aiTaskService.updateTaskStatus(task.getId(), 1, null, null);
                
                BillResponse billResponse = aiService.recognizeBillScreenshot(picture, userId);
                
                // 更新任务状态为成功，同时更新结束时间
                if(billResponse != null){
                    aiTaskService.updateTaskStatus(task.getId(), 2, billResponse.toString(), null, LocalDateTime.now());
                }
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("bill", billResponse);
                result.put("taskId", task.getId());
                result.put("status", "success");
                
                return ResponseEntity.ok(ApiResponse.success(result));
            } catch (Exception e) {
                // 更新任务状态为失败，同时更新结束时间
                aiTaskService.updateTaskStatus(task.getId(), 3, null, e.getMessage(), LocalDateTime.now());
                log.error("AI识别失败", e);
                
                // 失败直接告知用户，让用户手动重试
                return ResponseEntity.ok(ApiResponse.error("识别失败，请点击重试按钮重新识别"));
            }
        } catch (Exception e) {
            log.error("处理AI识别请求失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 重试识别
     * @param taskId 任务ID
     * @return 重试结果
     */
    @PostMapping("/recognize/retry/pic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryRecognition(@RequestParam Long taskId) {
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 检查AI识别剩余次数
            Integer remainingAiCount = userMemberService.getRemainingAiCount(userId);
            if (remainingAiCount <= 0) {
                return ResponseEntity.ok(ApiResponse.error("AI识别次数不足，请订阅会员获取更多次数"));
            }
            
            // 查询任务
            AiTask task = aiTaskService.getTaskById(taskId);
            if (task == null) {
                return ResponseEntity.ok(ApiResponse.error("任务不存在"));
            }
            
            // 验证用户权限
            if (!task.getUserId().equals(userId)) {
                return ResponseEntity.ok(ApiResponse.error("无权操作此任务"));
            }
            
            // 增加重试次数
            aiTaskService.incrementRetryCount(taskId);
            
            // 查询图片信息
            Picture picture = pictureService.findById(Long.parseLong(task.getRequest()));
            if (picture == null) {
                return ResponseEntity.ok(ApiResponse.error("图片不存在"));
            }
            
            try {
                // 更新任务状态为识别中
                aiTaskService.updateTaskStatus(taskId, 1, null, null);
                
                // 重新调用AI服务识别图片
                BillResponse billResponse = aiService.recognizeBillScreenshot(picture, userId);
                
                // 更新任务状态为成功，同时更新结束时间
                aiTaskService.updateTaskStatus(taskId, 2, billResponse.toString(), null, LocalDateTime.now());
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("bill", billResponse);
                result.put("taskId", taskId);
                result.put("status", "success");
                
                return ResponseEntity.ok(ApiResponse.success(result));
            } catch (Exception e) {
                // 更新任务状态为失败，同时更新结束时间
                aiTaskService.updateTaskStatus(taskId, 3, null, e.getMessage(), LocalDateTime.now());
                log.error("AI识别重试失败", e);
                
                return ResponseEntity.ok(ApiResponse.error("识别失败，请稍后重试"));
            }
        } catch (Exception e) {
            log.error("处理AI识别重试请求失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskStatus(@RequestParam Long taskId) {
        try {
            // 获取用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录"));
            }
            
            // 查询任务
            AiTask task = aiTaskService.getTaskById(taskId);
            if (task == null) {
                return ResponseEntity.ok(ApiResponse.error("任务不存在"));
            }
            
            // 验证用户权限
            if (!task.getUserId().equals(userId)) {
                return ResponseEntity.ok(ApiResponse.error("无权查看此任务"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", task.getStatus());
            result.put("retryCount", task.getRetryCount());
            result.put("errorMsg", task.getErrorMsg());
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务器内部错误"));
        }
    }
}