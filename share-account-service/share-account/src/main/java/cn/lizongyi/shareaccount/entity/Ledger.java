package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 账本表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "ledger")
@Entity
public class Ledger {
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
     * 账本名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 类型 0、共享账本（默认）  1、个人账本
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private Integer type = 0;

    /**
     * 属性 0、普通账本（默认） 1、AA账本(比如旅游、聚会、合租等等)
     */
    @Column(name = "property", columnDefinition = "int default 0")
    private Integer property = 0;

    /**
     * 从分类枚举里找id
     */
    @Column(name = "class_id", columnDefinition = "bigint default -1")
    private Long classId = -1L;

    /**
     * 状态 0、正常 1、弃用
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
     * 是否默认账本
     */
    @Column(name = "is_default", columnDefinition = "tinyint default 0")
    private Integer isDefault = 0;
}
