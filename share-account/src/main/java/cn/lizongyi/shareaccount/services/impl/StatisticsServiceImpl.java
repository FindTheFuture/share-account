package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.response.ClassResponse;
import cn.lizongyi.shareaccount.response.StatisticsResponse;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private ClassEntityService classEntityService;

    @Autowired
    private BaseHandler baseHandler;

    @Override
    public StatisticsResponse getStatistics(Long userId,
                                            String dimension,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            Integer year,
                                            Integer month,
                                            Long ledgerId,
                                            Long accountId,
                                            Long categoryId) {

        // 查询分类下面的子分类
        List<Long> childCategoryIds = baseHandler.getChildCategoryIds(categoryId);

        StatisticsResponse result = new StatisticsResponse();
        try {
            // 查询正常状态账单（status=0），AND 精确匹配 ledgerId/accountId/categoryId（若传入）
            List<Bill> bills;
            if (ledgerId == null || ledgerId <= 0) {
                // 如果没传递账本id，默认查询当前用户可访问的 所有账本
                List<Long> accessibleLedgerIds = baseHandler.getAccessibleLedgerIdsForCurrentUser();
                if (accessibleLedgerIds == null || accessibleLedgerIds.isEmpty()) {
                    accessibleLedgerIds = java.util.Collections.singletonList(-1L);
                }
                bills = billMapper.findByLedgerIdsAndConditions(
                        accessibleLedgerIds,
                        startTime,
                        endTime,
                        accountId,
                        childCategoryIds,
                        null,
                        null,
                        0,
                        1 // 默认按时间由近到远排序
                );
            } else {
                bills = billMapper.findByUserIdAndConditions(
                        null,
                        null,
                        startTime,
                        endTime,
                        ledgerId,
                        accountId,
                        childCategoryIds,
                        null,
                        null,
                        0,
                        1 // 默认按时间由近到远排序
                );
            }

            // 设置总数量（账单条数）
            result.setTotalCount((long) (CollectionUtils.isEmpty(bills) ? 0 : bills.size()));
            if (CollectionUtils.isEmpty(bills)) {
                // 空态：返回 0 汇总与空集合
                return result;
            }

            // 汇总总收入/总支出
            long totalIncome = 0L;
            long totalExpense = 0L;
            for (Bill bill : bills) {
                Long price = bill.getPrice();
                if (price == null) continue;
                if (price > 0) {
                    totalIncome += price;
                } else if (price < 0) {
                    totalExpense += Math.abs(price);
                }
            }
            result.setTotalIncome(totalIncome);
            result.setTotalExpense(totalExpense);
            result.setBalance(totalIncome - totalExpense);

            // 时间线聚合
            List<StatisticsResponse.TimelinePoint> timeline = buildTimeline(dimension, bills, startTime, endTime, year, month);
            result.setTimeline(timeline);

            // 分类统计聚合
            List<StatisticsResponse.CategoryStat> categoryStats = buildCategoryStats(bills);
            result.setCategories(categoryStats);

            // 年份列表（仅 total 维度）
            if ("total".equalsIgnoreCase(dimension)) {
                Set<Integer> yearSet = bills.stream()
                        .map(b -> b.getBillTime().getYear())
                        .collect(Collectors.toCollection(TreeSet::new));
                result.setYears(new ArrayList<>(yearSet));
            }
        } catch (Exception e) {
            log.error("统计计算失败: userId={}, dimension={}, startTime={}, endTime={}, ledgerId={}, accountId={}, categoryId={}",
                    userId, dimension, startTime, endTime, ledgerId, accountId, categoryId, e);
        }
        return result;
    }

    private List<StatisticsResponse.TimelinePoint> buildTimeline(String dimension,
                                                                 List<Bill> bills,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime,
                                                                 Integer year,
                                                                 Integer month) {
        Map<String, StatisticsResponse.TimelinePoint> map = new LinkedHashMap<>();
        DateTimeFormatter yFmt = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter ymdFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if ("year".equalsIgnoreCase(dimension)) {
            // 按月份：yyyy-MM
            for (Bill bill : bills) {
                String label = bill.getBillTime().format(ymFmt);
                accumulateTimeline(map, label, bill);
            }
        } else if ("month".equalsIgnoreCase(dimension)) {
            // 按日：yyyy-MM-dd
            for (Bill bill : bills) {
                String label = bill.getBillTime().format(ymdFmt);
                accumulateTimeline(map, label, bill);
            }
        } else if ("day".equalsIgnoreCase(dimension)) {
            // 当天按小时段（简化为按当日单点）
            String label = startTime.toLocalDate().format(ymdFmt);
            StatisticsResponse.TimelinePoint tp = map.computeIfAbsent(label, k -> new StatisticsResponse.TimelinePoint(k, 0L, 0L, 0L));
            for (Bill bill : bills) {
                accumulateTimeline(map, label, bill);
            }
        } else if ("custom".equalsIgnoreCase(dimension)) {
            // 自定义范围按日
            for (Bill bill : bills) {
                String label = bill.getBillTime().format(ymdFmt);
                accumulateTimeline(map, label, bill);
            }
        } else {
            // total 按年
            for (Bill bill : bills) {
                String label = bill.getBillTime().format(yFmt);
                accumulateTimeline(map, label, bill);
            }
        }

        // 计算 balance
        for (StatisticsResponse.TimelinePoint tp : map.values()) {
            tp.setBalance(tp.getIncome() - tp.getExpense());
        }
        return new ArrayList<>(map.values());
    }

    private void accumulateTimeline(Map<String, StatisticsResponse.TimelinePoint> map, String label, Bill bill) {
        StatisticsResponse.TimelinePoint tp = map.computeIfAbsent(label, k -> new StatisticsResponse.TimelinePoint(k, 0L, 0L, 0L));
        Long price = bill.getPrice();
        if (price == null) return;
        if (price > 0) {
            tp.setIncome(tp.getIncome() + price);
        } else if (price < 0) {
            tp.setExpense(tp.getExpense() + Math.abs(price));
        }
    }

    private List<StatisticsResponse.CategoryStat> buildCategoryStats(List<Bill> bills) {
        Map<Long, StatisticsResponse.CategoryStat> map = new LinkedHashMap<>();
        for (Bill bill : bills) {
            Long classId = bill.getClassId();
            if (classId == null || classId <= 0) continue;
            StatisticsResponse.CategoryStat cs = map.computeIfAbsent(classId, k -> {
                StatisticsResponse.CategoryStat tmp = new StatisticsResponse.CategoryStat();
                tmp.setCategoryId(k);
                ClassResponse cr = classEntityService.selectById(k);
                tmp.setCategoryName(cr != null ? cr.getName() : "");
                tmp.setIncome(0L);
                tmp.setExpense(0L);
                return tmp;
            });
            Long price = bill.getPrice();
            if (price == null) continue;
            if (price > 0) {
                cs.setIncome(cs.getIncome() + price);
            } else if (price < 0) {
                cs.setExpense(cs.getExpense() + Math.abs(price));
            }
        }
        return new ArrayList<>(map.values());
    }
}