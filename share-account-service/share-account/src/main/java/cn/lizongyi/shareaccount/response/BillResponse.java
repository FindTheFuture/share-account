package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class BillResponse {

    private Long id;          // 账单ID
    private Long userId;      // 用户ID
    private Long ledgerId;    // 账本ID
    private Long accountId;   // 账户ID
    private Long classId;     // 分类ID
    private Long topClassId;  // 顶部分类ID
    private String className; // 分类名称
    private String classIcon; // 分类图标
    private String ledgerName; // 账本名称
    private String accountName; // 账户名称
    private String accountTypeName; // 账户类型
    private Integer isBudget; // 是否计入预算 0、计入（默认）1、不计入
    private String isBudgetName; // 是否计入预算中文名称
    private String billTime;  // 账单时间
    private Long price;       // 金额
    private Integer status;   // 状态 0、正常 1、删除
    private String memo;      // 备注
    private String createTime; // 创建时间
    private String createUserName; // 创建人名称
    private String otherMemo; // 返回的其他内容
    private Boolean showOtherMemo; // 前端是否显示otherMemo
}