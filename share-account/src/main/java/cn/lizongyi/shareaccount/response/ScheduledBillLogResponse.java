package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时记账执行日志响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class ScheduledBillLogResponse {
    // 总条数（分页总数）
    private Long total = 0L;
    // 当前页码
    private Integer pageNum = 1;
    // 每页大小
    private Integer pageSize = 10;
    // 总页数（由后端计算并返回）
    private Integer totalPages = 0;

    // 定时记账执行日志列表
    private List<ScheduledBillLogResponseItem> itemList = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduledBillLogResponseItem {
        private Long id;
        private Long scheduledId;
        private String executeTime; // yyyy-MM-dd HH:mm:ss
        private Integer status; // 1-成功，2-失败
        private String statusName;
        private Long billId;
        private String errorMsg;
        private String createdAt; // yyyy-MM-dd HH:mm:ss
        private BillResponse bill; // 生成的账单数据
    }
}
