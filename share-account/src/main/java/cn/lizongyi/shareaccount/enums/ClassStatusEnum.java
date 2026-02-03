package cn.lizongyi.shareaccount.enums;

public enum ClassStatusEnum {
    NORMAL(0, "正常"),
    DELETE(1, "删除");

    private final int id;
    private final String name;

    ClassStatusEnum(int id, String name) {
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
    public static ClassStatusEnum fromId(Integer id) {
        if (id == null) {
            return NORMAL;
        }
        for (ClassStatusEnum item : ClassStatusEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("ClassStatusEnum Invalid ID: " + id);
    }
}
