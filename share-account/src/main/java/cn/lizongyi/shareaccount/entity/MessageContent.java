package cn.lizongyi.shareaccount.entity;

import lombok.Data;

@Data
public class MessageContent {

    // 类型 ： bill_comment：账单评论
    private String bizType;
    // 关联的账本ID
    private Long ledgerId;
    // 关联的账单ID
    private Long billId;
    // 关联的评论ID
    private Long commentId;
    // 评论者用户ID
    private Long fromUserId;

}
