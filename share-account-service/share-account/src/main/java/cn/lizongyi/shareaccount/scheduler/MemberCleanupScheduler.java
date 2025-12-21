package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.MemberMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 成员邀请的到期与清理任务：
 * - 到期标记：将超过2天未接受的邀请标记为 status=2（过期）；
 * - 物理删除：删除 status=2 且创建时间超过3天的记录。
 * 全部使用 Asia/Shanghai 时区。
 */
@Component
@Slf4j
public class MemberCleanupScheduler {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 每天凌晨3点执行到期标记与过期删除。
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Shanghai")
    public void cleanupExpiredInvites() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime expireCutoff = now.minusDays(2);
        LocalDateTime deleteCutoff = now.minusDays(3);
        try {
            int expired = memberMapper.expirePendingInvites(expireCutoff);
            int deleted = memberMapper.deleteExpired(deleteCutoff);
            log.info("成员邀请清理完成: 标记过期={} 删除过期={}", expired, deleted);
        } catch (Exception e) {
            log.error("成员邀请清理任务失败", e);
        }
    }
}