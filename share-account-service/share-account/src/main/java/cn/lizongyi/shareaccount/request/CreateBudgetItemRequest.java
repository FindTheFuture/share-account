package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateBudgetItemRequest {

    private Long id;

    /**
     * 预算id
     */
    private Long budgetId;

    /**
     * 账本id（必填）
     */
    private Long ledgerId;

    /**
     * 分类id
     */
    private Long classId;

    /**
     * 设定总余额
     */
    private Long totalBalance;

    /**
     * 备注
     */
    private String memo;
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
}