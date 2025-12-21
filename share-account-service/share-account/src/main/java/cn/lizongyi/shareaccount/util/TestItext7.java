package cn.lizongyi.shareaccount.util;

// iText 7 依赖


// 极简测试代码
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class TestItext7 {
    public static void main(String[] args) throws Exception {
        String dest = "/Users/lizongyi/Desktop/pdf/test7.pdf";
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // 加载思源宋体（.otf）
        PdfFont font = PdfFontFactory.createFont("SourceHanSerifCN-Regular-1.otf", PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        document.add(new Paragraph("清甜账单导出 测试中文").setFont(font).setFontSize(20));
        
        document.close();
        System.out.println("PDF生成完成");
    }
}
