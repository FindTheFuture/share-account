package cn.lizongyi.shareaccount.enums;

/**
 * 退款状态枚举
 */
public enum RefundStatusEnum {
    NONE(0, "未退款"),
    REFUNDING(1, "退款中"),
    REFUNDED(2, "已退款");
    
    private int id;
    private String name;
    
    RefundStatusEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}