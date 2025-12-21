package cn.lizongyi.shareaccount.enums;

public enum SexEnum {
    DEFAULT(0, "未知"),
    MAN(1, "男"),
    WOMAN(2, "女");

    private final int id;
    private final String name;

    SexEnum(int id, String name) {
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
    public static SexEnum fromId(Integer id) {
        if (id == null || id == 0) {
            return DEFAULT;
        }
        for (SexEnum item : SexEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("SexEnum Invalid ID: " + id);
    }
}
