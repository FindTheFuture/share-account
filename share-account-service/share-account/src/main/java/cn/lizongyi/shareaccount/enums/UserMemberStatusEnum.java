package cn.lizongyi.shareaccount.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户会员状态枚举
 * 0：正常
 * 1：已过期
 * 2：已退款
 */
@Getter
@AllArgsConstructor
public enum UserMemberStatusEnum {
    
    NORMAL(0, "正常"),
    EXPIRED(1, "已过期"),
    REFUNDED(2, "已退款");
    
    private final Integer code;
    private final String name;
    
    /**
     * 根据code获取枚举值
     */
    public static UserMemberStatusEnum getByCode(Integer code) {
        for (UserMemberStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}