package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-20
 * @description
 */
@Data
@Accessors(chain = true)
@Table(name = "wechat_access_token")
public class AccessToken {
    private Integer id;
    private String accessToken;
    private Timestamp expiresAt;
}