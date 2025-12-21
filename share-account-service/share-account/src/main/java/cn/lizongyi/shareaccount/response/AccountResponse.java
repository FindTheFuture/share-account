package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.Account;
import cn.lizongyi.shareaccount.enums.AccountTypeEnum;
import cn.lizongyi.shareaccount.enums.BudgetNameEnum;
import cn.lizongyi.shareaccount.enums.TotalMoneyStatusEnum;
import cn.lizongyi.shareaccount.enums.AccountStatusEnum;
import cn.lizongyi.shareaccount.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {

    private Long id;          // 账户 ID
    private String name;       // 账户名称
    //private Long userId;      // 用户 id
    private Integer type;     // 类型 0、储蓄账户 1、信贷账户 2、充值账户 3、投资理财
    private String typeName;  // 类型名称
    private Integer isBudget; // 是否计入预算 0、计入（默认）1、不计入
    private String isBudgetName; // 是否计入预算中文
    private Integer isTotalMoney; // 是否计入总资产   0、计入（默认）1、不计入
    private String isTotalMoneyName; // 是否计入总资产中文
    private Integer isDefault; // 是否默认账户 0、否 1、是
    private Long balance;     // 余额
    private Integer status;   // 状态 0、正常 1、弃用
    private String statusName; // 状态中文
    private String memo;      // 备注
    private String createTime; // 创建时间

    public static AccountResponse fromAccount(Account account) {
        if (account == null) {
            return null;
        }

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        //accountResponse.setUserId(account.getUserId());
        accountResponse.setType(account.getType());
        accountResponse.setTypeName(AccountTypeEnum.fromId(account.getType()).getName());
        accountResponse.setIsBudget(account.getIsBudget());
        accountResponse.setIsBudgetName(BudgetNameEnum.fromId(account.getIsBudget()).getName());
        accountResponse.setIsTotalMoney(account.getIsTotalMoney());
        accountResponse.setIsTotalMoneyName(TotalMoneyStatusEnum.fromId(account.getIsTotalMoney()).getName());
        accountResponse.setIsDefault(account.getIsDefault());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setStatus(account.getStatus());
        accountResponse.setStatusName(AccountStatusEnum.fromId(account.getStatus()).getName());
        accountResponse.setMemo(account.getMemo());
        accountResponse.setCreateTime(DateUtil.localDateTimeToString(account.getCreateTime()));
        accountResponse.setName(account.getName());
        return accountResponse;
    }
}