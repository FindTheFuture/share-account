package cn.lizongyi.shareaccount.enums;

public enum MemberPackageTypeEnum {
    DEFAULT(0, "周期套餐"),
    FUNCTION(1, "功能次数套餐");

    private final int id;
    private final String name;

    MemberPackageTypeEnum(int id, String name) {
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
    public static MemberPackageTypeEnum fromId(Integer id) {
        if (id == null || id == 0) {
            return DEFAULT;
        }
        for (MemberPackageTypeEnum item : MemberPackageTypeEnum.values()) { 
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("MemberPackageTypeEnum Invalid ID: " + id);
    }
}
