package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 定时记账配置表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "scheduled_bill")
@Entity
public class ScheduledBill {
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
     * 记账名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 金额
     */
    @Column(name = "price", columnDefinition = "bigint default 0")
    private Long price = 0L;

    /**
     * 账本id
     */
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    /**
     * 账户id
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * 分类id
     */
    @Column(name = "class_id", nullable = false)
    private Long classId;

    /**
     * 周期类型：1-每天，2-每周，3-每月，4-每季度，5-每年
     */
    @Column(name = "cycle_type", nullable = false)
    private Integer cycleType;

    /**
     * 周期值：每天-0，每周-1-7，每月-1-31，每季度-1-3，每年-1-12
     */
    @Column(name = "cycle_value", nullable = false)
    private Integer cycleValue;

    /**
     * 执行时间
     */
    @Column(name = "execute_time", nullable = false)
    private LocalDateTime executeTime;

    /**
     * 开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 状态：1-启用，2-暂停，3-删除
     */
    @Column(name = "status", columnDefinition = "int default 1")
    private Integer status = 1;

    /**
     * 是否提醒：0-不提醒，1-提醒
     */
    @Column(name = "is_remind", columnDefinition = "int default 0")
    private Integer isRemind = 0;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
