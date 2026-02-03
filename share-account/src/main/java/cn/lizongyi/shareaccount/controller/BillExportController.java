package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.PdfExportRecord;
import cn.lizongyi.shareaccount.request.QueryBillListRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.services.BillService;
import cn.lizongyi.shareaccount.services.PdfExportRecordService;
import cn.lizongyi.shareaccount.services.PdfExportService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.lizongyi.shareaccount.services.UserMemberService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 账单导出Controller
 */
@RestController
@RequestMapping("/bill")
@Slf4j
public class BillExportController {
    
    @Autowired
    private BillService billService;
    
    @Autowired
    private PdfExportRecordService pdfExportRecordService;
    
    @Autowired
    private PdfExportService pdfExportService;
    
    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private UserMemberService userMemberService;
    
    /**
     * 导出账单为PDF
     */
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportBillToPdf(@RequestBody QueryBillListRequest request) {
        try {
            // 获取当前用户ID
            Long userId = baseHandler.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.error("请先登录"));
            }

            log.info("用户 {} 导出账单为空, 请求参数: {}", userId, JSONObject.toJSONString(request));
            
            // 参数校验
            if (request == null) {
                return ResponseEntity.ok(ApiResponse.error("请求参数不能为空"));
            }

            // 检查PDF导出次数是否足够
            int remainingPdfCount = userMemberService.getRemainingPdfCount(userId);
            if (remainingPdfCount <= 0) {
                return ResponseEntity.ok(ApiResponse.error("PDF导出次数已用完，请订阅会员获取更多次数"));
            }
            
            // 校验时间范围（最多3个月）
            if (!validateTimeRange(request.getStartTime(), request.getEndTime())) {
                return ResponseEntity.ok(ApiResponse.error("导出时间范围不能超过3个月"));
            }
            
            // 检查导出频率（半小时内最多1次）
            if (pdfExportRecordService.checkExportFrequency(userId, 30, 1)) {
                return ResponseEntity.ok(ApiResponse.error("导出频率过高，请30分钟后再试"));
            }
            
            // 创建导出记录
            PdfExportRecord record = pdfExportRecordService.createRecord(userId, JSON.toJSONString(request));
            
            // 生成PDF并上传到COS
            String fileUrl = pdfExportService.exportBillToPdf(request, record.getId(), userId);
            if (fileUrl == null || fileUrl.isEmpty()) {
                // 更新导出记录为失败状态
                pdfExportRecordService.updateRecordFailed(record.getId());
                return ResponseEntity.ok(ApiResponse.error("导出文件URL为空"));
            }
            
            // 更新导出记录
            boolean isSuccess = pdfExportRecordService.updateRecordSuccess(record.getId(), fileUrl.substring(fileUrl.lastIndexOf('/') + 1), fileUrl);
            if (isSuccess) {
                userMemberService.incrementPdfUsedCount(userId);
            }
            
            // 返回文件访问路径
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", fileUrl.substring(fileUrl.lastIndexOf('/') + 1));
            
            log.info("账单导出成功，记录ID: {}, 文件URL: {}", record.getId(), fileUrl);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("导出账单PDF失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("导出失败，请稍后重试"));
        }
    }
    
    /**
     * 校验时间范围是否在3个月内
     */
    private boolean validateTimeRange(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return true; // 允许不指定时间范围
        }
        
        try {
            // 解析时间
            LocalDate start = LocalDate.parse(startTime.length() > 10 ? startTime.substring(0, 10) : startTime);
            LocalDate end = LocalDate.parse(endTime.length() > 10 ? endTime.substring(0, 10) : endTime);
            
            // 计算天数差
            long days = ChronoUnit.DAYS.between(start, end);
            return days <= 90; // 最多90天（3个月）
        } catch (Exception e) {
            log.warn("时间解析失败: {}", e.getMessage());
            return false;
        }
    }
}