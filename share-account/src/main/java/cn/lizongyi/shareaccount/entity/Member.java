package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table(name = "member")
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "parent_user_id", nullable = false)
    private Long parentUserId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "percentage", columnDefinition = "int default 0")
    private Integer percentage = 0;

    @Column(name = "status", nullable = false, columnDefinition = "int default 0")
    private Integer status = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
