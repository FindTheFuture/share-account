package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table(name = "payment_record")
@Entity
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "member_package_id")
    private Long memberPackageId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status")
    private Integer status; // 支付状态：0-待支付，1-成功，2-失败，3-取消

    @Column(name = "payment_type")
    private Integer paymentType; // 支付方式：0-微信支付

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "out_trade_no")
    private String outTradeNo;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "package_content_snapshot", columnDefinition = "text")
    private String packageContentSnapshot;

    @Column(name = "points_earned")
    private Integer pointsEarned;
}