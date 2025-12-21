package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.Budget;
import cn.lizongyi.shareaccount.enums.BudgetStatusEnum;
import cn.lizongyi.shareaccount.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {

    private Long id;          // 预算 ID
    private Long userId;      // 用户 id
    private Long totalBalance; // 设定总余额
    private Integer year;     // 年份
    private Integer month;    // 月份
    private Integer status;   // 状态 0、正常 1、删除
    private String statusName; // 状态中文
    private String createTime; // 创建时间
    private String memo;      // 备注

    public static BudgetResponse fromBudget(Budget budget) {
        if (budget == null) {
            return null;
        }

        BudgetResponse budgetResponse = new BudgetResponse();
        budgetResponse.setId(budget.getId());
        budgetResponse.setUserId(budget.getUserId());
        budgetResponse.setTotalBalance(budget.getTotalBalance());
        budgetResponse.setYear(budget.getYear());
        budgetResponse.setMonth(budget.getMonth());
        budgetResponse.setStatus(budget.getStatus());
        budgetResponse.setStatusName(BudgetStatusEnum.fromId(budget.getStatus()).getName());
        budgetResponse.setCreateTime(DateUtil.localDateTimeToString(budget.getCreateTime()));
        return budgetResponse;
    }
}