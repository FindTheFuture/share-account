package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.BudgetItem;
import cn.lizongyi.shareaccount.request.CreateBudgetItemRequest;
import cn.lizongyi.shareaccount.response.BudgetItemResponse;

import java.util.List;

/**
 * 预算明细服务接口
 */
public interface BudgetItemService {

    /**
     * 查询所有预算明细
     * @return 预算明细列表
     */
    List<BudgetItem> findAll();

    /**
     * 根据ID查询预算明细
     * @param id 预算明细ID
     * @return 预算明细对象
     */
    BudgetItem findById(Long id);

    /**
     * 根据预算ID查询预算明细列表
     * @param budgetId 预算ID
     * @return 预算明细列表
     */
    List<BudgetItem> findByBudgetId(Long budgetId);

    /**
     * 根据用户ID查询预算明细列表
     * @param userId 用户ID
     * @return 预算明细列表
     */
    List<BudgetItem> findByUserId(Long userId);

    /**
     * 创建或更新预算明细
     * @param request 预算明细请求对象
     * @return 是否成功
     */
    Boolean save(CreateBudgetItemRequest request);

    /**
     * 检查预算明细是否超出总预算
     * @param budgetId 预算ID
     * @param classId 分类ID
     * @param amount 预算金额
     * @param id 预算明细ID（如果是更新操作）
     * @return 超出的金额，如果返回null表示没有超出
     */
    Double checkBudgetLimit(Long budgetId, Long classId, Double amount, Long id);

    /**
     * 更新预算明细状态
     * @param id 预算明细ID
     * @param status 状态值（0:启用, 1:停用）
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 根据ID删除预算明细
     * @param id 预算明细ID
     */
    void deleteById(Long id);

    /**
     * 根据预算ID删除预算明细
     * @param budgetId 预算ID
     */
    void deleteByBudgetId(Long budgetId);

    /**
     * 根据ID查询预算明细响应对象
     * @param id 预算明细ID
     * @return 预算明细响应对象
     */
    BudgetItemResponse findResponseById(Long id);

    /**
     * 根据预算ID查询预算明细响应对象列表
     * @param budgetId 预算ID
     * @return 预算明细响应对象列表
     */
    List<BudgetItemResponse> getBudgetItemListByBudgetId(Long budgetId);

    /**
     * 按年月+账本ID关联查询预算明细响应对象列表（不依赖userId）
     * @param year 年份
     * @param month 月份
     * @param ledgerId 账本ID
     * @return 预算明细响应对象列表
     */
    List<BudgetItemResponse> getBudgetItemListByYearMonthAndLedger(Integer year, Integer month, Long ledgerId);
}