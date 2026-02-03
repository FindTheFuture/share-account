package cn.lizongyi.shareaccount.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedLedgerResponse {
    // 分享关系（成员）维度
    private Long parentUserId;
    private String parentUserName;
    private String parentUserPictureAddress;
    private String shareTime; // 分享创建时间（member.create_time）

    // 账本维度
    private Long ledgerId;
    private String ledgerName;
    private Integer status; // 0 正常，1 弃用
    private String statusName;
    private Integer type; // 0 共享账本，1 个人账本
    private String typeName;
    private Integer property; // 0 普通，1 AA
    private String propertyName;
    private String memo;
}