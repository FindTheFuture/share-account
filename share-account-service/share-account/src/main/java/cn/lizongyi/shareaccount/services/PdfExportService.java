package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.request.QueryBillListRequest;

/**
 * PDF导出服务接口
 */
public interface PdfExportService {
    
    /**
     * 导出账单为PDF并上传到COS
     * @param request 账单查询请求参数
     * @param recordId 导出记录ID
     * @param userId 用户ID
     * @return COS文件访问URL
     */
    String exportBillToPdf(QueryBillListRequest request, Long recordId, Long userId);
}