package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.dao.MessageMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.dao.UserMessageMapper;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.entity.Message;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.UserMessage;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.TemplateMessageService;
import cn.lizongyi.shareaccount.response.ClassResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每小时整点生成用户昨天日报的系统消息
 */
@Slf4j
@Component
public class DailyReportScheduler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Autowired
    private ClassEntityService classEntityService;
    
    @Autowired
    private LedgerMapper ledgerMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private TemplateMessageService templateMessageService;

    @Scheduled(cron = "0 0 * * * ?")
    public void generateDailyReports() {
        int currentHour = LocalDateTime.now().getHour();
        log.info("定时任务启动--生成昨天日报--开始，当前小时: {}", currentHour);

        List<User> targetUsers = userMapper.findByCanSendMessageAndNotityBill(1, currentHour);
        if (targetUsers == null || targetUsers.isEmpty()) {
            log.info("当前{}点 没有开启通知功能的用户，跳过 日报 发送", currentHour);
            return;
        }
        log.info("当前{}点 开启通知功能的用户数量: {}", currentHour, targetUsers.size());

        LocalDateTime now = LocalDateTime.now();

        // 昨天的时间范围
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.withHour(23).withMinute(59).withSecond(59);

        // 本月时间范围（从本月1号到当前时间）
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime monthEnd = now;

        // 上月整月时间范围
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate firstDayOfLastMonth = firstDayOfCurrentMonth.minusMonths(1);
        LocalDateTime lastMonthStart = firstDayOfLastMonth.atStartOfDay();
        LocalDateTime lastMonthEnd = firstDayOfCurrentMonth.atStartOfDay().minusSeconds(1);

        int monthNumber = LocalDate.now().getMonthValue();
        
        // 遍历所有需要发送日报的用户
        for (User user : targetUsers) {
            if (user == null) continue;
            log.info("开始查询 用户：{}  昨天日报------------------------------", user.getId());
            try {
                // 1. 查询用户创建的账本ID列表
                List<Long> createdLedgerIds = new ArrayList<>();
                try {
                    List<Ledger> createdLedgers = ledgerMapper.findByUserId(user.getId());
                    if (createdLedgers != null) {
                        for (Ledger ledger : createdLedgers) {
                            if (ledger != null && ledger.getId() != null) {
                                createdLedgerIds.add(ledger.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取用户 {} 创建的账本失败", user.getId(), e);
                }
                
                // 2. 查询用户共享的账本ID列表
                List<Long> sharedLedgerIds = new ArrayList<>();
                try {
                    List<Member> members = memberMapper.findNormalByUserId(user.getId(), 1);
                    if (members != null) {
                        for (Member member : members) {
                            if (member != null && member.getLedgerId() != null && member.getBillId() == null) {
                                sharedLedgerIds.add(member.getLedgerId());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取用户 {} 共享的账本失败", user.getId(), e);
                }
                
                // 3. 合并所有账本ID
                Set<Long> allLedgerIds = new HashSet<>();
                if (createdLedgerIds != null) {
                    allLedgerIds.addAll(createdLedgerIds);
                }
                if (sharedLedgerIds != null) {
                    allLedgerIds.addAll(sharedLedgerIds);
                }
                
                // 如果没有任何账本，跳过该用户
                if (allLedgerIds.isEmpty()) {
                    log.info("用户 {} 没有任何账本，跳过日报生成", user.getId());
                    continue;
                }
                allLedgerIds = allLedgerIds.stream().distinct().collect(Collectors.toSet());
                
                // 查询昨日账单（状态=0）
                List<Bill> yesterdayBills = new ArrayList<>();
                for (Long ledgerId : allLedgerIds) {
                    List<Bill> ledgerBills = billMapper.findByLedgerIdAndTimeRange(ledgerId, yesterdayStart, yesterdayEnd);
                    // 过滤状态为0的账单
                    if (ledgerBills != null) {
                        for (Bill bill : ledgerBills) {
                            if (bill != null && bill.getStatus() != null && bill.getStatus() == 0) {
                                yesterdayBills.add(bill);
                            }
                        }
                    }
                }

                if (yesterdayBills.isEmpty()) {
                    log.info("用户 {} 没有新增账单，跳过日报发送", user.getId());
                    continue;
                }

                long yesterdayExpenseTotal = 0L;
                int yesterdayExpenseCount = 0;
                long yesterdayIncomeTotal = 0L;
                int yesterdayIncomeCount = 0;

                for (Bill bill : yesterdayBills) {
                    Long price = bill.getPrice();
                    if (price == null || bill.getStatus() != 0) {
                        continue;
                    }
                    if (price > 0) {
                        yesterdayIncomeTotal += price;
                        yesterdayIncomeCount++;
                    } else if (price < 0) {
                        yesterdayExpenseTotal += Math.abs(price);
                        yesterdayExpenseCount++;
                    }
                }

                // 查询本月账单（状态=0）
                List<Bill> monthBills = new ArrayList<>();
                for (Long ledgerId : allLedgerIds) {
                    List<Bill> ledgerBills = billMapper.findByLedgerIdAndTimeRange(ledgerId, monthStart, monthEnd);
                    // 过滤状态为0的账单
                    if (ledgerBills != null) {
                        for (Bill bill : ledgerBills) {
                            if (bill != null && bill.getStatus() != null && bill.getStatus() == 0) {
                                monthBills.add(bill);
                            }
                        }
                    }
                }
                
                // 查询上月账单（状态=0，整月）
                List<Bill> lastMonthBills = new ArrayList<>();
                for (Long ledgerId : allLedgerIds) {
                    List<Bill> ledgerBills = billMapper.findByLedgerIdAndTimeRange(ledgerId, lastMonthStart, lastMonthEnd);
                    // 过滤状态为0的账单
                    if (ledgerBills != null) {
                        for (Bill bill : ledgerBills) {
                            if (bill != null && bill.getStatus() != null && bill.getStatus() == 0) {
                                lastMonthBills.add(bill);
                            }
                        }
                    }
                }

                long monthExpenseTotal = 0L;
                long monthIncomeTotal = 0L;
                for (Bill bill : monthBills) {
                    Long price = bill.getPrice();
                    if (price == null || bill.getStatus() != 0) {
                        continue;
                    }
                    if (price > 0) {
                        monthIncomeTotal += price;
                    } else if (price < 0) {
                        monthExpenseTotal += Math.abs(price);
                    }
                }

                long lastMonthExpenseTotal = 0L;
                for (Bill bill : lastMonthBills) {
                    Long price = bill.getPrice();
                    if (price == null || bill.getStatus() != 0) {
                        continue;
                    }
                    if (price < 0) {
                        lastMonthExpenseTotal += Math.abs(price);
                    }
                }

                // 分类维度：按 class_id 聚合（仅支出）
                Map<Long, Long> monthExpenseByClass = aggregateExpenseByClass(monthBills);
                Map<Long, Long> lastMonthExpenseByClass = aggregateExpenseByClass(lastMonthBills);

                StringBuilder categoryLines = new StringBuilder();
                Set<Long> allClassIds = new HashSet<>();
                allClassIds.addAll(monthExpenseByClass.keySet());
                allClassIds.addAll(lastMonthExpenseByClass.keySet());
                List<Long> sortedClassIds = new ArrayList<>(allClassIds);
                sortedClassIds.sort((a, b) -> {
                    long diffB = monthExpenseByClass.getOrDefault(b, 0L) - lastMonthExpenseByClass.getOrDefault(b, 0L);
                    long diffA = monthExpenseByClass.getOrDefault(a, 0L) - lastMonthExpenseByClass.getOrDefault(a, 0L);
                    return Long.compare(diffB, diffA);
                });
                for (Long classId : sortedClassIds) {
                    long current = monthExpenseByClass.getOrDefault(classId, 0L);
                    long previous = lastMonthExpenseByClass.getOrDefault(classId, 0L);
                    if (current <= 0) {
                        continue; // 本月无支出则不输出此分类
                    }
                    ClassResponse cr = classEntityService != null && classId != null ? classEntityService.selectById(classId) : null;
                    String className = (cr != null && cr.getName() != null) ? cr.getName() : "未分类";

                    if (previous > 0 && current > previous) {
                        long diff = current - previous;
                        BigDecimal percent = BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(previous), 0, RoundingMode.HALF_UP);
                        categoryLines.append("本月【").append(className)
                                .append("】超支").append(percent.toPlainString()).append("%")
                                .append("，本月支出￥").append(formatYuan(current))
                                .append("，上月支出￥").append(formatYuan(previous)).append("\n");
                    } else if (previous == 0 && current > 0) {
                        categoryLines.append("本月【").append(className)
                                .append("】超支￥").append(formatYuan(current))
                                .append("，上月为￥0").append("\n");
                    }
                }

                String content = 
                        "主人：\n" +
                        "昨日支出￥" + formatYuan(yesterdayExpenseTotal) + "，共" + yesterdayExpenseCount + "笔\n" +
                        "昨日收入￥" + formatYuan(yesterdayIncomeTotal) + "，共" + yesterdayIncomeCount + "笔\n" +
                        "本月统计：" + monthNumber + "月已支出￥" + formatYuan(monthExpenseTotal) + "，已入账￥" + formatYuan(monthIncomeTotal) + "\n" +
                        "与上月对比：本月支出￥" + formatYuan(monthExpenseTotal) + "，比上月多￥" + formatYuan(Math.max(monthExpenseTotal - lastMonthExpenseTotal, 0)) + "\n\n" +
                        categoryLines.toString() + "\n" +
                        "包含别人分享给你的账本";

                // 创建系统消息（类型=1 系统消息，优先级=0 普通，状态=0 正常）
                Message message = new Message();
                message.setTitle("昨天日报");
                message.setContent(content);
                message.setType(2); // 业务消息类型
                message.setPriority(0);
                message.setStatus(0);
                message.setCreatedAt(new Date());
                message.setUpdatedAt(new Date());

                messageMapper.insert(message);

                // 创建用户消息关联（未读）
                UserMessage userMessage = new UserMessage();
                userMessage.setMessageId(message.getId());
                userMessage.setUserId(user.getId());
                userMessage.setIsRead(0);
                userMessage.setCreatedAt(new Date());
                userMessageMapper.insert(userMessage);
                
                try {
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("amount1", createDataEntry(formatYuan(yesterdayExpenseTotal)));
                    reportData.put("amount2", createDataEntry(formatYuan(yesterdayIncomeTotal)));
                    reportData.put("amount3", createDataEntry(formatYuan(monthExpenseTotal)));
                    reportData.put("amount4", createDataEntry(formatYuan(monthIncomeTotal)));
                    templateMessageService.sendDailyReport(user.getId(), reportData);
                } catch (Exception e) {
                    log.error("发送日报模板消息失败，用户ID: {}", user.getId(), e);
                }

                log.info("已为用户 {} 生成昨天日报消息 msgId={} ", user.getId(), message.getId());
            } catch (Exception e) {
                log.error("生成用户 {} 的昨天日报失败", user.getId(), e);
            }
        }

        log.info("定时任务结束--生成昨天日报--结束");
    }

    private String formatYuan(long amountInCents) {
        BigDecimal yuan = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return yuan.toPlainString();
    }

    // 聚合支出：仅统计 status=0 且 price<0，按 class_id 求和（绝对值）
    // 如果 class 的 type=2（子分类），则把自己的支出合并到 parent_id 对应的 type=1 父分类
    // 最终只返回 type=1 的父分类聚合数据
    private Map<Long, Long> aggregateExpenseByClass(List<Bill> bills) {
        Map<Long, Long> map = new HashMap<>();
        if (bills == null) return map;

        // 第一步：先按原始 class_id 聚合支出
        for (Bill bill : bills) {
            if (bill == null) continue;
            if (bill.getStatus() != null && bill.getStatus() == 0) {
                Long price = bill.getPrice();
                if (price != null && price < 0) {
                    Long classId = bill.getClassId();
                    if (classId != null) {
                        long add = Math.abs(price);
                        map.put(classId, map.getOrDefault(classId, 0L) + add);
                    }
                }
            }
        }

        // 第二步：获取所有涉及的 class 信息，构建 (classId -> parentId, type) 映射
        Map<Long, ClassResponse> classInfoMap = new HashMap<>();
        Set<Long> allClassIds = new HashSet<>(map.keySet());
        for (Long classId : allClassIds) {
            ClassResponse classEntity = classEntityService.selectById(classId);
            if (classEntity != null) {
                classInfoMap.put(classId, classEntity);
            }
        }

        // 第三步：如果 class 的 type=2，把支出合并到 parentId
        Map<Long, Long> finalMap = new HashMap<>();
        for (Map.Entry<Long, Long> entry : map.entrySet()) {
            Long classId = entry.getKey();
            Long amount = entry.getValue();
            ClassResponse classEntity = classInfoMap.get(classId);

            if (classEntity != null && classEntity.getType() != null && classEntity.getType() == 2) {
                // type=2 是子分类，合并到 parentId
                Long parentId = classEntity.getParentId();
                if (parentId != null) {
                    finalMap.put(parentId, finalMap.getOrDefault(parentId, 0L) + amount);
                } else {
                    // 如果没有 parentId，保留自己（理论上不应该）
                    finalMap.put(classId, finalMap.getOrDefault(classId, 0L) + amount);
                }
            } else {
                // type=1 或 type=0 或未知类型，直接使用原始 classId
                finalMap.put(classId, finalMap.getOrDefault(classId, 0L) + amount);
            }
        }

        return finalMap;
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