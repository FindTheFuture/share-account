package cn.lizongyi.shareaccount.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户消息订阅实体类
 */
@Data
public class UserMessageSubscription {
    private Long id;                 // 主键ID
    private Long userId;             // 用户ID
    private String templateId;       // 模板消息ID
    private Integer templateKeyId;   // 模板消息枚举ID
    private Integer authorizeCount;  // 授权次数（可发送消息的次数）
    private Date authorizedAt;       // 授权时间
    private Boolean neverRemind;     // 是否不再提醒（0:否，1:是）
    private Date createdAt;          // 创建时间
    private Date updatedAt;          // 更新时间
}