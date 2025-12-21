package cn.lizongyi.shareaccount.response;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 多线程统计响应实体类
 * 用于返回详细的统计结果信息
 */
@Data
public class MultiThreadStatisticsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long ledgerId; // 账本ID
    private LocalDateTime startTime; // 统计开始时间
    private LocalDateTime endTime; // 统计结束时间
    
    // 总体统计数据
    private Long totalIncome; // 总收入（以分为单位）
    private Long totalExpense; // 总支出（以分为单位）
    private Long totalBalance; // 总结余（以分为单位）
    
    // 分类统计数据
    private List<CategoryStatistics> incomeCategoryStats; // 收入分类统计
    private List<CategoryStatistics> expenseCategoryStats; // 支出分类统计
    
    // 月度趋势数据
    private List<MonthlyTrend> monthlyTrends; // 月度收支趋势
    
    // 账户统计数据
    private List<AccountStatistics> accountStats; // 账户统计
    
    // 耗时统计（用于性能监控）
    private long processingTimeMs; // 处理耗时（毫秒）
    
    /**
     * 分类统计内部类
     */
    @Data
    public static class CategoryStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long categoryId; // 分类ID
        private String categoryName; // 分类名称
        private String categoryIcon; // 分类图标
        private Long amount; // 金额（以分为单位）
        private double percentage; // 占比（百分比）
    }
    
    /**
     * 月度趋势内部类
     */
    @Data
    public static class MonthlyTrend implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer year; // 年份
        private Integer month; // 月份
        private Long income; // 月收入
        private Long expense; // 月支出
        private Long balance; // 月结余
        private Long budget; // 月预算总金额
        private Long billCount; // 月账单总数量
    }
    
    /**
     * 账户统计内部类
     */
    @Data
    public static class AccountStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long accountId; // 账户ID
        private String accountName; // 账户名称
        private Long totalIncome; // 总收入
        private Long totalExpense; // 总支出
    }
}