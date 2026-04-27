package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Contact {

    public static final int STATUS_PENDING = 0;  // 状态：待同意
    public static final int STATUS_ADDED = 1;  // 状态：已添加
    public static final int STATUS_REJECTED = 2;  // 状态：已拒绝

    private Long id;  // 主键ID
    private Long userId;  // 用户ID（发起请求的一方）
    private Long friendId;  // 联系人用户ID（接收请求的一方）
    private Integer status;  // 关系状态：0待同意 1已添加 2已拒绝
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}
