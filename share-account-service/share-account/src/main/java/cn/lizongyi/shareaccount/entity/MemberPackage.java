package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table(name = "member_package")
@Entity
public class MemberPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private Integer type; // 0为周期套餐，1为功能次数套餐

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "duration_days")
    private Integer durationDays; // 有效期天数（周期套餐使用）

    @Column(name = "ai_count")
    private Integer aiCount; // AI识别次数

    @Column(name = "pdf_count")
    private Integer pdfCount; // PDF导出次数

    @Column(name = "description", length = 500)
    private String description; // 权益描述

    @Column(name = "points")
    private Integer points; // 购买后获得积分

    @Column(name = "status")
    private Integer status; // 状态：0-禁用，1-启用

    @Column(name = "sort")
    private Integer sort; // 排序顺序
    
    @Column(name = "is_recommend")
    private Integer isRecommend; // 是否推荐：0-不推荐，1-推荐
    
    @Column(name = "max_purchase_count")
    private Integer maxPurchaseCount; // 最大购买次数，null表示不限制

    @Column(name = "create_time")
    private LocalDateTime createTime;
}