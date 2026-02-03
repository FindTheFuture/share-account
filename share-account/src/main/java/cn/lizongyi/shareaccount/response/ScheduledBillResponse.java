package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时记账配置响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class ScheduledBillResponse {
    // 总条数（分页总数）
    private Long total = 0L;
    // 当前页码
    private Integer pageNum = 1;
    // 每页大小
    private Integer pageSize = 10;
    // 总页数（由后端计算并返回）
    private Integer totalPages = 0;

    // 定时记账配置列表
    private List<ScheduledBillResponseItem> itemList = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduledBillResponseItem {
        private Long id;
        private String name;
        private String price;
        private Long classId;
        private String className;
        private Long ledgerId;
        private String ledgerName;
        private Long accountId;
        private String accountName;
        private Integer cycleType; // 1-每天，2-每周，3-每月，4-每季度，5-每年
        private Integer cycleValue;
        private String executeTime;
        private String startDate;
        private String endDate;
        private Integer status; // 1-启用，2-暂停，3-删除
        private Integer isRemind; // 0-不提醒，1-提醒
        private String description;
        private String createdAt; // yyyy-MM-dd HH:mm:ss

    }
}
