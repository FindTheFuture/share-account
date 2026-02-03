package cn.lizongyi.shareaccount.enums;

public enum BudgetStatusEnum {
    NORMAL(0, "正常"),
    DELETE(1, "删除");

    private final int id;
    private final String name;

    BudgetStatusEnum(int id, String name) {
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
    public static BudgetStatusEnum fromId(Integer id) {
        if (id == null) {
            return NORMAL;
        }
        for (BudgetStatusEnum item : BudgetStatusEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("BudgetStatusEnum Invalid ID: " + id);
    }
}