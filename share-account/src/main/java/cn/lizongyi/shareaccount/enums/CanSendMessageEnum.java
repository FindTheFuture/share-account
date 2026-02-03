package cn.lizongyi.shareaccount.enums;

public enum CanSendMessageEnum {
    NO(0, "不发送"),
    YES(1, "发送");

    private final int id;
    private final String name;

    CanSendMessageEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 根据ID获取对应的枚举对象
    public static CanSendMessageEnum fromId(Integer id) {
        if (id == null) {
            return YES;
        }
        for (CanSendMessageEnum item : CanSendMessageEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("CanSendMessageEnum Invalid ID: " + id);
    }
}
