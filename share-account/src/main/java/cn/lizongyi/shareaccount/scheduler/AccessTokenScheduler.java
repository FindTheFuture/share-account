package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.AccessTokenMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-26
 * @description 每天凌晨1点执行删除过期 AccessToken 的定时任务
 */
@Slf4j
@Component
public class AccessTokenScheduler {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    // 每天凌晨1点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteExpiredAccessTokens() {
        log.info("定时任务启动--每天凌晨1点执行--删除过期 AccessToken");

        // 获取当天0点0分0秒的时间戳
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        // 删除过期的 AccessToken
        int deletedCount = accessTokenMapper.deleteExpiredBefore(todayStart);
        log.info("已删除 {} 条过期的 AccessToken 记录", deletedCount);

        log.info("定时任务结束--每天凌晨1点执行--删除过期 AccessToken");
    }
}