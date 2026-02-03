package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.request.CreateBillRequest;
import cn.lizongyi.shareaccount.request.QueryBillListRequest;
import cn.lizongyi.shareaccount.response.BillListWithStatisticsResponse;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.response.ClassResponse;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.response.AccountResponse;
import cn.lizongyi.shareaccount.response.MonthlyStatisticsResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.BillService;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.LedgerService;
import cn.lizongyi.shareaccount.services.AccountService;
import cn.lizongyi.shareaccount.services.UserService;
import cn.lizongyi.shareaccount.response.UserResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import cn.lizongyi.shareaccount.util.DateUtil;
import cn.lizongyi.shareaccount.enums.BudgetNameEnum;


@Service
@Slf4j
public class BillServiceImpl implements BillService {

    @Resource
    private BillMapper billMapper;

    @Resource
    private BaseHandler baseHandler;

    @Resource
    private ClassEntityService classEntityService;
    
    @Resource
    private LedgerService ledgerService;
    
    @Resource
    private AccountService accountService;
    @Resource
    private UserService userService;

    @Override
    public List<Bill> findAll() {
        return billMapper.findAll();
    }

    @Override
    public Bill findById(Long id) {
        return billMapper.findById(id);
    }

    @Override
    public List<Bill> findByUserId(Long userId) {
        return billMapper.findByUserId(userId);
    }

    @Override
    public List<Bill> findByLedgerId(Long ledgerId) {
        return billMapper.findByLedgerId(ledgerId);
    }

    @Override
    public Boolean save(CreateBillRequest request) {
        if (request == null) {
            return false;
        }
        
        // 如果请求中包含id，则执行更新操作
        if (request.getId() != null && request.getId() > 0) {
            return updateBill(request);
        } else {
            // 否则执行创建操作
            return createBill(request);
        }
    }
    
    /**
     * 创建新账单
     */
    private Boolean createBill(CreateBillRequest request) {
        Bill bill = new Bill();
        
        // 计算最顶层分类ID
        Long topClassId = null;
        if (request.getClassId() != null && request.getClassId() > 0) {
            topClassId = classEntityService.getTopClassId(request.getClassId());
        }
        
        // 处理金额：当顶层分类ID=1时（支出账单），保存金额为负数
        Long finalPrice = request.getPrice();
        if (topClassId != null && topClassId == 1L && finalPrice != null && finalPrice > 0) {
            finalPrice = -finalPrice;
        }
        
        bill.setUserId(baseHandler.getUserId())
                .setLedgerId(request.getLedgerId())
                .setAccountId(request.getAccountId())
                .setClassId(request.getClassId())
                .setTopClassId(topClassId)
                .setIsBudget(request.getIsBudget())
                .setBillTime(request.getBillTime() != null ? request.getBillTime() : LocalDateTime.now())
                .setPrice(finalPrice)
                .setStatus(request.getStatus())
                .setMemo(request.getMemo())
                .setCreateTime(LocalDateTime.now());
        
        int result = billMapper.insert(bill);

        if (result > 0) {
            request.setId(bill.getId());
        }

        return result > 0;
    }
    
    /**
     * 更新账单
     */
    private Boolean updateBill(CreateBillRequest request) {
        // 先查询原账单
        Bill bill = billMapper.findById(request.getId());
        if (bill == null) {
            return false;
        }
        
        // 权限校验：仅账单创建人允许更新
        if (!verifyUserAccess(bill.getUserId())) {
            log.warn("无权限更新账单: billId={}, currentUserId={}", bill.getId(), baseHandler.getUserId());
            return false;
        }
        
        // 计算最顶层分类ID
        Long topClassId = null;
        if (request.getClassId() != null && request.getClassId() > 0) {
            topClassId = classEntityService.getTopClassId(request.getClassId());
        }
        
        // 处理金额：当顶层分类ID=1时（支出账单），保存金额为负数
        Long finalPrice = request.getPrice();
        if (topClassId != null && topClassId == 1L && finalPrice != null && finalPrice > 0) {
            finalPrice = -finalPrice;
        }
        
        // 更新账单信息
        bill.setLedgerId(request.getLedgerId())
                .setAccountId(request.getAccountId())
                .setClassId(request.getClassId())
                .setTopClassId(topClassId)
                .setIsBudget(request.getIsBudget())
                .setBillTime(request.getBillTime())
                .setPrice(finalPrice)
                .setMemo(request.getMemo());
        
        int result = billMapper.update(bill);
        return result > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        // 先查询账单并校验权限
        Bill existingBill = billMapper.findById(id);
        if (existingBill == null) {
            log.warn("删除账单失败: 账单不存在 billId={}", id);
            return false;
        }
        if (!verifyUserAccess(existingBill.getUserId())) {
            log.warn("无权限删除账单: billId={}, currentUserId={}", id, baseHandler.getUserId());
            return false;
        }

        if (existingBill.getStatus() == 0) {
            int result = billMapper.deleteById(id);
            return result > 0;
        }
        if (existingBill.getStatus() == 1) {
            int result = billMapper.deleteCompletelyById(id);
            return result > 0;
        }
        log.warn("删除账单失败: 未知状态 billId={}  status={}", id, existingBill.getStatus());
        return false;
    }
    
