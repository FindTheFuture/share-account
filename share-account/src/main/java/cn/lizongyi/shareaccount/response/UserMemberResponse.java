package cn.lizongyi.shareaccount.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import cn.lizongyi.shareaccount.entity.UserMember;
import cn.lizongyi.shareaccount.enums.UserMemberStatusEnum;
import cn.lizongyi.shareaccount.enums.MemberPackageTypeEnum;
import cn.lizongyi.shareaccount.util.DateUtil;

/**
 * 用户会员响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMemberResponse {
    private Long id;
    private Long userId;
    private Long packageId;
    private String packageName;
    private BigDecimal price;
    private Integer packageType;
    private String packageTypeName;
    private Long paymentRecordId;
    private Integer aiCount;
    private Integer aiUsedCount;
    private Integer pdfCount;
    private Integer pdfUsedCount;
    private String startTime;
    private String endTime;
    private String createTime;
    private Integer status;
    private String statusName;

    public static UserMemberResponse fromUserMember(UserMember userMember) {
        return new UserMemberResponse(
                userMember.getId(),
                userMember.getUserId(),
                userMember.getPackageId(),
                userMember.getPackageName(),
                userMember.getPrice(),
                userMember.getPackageType(),
                MemberPackageTypeEnum.fromId(userMember.getPackageType()).getName(),
                userMember.getPaymentRecordId(),
                userMember.getAiCount(),
                userMember.getAiUsedCount(),
                userMember.getPdfCount(),
                userMember.getPdfUsedCount(),
                DateUtil.localDateTimeToString(userMember.getStartTime()),
                DateUtil.localDateTimeToString(userMember.getEndTime()),
                DateUtil.localDateTimeToString(userMember.getCreateTime()),
                userMember.getStatus(),
                UserMemberStatusEnum.getByCode(userMember.getStatus()).getName()
        );
    }

    public static List<UserMemberResponse> fromUserMembers(List<UserMember> userMembers) {
        return userMembers.stream().map(UserMemberResponse::fromUserMember).collect(Collectors.toList());
    }




}
