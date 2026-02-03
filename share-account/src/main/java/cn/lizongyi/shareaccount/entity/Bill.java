package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 账单表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "bill")
@Entity
public class Bill {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户id
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 账本id
     */
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    /**
     * 账户id
     */
    @Column(name = "account_id")
    private Long accountId;

    /**
     * 从分类枚举里找id
     */
    @Column(name = "class_id", columnDefinition = "bigint default -1")
    private Long classId = -1L;

    /**
     * 是否计入预算 0、计入（默认）1、不计入
     */
    @Column(name = "is_budget", columnDefinition = "int default 0")
    private Integer isBudget = 0;

    /**
     * 用户可选，默认是当前时间, 账单在生活中发生的时间，可能是历史账单
     */
    @Column(name = "bill_time")
    private LocalDateTime billTime;

    /**
     * 金额
     */
    @Column(name = "price", columnDefinition = "bigint default 0")
    private Long price = 0L;

    /**
     * 0、正常 1、删除
     */
    @Column(name = "status", columnDefinition = "int default 0")
    private Integer status = 0;

    /**
     * 备注
     */
    @Lob
    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 最顶层分类id
     */
    @Column(name = "top_class_id", nullable = false)
    private Long topClassId;


    
}
