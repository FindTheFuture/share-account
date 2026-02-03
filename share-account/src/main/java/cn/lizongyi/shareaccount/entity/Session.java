package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-05
 * @description
 */
@Data
@Accessors(chain = true)
@Table(name = "sessions")
public class Session {

    @Id
    private String id;

    @Column(name = "openid")
    private String openId;

    @Column(name = "session_key")
    private String sessionKey;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "expire_time")
    private Date expireTime;

    @Column(name = "last_activity")
    private Date lastActivity;

    @Column(name = "device_info")
    private String deviceInfo;

}