    @Override
    public MonthlyStatisticsResponse getMonthlyStatisticsByLedgerId(Long ledgerId) {
        if (ledgerId == null || ledgerId <= 0) {
            return null;
        }
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewLedger(currentUserId, ledgerId)) {
            log.info("无权限查看该账本统计: userId={}, ledgerId={}", currentUserId, ledgerId);
            return null;
        }
        
        // 获取当前年份和月份
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        // 获取本月第一天和最后一天的时间范围
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(year, month, startOfMonth.toLocalDate().lengthOfMonth(), 23, 59, 59);
        
        // 查询该账本下的所有账单
        List<Bill> bills = billMapper.findByLedgerIdAndTimeRange(ledgerId, startOfMonth, endOfMonth);
        
        long income = 0;
        long expense = 0;
        for (Bill bill : bills) {
            if (bill.getStatus() == 0) {
                Long price = bill.getPrice();
                if (price != null) {
                    if (price > 0) {
                        income += price;
                    } else {
                        expense += Math.abs(price);
                    }
                }
            }
        }
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setLedgerId(ledgerId);
        response.setYear(year);
        response.setMonth(month);
        response.setIncome(income);
        response.setExpense(expense);
        response.setBalance(income - expense);
        return response;
    }
    
    @Override
    public List<BillResponse> getBillResponsesByLedgerIdAndTimeRange(Long ledgerId, LocalDateTime startTime, LocalDateTime endTime) {
        if (ledgerId == null || ledgerId <= 0 || startTime == null || endTime == null) {
            return List.of();
        }
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewLedger(currentUserId, ledgerId)) {
            log.info("无权限查看账本时间范围账单: userId={}, ledgerId={}", currentUserId, ledgerId);
            return List.of();
        }
        List<Bill> bills = billMapper.findByLedgerIdAndTimeRange(ledgerId, startTime, endTime);
        return bills.stream()
                .filter(bill -> bill.getStatus() == 0)
                .map(this::convertBillToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public java.util.Map<String, Long> getUserMonthlyTotalExpense(Long userId, Integer year, Integer month, Long ledgerId) {
        java.util.Map<String, Long> result = new java.util.HashMap<>();
        result.put("budgetIncluded", 0L);
        result.put("budgetExcluded", 0L);
        
        if (userId == null || userId <= 0) {
            log.warn("获取用户支出统计失败: userId 为空或无效");
            return result;
        }
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewLedger(currentUserId, ledgerId)) {
            log.info("无权限查看该账本的统计: currentUserId={}, ledgerId={}", currentUserId, ledgerId);
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        LocalDateTime startOfMonth = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(targetYear, targetMonth, startOfMonth.toLocalDate().lengthOfMonth(), 23, 59, 59);
        
        // 不按是否计入预算筛选，查询该账本所有支出账单
        List<Bill> bills = billMapper.findByUserIdAndConditions(
                null,
                null,
                startOfMonth,
                endOfMonth,
                ledgerId,
                null,
                null,
                null,
                null,
                0,
                1 // 默认按时间由近到远排序
        );
        long included = 0L;
        long excluded = 0L;
        for (Bill bill : bills) {
            Long price = bill.getPrice();
            if (price != null && price < 0) {
                long abs = Math.abs(price);
                Integer budgetFlag = bill.getIsBudget();
                if (budgetFlag != null && budgetFlag == 1) {
                    excluded += abs;
                } else {
                    // 默认视为计入预算（含0或null）
                    included += abs;
                }
            }
        }
        result.put("budgetIncluded", included);
        result.put("budgetExcluded", excluded);
        return result;
    }
    
    @Override
    public List<BillResponse> getAllBillResponsesByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (userId == null || userId <= 0 || startTime == null || endTime == null) {
            return List.of();
        }
        
        // 查询该用户下指定时间范围内的所有账单（不区分账本和账户，包含所有状态）
        List<Bill> bills = billMapper.findByUserIdAndTimeRange(userId, startTime, endTime);
        
        // 将所有账单转换为带分类信息的响应对象
        // 注意：这里不进行状态过滤，包含所有状态的账单
        // 排序已在数据库查询中完成（ORDER BY bill_time DESC）
        return bills.stream()
                .map(this::convertBillToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public BillResponse findBillResponseById(Long billId) {
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewBill(currentUserId, billId)) {
            log.info("无权限查看账单: userId={}, billId={}", currentUserId, billId);
            return null;
        }
        Bill bill = billMapper.findById(billId);
        if (bill == null) {
            return null;
        }
        return convertBillToResponse(bill);
    }
    
    /**
     * 根据用户ID查询带分类信息的账单响应列表
     * @param userId 用户ID
     * @return 带分类信息的账单响应列表
     */
    @Override
    public List<BillResponse> findBillResponsesByUserId(Long userId) {
        List<Bill> bills = billMapper.findByUserId(userId);
        return bills.stream()
                .map(bill -> convertBillToResponse(bill))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BillResponse> findBillResponsesByLedgerId(Long ledgerId) {
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewLedger(currentUserId, ledgerId)) {
            log.info("无权限查看该账本的账单: userId={}, ledgerId={}", currentUserId, ledgerId);
            return List.of();
        }
        List<Bill> bills = billMapper.findByLedgerId(ledgerId);
        return bills.stream()
                .map(this::convertBillToResponse)
                .collect(Collectors.toList());
    }
    

    @Override
    public BillListWithStatisticsResponse<BillResponse> getBillListWithPagination(QueryBillListRequest request) {
        if (request == null) {
            return new BillListWithStatisticsResponse<>();
        }
        try {
            Long currentUserId = baseHandler.getUserId();
            // 权限校验：按账本筛选
            if (request.getLedgerId() != null && request.getLedgerId() > 0) {
                if (!baseHandler.canViewLedger(currentUserId, request.getLedgerId())) {
                    log.info("无权限查看该账本的账单: userId={}, ledgerId={}", currentUserId, request.getLedgerId());
                    return new BillListWithStatisticsResponse<>();
                }
            }
            // 权限校验：按账单ID集合筛选
            if (request.getBillIds() != null && !request.getBillIds().isEmpty()) {
                for (Long bid : request.getBillIds()) {
                    if (!baseHandler.canViewBill(currentUserId, bid)) {
                        log.info("存在无权限查看的账单: userId={}, billId={}", currentUserId, bid);
                        return new BillListWithStatisticsResponse<>();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("分页查询前置权限校验失败: {}", e.getMessage());
        }

        try {
            // 分页参数健壮性
            Integer pageNum = request.getPageNum();
            Integer pageSize = request.getPageSize();
            pageNum = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
            pageSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;

            // 时间参数默认值与解析
            LocalDate now = LocalDate.now();
            if (request.getStartTime() == null || request.getStartTime().isEmpty()) {
                request.setStartTime(now.minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            if (request.getEndTime() == null || request.getEndTime().isEmpty()) {
                // 今天
                //request.setEndTime(now.format(DateTimeFormatter.ISO_LOCAL_DATE));

                // 设置为当月最后一天
                request.setEndTime(now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            final LocalDateTime startDt = parseDateTime(request.getStartTime());
            final LocalDateTime endRaw = parseDateTime(request.getEndTime());
            // 若仅传日期（yyyy-MM-dd），将结束时间扩展到当日末尾
            final LocalDateTime endDt = (request.getEndTime() != null && request.getEndTime().length() == 10 && request.getEndTime().contains("-"))
                    ? endRaw.withHour(23).withMinute(59).withSecond(59)
                    : endRaw;

            log.info("分页查询账单列表，请求参数：{}", request);

            // 如果没传递账本id，默认查询当前用户可访问的 所有账本
            java.util.List<Long> accessibleLedgerIdsForPaging = null;
            if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
                accessibleLedgerIdsForPaging = baseHandler.getAccessibleLedgerIdsForCurrentUser();
                if (accessibleLedgerIdsForPaging == null || accessibleLedgerIdsForPaging.isEmpty()) {
                    accessibleLedgerIdsForPaging = java.util.Collections.singletonList(-1L);
                }
            }

             // 查询分类下面的子分类
             List<Long> childCategoryIds = baseHandler.getChildCategoryIds(request.getCategoryId());

             // 分页查询列表
             com.github.pagehelper.PageHelper.startPage(pageNum, pageSize);
             java.util.List<Bill> billPageList;
             // 按账单ID集合筛选
             if (request.getBillIds() != null && !request.getBillIds().isEmpty()) {
                 billPageList = billMapper.findByIds(request.getBillIds(), request.getSortBy());
             } else if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
                // 按可访问账本ID集合筛选
                billPageList = billMapper.findByLedgerIdsAndConditions(
                        accessibleLedgerIdsForPaging,
                         startDt,
                         endDt,
                         request.getAccountId(),
                         childCategoryIds,
                         request.getIsBudget(),
                         null,
                         request.getStatus(),
                         request.getSortBy()
                 );
             } else {
                // 按固定 账本id筛选
                 billPageList = billMapper.findByUserIdAndConditions(
                         null,
                         null,
                         startDt,
                         endDt,
                         request.getLedgerId(),
                         request.getAccountId(),
                         childCategoryIds,
                         request.getIsBudget(),
                         null,
                         request.getStatus(),
                         request.getSortBy()
                 );
             }
             com.github.pagehelper.PageInfo<Bill> pageBills = new com.github.pagehelper.PageInfo<>(billPageList);
            // 将账单转换为响应对象，并保留分页元数据
            List<BillResponse> billResponses = pageBills.getList().stream().map(this::convertBillToResponse).collect(Collectors.toList());
            com.github.pagehelper.PageInfo<BillResponse> responsePageInfo = new com.github.pagehelper.PageInfo<>(billResponses);
            responsePageInfo.setTotal(pageBills.getTotal());
            responsePageInfo.setPageNum(pageBills.getPageNum());
            responsePageInfo.setPageSize(pageBills.getPageSize());


            // 统计总收入/总支出（不分页查询，保持与列表筛选条件一致）----------------------------------------
            com.github.pagehelper.PageHelper.clearPage();
            java.util.List<Bill> sumBills;

            // 按账单ID集合筛选
            if (request.getBillIds() != null && !request.getBillIds().isEmpty()) {
                 sumBills = billMapper.findByIds(request.getBillIds(), request.getSortBy());
             } else if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
                // 按可访问账本ID集合筛选
                 sumBills = billMapper.findByLedgerIdsAndConditions(
                         accessibleLedgerIdsForPaging,
                         startDt,
                         endDt,
                         request.getAccountId(),
                         childCategoryIds,
                         request.getIsBudget(),
                         null,
                         request.getStatus(),
                         request.getSortBy()
                 );
             } else {
                // 按固定 账本id筛选
                 sumBills = billMapper.findByUserIdAndConditions(
                         null,
                         null,
                         startDt,
                         endDt,
                         request.getLedgerId(),
                         request.getAccountId(),
                         childCategoryIds,
                         request.getIsBudget(),
                         null,
                         request.getStatus(),
                         request.getSortBy()
                 );
             }
             long totalIncome = 0L;
             long totalExpense = 0L;
             if (sumBills != null) {
                 for (Bill b : sumBills) {
                     Long price = b.getPrice();
                     if (price == null) continue;
                     if (price > 0) {
                         totalIncome += price;
                     } else if (price < 0) {
                         totalExpense += Math.abs(price);
                     }
                 }
             }
            BillListWithStatisticsResponse<BillResponse> statisticsResponse = new BillListWithStatisticsResponse<>(responsePageInfo);
            statisticsResponse.setTotalIncome(totalIncome);
            statisticsResponse.setTotalExpense(totalExpense);
            return statisticsResponse;
        } catch (Exception e) {
            log.error("分页查询账单列表失败: {}", e);
            return new BillListWithStatisticsResponse<>();
        }
    }

    @Override
    public Boolean updateStatus(Long billId, Integer status) {
        try {
            // 验证账单是否存在
            Bill existingBill = billMapper.findById(billId);
            if (existingBill == null) {
                log.warn("更新账单状态失败: 账单不存在 billId={}", billId);
                return false;
            }
            
            // 验证用户访问权限
            if (!verifyUserAccess(existingBill.getUserId())) {
                log.warn("无权限更新账单状态: billId={}, currentUserId={}", billId, baseHandler.getUserId());
                return false;
            }
            
            // 更新账单状态
            int rows = billMapper.updateStatus(billId, status);
            
            if (rows > 0) {
                log.info("更新账单状态成功: billId={}, status={}", billId, status);
                return true;
            } else {
                log.error("更新账单状态失败: 数据库更新失败");
                return false;
            }
        } catch (Exception e) {
            log.error("更新账单状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证用户是否有权限访问该资源
     */
    private boolean verifyUserAccess(Long targetUserId) {
        Long currentUserId = baseHandler.getUserId();
        return currentUserId != null && currentUserId.equals(targetUserId);
    }
    
    /**
     * 解析日期时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        // 先尝试完整的日期时间格式
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            // 如果失败，尝试只包含日期的格式
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateTimeStr, dateFormatter);
                return date.atStartOfDay(); // 设置为当天的开始时间
            } catch (Exception ex) {
                throw new RuntimeException("日期时间格式不正确", ex);
            }
        }
    }
    
    /**
     * 将Bill对象转换为BillResponse对象
     */
    private BillResponse convertBillToResponse(Bill bill) {
        if (bill == null) {
            return null;
        }
        
        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setUserId(bill.getUserId());
        response.setLedgerId(bill.getLedgerId());
        response.setAccountId(bill.getAccountId());
        response.setClassId(bill.getClassId());
        response.setTopClassId(bill.getTopClassId());
        response.setIsBudget(bill.getIsBudget());
        response.setIsBudgetName(BudgetNameEnum.fromId(bill.getIsBudget()).getName());
        response.setBillTime(DateUtil.localDateTimeToString(bill.getBillTime()));
        response.setPrice(bill.getPrice());
        response.setStatus(bill.getStatus());
        response.setMemo(bill.getMemo());
        response.setCreateTime(DateUtil.localDateTimeToString(bill.getCreateTime()));
        // 设置创建人名称
        if (bill.getUserId() != null) {
            try {
                UserResponse user = userService.findResponseById(bill.getUserId());
                if (user != null) {
                    response.setCreateUserName(user.getNickName());
                }
            } catch (Exception e) {
                log.error("获取创建人名称失败, billId={}, userId={}, err={}", bill.getId(), bill.getUserId(), e.getMessage());
            }
        }
        
        // 查询并设置分类信息
        if (bill.getClassId() != null && bill.getClassId() > 0) {
            ClassResponse classResponse = classEntityService.selectById(bill.getClassId());
            if (classResponse != null) {
                response.setClassName(classResponse.getName());
                response.setClassIcon(classResponse.getIcon());
            }
        }
        
        // 查询并设置账本信息
        if (bill.getLedgerId() != null && bill.getLedgerId() > 0) {
            Ledger ledger = ledgerService.findById(bill.getLedgerId());
            if (ledger != null) {
                response.setLedgerName(ledger.getName());
            }
        }
        
        // 查询并设置账户信息
        if (bill.getAccountId() != null && bill.getAccountId() > 0) {
            AccountResponse accountResponse = accountService.findResponseById(bill.getAccountId());
            if (accountResponse != null) {
                response.setAccountTypeName(accountResponse.getTypeName());
                response.setAccountName(accountResponse.getName());
            }
        }
        
        return response;
    }
}