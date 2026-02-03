package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Table(name = "member_level_config")
@Entity
public class MemberLevelConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level")
    private Integer level; // 等级数字，如1、2、3等

    @Column(name = "level_name")
    private String levelName; // 等级名称

    @Column(name = "min_points")
    private Integer minPoints; // 最低积分要求

    @Column(name = "max_points")
    private Integer maxPoints; // 最高积分要求

    @Column(name = "icon")
    private String icon; // 等级图标

    @Column(name = "description")
    private String description; // 等级描述
}