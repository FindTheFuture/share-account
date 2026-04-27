package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {

    private Long contactId;  // 联系人关系ID
    private Long userId;  // 联系人用户ID
    private String nickName;  // 联系人昵称
    private String phone;  // 联系人手机号（脱敏后）
    private String avatarUrl;  // 联系人头像URL
    private Integer unreadCount;  // 未读消息数量
    private String lastMessage;  // 最后一条消息内容
    private String lastMessageTime;  // 最后一条消息时间
}
