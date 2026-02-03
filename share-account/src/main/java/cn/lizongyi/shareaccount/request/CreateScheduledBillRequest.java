package cn.lizongyi.shareaccount.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建定时记账配置请求参数
 */
@Data
public class CreateScheduledBillRequest {

    private Long id;

    /**
     * 账本id
     */
    private Long ledgerId;

    /**
     * 记账名称
     */
    private String name;

    /**
     * 金额
     */
    private Long price = 0L;

    /**
     * 分类id
     */
    private Long classId;

    /**
     * 账户id
     */
    private Long accountId;

    /**
     * 周期类型：1-每天，2-每周，3-每月，4-每季度，5-每年
     */
    private Integer cycleType;

    /**
     * 周期值：每天-0，每周-1-7，每月-1-31，每季度-1-3，每年-1-12
     */
    private Integer cycleValue;

    /**
     * 执行时间
     */
    private String executeTime;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 状态：1-启用，2-暂停，3-删除
     */
    private Integer status = 1;

    /**
     * 是否提醒：0-不提醒，1-提醒
     */
    private Integer isRemind = 0;

    /**
     * 描述
     */
    private String description;
}
