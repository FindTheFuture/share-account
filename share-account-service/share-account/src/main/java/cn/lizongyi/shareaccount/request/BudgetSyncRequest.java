package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * 同步预算的请求参数
 * 支持从指定年月(sourceYear, sourceMonth)同步到目标年月(year, month)
 */
@Data
public class BudgetSyncRequest {
    private Integer year;        // 目标年份
    private Integer month;       // 目标月份
    private Integer sourceYear;  // 源年份
    private Integer sourceMonth; // 源月份
}