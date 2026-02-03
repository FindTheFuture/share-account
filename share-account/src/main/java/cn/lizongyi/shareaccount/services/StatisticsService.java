package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.response.StatisticsResponse;

import java.time.LocalDateTime;

public interface StatisticsService {
    StatisticsResponse getStatistics(
            Long userId,
            String dimension,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer year,
            Integer month,
            Long ledgerId,
            Long accountId,
            Long categoryId
    );
}