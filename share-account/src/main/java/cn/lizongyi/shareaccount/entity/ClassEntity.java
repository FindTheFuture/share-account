package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 分类表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "`class`")
@Entity
public class ClassEntity {
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
     * 分类名称
     */
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    /**
     * 分类图标
     */
    @Column(name = "icon", length = 20, nullable = false)
    private String icon;


    /**
     * 父级id
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 0、顶级 1、一级分类 2、二级分类 3、三级分类 4、四级分类
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 0、正常 1、删除
     */
    @Column(name = "status", columnDefinition = "int default 0")
    private Integer status = 0;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
