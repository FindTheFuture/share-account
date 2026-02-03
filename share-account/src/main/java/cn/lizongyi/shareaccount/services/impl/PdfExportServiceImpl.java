package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.request.QueryBillListRequest;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.services.BillService;
import cn.lizongyi.shareaccount.services.PdfExportService;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.properties.TextAlignment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.lizongyi.shareaccount.response.BillListWithStatisticsResponse;
import cn.lizongyi.shareaccount.services.FileService;



import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfExportServiceImpl implements PdfExportService {

    @Autowired
    private BillService billService;

    @Autowired
    private FileService fileService;

    private PdfFont font;

    private PdfFont createFont() {
        try {
            String fontPath = "SourceHanSerifCN-Regular-1.otf";
            font = PdfFontFactory.createFont(fontPath, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            log.info("字体加载成功！名称：{}", font.getFontProgram().getFontNames().getFontName());
            return font;
        } catch (Exception e) {
            log.error("加载字体失败！", e);
            throw new RuntimeException("字体加载失败", e);
        }
    }

    /**
     * 导出账单为PDF并上传到COS
     */
    @Override
    public String exportBillToPdf(QueryBillListRequest request, Long recordId, Long userId) {
        try {
            // 临时文件路径
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "清甜账单导出_" + recordId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            String tempFilePath = tempDir + File.separator + fileName;

            // 加载字体
            createFont();

            // 生成PDF
            generatePdf(tempFilePath, request, userId);

            // 上传到COS（原有逻辑不变）
            String cosPath = "/bill/export/userId" + userId + "/" + fileName;
            String cosUrl = fileService.uploadFile(tempFilePath, cosPath);

            // 删除临时文件
            new File(tempFilePath).delete();

            return cosUrl;

        } catch (Exception e) {
            log.error("生成PDF并上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF导出失败", e);
        }
    }

    /**
     * 生成PDF文件（iText 7 核心逻辑）
     */
    private void generatePdf(String filePath, QueryBillListRequest request, Long userId) throws Exception {
        
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        try {
            // 先预查询一次，为了统计信息
            BillListWithStatisticsResponse<BillResponse> billPage = laodBillList(userId, 1, 1, request);
            // 账单总数量
            long totalCount = billPage.getTotal();
            if (totalCount == 0) {
                log.error("用户{}没有账单数据", userId);
                throw new RuntimeException("没有账单数据");
            }

            // 添加页眉
            addHeader(document);

            // 统计信息
            addStatistics(document, userId, request, billPage);

            // 创建表格表头
            Table table = createCellHeader(document);

            // 账单列表
            int pageSize = 1000;
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                billPage = laodBillList(userId, pageNum, pageSize, request);
                if(billPage.getList() == null || billPage.getList().isEmpty()){
                    continue;
                }
                addBillList(document, table, billPage);
            }
            document.add(table);

            // 页脚
            addFooter(document);

        } finally {
            document.close(); // iText 7 关闭文档即可自动释放资源
        }
    }


    private BillListWithStatisticsResponse<BillResponse> laodBillList(Long userId, int pageNum, int pageSize, QueryBillListRequest request){
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        BillListWithStatisticsResponse<BillResponse> billPage = billService.getBillListWithPagination(request);
        log.info("用户{}，第{}页，每页{}条，当前页{}条，一共{}条", userId, pageNum, pageSize, (billPage.getList() != null ? billPage.getList().size() : 0), billPage.getTotal());
        return billPage;
    }



    /**
     * 添加页眉
     */
    private void addHeader(Document document) {
        Paragraph title = new Paragraph("清甜账单导出").setFont(font).setFontSize(20).setBold().setMarginBottom(20).setTextAlignment(TextAlignment.CENTER);
        document.add(title);
    }

    /**
     * 添加统计信息
     */
    private void addStatistics(Document document, Long userId, QueryBillListRequest request, BillListWithStatisticsResponse<BillResponse> billPage) {
        Paragraph statisticsTitle = new Paragraph("账单汇总").setFont(font).setFontSize(16).setBold().setMarginBottom(10);
        document.add(statisticsTitle);

        // 时间范围
        Paragraph timeRange = new Paragraph(String.format("时间范围: %s 至 %s", request.getStartTime() != null ? request.getStartTime() : "全部", request.getEndTime() != null ? request.getEndTime() : "全部")).setFont(font).setFontSize(12); // 设置字体大小
        document.add(timeRange);

        // 账单总数量
        Paragraph totalCount = new Paragraph(String.format("账单总数量: %d 条", billPage.getTotal())).setFont(font).setFontSize(12); // 设置字体大小
        document.add(totalCount);

        // 总支出金额
        BigDecimal expense = new BigDecimal(billPage.getTotalExpense() != null ? billPage.getTotalExpense() : 0).divide(new BigDecimal(100));
        Paragraph totalExpense = new Paragraph(String.format("总支出金额: %s 元", expense)).setFont(font).setFontSize(12); // 设置字体大小
        document.add(totalExpense);

        // 总收入金额
        BigDecimal income = new BigDecimal(billPage.getTotalIncome() != null ? billPage.getTotalIncome() : 0).divide(new BigDecimal(100));
        Paragraph totalIncome = new Paragraph(String.format("总收入金额: %s 元", income)).setFont(font).setFontSize(12); // 设置字体大小
        document.add(totalIncome);

        // 导出时间
        Paragraph exportTime = new Paragraph(String.format("导出时间: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))).setFont(font).setFontSize(12).setMarginBottom(20);
        document.add(exportTime);
    }


    /**
     * 创建表格表头
     */
    private Table createCellHeader(Document document){
        Paragraph listTitle = new Paragraph("账单明细").setFont(font).setFontSize(16).setBold().setMarginBottom(10);
        document.add(listTitle);

        float[] columnWidths = {1.5f, 1.0f, 1.0f, 1.5f, 1.0f, 0.8f, 0.8f, 1.5f}; // 各列相对宽度
        Table table = new Table(columnWidths);

        // 添加表头   蓝底白字
        String[] headers = {"时间", "创建人昵称", "账本名称", "账户名称（账户类型）", "分类", "计入预算", "金额（元）", "备注"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(font).setFontColor(new DeviceRgb(255, 255, 255)).setFontSize(10)).setBackgroundColor(new DeviceRgb(48, 113, 227)).setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }
        return table;
    }

    /**
     * 添加账单列表（表格）
     */
    private void addBillList(Document document, Table table, BillListWithStatisticsResponse<BillResponse> billPage) {
        List<BillResponse> bills = billPage.getList();

        // 添加数据行
        for (BillResponse bill : bills) {
            // 时间
            table.addCell(new Cell().add(new Paragraph(bill.getBillTime() != null ? bill.getBillTime() : "").setFont(font).setFontSize(7)));
            // 创建人昵称
            table.addCell(new Cell().add(new Paragraph(bill.getCreateUserName() != null ? bill.getCreateUserName() : "").setFont(font).setFontSize(7)));
            // 账本名称
            table.addCell(new Cell().add(new Paragraph(bill.getLedgerName() != null ? bill.getLedgerName() : "").setFont(font).setFontSize(7)));
            // 账户名称+类型
            String accountText = bill.getAccountName() != null ? bill.getAccountName() + "(" + (bill.getAccountTypeName() != null ? bill.getAccountTypeName() : "") + ")" : "";
            table.addCell(new Cell().add(new Paragraph(accountText).setFont(font).setFontSize(7)));
            // 分类
            table.addCell(new Cell().add(new Paragraph(bill.getClassName() != null ? bill.getClassName() : "").setFont(font).setFontSize(7)));
            // 计入预算
            table.addCell(new Cell().add(new Paragraph(bill.getIsBudgetName() != null ? bill.getIsBudgetName() : "").setFont(font).setFontSize(7)));
            // 金额（右对齐）
            String amount = new BigDecimal(bill.getPrice() != null ? bill.getPrice() : 0).divide(new BigDecimal(100)).toString();
            table.addCell(new Cell().add(new Paragraph(amount).setFont(font).setFontSize(7)).setTextAlignment(TextAlignment.RIGHT));
            // 备注
            table.addCell(new Cell().add(new Paragraph(bill.getMemo() != null ? bill.getMemo() : "").setFont(font).setFontSize(7)));
        }
    }

    /**
     * 添加页脚
     */
    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("注：本报告由清甜共享账单系统自动生成，所有数据均来源于用户授权提供的信息。报告仅作为账单记录参考，不构成任何法律文件、合同条款或商业决策依据，不具有法律效力。")
            .setFont(font)
            .setFontSize(10) // 设置字体大小
            .setMarginTop(20);
        document.add(footer);
    }
}