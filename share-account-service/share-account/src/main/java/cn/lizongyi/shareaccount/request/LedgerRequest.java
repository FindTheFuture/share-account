package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class LedgerRequest {
    private Long id; // 账本ID，创建时可选，更新时必填
    private String name; // 账本名称
    private Integer type; // 类型 0、共享账本（默认）  1、个人账本
    private Integer property; // 属性 0、普通账本（默认） 1、AA账本
    private Long classId; // 分类ID
    private String memo; // 备注
    private Integer isDefault; // 是否默认账本
}