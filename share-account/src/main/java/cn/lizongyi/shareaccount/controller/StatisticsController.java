package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.StatisticsResponse;
import cn.lizongyi.shareaccount.services.StatisticsService;
import cn.lizongyi.shareaccount.services.UserIdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/statistics")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserIdService userIdService;

    /**
     * 统计接口：多线程汇总（命名兼容前端 back_url.js）
     * 支持的维度：total|year|month|day|custom
     */
    @GetMapping("/getMultiThreadStatistics")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMultiThreadStatistics(
            @RequestParam(name = "dimension", required = false, defaultValue = "total") String dimension,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "ledgerId", required = false) Long ledgerId,
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        try {
            Long userId = userIdService.getUserId();
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(ApiResponse.badRequest("未获取到用户ID"));
            }

            // 解析日期范围（Asia/Shanghai，日维度与自定义需做 23:59:59 处理）
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            if ("custom".equalsIgnoreCase(dimension)) {
                if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
                    return ResponseEntity.ok(ApiResponse.badRequest("自定义维度需传入起止日期"));
                }
                startTime = parseDateTime(startDate);
                endTime = parseDateTime(endDate);
                if (startDate.length() == 10 && endDate.length() == 10) {
                    endTime = endTime.withHour(23).withMinute(59).withSecond(59);
                }
            } else if ("day".equalsIgnoreCase(dimension)) {
                // 日维度使用当日，或传入的 startDate
                if (StringUtils.hasText(startDate)) {
                    startTime = parseDateTime(startDate);
                    endTime = startTime.withHour(23).withMinute(59).withSecond(59);
                } else {
                    LocalDate today = LocalDate.now();
                    startTime = today.atStartOfDay();
                    endTime = today.atTime(23, 59, 59);
                }
            } else if ("month".equalsIgnoreCase(dimension)) {
                int y = (year != null && year > 0) ? year : LocalDate.now().getYear();
                int m = (month != null && month >= 1 && month <= 12) ? month : LocalDate.now().getMonthValue();
                LocalDate firstDay = LocalDate.of(y, m, 1);
                LocalDate lastDay = LocalDate.of(y, m, firstDay.lengthOfMonth());
                startTime = firstDay.atStartOfDay();
                endTime = lastDay.atTime(23, 59, 59);
            } else if ("year".equalsIgnoreCase(dimension)) {
                int y = (year != null && year > 0) ? year : LocalDate.now().getYear();
                LocalDate firstDay = LocalDate.of(y, 1, 1);
                LocalDate lastDay = LocalDate.of(y, 12, 31);
                startTime = firstDay.atStartOfDay();
                endTime = lastDay.atTime(23, 59, 59);
            } else {
                // total 维度：从很早到现在
                startTime = LocalDate.of(1970, 1, 1).atStartOfDay();
                endTime = LocalDateTime.now();
            }

            StatisticsResponse response = statisticsService.getStatistics(
                    userId,
                    dimension,
                    startTime,
                    endTime,
                    year,
                    month,
                    ledgerId,
                    accountId,
                    categoryId
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("统计接口异常: dimension={}, year={}, month={}, startDate={}, endDate={}, ledgerId={}, accountId={}, categoryId={}",
                    dimension, year, month, startDate, endDate, ledgerId, accountId, categoryId, e);
            return ResponseEntity.ok(ApiResponse.error("统计接口异常"));
        }
    }

    /** 支持 yyyy-MM-dd HH:mm:ss 与 yyyy-MM-dd */
    private LocalDateTime parseDateTime(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            if (dateStr.length() == 19 && dateStr.contains(":") && dateStr.contains("-")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(dateStr, formatter);
            } else if (dateStr.length() == 10 && dateStr.contains("-")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            }
        } catch (Exception e) {
            log.error("日期解析失败: {}", dateStr, e);
        }
        return LocalDateTime.now();
    }
}