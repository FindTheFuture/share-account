package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.Ledger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerResponse {
    
    private Long id; // 账本ID
    private Long userId; // 用户ID
    private String name; // 账本名称
    private Integer type; // 类型 0、共享账本（默认）  1、个人账本
    private String typeName; // 类型名称
    private Integer property; // 属性 0、普通账本（默认） 1、AA账本
    private String propertyName; // 属性名称
    private Long classId; // 分类ID
    private Integer status; // 状态 0、正常 1、弃用
    private String statusName; // 状态名称
    private String memo; // 备注
    private String createTime; // 创建时间
    private Integer isDefault; // 是否默认账本

    public static LedgerResponse fromLedger(Ledger ledger) {
        if (ledger == null) {
            return null;
        }

        LedgerResponse response = new LedgerResponse();
        response.setId(ledger.getId());
        response.setUserId(ledger.getUserId());
        response.setName(ledger.getName());
        response.setType(ledger.getType());
        response.setTypeName(ledger.getType() == 1 ? "个人账本" : "共享账本");
        response.setProperty(ledger.getProperty());
        response.setPropertyName(ledger.getProperty() == 1 ? "AA账本" : "普通账本");
        response.setClassId(ledger.getClassId());
        response.setStatus(ledger.getStatus());
        response.setStatusName(ledger.getStatus() == 1 ? "已弃用" : "正常");
        response.setMemo(ledger.getMemo());
        response.setCreateTime(ledger.getCreateTime() != null ? ledger.getCreateTime().toString() : "");
        response.setIsDefault(ledger.getIsDefault());
        
        return response;
    }
}