package cn.lizongyi.shareaccount.enums;

public enum AccountStatusEnum {
    NORMAL(0, "正常"),
    DISUSED(1, "弃用");

    private final int id;
    private final String name;

    AccountStatusEnum(int id, String name) {
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
    public static AccountStatusEnum fromId(Integer id) {
        if (id == null) {
            return NORMAL;
        }
        for (AccountStatusEnum item : AccountStatusEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("AccountStatusEnum Invalid ID: " + id);
    }
}