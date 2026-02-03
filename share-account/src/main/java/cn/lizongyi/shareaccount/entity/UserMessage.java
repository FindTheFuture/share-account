package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息关联实体类
 */
@Data
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long messageId;
    private Long userId;
    private Integer isRead; // 0-未读，1-已读
    private Date readAt;
    private Date createdAt;
}