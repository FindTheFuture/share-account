package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.enums.CanSendMessageEnum;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.enums.SexEnum;
import cn.lizongyi.shareaccount.util.DateUtil;
import cn.lizongyi.shareaccount.util.PhoneUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long haoe;          // 用户id
    private String nickName;    // 用户昵称
    private String pictureAddress;  // 用户头像地址
    private String phone;       // 手机号
    private String password;    // 密码
    private Integer sex;        // 性别
    private String sexName;      // 性别名称
    private Integer validIntegral;  // 有效积分
    private Integer canSendMessage;  // 是否发送通知
    private String canSendMessageName;  // 是否发送通知中文
    private Integer notityBill;      // 是否通知账单
    private String lastLoginTime;    // 最后登录时间
    private String createTime;       // 创建时间
    private Integer role;       // 角色id
    private String roleName;    // 角色名称
    private Boolean isGuest;    // 是否为游客账号
    private Integer memberLevel; // 会员等级
    private String memberLevelIcon; // 等级图标


    public static UserResponse fromUser(User user) {

        if(user == null) {
            return null;
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setHaoe(user.getId());
        userResponse.setNickName(user.getNickName());
        userResponse.setSex(user.getSex());
        userResponse.setSexName(SexEnum.fromId(user.getSex()).getName());
        userResponse.setValidIntegral(user.getValidIntegral());
        userResponse.setCanSendMessage(user.getCanSendMessage());
        userResponse.setCanSendMessageName(CanSendMessageEnum.fromId(user.getCanSendMessage()).getName());
        userResponse.setNotityBill(user.getNotityBill());
        userResponse.setPhone(PhoneUtil.maskPhoneNumber(user.getPhone()));
        userResponse.setIsGuest(user.getOpenid() != null && user.getOpenid().startsWith("guest_"));
        return userResponse;
    }



    public static UserResponse fromUser(User user, Integer role) {

        UserResponse userResponse = fromUser(user);
        if(userResponse == null || role == null || role == RoleTypeEnum.USER.getId()){
            return userResponse;
        }

        userResponse.setPhone(user.getPhone());
        //userResponse.setPassword(user.getPassword());
        userResponse.setRole(user.getRole());
        userResponse.setRoleName(RoleTypeEnum.fromId(user.getRole()).getName());
        userResponse.setLastLoginTime(DateUtil.localDateTimeToString(user.getLastLoginTime()));
        userResponse.setCreateTime(DateUtil.localDateTimeToString(user.getCreateTime()));
        return userResponse;
    }



}
