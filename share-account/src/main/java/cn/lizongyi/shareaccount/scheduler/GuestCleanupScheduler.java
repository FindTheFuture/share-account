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
import java.util.*;

/**
 * 游客清理定时任务：每天清理超过保留期的游客数据
 * 如果有多个游客账号，必须保留最新登录的一个
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

        List<User> guestUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (user == null || user.getId() == null) {
                continue;
            }
            if (baseHandler.isGuestUser(user.getId())) {
                guestUsers.add(user);
            }
        }

        if (guestUsers.isEmpty()) {
            log.info("无游客用户，结束游客清理");
            return;
        }

        log.info("发现 {} 个游客账号", guestUsers.size());

        // 找出最新登录的游客账号（保留）
        User latestGuest = null;
        LocalDateTime latestLoginTime = null;
        for (User guest : guestUsers) {
            LocalDateTime lastLogin = guest.getLastLoginTime();
            if (lastLogin != null) {
                if (latestLoginTime == null || lastLogin.isAfter(latestLoginTime)) {
                    latestLoginTime = lastLogin;
                    latestGuest = guest;
                }
            }
        }

        // 如果所有游客都没有登录时间，保留ID最大的一个
        if (latestGuest == null) {
            for (User guest : guestUsers) {
                if (latestGuest == null || guest.getId() > latestGuest.getId()) {
                    latestGuest = guest;
                }
            }
        }

        log.info("保留最新的游客账号: userId={}, lastLoginTime={}", latestGuest.getId(), latestGuest.getLastLoginTime());

        int cleanedCount = 0;

        for (User user : guestUsers) {
            // 跳过需要保留的最新游客
            if (user.getId().equals(latestGuest.getId())) {
                continue;
            }

            Long userId = user.getId();
            LocalDateTime lastLogin = user.getLastLoginTime();

            // 如果是最新登录的游客且在保留期内，跳过
            if (lastLogin != null && !lastLogin.isBefore(cutoff)) {
                log.info("游客 userId={} 在保留期内(lastLogin={})，跳过清理", userId, lastLogin);
                continue;
            }

            try {
                log.info("开始清理过期游客 userId={}, lastLoginTime={}", userId, lastLogin);

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
