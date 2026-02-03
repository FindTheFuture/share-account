package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.Member;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MemberResponse extends Member {
    /**
     * 账本名称
     */
    private String ledgerName;

    /**
     * 父用户名称
     */
    private String parentUserName;
    /**
     * 父用户头像地址
     */
    private String parentUserPictureAddress;

    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户头像地址
     */
    private String userPictureAddress;
    
    /**
     * 转换Member为MemberResponse
     */
    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        if (member != null) {
            response.setId(member.getId())
                   .setName(member.getName())
                   .setLedgerId(member.getLedgerId())
                   .setBillId(member.getBillId())
                   .setParentUserId(member.getParentUserId())
                   .setUserId(member.getUserId())
                   .setPercentage(member.getPercentage())
                   .setStatus(member.getStatus())
                   .setCreateTime(member.getCreateTime());
        }
        return response;
    }
}