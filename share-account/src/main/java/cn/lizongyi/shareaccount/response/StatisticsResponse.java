package cn.lizongyi.shareaccount.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 总收入（分） */
    private Long totalIncome = 0L;
    /** 总支出（分，正数） */
    private Long totalExpense = 0L;
    /** 结余（分）=收入-支出 */
    private Long balance = 0L;

    /** 总数量（过滤条件后的账单总条数） */
    private Long totalCount = 0L;

    /** 时间线数据 */
    private List<TimelinePoint> timeline = new ArrayList<>();

    /** 分类统计 */
    private List<CategoryStat> categories = new ArrayList<>();

    /** 年份列表（用于"总览"维度下的年度选择） */
    private List<Integer> years = new ArrayList<>();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelinePoint implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 标签：如 2024、2024-10、2024-10-01 等 */
        private String label;
        private Long income = 0L;
        private Long expense = 0L;
        private Long balance = 0L;
        /** 该月/日预算总金额（分） */
        private Long budgetTotal = 0L;
        /** 该月/日预算使用率（总支出/预算总金额，范围0-1） */
        private Double budgetUsageRate = 0.0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long categoryId;
        private String categoryName;
        private Long income = 0L;
        private Long expense = 0L;
    }
}