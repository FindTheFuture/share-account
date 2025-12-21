package cn.lizongyi.shareaccount.enums;

public enum AccountTypeEnum {
    SAVINGS_ACCOUNT(0, "储蓄账户"),
    CREDIT_ACCOUNT(1, "信贷账户"),
    RECHARGE_ACCOUNT(2, "充值账户"),
    INVESTMENT_ACCOUNT(3, "投资理财");

    private final int id;
    private final String name;

    AccountTypeEnum(int id, String name) {
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
    public static AccountTypeEnum fromId(Integer id) {
        if (id == null) {
            return SAVINGS_ACCOUNT;
        }
        for (AccountTypeEnum item : AccountTypeEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("AccountTypeEnum Invalid ID: " + id);
    }
}