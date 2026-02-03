package cn.lizongyi.shareaccount.response;

import java.math.BigDecimal;

import cn.lizongyi.shareaccount.entity.MemberPackage;
import cn.lizongyi.shareaccount.enums.MemberPackageTypeEnum;
import cn.lizongyi.shareaccount.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberPackageResponse {
    private Long id;
    private String name;
    private Integer type; // 0为周期套餐，1为功能次数套餐
    private String typeName; // 套餐类型名称
    private BigDecimal price;
    private Integer durationDays; // 有效期天数（周期套餐使用）
    private Integer aiCount; // AI识别次数
    private Integer pdfCount; // PDF导出次数
    private String description; // 权益描述
    private Integer points; // 购买后获得积分
    private Integer status; // 状态：0-禁用，1-启用
    private Integer sort; // 排序顺序
    private Integer isRecommend; // 是否推荐：0-不推荐，1-推荐
    private String createTime;

    public static MemberPackageResponse fromMemberPackage(MemberPackage memberPackage) {
        if (memberPackage == null) {
            return null;
        }

        MemberPackageResponse memberPackageResponse = new MemberPackageResponse();
        memberPackageResponse.setId(memberPackage.getId());
        memberPackageResponse.setName(memberPackage.getName());
        memberPackageResponse.setType(memberPackage.getType());
        memberPackageResponse.setTypeName(MemberPackageTypeEnum.fromId(memberPackage.getType()).getName());
        memberPackageResponse.setPrice(memberPackage.getPrice());
        memberPackageResponse.setDurationDays(memberPackage.getDurationDays());
        memberPackageResponse.setAiCount(memberPackage.getAiCount());
        memberPackageResponse.setPdfCount(memberPackage.getPdfCount());
        memberPackageResponse.setDescription(memberPackage.getDescription());
        memberPackageResponse.setPoints(memberPackage.getPoints());
        memberPackageResponse.setStatus(memberPackage.getStatus());
        memberPackageResponse.setSort(memberPackage.getSort());
        memberPackageResponse.setIsRecommend(memberPackage.getIsRecommend());
        memberPackageResponse.setCreateTime(DateUtil.localDateTimeToString(memberPackage.getCreateTime()));
        return memberPackageResponse;   
    }

}
