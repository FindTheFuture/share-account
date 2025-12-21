package cn.lizongyi.shareaccount.enums;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-10
 * @description
 */

public enum RoleTypeEnum {
    USER(0, "普通用户"),
    ADMIN(1, "管理员"),
    ;
    private final int id;
    private final String name;

    RoleTypeEnum(int id, String name) {
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
    public static RoleTypeEnum fromId(Integer id) {
        if (id == null) {
            return USER;
        }
        for (RoleTypeEnum item : RoleTypeEnum.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException("RoleTypeEnum Invalid ID: " + id);
    }
}
