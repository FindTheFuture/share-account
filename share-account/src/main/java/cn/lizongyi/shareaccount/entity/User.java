package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table(name = "user")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "openid")
    private String openid;

    @Column(name = "unionid")
    private String unionid;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password")
    private String password;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "role")
    private Integer role;

    @Column(name = "valid_integral")
    private Integer validIntegral;

    @Column(name = "can_send_message")
    private Integer canSendMessage;

    @Column(name = "notity_bill")
    private Integer notityBill;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "member_status")
    private Integer memberStatus; // 会员状态：0-非会员，1-会员，2-过期

}