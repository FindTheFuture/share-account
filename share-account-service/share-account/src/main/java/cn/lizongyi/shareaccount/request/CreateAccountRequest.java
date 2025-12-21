package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateAccountRequest {

    private Long id;

    /**
     * 类型 0、储蓄账户 1、信贷账户 2、充值账户 3、投资理财
     */
    private Integer type;

    /**
     * 是否计入预算 0、计入（默认）1、不计入
     */
    private Integer isBudget = 0;

    /**
     * 是否计入总资产 0、计入（默认） 1、不计入
     */
    private Integer isTotalMoney = 0;

    /**
     * 是否默认账户 0、否（默认） 1、是
     */
    private Integer isDefault = 0;

    /**
     * 余额
     */
    private Long balance = 0L;

    /**
     * 状态 0、正常 1、弃用
     */
    private Integer status;

    /**
     * 备注
     */
    private String memo;

    /**
     * 账户名称（最长100字符）
     */
    private String name;
}