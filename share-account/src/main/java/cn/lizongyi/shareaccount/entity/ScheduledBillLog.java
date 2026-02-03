package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 定时记账执行日志表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "scheduled_bill_log")
@Entity
public class ScheduledBillLog {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 定时记账配置ID
     */
    @Column(name = "scheduled_id", nullable = false)
    private Long scheduledId;

    /**
     * 执行时间
     */
    @Column(name = "execute_time", nullable = false)
    private LocalDateTime executeTime;

    /**
     * 执行状态：1-成功，2-失败
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 生成的账单ID
     */
    @Column(name = "bill_id")
    private Long billId;

    /**
     * 错误信息
     */
    @Column(name = "error_msg")
    private String errorMsg;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
