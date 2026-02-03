package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateBudgetRequest {

    private Long id;

    /**
     * 设定总余额
     */
    private Long totalBalance;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 备注
     */
    private String memo;
}