package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 账单评论实体
 */
@Data
@Accessors(chain = true)
@Table(name = "bill_comment")
@Entity
public class BillComment {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 账单ID
     */
    @Column(name = "bill_id", nullable = false)
    private Long billId;

    /**
     * 评论用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评论内容
     * 当 type=0 时为文本内容；当 type=1 时为图片ID（字符串）
     */
    @Lob
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /**
     * 评论类型：0-文本，1-图片
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}