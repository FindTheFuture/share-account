package cn.lizongyi.shareaccount.request;

import lombok.Data;

/**
 * 创建/更新账单评论请求DTO
 */
@Data
public class CreateBillCommentRequest {
    /**
     * 评论ID，更新时使用（当前仅支持创建与删除）
     */
    private Long id;

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 评论内容
     * 当 type=0 时为文本内容；当 type=1 时为图片ID（字符串）
     */
    private String content;

    /**
     * 评论类型：0-文本，1-图片
     */
    private Integer type;
}