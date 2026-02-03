package cn.lizongyi.shareaccount.enums;


public enum PictureTypeEnum {

    // 头像
    AVATAR_USER(1, "用户头像"),
    AVATAR_ZHANGBEN(2, "账本头像"),
    AVATAR_ZHANGHU(3, "账户头像"),

    // icon
    xxx_icon(101, "xxx功能icon"),

    // 评论图片
    COMMENT_IMAGE(201, "评论图片"),

    // 消费截图
    BILL_SCREENSHOT(301, "消费截图"),

    // 其他图片类型-401开头

    ;

    private final int id;
    private final String name;

    PictureTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 根据id获取对应的枚举项
    public static PictureTypeEnum fromId(int id) {
        for (PictureTypeEnum type : PictureTypeEnum.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("PictureTypeEnum: No enum constant with id: " + id);
    }
}
