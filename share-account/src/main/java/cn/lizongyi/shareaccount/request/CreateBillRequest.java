package cn.lizongyi.shareaccount.request;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class CreateBillRequest {

    private Long id;

    /**
     * 账本id
     */
    private Long ledgerId;

    /**
     * 账户id
     */
    private Long accountId;

    /**
     * 分类id
     */
    private Long classId = -1L;

    /**
     * 是否计入预算 0、计入（默认）1、不计入
     */
    private Integer isBudget = 0;

    /**
     * 账单时间戳
     */
    private Long billTimestamp;

    /**
     * 账单时间
     */
    private LocalDateTime billTime;

    /**
     * 设置账单时间戳，并转换为LocalDateTime
     */
    public void setBillTimestamp(Long billTimestamp) {
        this.billTimestamp = billTimestamp;
        if (billTimestamp != null) {
            this.billTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(billTimestamp), ZoneId.systemDefault());
        }
    }

    /**
     * 兼容前端直接设置billTime字段
     */
    public void setBillTime(LocalDateTime billTime) {
        this.billTime = billTime;
    }

    /**
     * 金额
     */
    private Long price = 0L;

    /**
     * 状态 0、正常 1、删除
     */
    private Integer status = 0;

    /**
     * 备注
     */
    private String memo;
    

}