package cn.lizongyi.shareaccount.enums;

public enum PayStatusEnum {
    PENDING(1, "待支付"),
    SUCCESS(2, "支付成功"),
    FAILURE(3, "支付失败"),
    REFUNDED(4, "已退款"),
    EXPIRED(5, "已过期"),
    CANCEL(6, "已取消");

    private final int id;
    private final String name;

    PayStatusEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static PayStatusEnum fromId(int id) {
        for (PayStatusEnum status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid pay status id: " + id);
    }
}