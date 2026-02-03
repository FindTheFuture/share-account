package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * 查询定时记账配置列表请求参数
 */
@Data
public class QueryScheduledBillListRequest {

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 账本id（可选）
     */
    private Long ledgerId;

    /**
     * 状态（可选）：1-启用，2-暂停，3-删除
     */
    private Integer status;

    /**
     * 周期类型（可选）：1-每天，2-每周，3-每月，4-每季度，5-每年
     */
    private Integer cycleType;
}
