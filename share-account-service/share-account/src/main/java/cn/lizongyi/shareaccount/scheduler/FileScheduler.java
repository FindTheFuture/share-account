package cn.lizongyi.shareaccount.scheduler;

import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.PdfExportRecord;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.FileService;
import cn.lizongyi.shareaccount.services.PdfExportRecordService;
import cn.lizongyi.shareaccount.services.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-11
 * @description
 */
@Slf4j
@Component
public class FileScheduler {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private PdfExportRecordService pdfExportRecordService;
    
    @Autowired
    private BaseHandler baseHandler;
    
    private String getCosUrl() {
        return baseHandler.getCosUrl();
    }

    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void deleteFile() {
        log.info("定时任务启动--每天凌晨1点执行--删除无用图片");
        // 获取所有弃用状态的图片
        List<Picture> unusedPictures = pictureService.findByStatus(1);
        if(CollectionUtils.isEmpty(unusedPictures)) {
            log.info("没有查询到无用图片");
            return;
        }
        log.info("查询到 {}张无用图片", unusedPictures.size());
        for (Picture picture : unusedPictures) {
            boolean success = fileService.delete(picture.getPath(), picture.getName());

            if(success){
                // 删除数据库中的记录
                pictureService.deleteById(picture.getId());
            }
        }
        log.info("定时任务结束--每天凌晨1点执行--删除无用图片");
    }
    
    /**
     * 每天凌晨0点30分执行清理任务
     * 清理30天前的PDF文件
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void cleanupExpiredPdfs() {
        log.info("开始执行PDF文件清理任务");
        
        try {
            // 查询30天前的过期记录
            List<PdfExportRecord> expiredRecords = pdfExportRecordService.getExpiredRecords(30);
            
            if (expiredRecords.isEmpty()) {
                log.info("没有发现过期的PDF文件记录");
                return;
            }
            
            log.info("发现 {} 条过期的PDF文件记录", expiredRecords.size());
            
            int deleteSuccessCount = 0;
            int deleteFailCount = 0;
            
            // 遍历记录并删除COS文件，每个文件单独处理
            for (PdfExportRecord record : expiredRecords) {
                // 使用包装类型以便在方法内部修改值
                int[] successCountWrapper = {deleteSuccessCount};
                int[] failCountWrapper = {deleteFailCount};
                processSingleRecord(record, successCountWrapper, failCountWrapper);
                deleteSuccessCount = successCountWrapper[0];
                deleteFailCount = failCountWrapper[0];
            }
            
            log.info("PDF清理任务完成：成功删除 {} 个文件，失败 {} 个文件", deleteSuccessCount, deleteFailCount);
            
        } catch (Exception e) {
            log.error("执行PDF清理任务时发生异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理单个文件记录，执行删除操作并更新状态
     */
    private void processSingleRecord(PdfExportRecord record, int[] deleteSuccessCount, int[] deleteFailCount) {
        try {
            if (record.getFileUrl() != null) {
                // 从URL中提取COS路径
                String cosPath = extractCosPath(record.getFileUrl());
                if (cosPath != null) {
                    // 删除COS文件
                    boolean deleteSuccess = fileService.deleteFile(cosPath);
                    if (deleteSuccess) {
                        deleteSuccessCount[0]++;
                        boolean deleteRecords = pdfExportRecordService.deleteRecords(record.getId());
                        log.info("删除COS文件: {}, 记录ID: {}, 结果{}，已更新状态", cosPath, record.getId(), deleteRecords);
                    } else {
                        deleteFailCount[0]++;
                        log.warn("删除COS文件失败: {}, 记录ID: {}", cosPath, record.getId());
                    }
                }
            }
        } catch (Exception e) {
            deleteFailCount[0]++;
            log.error("处理记录ID: {} 时发生异常: {}", record.getId(), e.getMessage(), e);
            // 即使发生异常，也不影响其他文件的处理
        }
    }
    
    /**
     * 从COS文件URL中提取路径
     * 将fileUrl去掉cosUrl前缀，剩下的就是文件在存储桶中的路径
     */
    private String extractCosPath(String fileUrl) {
        try {
            String cosUrl = getCosUrl();
            if (fileUrl != null && cosUrl != null && fileUrl.startsWith(cosUrl)) {
                // 去掉cosUrl前缀，得到存储桶中的路径
                return fileUrl.substring(cosUrl.length());
            }
            log.warn("URL不包含cosUrl前缀，无法提取路径: {}", fileUrl);
            return null;
        } catch (Exception e) {
            log.error("提取COS路径失败: {}", e.getMessage());
            return null;
        }
    }
}
