package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@Table(name = "wxad_info")
public class WxadInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "lottery_id")
    private Long lotteryId;

    @Column(name = "source")
    private Integer source;

    @Column(name = "type")
    private Integer type;

    @Column(name = "status")
    private Integer status;

    @Column(name = "code")
    private Integer code;

    @Column(name = "message")
    private String message;

    @Column(name = "ad_unit_id")
    private String adUnitId;

    @Column(name = "load_time")
    private Long loadTime;

    @Column(name = "is_shown")
    private Boolean isShown;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "create_time")
    private Timestamp createTime;
}