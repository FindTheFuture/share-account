package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestResponse {

    private Long contactId;  // 联系人关系ID
    private Long userId;  // 请求发送者用户ID
    private String nickName;  // 请求发送者昵称
    private String phone;  // 请求发送者手机号（脱敏后）
    private String avatarUrl;  // 请求发送者头像URL
    private String createTime;  // 请求发送时间
}
