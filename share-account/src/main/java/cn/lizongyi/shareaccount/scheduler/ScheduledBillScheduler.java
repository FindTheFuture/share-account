package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.entity.ScheduledBill;
import cn.lizongyi.shareaccount.services.ScheduledBillService;
import cn.lizongyi.shareaccount.response.ScheduledBillLogResponse;
import cn.lizongyi.shareaccount.entity.ScheduledBillLog;
import cn.lizongyi.shareaccount.services.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 定时记账任务调度器
 */
@Slf4j
@Component
public class ScheduledBillScheduler {

    @Autowired
    private ScheduledBillService scheduledBillService;

    @Autowired
    @Qualifier("scheduledBillThreadPool")
    private Executor executor;

    @Autowired
    private BaseHandler baseHandler;

    /**
     * 每5分钟执行一次，扫描并执行定时记账配置
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void scanAndExecuteScheduledBills() {
        log.info("开始扫描定时记账配置");
        try {
            // 获取所有启用状态的定时记账配置
            List<ScheduledBill> scheduledBills = scheduledBillService.findAllEnabled();
            if (scheduledBills == null || scheduledBills.isEmpty()) {
                log.info("没有启用的定时记账配置");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();

            // 遍历检查每个配置是否需要执行
            for (ScheduledBill bill : scheduledBills) {

                // 检查开始日期和结束日期
                if (!checkDateRange(bill, today)) {
                    continue;
                }

                // 检查执行时间
                if (!checkExecuteTime(bill, currentTime)) {
                    continue;
                }

                // 检查周期
                if (!checkCycle(bill, now)) {
                    continue;
                }

                // 检查是否已经执行过（在最近5分钟内）
                if (hasExecutedRecently(bill, now)) {
                    log.info("定时记账配置已在最近5分钟内执行过，ID: {}", bill.getId());
                    continue;
                }

                // 异步执行记账操作
                executor.execute(() -> {
                    try {
                        scheduledBillService.executeScheduledBill(bill);
                    } catch (Exception e) {
                        log.error("执行定时记账配置失败，ID: {}", bill.getId(), e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("扫描定时记账配置失败", e);
        } finally {
            log.info("定时记账配置扫描完成");
        }
    }

    /**
     * 检查日期范围
     * @param bill 定时记账配置
     * @param today 当前日期
     * @return 是否在日期范围内
     */
    private boolean checkDateRange(ScheduledBill bill, LocalDate today) {
        // 检查开始日期
        if (bill.getStartDate() != null && today.isBefore(bill.getStartDate())) {
            return false;
        }

        // 检查结束日期
        if (bill.getEndDate() != null && today.isAfter(bill.getEndDate())) {
            return false;
        }

        return true;
    }

    /**
     * 检查执行时间
     * @param bill 定时记账配置
     * @param currentTime 当前时间
     * @return 是否达到执行时间
     */
    private boolean checkExecuteTime(ScheduledBill bill, LocalTime currentTime) {
        LocalDateTime executeTime = bill.getExecuteTime();
        if (executeTime == null) {
            return false;
        }

        // 提取执行时间的时分秒部分
        LocalTime executeTimeOnly = executeTime.toLocalTime();
        
        // 检查是否在执行时间前后5分钟内
        LocalTime startTime = executeTimeOnly.minusMinutes(5);
        LocalTime endTime = executeTimeOnly.plusMinutes(5);

        return (currentTime.isAfter(startTime) || currentTime.equals(startTime)) &&
               (currentTime.isBefore(endTime) || currentTime.equals(endTime));
    }

