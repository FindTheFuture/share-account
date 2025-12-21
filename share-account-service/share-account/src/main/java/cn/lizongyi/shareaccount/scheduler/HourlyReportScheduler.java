package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.dao.MessageMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.dao.UserMessageMapper;
import cn.lizongyi.shareaccount.services.TemplateMessageService;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.entity.Message;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每小时生成用户上一小时支出报告的系统消息
 */
@Slf4j
@Component
public class HourlyReportScheduler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMessageMapper userMessageMapper;
    
    @Autowired
    private LedgerMapper ledgerMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private TemplateMessageService templateMessageService;
    
    // 每个小时的第0分钟执行
    @Scheduled(cron = "0 0 * * * ?")
    public void generateHourlyReports() {
        int currentHour = LocalDateTime.now().getHour();
        log.info("定时任务启动--小时报--开始，当前小时: {}", currentHour);

        try {
            // 查询所有开启通知功能的用户列表
            List<User> targetUsers = userMapper.findByCanSendMessage(1);
            if (targetUsers == null || targetUsers.isEmpty()) {
                log.info("当前{}点 没有开启通知功能的用户，跳过 小时报 发送", currentHour);
                return;
            }
            log.info("当前{}点 开启通知功能，可以发送 小时报 用户数量: {}", currentHour, targetUsers.size());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime hourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = now.withMinute(0).withSecond(0).withNano(0).minusNanos(1);
            LocalDateTime dayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = now.minusNanos(1);

            for (User user : targetUsers) {
                try {
                    Long userId = user.getId();
                    log.info("开始查询 用户：{}  小时报------------------------------", userId);
                    
                    // 1. 查询用户自己创建的账本
                    List<Ledger> createdLedgers = ledgerMapper.findByUserId(userId);
                    Set<Long> userLedgerIds = new HashSet<>();
                    
                    // 添加用户创建的账本ID
                    if (createdLedgers != null) {
                        for (Ledger ledger : createdLedgers) {
                            userLedgerIds.add(ledger.getId());
                        }
                    }
                    
                    // 2. 查询用户参与的共享账本（通过Member表）
                    List<Member> members = memberMapper.findNormalByUserId(userId, 1); // status=1表示正常状态
                    if (members != null) {
                        for (Member member : members) {
                            if (member != null && member.getLedgerId() != null && member.getBillId() == null) {
                                userLedgerIds.add(member.getLedgerId());
                            }
                        }
                    }
                    
                    // 如果用户没有任何账本，跳过
                    if (userLedgerIds.isEmpty()) {
                        log.info("用户 {} 没有任何账本，跳过小时报发送", userId);
                        continue;
                    }

                    userLedgerIds = userLedgerIds.stream().distinct().collect(Collectors.toSet());
                    
                    // 3. 查询最近一小时内新增的账单列表
                    List<Bill> hourlyBills = new ArrayList<>();
                    for (Long ledgerId : userLedgerIds) {
                        List<Bill> bills = billMapper.findByLedgerIdAndTimeRange(ledgerId, hourStart, hourEnd);
                        if (bills != null) {
                            hourlyBills.addAll(bills);
                        }
                    }
                    
                    // 过滤状态为0的有效账单
                    hourlyBills = hourlyBills.stream().filter(bill -> bill.getStatus() == 0).toList();
                    
                    // 如果没有新增账单，跳过
                    if (hourlyBills.isEmpty()) {
                        log.info("用户 {} 没有新增账单，跳过小时报发送", userId);
                        continue;
                    }
                    
                    // 4. 汇总账单数据
                    long hourlyExpenseTotal = 0L;
                    long hourlyIncomeTotal = 0L;
                    int hourlyExpenseCount = 0;
                    
                    for (Bill bill : hourlyBills) {
                        Long price = bill.getPrice();
                        if (price == null || bill.getStatus() != 0) {
                            continue;
                        }
                        if(price < 0){
                            hourlyExpenseTotal += Math.abs(price);
                            hourlyExpenseCount++;
                        } else if(price > 0){
                            hourlyIncomeTotal += price;
                        }
                    }
                    
                    // 5. 查询今日总支出和收入
                    long dayExpenseTotal = 0L;
                    long dayIncomeTotal = 0L;
                    
                    for (Long ledgerId : userLedgerIds) {
                        List<Bill> dayBills = billMapper.findByLedgerIdAndTimeRange(ledgerId, dayStart, dayEnd);
                        if (dayBills != null) {
                            for (Bill bill : dayBills) {
                                Long price = bill.getPrice();
                                if (price == null || bill.getStatus() != 0) {
                                    continue;
                                }
                                if (price < 0) {
                                    dayExpenseTotal += Math.abs(price);
                                } else if (price > 0) {
                                    dayIncomeTotal += price;
                                }
                            }
                        }
                    }
                    
                    // 6. 生成系统消息
                    String messageContent = 
                        "主人：\n" +
                        "上一小时总支出￥" + formatYuan(hourlyExpenseTotal) + "\n" +
                        "上一小时总收入￥" + formatYuan(hourlyIncomeTotal) + "\n" +
                        "今天总支出￥" + formatYuan(dayExpenseTotal) + "，总收入￥" + formatYuan(dayIncomeTotal) + "\n" +
                        "包含别人分享给你的账本";
                    
                    // 创建系统消息
                    Message message = new Message();
                    message.setTitle("小时报");
                    message.setContent(messageContent);
                    message.setType(1); // 系统消息类型
                    message.setPriority(0);
                    message.setStatus(0);
                    message.setCreatedAt(new Date());
                    message.setUpdatedAt(new Date());
                    messageMapper.insert(message);
                    
                    // 创建用户消息关联
                    UserMessage userMessage = new UserMessage();
                    userMessage.setUserId(userId);
                    userMessage.setMessageId(message.getId());
                    userMessage.setIsRead(0); // 未读
                    userMessage.setCreatedAt(new Date());
                    userMessageMapper.insert(userMessage);
                    
                    // 7. 发送微信模板消息
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("amount3", createDataEntry(formatYuan(hourlyExpenseTotal)));
                    reportData.put("number7", createDataEntry(hourlyExpenseCount));
                    reportData.put("thing5", createDataEntry("今天总支出￥" + formatYuan(dayExpenseTotal) + "，总收入￥" + formatYuan(dayIncomeTotal)));
                    
                    templateMessageService.sendHourlyReport(userId, reportData);
                    
                    log.info("向用户 {} 发送小时报成功，支出总额：{}，支出笔数：{}", userId, hourlyExpenseTotal, hourlyExpenseCount);
                    
                } catch (Exception e) {
                    log.error("处理用户 {} 的小时报时发生异常", user.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }

        log.info("定时任务结束--小时报--结束");
    }

    private String formatYuan(long amountInCents) {
        BigDecimal yuan = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return yuan.toPlainString();
    }

    /**
     * 创建一个包含 value 的 Map 条目。
     *
     * @param value 消息内容
     * @param color 颜色代码，可以是 null 表示默认颜色
     * @return 包含 value 的 Map
     */
    public Map<String, Object> createDataEntry(Object value) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("value", value);
        return entry;
    }
}