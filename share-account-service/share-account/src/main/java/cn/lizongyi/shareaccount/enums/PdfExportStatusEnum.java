package cn.lizongyi.shareaccount.enums;

public enum PdfExportStatusEnum {

    PROCESSING(0, "处理中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    DELETED(3, "已删除");
    
    private final Integer code;
    private final String desc;
    
    PdfExportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static PdfExportStatusEnum getByCode(Integer code) {
        for (PdfExportStatusEnum status : PdfExportStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
