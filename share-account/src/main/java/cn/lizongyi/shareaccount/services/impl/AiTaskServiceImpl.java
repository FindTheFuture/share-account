package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.AiTaskMapper;
import cn.lizongyi.shareaccount.entity.AiTask;
import cn.lizongyi.shareaccount.services.AiTaskService;
import cn.lizongyi.shareaccount.services.UserMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI任务服务实现类
 * @author lizongyi
 */
@Service
@Slf4j
public class AiTaskServiceImpl implements AiTaskService {
    
    @Autowired
    private AiTaskMapper aiTaskMapper;
    
    @Autowired
    private UserMemberService userMemberService;
    
    // 创建线程池用于异步操作
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    @Override
    public AiTask createTask(String requestStr, Integer type, Long userId) {
        AiTask task = new AiTask();
        task.setRequest(requestStr.trim());
        task.setType(type);
        task.setUserId(userId);
        task.setStatus(0); // 等待识别
        task.setRetryCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        aiTaskMapper.insert(task);
        return task;
    }
    
    @Override
    public boolean updateTaskStatus(Long taskId, Integer status, String result, String errorMsg) {
        AiTask task = aiTaskMapper.findById(taskId);
        if (task == null) {
            return false;
        }
        
        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        
        if (result != null) {
            task.setResult(result);
            return aiTaskMapper.updateTaskStatus(taskId, status, result, task.getUpdateTime(), null) > 0;
        } else if (errorMsg != null) {
            task.setErrorMsg(errorMsg);
            task.setRetryCount(task.getRetryCount() + 1);
            return aiTaskMapper.updateTaskError(taskId, status, errorMsg, task.getRetryCount(), task.getUpdateTime(), null) > 0;
        }
        
        return aiTaskMapper.update(task) > 0;
    }
    
    @Override
    public boolean updateTaskStatus(Long taskId, Integer status, String result, String errorMsg, LocalDateTime endTime) {
        AiTask task = aiTaskMapper.findById(taskId);
        if (task == null) {
            return false;
        }
        
        task.setStatus(status);
        task.setEndTime(endTime);
        task.setUpdateTime(LocalDateTime.now());
        
        if (status == 2) { // 识别成功
            task.setRecognizeTime(LocalDateTime.now());
            
            // 任务类型：0-消费截图识别 1-发票识别 2-收据识别 3-通用OCR 4-聊天内容
            // 暂时 聊天内容任务 不更新用户AI识别使用次数
            if (task.getType() != 4) {
                // 异步更新用户AI识别使用次数
                final Long userId = task.getUserId();
                executorService.submit(() -> {
                    try {
                        // 更新用户AI识别使用次数
                        boolean updateResult = userMemberService.incrementAiUsedCount(userId);
                        if (updateResult) {
                            log.info("成功更新用户 {} 的AI识别使用次数", userId);
                        } else {
                            log.warn("更新用户 {} 的AI识别使用次数失败", userId);
                        }
                    } catch (Exception e) {
                        log.error("异步更新用户 {} 的AI识别使用次数失败", userId, e);
                    }
                });
            }
        }
        
        if (result != null) {
            task.setResult(result);
            return aiTaskMapper.updateTaskStatus(taskId, status, result, task.getUpdateTime(), endTime) > 0;
        } else if (errorMsg != null) {
            task.setErrorMsg(errorMsg);
            task.setRetryCount(task.getRetryCount() + 1);
            return aiTaskMapper.updateTaskError(taskId, status, errorMsg, task.getRetryCount(), task.getUpdateTime(), endTime) > 0;
        }
        
        return aiTaskMapper.update(task) > 0;
    }
    
    @Override
    public AiTask getTaskById(Long taskId) {
        return aiTaskMapper.findById(taskId);
    }
    
    @Override
    public boolean incrementRetryCount(Long taskId) {
        AiTask task = aiTaskMapper.findById(taskId);
        if (task == null) {
            return false;
        }
        
        task.setRetryCount(task.getRetryCount() + 1);
        task.setUpdateTime(LocalDateTime.now());
        return aiTaskMapper.update(task) > 0;
    }
}