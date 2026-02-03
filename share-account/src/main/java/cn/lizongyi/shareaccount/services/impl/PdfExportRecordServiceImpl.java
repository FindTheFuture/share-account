package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.PdfExportRecordMapper;
import cn.lizongyi.shareaccount.entity.PdfExportRecord;
import cn.lizongyi.shareaccount.services.PdfExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import cn.lizongyi.shareaccount.enums.PdfExportStatusEnum;

@Service
@Slf4j
public class PdfExportRecordServiceImpl implements PdfExportRecordService {
    
    @Resource
    private PdfExportRecordMapper pdfExportRecordMapper;
    
    @Override
    public PdfExportRecord createRecord(Long userId, String requestParams) {
        PdfExportRecord record = new PdfExportRecord();
        record.setUserId(userId);
        record.setRequestParams(requestParams);
        record.setStatus(PdfExportStatusEnum.PROCESSING.getCode()); // 0-处理中
        record.setCreatedAt(LocalDateTime.now());
        
        pdfExportRecordMapper.insert(record);
        log.info("创建PDF导出记录: {}", record.getId());
        return record;
    }
    
    @Override
    public boolean updateRecordSuccess(Long recordId, String fileName, String fileUrl) {
        // 检查记录是否存在
        PdfExportRecord record = pdfExportRecordMapper.selectById(recordId);
        if (record == null) {
            log.warn("尝试更新不存在的PDF导出记录: {}", recordId);
            return false;
        }
        record.setStatus(PdfExportStatusEnum.SUCCESS.getCode()); // 1-成功
        record.setFileName(fileName);
        record.setFileUrl(fileUrl);
        record.setCompletedAt(LocalDateTime.now());
        
        int rows = pdfExportRecordMapper.updateById(record);
        if (rows <= 0) {
            log.error("更新PDF导出记录失败: {}", recordId);
            return false;
        }
        log.info("PDF导出记录更新成功: {}", recordId);
        return true;
    }
    
    @Override
    public boolean updateRecordFailed(Long recordId) {
        // 检查记录是否存在
        PdfExportRecord record = pdfExportRecordMapper.selectById(recordId);
        if (record == null) {
            log.warn("尝试更新不存在的PDF导出记录: {}", recordId);
            return false;
        }
        record.setStatus(PdfExportStatusEnum.FAILED.getCode()); // 2-失败
        record.setCompletedAt(LocalDateTime.now());
        
        int rows = pdfExportRecordMapper.updateById(record);
        if (rows <= 0) {
            log.error("更新PDF导出记录失败: {}", recordId);
            return false;
        }
        log.info("PDF导出记录更新失败: {}", recordId);
        return true;
    }
    
    @Override
    public boolean checkExportFrequency(Long userId, Integer minutes, Integer limit) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(minutes);
        
        int count = pdfExportRecordMapper.countByUserIdAndTimeRange(userId, startTime, endTime);
        log.info("用户 {} 在过去 {} 分钟内导出次数: {}", userId, minutes, count);
        
        return count >= limit;
    }
    
    @Override
    public List<PdfExportRecord> getExpiredRecords(Integer days) {
        return pdfExportRecordMapper.selectExpiredRecords(days, PdfExportStatusEnum.SUCCESS.getCode()); // 1-成功状态
    }
    
    @Override
    public void batchDeleteExpiredRecords(List<Long> recordIds, LocalDateTime deletedAt) {
        if (recordIds == null || recordIds.isEmpty()) {
            return;
        }
        
        pdfExportRecordMapper.batchUpdateStatus(recordIds, PdfExportStatusEnum.DELETED.getCode(), deletedAt); // 3-已删除
        log.info("批量删除过期PDF导出记录数量: {}", recordIds.size());
    }

    @Override
    public boolean deleteRecords(Long recordId) {
        int rows = pdfExportRecordMapper.updateDeleteStatus(recordId, PdfExportStatusEnum.DELETED.getCode(), LocalDateTime.now());  // 3-已删除
        if (rows > 0) {
            log.info("删除PDF导出记录: {}", recordId);
            return true;
        }
        return false;
    }
}