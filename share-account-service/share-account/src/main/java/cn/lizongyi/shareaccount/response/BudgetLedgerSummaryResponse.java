package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetLedgerSummaryResponse {
    private Long budgetId;              // 预算ID（当月当前用户）
    private Long totalBalance;          // 当前选择账本的预算总额（分）
    private List<BudgetItemResponse> items; // 当前选择账本的预算明细列表（仅status=0）
}