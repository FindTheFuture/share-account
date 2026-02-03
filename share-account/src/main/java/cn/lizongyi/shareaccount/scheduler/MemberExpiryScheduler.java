package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.UserMember;
import cn.lizongyi.shareaccount.services.UserMemberService;
import cn.lizongyi.shareaccount.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员过期检查与提醒任务
 */
@Component
@Slf4j
public class MemberExpiryScheduler {

    @Autowired
    private UserMemberService userMemberService;
    
    @Autowired
    private UserService userService;

    /**
     * 每天凌晨0点5分执行会员过期检查
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void checkExpiredMembers() {
        log.info("开始执行会员过期检查任务");
        try {
            userMemberService.checkAndUpdateExpiredMembers();
            log.info("会员过期检查任务完成");
        } catch (Exception e) {
            log.error("执行会员过期检查任务失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每天凌晨0点10分执行，查询所有用户，如果用户的剩余AI次数为0，新增user_member记录
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void addUserMemberForZeroRemaining() {
        log.info("开始为剩余AI次数为0的用户添加会员记录");
        try {
            // 获取所有用户
            List<User> allUsers = userService.findAll();
            log.info("共查询到 {} 个用户", allUsers.size());
            
            // 设置2099年12月31日23时59分59秒的截止时间
            LocalDateTime endTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
            
            // 遍历所有用户
            for (User user : allUsers) {
                try {
                    // 获取用户剩余AI次数和PDF次数
                    Integer remainingAiCount = userMemberService.getRemainingAiCount(user.getId());
                    Integer remainingPdfCount = userMemberService.getRemainingPdfCount(user.getId());
                    
                    // 如果剩余AI次数为0，添加会员记录
                    if ((remainingAiCount != null && remainingAiCount == 0) || (remainingPdfCount != null && remainingPdfCount == 0)) {
                        log.info("用户ID: {} 剩余AI次数/PDF次数 为0，准备添加会员记录", user.getId());
                        
                        int aiCountToSet = (remainingAiCount != null && remainingAiCount == 0) ? 1 : 0;
                        int pdfCountToSet = (remainingPdfCount != null && remainingPdfCount == 0) ? 1 : 0;
                        log.info("用户ID: {} 当前剩余PDF次数: {}，将设置为: {}", user.getId(), remainingPdfCount, pdfCountToSet);
                        
                        // 创建用户会员记录
                        UserMember userMember = new UserMember();
                        userMember.setUserId(user.getId())
                                .setPackageId(-1L) // 默认包ID
                                .setPackageName("1次AI体验/1次PDF体验") // 默认包名称
                                .setPrice(BigDecimal.ZERO) // 免费
                                .setPackageType(0) // 默认类型
                                .setPaymentRecordId(0L)
                                .setAiCount(aiCountToSet) // AI次数为1
                                .setAiUsedCount(0)
                                .setPdfCount(pdfCountToSet)
                                .setPdfUsedCount(0)
                                .setStartTime(LocalDateTime.now())
                                .setEndTime(endTime)
                                .setCreateTime(LocalDateTime.now())
                                .setStatus(0); // 正常状态
                        
                        // 保存到数据库
                        userMemberService.createUserMember(userMember);
                        log.info("用户ID: {} 会员记录添加成功", user.getId());
                    } else {
                        log.info("用户ID: {} 剩余AI次数为: {}，无需添加会员记录", user.getId(), remainingAiCount);
                    }
                } catch (Exception e) {
                    log.error("处理用户ID: {} 时出错: {}", user.getId(), e.getMessage(), e);
                }
            }
            
            log.info("为剩余AI次数为0的用户添加会员记录完成");
        } catch (Exception e) {
            log.error("执行添加会员记录任务失败: {}", e.getMessage(), e);
        }
    }
}