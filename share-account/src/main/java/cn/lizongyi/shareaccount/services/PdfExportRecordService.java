package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.PdfExportRecord;
import java.time.LocalDateTime;
import java.util.List;

public interface PdfExportRecordService {
    
    /**
     * 创建导出记录
     */
    PdfExportRecord createRecord(Long userId, String requestParams);
    
    /**
     * 更新记录为成功状态
     */
    boolean updateRecordSuccess(Long recordId, String fileName, String fileUrl);
    
    /**
     * 更新记录为失败状态
     */
    boolean updateRecordFailed(Long recordId);
    
    /**
     * 检查用户导出频率是否超限
     * @param userId 用户ID
     * @param minutes 时间范围（分钟）
     * @param limit 限制次数
     * @return 是否超限
     */
    boolean checkExportFrequency(Long userId, Integer minutes, Integer limit);
    
    /**
     * 查询过期的导出记录
     */
    List<PdfExportRecord> getExpiredRecords(Integer days);
    
    /**
     * 批量删除过期记录
     */
    void batchDeleteExpiredRecords(List<Long> recordIds, LocalDateTime deletedAt);

    /**
     * 删除导出记录
     */
    boolean deleteRecords(Long recordId);
}