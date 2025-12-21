package cn.lizongyi.shareaccount.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id; // 成员ID，用于更新操作
    private String name; // 名称
    private Long ledgerId; // 账本ID
    private Long billId; // 账单ID
    private Long parentUserId; // 父级用户ID
    private Long userId; // 用户ID
    private Integer percentage = 0; // 占比
    private Integer status = 0; // 状态，0：添加中，1：正常，2：已删除
}