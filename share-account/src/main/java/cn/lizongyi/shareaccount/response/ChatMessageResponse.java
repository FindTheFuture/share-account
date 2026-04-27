package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {

    private Long id;  // 消息ID
    private Long fromUserId;  // 发送者用户ID
    private String fromUserName;  // 发送者昵称
    private String fromUserAvatar;  // 发送者头像URL
    private Long toUserId;  // 接收者用户ID
    private Integer type;  // 消息类型：0文字 1图片 2账本卡片 3账单卡片
    private String content;  // 消息内容（文字/图片URL/卡片JSON对象）
    private String imageUrl;  // 图片消息的图片URL
    private Integer isRead;  // 是否已读：0未读 1已读
    private String createTime;  // 消息创建时间
}
