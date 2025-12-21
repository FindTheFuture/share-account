package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 配置表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "`config`")
@Entity
public class Config {
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
    @Column(name = "user_id")
    private Long userId;

    /**
     * 1、币种 2、小数点位数 3、tips配置 4、用户默认头像 5、用户默认昵称  6、后端url地址  7、公告信息  15、cos配置  16、微信支付v2配置  17、微信支付v3配置    18、JWT配置
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 父级id
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 配置key
     */
    @Column(name = "key", length = 64, nullable = false)
    private String key;

    /**
     * 配置value
     */
    @Column(name = "value", length = 256)
    private String value;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