    /**
     * 检查周期
     * @param bill 定时记账配置
     * @param now 当前时间
     * @return 是否符合周期条件
     */
    private boolean checkCycle(ScheduledBill bill, LocalDateTime now) {
        Integer cycleType = bill.getCycleType();
        Integer cycleValue = bill.getCycleValue();

        if (cycleType == null || cycleValue == null) {
            return false;
        }

        // 第一次执行时间就是executeTime字段的值
        LocalDateTime firstExecuteTime = bill.getExecuteTime();
        
        // 检查当前时间是否在开始日期和结束日期范围内
        LocalDate today = now.toLocalDate();
        if (!checkDateRange(bill, today)) {
            return false;
        }

        switch (cycleType) {
            case 1: // 天
                // 每隔cycleValue天执行一次
                int cycleDays = cycleValue;
                // 计算从第一次执行时间到现在经过了多少天
                long daysSinceFirst = java.time.Duration.between(firstExecuteTime.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay()).toDays();
                // 检查是否是cycleValue的整数倍天数
                return daysSinceFirst >= 0 && daysSinceFirst % cycleDays == 0;
            // case 2: // 每周
            //     // 检查当前是周几（1-7，周一至周日）
            //     int dayOfWeek = now.getDayOfWeek().getValue();
            //     return dayOfWeek == cycleValue;
            case 3: // 月
                // 每隔cycleValue个月执行一次
                int cycleMonths = cycleValue;
                // 计算从第一次执行时间到现在经过了多少个月
                int monthsSinceFirst = (now.getYear() - firstExecuteTime.getYear()) * 12 + (now.getMonthValue() - firstExecuteTime.getMonthValue());
                // 检查是否是cycleValue的整数倍月数
                if (monthsSinceFirst >= 0 && monthsSinceFirst % cycleMonths == 0) {
                    // 检查日期是否匹配（处理月份天数不足的情况）
                    int dayOfMonth = now.getDayOfMonth();
                    int daysInMonth = now.toLocalDate().lengthOfMonth();
                    int targetDay = firstExecuteTime.getDayOfMonth();
                    return dayOfMonth == targetDay || (targetDay > daysInMonth && dayOfMonth == daysInMonth);
                }
                return false;
            // case 4: // 每季度
            //     // 检查当前是季度的第几个月（1-3）
            //     int month = now.getMonthValue();
            //     int monthInQuarter = ((month - 1) % 3) + 1;
            //     return monthInQuarter == cycleValue;
            case 5: // 年
                // 每隔cycleValue年执行一次
                int cycleYears = cycleValue;
                // 计算从第一次执行时间到现在经过了多少年
                int yearsSinceFirst = now.getYear() - firstExecuteTime.getYear();
                // 检查是否是cycleValue的整数倍年数
                if (yearsSinceFirst >= 0 && yearsSinceFirst % cycleYears == 0) {
                    // 检查月份是否匹配
                    if (now.getMonthValue() == firstExecuteTime.getMonthValue()) {
                        // 检查日期是否匹配（处理月份天数不足的情况）
                        int dayOfMonth = now.getDayOfMonth();
                        int daysInMonth = now.toLocalDate().lengthOfMonth();
                        int targetDay = firstExecuteTime.getDayOfMonth();
                        return dayOfMonth == targetDay || (targetDay > daysInMonth && dayOfMonth == daysInMonth);
                    }
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * 检查定时记账配置是否在当前周期内已经执行过
     * @param scheduledBill 定时记账配置
     * @param now 当前时间
     * @return 是否已执行过（只考虑成功的执行记录）
     */
    private boolean hasExecutedRecently(ScheduledBill scheduledBill, LocalDateTime now) {
        try {
            // 查询最近的执行记录（限制查询最近10条，足够判断）
            List<ScheduledBillLog> logItems = scheduledBillService.findLogsByScheduledId(scheduledBill.getId(), 1, 10);

            if (logItems != null && !logItems.isEmpty()) {
                Integer cycleType = scheduledBill.getCycleType();
                Integer cycleValue = scheduledBill.getCycleValue();
                LocalDateTime firstExecuteTime = scheduledBill.getExecuteTime();
                
                // 检查是否有成功的记录在当前周期内已经执行过
                for (ScheduledBillLog logItem : logItems) {
                    // 只考虑状态为成功的执行记录
                    if (logItem.getStatus() == 1 && logItem.getExecuteTime() != null) {
                        LocalDateTime executeTime = logItem.getExecuteTime();
                        
                        // 检查执行时间是否在当前周期内
                        if (isInCurrentCycle(executeTime, now, cycleType, cycleValue, firstExecuteTime)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查定时记账配置执行记录失败，ID: {}", scheduledBill.getId(), e);
        }
        return false;
    }
    
    /**
     * 检查执行时间是否在当前周期内
     * @param executeTime 执行时间
     * @param now 当前时间
     * @param cycleType 周期类型
     * @param cycleValue 周期值
     * @param firstExecuteTime 第一次执行时间
     * @return 是否在当前周期内
     */
    private boolean isInCurrentCycle(LocalDateTime executeTime, LocalDateTime now, Integer cycleType, Integer cycleValue, LocalDateTime firstExecuteTime) {
        if (cycleType == null || cycleValue == null || firstExecuteTime == null) {
            return false;
        }
        
        // 首先检查执行时间是否在当前时间之前（避免未来时间的执行记录）
        if (executeTime.isAfter(now)) {
            return false;
        }
        
        switch (cycleType) {
            case 1: // 天 - 每隔cycleValue天执行一次
                // 计算从第一次执行时间到现在的周期数
                long daysSinceFirst = java.time.Duration.between(firstExecuteTime.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay()).toDays();
                long executeDaysSinceFirst = java.time.Duration.between(firstExecuteTime.toLocalDate().atStartOfDay(), executeTime.toLocalDate().atStartOfDay()).toDays();
                
                // 检查是否在同一个周期内
                return daysSinceFirst >= 0 && executeDaysSinceFirst >= 0 && 
                       (daysSinceFirst / cycleValue) == (executeDaysSinceFirst / cycleValue);
            case 3: // 月 - 每隔cycleValue个月执行一次
                // 计算从第一次执行时间到现在的周期数
                int monthsSinceFirst = (now.getYear() - firstExecuteTime.getYear()) * 12 + (now.getMonthValue() - firstExecuteTime.getMonthValue());
                int executeMonthsSinceFirst = (executeTime.getYear() - firstExecuteTime.getYear()) * 12 + (executeTime.getMonthValue() - firstExecuteTime.getMonthValue());
                
                // 检查是否在同一个周期内
                return monthsSinceFirst >= 0 && executeMonthsSinceFirst >= 0 && 
                       (monthsSinceFirst / cycleValue) == (executeMonthsSinceFirst / cycleValue);
            case 5: // 年 - 每隔cycleValue年执行一次
                // 计算从第一次执行时间到现在的周期数
                int yearsSinceFirst = now.getYear() - firstExecuteTime.getYear();
                int executeYearsSinceFirst = executeTime.getYear() - firstExecuteTime.getYear();
                
                // 检查是否在同一个周期内
                return yearsSinceFirst >= 0 && executeYearsSinceFirst >= 0 && 
                       (yearsSinceFirst / cycleValue) == (executeYearsSinceFirst / cycleValue);
            default:
                // 对于其他周期类型，使用默认的5分钟检查
                LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
                return executeTime.isAfter(fiveMinutesAgo) || executeTime.isEqual(fiveMinutesAgo);
        }
    }
}
