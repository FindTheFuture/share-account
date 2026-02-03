package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.SessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-05
 * @description
 */
@Slf4j
@Component
public class SessionCleanupScheduler {

    @Autowired
    private SessionMapper sessionMapper;

    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void cleanupExpiredSessions() {
        log.info("定时任务启动--每小时执行--删除过期session");
        Date now = new Date();
        sessionMapper.deleteExpiredSessions(now);
        log.info("定时任务结束--每小时执行--删除过期session");
    }
}
