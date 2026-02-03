package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 预算表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "budget")
@Entity
public class Budget {
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
     * 设定总余额
     */
    @Column(name = "total_balance", nullable = false)
    private Long totalBalance;

    /**
     * 年份
     */
    @Column(name = "year", columnDefinition = "int default 0")
    private Integer year = 0;

    /**
     * 月份
     */
    @Column(name = "month", columnDefinition = "int default 0")
    private Integer month = 0;

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
