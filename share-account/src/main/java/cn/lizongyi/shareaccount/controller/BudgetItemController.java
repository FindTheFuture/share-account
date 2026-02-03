package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateBudgetItemRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.BudgetItemResponse;
import cn.lizongyi.shareaccount.services.BudgetItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgetItem")
@Slf4j
public class BudgetItemController {

    @Autowired
    private BudgetItemService budgetItemService;

    /**
     * 根据 ID 查询预算明细信息
     * @param itemId 预算明细 ID
     * @return 包含预算明细信息的响应实体
     */
    @GetMapping("/getById/{itemId}")
    public ResponseEntity<ApiResponse<BudgetItemResponse>> getById(@PathVariable Long itemId) {
        if (itemId == null || itemId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据预算明细 id:{}, 查询预算明细信息", itemId);

        BudgetItemResponse item = budgetItemService.findResponseById(itemId);
        if (item == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到预算明细信息"));
        }

        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 根据预算 ID 查询预算明细列表
     * @param budgetId 预算 ID
     * @return 包含预算明细列表响应信息的响应实体
     */
    @GetMapping("/getByBudgetId/{budgetId}")
    public ResponseEntity<ApiResponse<List<BudgetItemResponse>>> getByBudgetId(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据预算 id:{}, 查询预算明细列表", budgetId);

        try {
            List<BudgetItemResponse> list = budgetItemService.getBudgetItemListByBudgetId(budgetId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查询预算明细失败: " + e.getMessage()));
        }
    }

    /**
     * 创建或更新预算明细
     * @param request 创建预算明细请求对象
     * @return 包含创建结果的响应实体
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateBudgetItemRequest request) {
        log.info("创建或更新预算明细信息");
        try {
            // 基础校验：账本id必填
            if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
                return ResponseEntity.ok(ApiResponse.error("账本id不能为空"));
            }
            Boolean success = budgetItemService.save(request);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建或更新预算明细信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建预算明细
     * @param request 创建预算明细请求对象
     * @return 包含创建结果的响应实体
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Boolean>> create(@RequestBody CreateBudgetItemRequest request) {
        // 直接调用save方法，save方法内部会处理创建逻辑
        return save(request);
    }

    /**
     * 根据 ID 删除预算明细
     * @param itemId 预算明细 ID
     * @return 包含删除结果的响应实体
     */
    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteById(@PathVariable Long itemId) {
        if (itemId == null || itemId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("根据预算明细 id:{}, 删除预算明细信息", itemId);
        try {
            budgetItemService.deleteById(itemId);
            return ResponseEntity.ok(ApiResponse.success(true));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除预算明细信息失败: " + e.getMessage()));
        }
    }

    /**
     * 检查预算明细是否超出总预算
     * @param budgetId 预算ID
     * @param classId 分类ID
     * @param amount 预算金额
     * @param id 预算明细ID（如果是更新操作）
     * @return 包含检查结果的响应实体
     */
    @GetMapping("/checkLimit")
    public ResponseEntity<ApiResponse<Double>> checkLimit(
            @RequestParam Long budgetId,
            @RequestParam Long classId,
            @RequestParam Double amount,
            @RequestParam(required = false) Long id) {
        if (budgetId == null || budgetId <= 0 || classId == null || classId <= 0 || amount == null) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("检查预算明细是否超出总预算: budgetId={}, classId={}, amount={}, id={}", budgetId, classId, amount, id);
        try {
            Double exceedAmount = budgetItemService.checkBudgetLimit(budgetId, classId, amount, id);
            return ResponseEntity.ok(ApiResponse.success(exceedAmount));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("检查预算明细失败: " + e.getMessage()));
        }
    }

    /**
     * 更新预算明细状态
     * @param id 预算明细ID
     * @param status 状态值（0:启用, 1:停用）
     * @return 包含更新结果的响应实体
     */
    @PostMapping("/updateStatus")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(
            @RequestParam Long id,
            @RequestParam Integer status) {
        if (id == null || id <= 0 || status == null || (status != 0 && status != 1)) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        log.info("更新预算明细状态: id={}, status={}", id, status);
        try {
            Boolean success = budgetItemService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(success));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新预算明细状态失败: " + e.getMessage()));
        }
    }
}