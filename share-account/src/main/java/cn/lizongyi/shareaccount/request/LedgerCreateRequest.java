package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class LedgerCreateRequest {
    
    private Integer type; // 类型 0、共享账本（默认）  1、个人账本
    private Integer property; // 属性 0、普通账本（默认） 1、AA账本
    private Long classId; // 分类ID
    private String memo; // 备注
}