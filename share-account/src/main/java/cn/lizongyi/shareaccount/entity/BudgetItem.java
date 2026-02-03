package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 预算明细表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "budget_item")
@Entity
public class BudgetItem {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 预算id
     */
    @Column(name = "budget_id", nullable = false)
    private Long budgetId;

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
     * 从分类枚举里找id
     */
    @Column(name = "class_id", columnDefinition = "bigint default -1")
    private Long classId = -1L;

    /**
     * 设定总余额
     */
    @Column(name = "total_balance", nullable = false)
    private Long totalBalance;

    /**
     * 0、正常 1、删除
     */
    @Column(name = "status", columnDefinition = "int default 0")
    private Integer status = 0;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
