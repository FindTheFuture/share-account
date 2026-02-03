package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 账户表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "account")
@Entity
public class Account {
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
     * 类型 0、储蓄账户 1、信贷账户 2、充值账户 3、投资理财
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private Integer type = 0;

    /**
     * 是否计入预算 0、计入（默认）1、不计入
     */
    @Column(name = "is_budget", columnDefinition = "int default 0")
    private Integer isBudget = 0;

    /**
     * 是否计入总资产 0、计入（默认） 1、不计入
     */
    @Column(name = "is_total_money", columnDefinition = "int default 0")
    private Integer isTotalMoney = 0;

    /**
     * 是否默认账户 0、否（默认） 1、是
     */
    @Column(name = "is_default", columnDefinition = "int default 0")
    private Integer isDefault = 0;

    /**
     * 余额
     */
    @Column(name = "balance", columnDefinition = "bigint default 0")
    private Long balance = 0L;

    /**
     * 0、正常 1、弃用
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
     * 账户名称
     */
    @Column(name = "name", length = 100)
    private String name;
}
