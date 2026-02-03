package cn.lizongyi.shareaccount.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;


@Data
public class QueryBillListRequest {
    
    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2})(\\s\\d{2}:\\d{2}:\\d{2})?$", message = "开始时间格式不正确，请使用yyyy-MM-dd或yyyy-MM-dd HH:mm:ss格式")
    private String startTime;
    
    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2})(\\s\\d{2}:\\d{2}:\\d{2})?$", message = "结束时间格式不正确，请使用yyyy-MM-dd或yyyy-MM-dd HH:mm:ss格式")
    private String endTime;
    
    /**
     * 账本ID（可选）
     */
    @Min(value = 1, message = "账本ID必须大于0")
    private Long ledgerId;
    
    /**
     * 账户ID（可选）
     */
    @Min(value = 1, message = "账户ID必须大于0")
    private Long accountId;
    
    /**
     * 分类ID（可选）
     */
    @Min(value = 1, message = "分类ID必须大于0")
    private Long categoryId;
    
    /**
     * 是否计入预算（可选）
     */
    private Integer isBudget;
    
    /**
     * 状态（可选）
     */
    private Integer status;
    
    /**
     * 页码（可选，默认1）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    /**
     * 每页数量（可选，默认10）
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer pageSize = 10;
    
    /**
     * 账单ID列表（可选，用于查询指定ID的账单列表）
     */
    private List<Long> billIds;
    
    /**
     * 排序方式（可选）：
     * 1 - 时间由近到远（默认）
     * 2 - 时间由远到近
     * 3 - 金额由大到小
     * 4 - 金额由小到大
     */
    private Integer sortBy = 1;
}