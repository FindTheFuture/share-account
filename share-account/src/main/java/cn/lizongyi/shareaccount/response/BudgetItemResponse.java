package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetItemResponse {

    private Long id;          // 预算明细 ID
    private Long budgetId;    // 预算 id
    private Long userId;      // 用户 id
    private Long ledgerId;    // 账本 id
    private Long classId;     // 分类 id
    private String className; // 分类名称
    private Long totalBalance; // 设定总余额
    private Integer status;   // 状态 0、正常 1、删除
    private String statusName; // 状态中文
    private String createTime; // 创建时间
    private String icon;      // 分类图标
    private String ledgerName; // 账本名称
    // 注意：分类信息现在由服务层(BudgetItemServiceImpl)填充
}