package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Budget;
import cn.lizongyi.shareaccount.request.CreateBudgetRequest;
import cn.lizongyi.shareaccount.response.BudgetResponse;

import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService {

    /**
     * 查询所有预算
     * @return 预算列表
     */
    List<Budget> findAll();

    /**
     * 根据ID查询预算
     * @param id 预算ID
     * @return 预算对象
     */
    Budget findById(Long id);

    /**
     * 根据用户ID查询预算列表
     * @param userId 用户ID
     * @return 预算列表
     */
    List<Budget> findByUserId(Long userId);

    /**
     * 根据用户ID、年份和月份查询预算
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 预算对象
     */
    Budget findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    /**
     * 创建或更新预算
     * @param request 预算请求对象
     * @return 是否成功
     */
    Boolean save(CreateBudgetRequest request);

    /**
     * 删除预算
     * @param id 预算ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询预算响应对象
     * @param id 预算ID
     * @return 预算响应对象
     */
    BudgetResponse findResponseById(Long id);

    /**
     * 获取用户的预算列表响应对象
     * @param userId 用户ID
     * @return 预算响应对象列表
     */
    List<BudgetResponse> getUserBudgetList(Long userId);

    /**
     * 根据用户ID、年份和月份查询预算响应对象
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 预算响应对象
     */
    BudgetResponse findResponseByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    
    /**
     * 更新预算总金额
     * @param budgetId 预算ID
     */
    void updateTotalBalance(Long budgetId);
    
    Budget findByYearMonthAndLedger(Integer year, Integer month, Long ledgerId);

    BudgetResponse findResponseByYearMonthAndLedger(Integer year, Integer month, Long ledgerId);
    
    /**
     * 同步指定年月的预算到目标年月
     * @param userId 用户ID
     * @param targetYear 目标年份
     * @param targetMonth 目标月份
     * @param sourceYear 源年份
     * @param sourceMonth 源月份
     * @return 是否同步成功
     */
    Boolean syncLastMonthBudget(Long userId, Integer targetYear, Integer targetMonth, Integer sourceYear, Integer sourceMonth);
}