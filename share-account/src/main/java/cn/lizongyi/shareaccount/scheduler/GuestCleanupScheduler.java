package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.*;
import cn.lizongyi.shareaccount.entity.*;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游客清理定时任务：每天清理超过保留期的游客数据
 */
@Slf4j
@Component
public class GuestCleanupScheduler {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BillMapper billMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private LedgerMapper ledgerMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private UserMessageMapper userMessageMapper;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private BaseHandler baseHandler;

    // 每天凌晨3点执行一次
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredGuests() {
        log.info("游客清理任务启动：扫描并清理过期游客数据");

        // 配置中心：清理开关
        String cleanupSwitch = baseHandler.getCongigValue(null, Constants.GUEST_CLEANUP_ENABLED);
        if (cleanupSwitch != null && ("false".equalsIgnoreCase(cleanupSwitch.trim()) || "0".equals(cleanupSwitch.trim()))) {
            log.info("游客清理开关关闭（guest.cleanup.enabled={}），跳过此次任务", cleanupSwitch);
            return;
        }

        // 配置中心：保留天数（默认1天，不小于1）
        String daysStr = baseHandler.getCongigValue(null, Constants.GUEST_CLEANUP_DAYS);
        int retentionDays = 1;
        try {
            if (daysStr != null && !daysStr.trim().isEmpty()) {
                retentionDays = Integer.parseInt(daysStr.trim());
            }
        } catch (Exception ignore) {
            log.warn("游客清理保留天数配置解析失败: {}，使用默认1天", daysStr);
            retentionDays = 1;
        }
        if (retentionDays < 1) {
            retentionDays = 1;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        log.info("游客清理过期截止时间: {}（保留{}天）", cutoff, retentionDays);

        List<User> allUsers = userMapper.findAll();
        if (allUsers == null || allUsers.isEmpty()) {
            log.info("无用户，结束游客清理");
            return;
        }

        int cleanedCount = 0;

        for (User user : allUsers) {
            try {
                if (user == null || user.getId() == null) {
                    continue;
                }
                Long userId = user.getId();

                // 仅清理游客且超过保留期（按最后登录时间）
                if (!baseHandler.isGuestUser(userId)) {
                    continue;
                }
                LocalDateTime lastLogin = user.getLastLoginTime();
                if (lastLogin == null || !lastLogin.isBefore(cutoff)) {
                    continue;
                }

                // 删除与用户相关的账单（逻辑删除）
                List<Bill> bills = billMapper.findByUserId(userId);
                if (bills != null) {
                    for (Bill bill : bills) {
                        try {
                            billMapper.deleteById(bill.getId());
                        } catch (Exception ex) {
                            log.warn("删除游客账单失败 userId={} billId={} err={}", userId, bill.getId(), ex.getMessage());
                        }
                    }
                }

                // 删除成员关系
                List<Member> members = memberMapper.findByUserId(userId);
                if (members != null) {
                    for (Member member : members) {
                        try {
                            memberMapper.deleteById(member.getId());
                        } catch (Exception ex) {
                            log.warn("删除游客成员失败 userId={} memberId={} err={}", userId, member.getId(), ex.getMessage());
                        }
                    }
                }

                // 删除账户（逻辑删除）
                List<Account> accounts = accountMapper.findByUserId(userId);
                if (accounts != null) {
                    for (Account account : accounts) {
                        try {
                            accountMapper.deleteById(account.getId());
                        } catch (Exception ex) {
                            log.warn("删除游客账户失败 userId={} accountId={} err={}", userId, account.getId(), ex.getMessage());
                        }
                    }
                }

                // 删除账本（物理删除）
                List<Ledger> ledgers = ledgerMapper.findByUserId(userId);
                if (ledgers != null) {
                    for (Ledger ledger : ledgers) {
                        try {
                            ledgerMapper.deleteById(ledger.getId());
                        } catch (Exception ex) {
                            log.warn("删除游客账本失败 userId={} ledgerId={} err={}", userId, ledger.getId(), ex.getMessage());
                        }
                    }
                }

                // 删除用户消息关联（共享消息本身保留）
                try {
                    userMessageMapper.deleteByUserId(userId);
                } catch (Exception ex) {
                    log.warn("删除游客消息关联失败 userId={} err={}", userId, ex.getMessage());
                }

                // 删除游客会话记录
                try {
                    sessionMapper.deleteByOpenId(user.getOpenid());
                } catch (Exception ex) {
                    log.warn("删除游客会话失败 openid={} err={}", user.getOpenid(), ex.getMessage());
                }

                // 最后删除用户
                try {
                    userMapper.deleteById(userId);
                } catch (Exception ex) {
                    log.warn("删除游客用户失败 userId={} err={}", userId, ex.getMessage());
                }

                cleanedCount++;
            } catch (Exception e) {
                log.error("清理游客数据异常 userId={}", user != null ? user.getId() : null, e);
            }
        }

        log.info("游客清理任务完成：共清理 {} 个过期游客用户", cleanedCount);
    }
}