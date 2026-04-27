package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {

    public static final int TYPE_TEXT = 0;  // 消息类型：文字
    public static final int TYPE_IMAGE = 1;  // 消息类型：图片
    public static final int TYPE_LEDGER_CARD = 2;  // 消息类型：账本卡片
    public static final int TYPE_BILL_CARD = 3;  // 消息类型：账单卡片

    public static final int UNREAD = 0;  // 未读状态：未读
    public static final int READ = 1;  // 已读状态：已读

    public static final int NOT_DELETED = 0;  // 删除状态：未删除
    public static final int DELETED = 1;  // 删除状态：已删除

    private Long id;  // 主键ID
    private Long fromUserId;  // 发送者用户ID
    private Long toUserId;  // 接收者用户ID
    private Integer type;  // 消息类型：0文字 1图片 2账本卡片 3账单卡片
    private String content;  // 消息内容（文字内容/图片URL/卡片JSON）
    private Integer isRead;  // 是否已读：0未读 1已读
    private Integer isDeleted;  // 是否删除：0未删除 1已删除
    private LocalDateTime createTime;  // 创建时间
}
