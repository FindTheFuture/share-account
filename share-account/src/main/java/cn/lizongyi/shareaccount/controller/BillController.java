package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateBillRequest;
import cn.lizongyi.shareaccount.request.QueryBillListRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.BillListWithStatisticsResponse;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.response.MonthlyStatisticsResponse;
import cn.lizongyi.shareaccount.services.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cn.lizongyi.shareaccount.services.UserIdService;
import cn.lizongyi.shareaccount.services.BaseHandler;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bill")
@Slf4j
public class BillController {

    @Autowired
    private BillService billService;

    @Autowired
    private UserIdService userIdService;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private cn.lizongyi.shareaccount.services.BillCommentService billCommentService;

    /**
     * 根据 ID 查询账单信息（带分类名称和图标）
     * @param billId 账单 ID
     * @return 包含账单信息的响应实体
     */
    @GetMapping("/getById/{billId}")
    public ResponseEntity<ApiResponse<BillResponse>> getById(@PathVariable Long billId) {
        if (billId == null || billId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        
        log.info("根据账单 id:{}} 查询账单信息", billId);
        BillResponse billResponse = billService.findBillResponseById(billId);
        if (billResponse == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到账单信息"));
        }
        return ResponseEntity.ok(ApiResponse.success(billResponse));
    }
    
    
    /**
     * 根据用户 ID 查询账单列表
     * @param userId 用户 ID
     * @return 包含账单列表的响应实体
     */
    @GetMapping("/getByUserId/{userId}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByUserId(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        Long currentUserId = baseHandler.getUserId();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.ok(ApiResponse.error("无权限查询该用户的账单"));
        }
        log.info("根据用户 id:{} 查询账单列表", userId);
        List<BillResponse> billResponses = billService.findBillResponsesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(billResponses));
    }

    /**
     * 根据账本 ID 查询账单列表
     * @param ledgerId 账本 ID
     * @return 包含账单列表的响应实体
     */
    @GetMapping("/getByLedgerId/{ledgerId}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByLedgerId(@PathVariable Long ledgerId) {
        if (ledgerId == null || ledgerId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        
        log.info("根据账本 id:{} 查询账单列表", ledgerId);
        List<BillResponse> billResponses = billService.findBillResponsesByLedgerId(ledgerId);
        return ResponseEntity.ok(ApiResponse.success(billResponses));
    }

    /**
     * 保存账单（创建或更新）
     * @param request 账单请求对象
     * @return 响应结果
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateBillRequest request) {
        log.info("保存账单请求: {}", request);
        
        // 参数校验
        if (request == null) {
            log.warn("保存账单请求参数为空");
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("请求参数不能为空"));
        }
        if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
            log.warn("保存账单账本ID不合法");
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("账本ID不能为空且必须大于0"));
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            log.warn("保存账单金额不合法");
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("金额不能为空且必须大于0"));
        }
        Long currentUserId = baseHandler.getUserId();
        // 更新操作：仅账单创建人允许
        if (request.getId() != null && request.getId() > 0) {
            cn.lizongyi.shareaccount.entity.Bill existing = billService.findById(request.getId());
            if (existing == null) {
                return ResponseEntity.ok(ApiResponse.badRequest("未找到账单信息"));
            }
            if (!currentUserId.equals(existing.getUserId())) {
                return ResponseEntity.ok(ApiResponse.error("仅账单创建人可更新该账单"));
            }
        }
        
        try {
            Boolean result = billService.save(request);
            String message = (request.getId() != null && request.getId() > 0) ? "更新成功" : "创建成功";
            
            if (result) {
                log.info(message);
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                log.error(message + "失败");
                return ResponseEntity.ok(ApiResponse.error(message + "失败"));
            }
        } catch (Exception e) {
            log.error("保存账单异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("保存失败"));
        }
    }

    /**
     * 根据 ID 删除账单（逻辑删除）
     * @param billId 账单 ID
     * @return 删除结果的响应实体
     */
    @PostMapping("/deleteById/{billId}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long billId) {
        if (billId == null || billId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        Long currentUserId = baseHandler.getUserId();
        cn.lizongyi.shareaccount.entity.Bill existing = billService.findById(billId);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到账单信息"));
        }
        if (!currentUserId.equals(existing.getUserId())) {
            return ResponseEntity.ok(ApiResponse.error("仅账单创建人可删除该账单"));
        }
        log.info("删除账单请求: id = {}", billId);
        Boolean result = billService.deleteById(billId);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除账单失败"));
        }
    }
    
    /**
     * 获取账本本月收支统计
     * @param ledgerId 账本 ID
     * @return 包含收支统计信息的响应实体
     */
    @GetMapping("/getMonthlyStatisticsByLedgerId/{ledgerId}")
    public ResponseEntity<ApiResponse<MonthlyStatisticsResponse>> getMonthlyStatisticsByLedgerId(@PathVariable Long ledgerId) {
        if (ledgerId == null || ledgerId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        
        log.info("获取账本 id:{} 的本月收支统计", ledgerId);
        
        MonthlyStatisticsResponse statistics = billService.getMonthlyStatisticsByLedgerId(ledgerId);
        if (statistics == null) {
            return ResponseEntity.ok(ApiResponse.error("无权限或未找到统计信息"));
        }
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    

    
    /**
     * 获取用户指定月份支出拆分（计入预算/不计入预算）
     * @param year 年份（可选，默认当前年份）
     * @param month 月份（可选，默认当前月份）
     * @param ledgerId 账本ID 必填
     * @return Map结构：{"budgetIncluded": 分, "budgetExcluded": 分}
     */
    @GetMapping("/getUserMonthlyTotalExpense")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserMonthlyTotalExpense(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = true) Long ledgerId) {
        Long userId = userIdService.getUserId();
        if (ledgerId == null || ledgerId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }

        log.info("获取用户 id:{} 的指定月份[{}年{}月]支出拆分，账本筛选:{}",
                userId,
                year != null ? year : "当前",
                month != null ? month : "当前",
                ledgerId);
        
        Map<String, Long> expenseSplit = billService.getUserMonthlyTotalExpense(userId, year, month, ledgerId);
        return ResponseEntity.ok(ApiResponse.success(expenseSplit));
    }
    
    /**
     * 根据账本ID和时间范围查询账单列表
     * @param ledgerId 账本ID
     * @param startTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 包含符合时间范围的账单列表的响应实体
     */
    @GetMapping("/getByLedgerIdAndTimeRange")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByLedgerIdAndTimeRange(
            @RequestParam Long ledgerId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        if (ledgerId == null || ledgerId <= 0) {
            return ResponseEntity.ok(ApiResponse.error("账本ID不正确"));
        }
        if (startTime == null || startTime.isEmpty() || endTime == null || endTime.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("时间范围参数不能为空"));
        }
        
        
        try {
            // 解析时间参数，支持两种格式
            LocalDateTime startDateTime = parseDateTime(startTime);
            LocalDateTime endDateTime = parseDateTime(endTime);
            
            // 如果是日期格式，需要设置结束时间为当天的23:59:59
            if (startTime.length() == 10 && startTime.contains("-")) {
                endDateTime = endDateTime.withHour(23).withMinute(59).withSecond(59);
            }
            
            log.info("获取账本 id:{}\n 在时间范围 {} 到 {} 的账单列表", ledgerId, startTime, endTime);
            
            List<BillResponse> billResponses = billService.getBillResponsesByLedgerIdAndTimeRange(ledgerId, startDateTime, endDateTime);
            return ResponseEntity.ok(ApiResponse.success(billResponses));
        } catch (Exception e) {
            log.error("解析时间参数异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("时间格式不正确，请使用yyyy-MM-dd或yyyy-MM-dd HH:mm:ss格式"));
        }
    }
    
    /**
     * 分页查询用户账单列表（支持多条件筛选）
     * @param request 查询账单列表请求实体
     * @return 包含符合条件的账单列表的分页响应实体
     */
    @PostMapping("/listWithPagination")
    public ResponseEntity<ApiResponse<BillListWithStatisticsResponse<BillResponse>>> getBillListWithPagination(@RequestBody QueryBillListRequest request) {
        try {
            // 参数必填校验
            if (request == null) {
                return ResponseEntity.ok(ApiResponse.error("请求参数不能为空"));
            }

            // 分页默认值与最大值限制
            if (request.getPageNum() == null || request.getPageNum() <= 0) {
                request.setPageNum(1);
            }
            if (request.getPageSize() == null || request.getPageSize() <= 0 || request.getPageSize() > 10) {
                request.setPageSize(10);
            }

            BillListWithStatisticsResponse<BillResponse> pageInfo = billService.getBillListWithPagination(request);
            return ResponseEntity.ok(ApiResponse.success(pageInfo));
        } catch (Exception e) {
            log.error("分页查询账单异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("查询账单失败，请稍后重试"));
        }
    }
    
    /**
     * 解析日期时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        // 先尝试完整的日期时间格式
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            // 如果失败，尝试只包含日期的格式
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateTimeStr, dateFormatter);
            return date.atStartOfDay(); // 设置为当天的开始时间
        }
    }
    
    /**
     * 更新账单状态
     * @param billId 账单ID
     * @param status 状态值（0:启用, 1:停用）
     * @return 包含更新结果的响应实体
     */
    @PostMapping("/updateStatus")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestBody Map<String, Object> request) {
        Long billId = request.get("billId") != null ? Long.valueOf(request.get("billId").toString()) : null;
        Integer status = request.get("status") != null ? Integer.valueOf(request.get("status").toString()) : null;
        if (billId == null || billId <= 0 || status == null || (status != 0 && status != 1)) {
            log.warn("更新账单状态参数不正确: billId={}, status={}", billId, status);
            return ResponseEntity.ok(ApiResponse.error("参数不正确"));
        }
        Long currentUserId = baseHandler.getUserId();
        cn.lizongyi.shareaccount.entity.Bill existing = billService.findById(billId);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.badRequest("未找到账单信息"));
        }
        if (!currentUserId.equals(existing.getUserId())) {
            return ResponseEntity.ok(ApiResponse.error("仅账单创建人可更新状态"));
        }
        
        log.info("更新账单状态: billId={}, status={}", billId, status);
        try {
            Boolean success = billService.updateStatus(billId, status);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(true));
            } else {
                return ResponseEntity.ok(ApiResponse.error("更新账单状态失败"));
            }
        } catch (Exception e) {
            log.error("更新账单状态异常: " + e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("更新账单状态失败: " + e.getMessage()));
        }
    }


}