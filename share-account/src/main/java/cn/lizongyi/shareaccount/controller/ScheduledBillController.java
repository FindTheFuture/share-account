package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.request.CreateScheduledBillRequest;
import cn.lizongyi.shareaccount.request.QueryScheduledBillListRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.ScheduledBillResponse;
import cn.lizongyi.shareaccount.response.ScheduledBillLogResponse;
import cn.lizongyi.shareaccount.services.ScheduledBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 定时记账配置控制器
 */
@RestController
@RequestMapping("/api/scheduledBill")
@Slf4j
public class ScheduledBillController extends BaseController {

    @Autowired
    private ScheduledBillService scheduledBillService;

    /**
     * 保存定时记账配置（新增或更新）
     * @param request 定时记账配置请求参数
     * @return 保存结果
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> save(@RequestBody CreateScheduledBillRequest request) {
        try {
            // 参数校验
            if (request == null) {
                log.warn("保存定时记账配置请求参数为空");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("请求参数不能为空"));
            }
            if (request.getLedgerId() == null || request.getLedgerId() <= 0) {
                log.warn("保存定时记账配置账本ID不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("账本ID不能为空且必须大于0"));
            }
            if (request.getName() == null || request.getName().isEmpty()) {
                log.warn("保存定时记账配置名称为空");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("名称不能为空"));
            }
            if (request.getPrice() == null || request.getPrice() <= 0) {
                log.warn("保存定时记账配置金额不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("金额不能为空且必须大于0"));
            }
            if (request.getClassId() == null || request.getClassId() <= 0) {
                log.warn("保存定时记账配置分类ID不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("分类ID不能为空且必须大于0"));
            }
            if (request.getAccountId() == null || request.getAccountId() <= 0) {
                log.warn("保存定时记账配置账户ID不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("账户ID不能为空且必须大于0"));
            }
            if (request.getCycleType() == null || (request.getCycleType() < 1 || request.getCycleType() > 5)) {
                log.warn("保存定时记账配置周期类型不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("周期类型不能为空且必须在1-5之间"));
            }
            if (request.getCycleValue() == null) {
                log.warn("保存定时记账配置周期值不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("周期值不能为空"));
            }
            if (request.getExecuteTime() == null) {
                log.warn("保存定时记账配置执行时间不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("执行时间不能为空"));
            }
            if (request.getStartDate() == null) {
                log.warn("保存定时记账配置开始日期不合法");
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("开始日期不能为空"));
            }

            Boolean result = scheduledBillService.save(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("保存定时记账配置失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID查询定时记账配置
     * @param id 定时记账配置ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledBillResponse.ScheduledBillResponseItem>> findById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("参数不正确"));
            }
            ScheduledBillResponse.ScheduledBillResponseItem item = scheduledBillService.findById(id);
            if (item == null) {
                return ResponseEntity.ok(ApiResponse.error("定时记账配置不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (Exception e) {
            log.error("查询定时记账配置失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 分页查询定时记账配置列表
     * @param request 查询请求参数
     * @return 查询结果
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<ScheduledBillResponse>> findByUserId(@RequestBody QueryScheduledBillListRequest request) {
        try {
            // 参数校验
            if (request == null) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("请求参数不能为空"));
            }

            // 分页默认值与最大值限制
            if (request.getPageNum() == null || request.getPageNum() <= 0) {
                request.setPageNum(1);
            }
            if (request.getPageSize() == null || request.getPageSize() <= 0 || request.getPageSize() > 20) {
                request.setPageSize(10);
            }

            ScheduledBillResponse response = scheduledBillService.findByUserId(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("查询定时记账配置列表失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除定时记账配置（逻辑删除）
     * @param id 定时记账配置ID
     * @return 删除结果
     */
    @PutMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Integer>> delete(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("参数不正确"));
            }
            int result = scheduledBillService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除定时记账配置失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新定时记账配置状态
     * @param id 定时记账配置ID
     * @param status 状态值
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Integer>> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("参数不正确"));
            }
            // 检查状态值是否合法
            if (status != 1 && status != 2) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("状态值不合法，只能是1（启用）或2（暂停）"));
            }
            int result = scheduledBillService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("更新定时记账配置状态失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID查询定时记账执行日志
     * @param id 定时记账执行日志ID
     * @return 查询结果
     */
    @GetMapping("/log/{id}")
    public ResponseEntity<ApiResponse<ScheduledBillLogResponse.ScheduledBillLogResponseItem>> findLogById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("参数不正确"));
            }
            ScheduledBillLogResponse.ScheduledBillLogResponseItem item = scheduledBillService.findLogById(id);
            if (item == null) {
                return ResponseEntity.ok(ApiResponse.error("定时记账执行日志不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (Exception e) {
            log.error("查询定时记账执行日志失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    
    /**
     * 分页查询定时记账执行日志（新路径）
     * @param id 定时记账配置ID
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 查询结果
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<ScheduledBillLogResponse>> findLogsByScheduledId(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "1") Integer pageNum, 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("参数不正确"));
            }
            if (pageNum == null || pageNum <= 0) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize <= 0 || pageSize > 20) {
                pageSize = 10;
            }
            ScheduledBillLogResponse response = scheduledBillService.findLogsResponseByScheduledId(id, pageNum, pageSize);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("查询定时记账执行日志失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
