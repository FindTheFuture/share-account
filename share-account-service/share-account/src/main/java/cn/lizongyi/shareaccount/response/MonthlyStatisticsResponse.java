package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 本月收支统计响应类
 * 用于存储账本本月的收入、支出和结余信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long ledgerId; // 账本ID
    private Integer year; // 年份
    private Integer month; // 月份
    private Long income; // 本月收入（以分为单位）
    private Long expense; // 本月支出（以分为单位）
    private Long balance; // 本月结余（以分为单位，收入-支出）
}