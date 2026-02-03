package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.BudgetSyncRequest;
import cn.lizongyi.shareaccount.request.CreateBudgetRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.BudgetResponse;
import cn.lizongyi.shareaccount.services.BudgetService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.util.CollectionUtils;

@RestController
@RequestMapping("/budget")
@Slf4j
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private cn.lizongyi.shareaccount.services.BudgetItemService budgetItemService;

    /**
     * 根据 ID 查询预算信息
     * @param budgetId 预算 ID
     * @return 包含预算信息的响应实体
     */
    @GetMapping("/getById/{budgetId}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据预算 id:{}, 查询预算信息", budgetId);

        BudgetResponse budget = budgetService.findResponseById(budgetId);
        if (budget == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到预算信息"));
        }

        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    /**
     * 查询用户预算列表
     * @return 包含预算列表响应信息的响应实体
     */
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetList() {
        try {
            List<BudgetResponse> list = budgetService.getUserBudgetList(baseHandler.getUserId());
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查询预算失败: " + e.getMessage()));
        }
    }

    /**
     * 根据年份和月份查询预算
     * @param year 年份
     * @param month 月份
     * @return 包含预算信息的响应实体
     */
    @GetMapping("/getByYearAndMonth")
    public ResponseEntity<ApiResponse<BudgetResponse>> getByYearAndMonth(
            @RequestParam Integer year, @RequestParam Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据年份:{}和月份:{}查询预算信息", year, month);

        BudgetResponse budget = budgetService.findResponseByUserIdAndYearAndMonth(
                baseHandler.getUserId(), year, month);
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @GetMapping("/getByYearAndMonthAndLedger")
    public ResponseEntity<ApiResponse<cn.lizongyi.shareaccount.response.BudgetLedgerSummaryResponse>> getByYearAndMonthAndLedger(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Long ledgerId) {
        if (year == null || month == null || month < 1 || month > 12 || ledgerId == null || ledgerId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        Long userId = baseHandler.getUserId();
        log.info("根据年份:{} 月份:{} 查询账本:{} 的预算汇总", year, month, ledgerId);

        // 权限校验：必须具备账本级权限
        if (!baseHandler.canViewLedger(userId, ledgerId)) {
            return ResponseEntity.ok(ApiResponse.error("无权限查看该账本预算"));
        }

        // 直接通过年月+账本ID关联查询预算明细（仅正常状态），无需再按budgetId过滤
        java.util.List<cn.lizongyi.shareaccount.response.BudgetItemResponse> items = budgetItemService.getBudgetItemListByYearMonthAndLedger(year, month, ledgerId);

        cn.lizongyi.shareaccount.response.BudgetLedgerSummaryResponse summary = new cn.lizongyi.shareaccount.response.BudgetLedgerSummaryResponse();
        if(CollectionUtils.isEmpty(items)){
            summary.setTotalBalance(0L);
            summary.setItems(java.util.Collections.emptyList());
            return ResponseEntity.ok(ApiResponse.success(summary));
        }

        // 汇总总预算金额（分）
        long total = 0L;
        for (cn.lizongyi.shareaccount.response.BudgetItemResponse i : items) {
            Long v = i.getTotalBalance();
            if (v != null) total += v;
        }
        summary.setTotalBalance(total);
        summary.setItems(items);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 创建或更新预算
     * @param request 创建预算请求对象
     * @return 包含创建结果的响应实体
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateBudgetRequest request) {
        log.info("创建或更新预算信息");
        try {
            Boolean success = budgetService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建或更新预算信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建预算
     * @param request 创建预算请求对象
     * @return 包含创建结果的响应实体
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Boolean>> create(@RequestBody CreateBudgetRequest request) {
        // 直接调用save方法，save方法内部会处理创建逻辑
        return save(request);
    }

    /**
     * 根据 ID 删除预算
     * @param budgetId 预算 ID
     * @return 包含删除结果的响应实体
     */
    @DeleteMapping("/delete/{budgetId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteById(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据预算 id:{}, 删除预算信息", budgetId);
        try {
            budgetService.deleteById(budgetId);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除预算信息失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/syncLastMonth")
    public ResponseEntity<ApiResponse<Boolean>> syncLastMonth(@RequestBody BudgetSyncRequest request) {
        log.info("开始同步预算信息，源年月: {}-{} 到目标年月: {}-{}", 
                request.getSourceYear(), request.getSourceMonth(), request.getYear(), request.getMonth());
        try {
            // 验证请求参数
            if (request.getYear() == null || request.getMonth() == null || 
                request.getSourceYear() == null || request.getSourceMonth() == null) {
                throw new RuntimeException("请求参数不完整");
            }
            
            // 验证月份范围
            if (request.getMonth() < 1 || request.getMonth() > 12 || 
                request.getSourceMonth() < 1 || request.getSourceMonth() > 12) {
                throw new RuntimeException("月份参数无效");
            }
            
            // 调用服务层方法执行同步操作
            boolean result = budgetService.syncLastMonthBudget(
                    baseHandler.getUserId(), 
                    request.getYear(), 
                    request.getMonth(), 
                    request.getSourceYear(), 
                    request.getSourceMonth());
            log.info("同步预算成功");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("同步预算失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}