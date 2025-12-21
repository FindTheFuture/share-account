package cn.lizongyi.shareaccount.enums;

public enum BudgetNameEnum {
    INCLUDED(0, "计入预算"),
    EXCLUDED(1, "不计入预算");

    private final int id;
    private final String name;

    BudgetNameEnum(int id, String name) {
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
    public static BudgetNameEnum fromId(Integer id) {
        if (id == null) {
            return INCLUDED;
        }
        for (BudgetNameEnum item : BudgetNameEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("BudgetNameEnum Invalid ID: " + id);
    }
}