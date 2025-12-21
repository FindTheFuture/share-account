package cn.lizongyi.shareaccount.enums;

public enum TotalMoneyStatusEnum {
    INCLUDED(0, "计入总资产"),
    EXCLUDED(1, "不计入总资产");

    private final int id;
    private final String name;

    TotalMoneyStatusEnum(int id, String name) {
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
    public static TotalMoneyStatusEnum fromId(Integer id) {
        if (id == null) {
            return INCLUDED;
        }
        for (TotalMoneyStatusEnum item : TotalMoneyStatusEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("TotalMoneyStatusEnum Invalid ID: " + id);
    }
}