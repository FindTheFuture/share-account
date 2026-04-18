package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.request.CreateBillRequest;
import cn.lizongyi.shareaccount.request.QueryBillListRequest;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.response.BillListWithStatisticsResponse;
import cn.lizongyi.shareaccount.response.MonthlyStatisticsResponse;

import java.util.List;

public interface BillService {
    /**
     * 查询所有账单信息
     * @return 账单实体列表
     */
    List<cn.lizongyi.shareaccount.entity.Bill> findAll();

    /**
     * 根据 ID 查询账单信息
     * @param id 账单 ID
     * @return 账单实体
     */
    cn.lizongyi.shareaccount.entity.Bill findById(Long id);

    /**
     * 根据用户 ID 查询该用户的账单列表
     * @param userId 用户 ID
     * @return 账单实体列表
     */
    List<cn.lizongyi.shareaccount.entity.Bill> findByUserId(Long userId);


    /**
     * 根据用户 ID 统计该用户的账单数量
     * @param userId 用户 ID
     * @return 账单数量
     */
    int countByUserId(Long userId);

    /**
     * 根据账本 ID 查询该账本的账单列表
     * @param ledgerId 账本 ID
     * @return 账单实体列表
     */
    List<cn.lizongyi.shareaccount.entity.Bill> findByLedgerId(Long ledgerId);

    /**
     * 保存账单（创建或更新）
     * @param request 账单请求对象，包含id时更新，不包含id时创建
     * @return 操作成功返回 true，失败返回 false
     */
    Boolean save(CreateBillRequest request);

    /**
     * 根据 ID 删除账单（逻辑删除）
     * @param id 账单 ID
     * @return 删除成功返回 true，失败返回 false
     */
    Boolean deleteById(Long id);
    
    /**
     * 根据用户ID查询带分类信息的账单响应列表
     * @param userId 用户ID
     * @return 带分类信息的账单响应列表
     */
    List<BillResponse> findBillResponsesByUserId(Long userId);
    
    /**
     * 根据账本ID查询带分类信息的账单响应列表
     * @param ledgerId 账本ID
     * @return 带分类信息的账单响应列表
     */
    List<BillResponse> findBillResponsesByLedgerId(Long ledgerId);
    
    /**
     * 根据账单ID查询带分类信息的账单响应对象
     * @param billId 账单ID
     * @return 带分类信息的账单响应对象
     */
    BillResponse findBillResponseById(Long billId);
    
    /**
     * 获取账本指定年月收支统计
     * @param ledgerId 账本ID
     * @param year 年份
     * @param month 月份
     * @return 包含收入、支出和结余的统计信息
     */
    MonthlyStatisticsResponse getMonthlyStatisticsByLedgerId(Long ledgerId, Integer year, Integer month);
    

    
    /**
     * 根据账本ID和时间范围查询账单列表
     * @param ledgerId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 带分类信息的符合时间范围的账单响应列表
     */
    List<BillResponse> getBillResponsesByLedgerIdAndTimeRange(Long ledgerId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 获取用户指定月份支出拆分（计入预算/不计入预算），按账本筛选
     * @param userId 用户ID
     * @param year 年份（可选，默认当前年份）
     * @param month 月份（可选，默认当前月份）
     * @param ledgerId 账本ID（必填）
     * @return Map结构，key为"budgetIncluded"与"budgetExcluded"，单位：分
     */
    java.util.Map<String, Long> getUserMonthlyTotalExpense(Long userId, Integer year, Integer month, Long ledgerId);
    
    /**
     * 根据用户ID和时间范围查询所有账单（不区分账本和账户，包含所有状态）
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 带分类信息的账单响应列表
     */
    List<BillResponse> getAllBillResponsesByUserIdAndTimeRange(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 使用请求实体类分页查询用户账单列表（支持多条件筛选）
     * @param request 查询账单列表请求实体
     * @return 带分类信息的账单响应分页结果
     */
    BillListWithStatisticsResponse<BillResponse> getBillListWithPagination(QueryBillListRequest request);
    
    /**
     * 更新账单状态
     * @param billId 账单ID
     * @param status 账单状态
     * @return 操作成功返回 true，失败返回 false
     */
    Boolean updateStatus(Long billId, Integer status);
}