package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.AiTask;
import java.time.LocalDateTime;


/**
 * AI任务服务接口
 * @author lizongyi
 */
public interface AiTaskService {
    
    /**
     * 创建AI识别任务
     * @param requestStr 任务请求字符串 
     * @param type 任务类型
     * @param userId 用户ID
     * @return 创建的任务
     */
    AiTask createTask(String requestStr, Integer type, Long userId);
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 新状态
     * @param result 识别结果（可选）
     * @param errorMsg 错误信息（可选）
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long taskId, Integer status, String result, String errorMsg);
    
    /**
     * 更新任务状态（包含结束时间）
     * @param taskId 任务ID
     * @param status 新状态
     * @param result 识别结果（可选）
     * @param errorMsg 错误信息（可选）
     * @param endTime 结束时间
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long taskId, Integer status, String result, String errorMsg, LocalDateTime endTime);
    
    /**
     * 根据ID获取任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    AiTask getTaskById(Long taskId);
    
    /**
     * 增加重试次数
     * @param taskId 任务ID
     * @return 是否增加成功
     */
    boolean incrementRetryCount(Long taskId);
}