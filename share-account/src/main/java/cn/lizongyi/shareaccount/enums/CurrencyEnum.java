package cn.lizongyi.shareaccount.enums;

/**
 * 币种枚举
 */
public enum CurrencyEnum {
    DEFAULT(0, "人民币");


    private final int id;
    private final String name;

    CurrencyEnum(int id, String name) {
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
    public static CurrencyEnum fromId(Integer id) {
        if (id == null || id == 0) {
            return DEFAULT;
        }
        for (CurrencyEnum item : CurrencyEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("CurrencyEnum Invalid ID: " + id);
    }
}
