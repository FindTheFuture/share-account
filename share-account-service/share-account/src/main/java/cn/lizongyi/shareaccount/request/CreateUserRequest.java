package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateUserRequest {

    private Long id;
    private String nickName;
    private String phone;
    private String password;
    private Integer sex;
    private Integer role;
    private Integer canSendMessage;
    private Integer notityBill;
}