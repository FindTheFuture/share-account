package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户会员表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "user_member")
@Entity
public class UserMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 20)
    private Long userId;
    
    @Column(name = "package_id", nullable = false, length = 20)
    private Long packageId;
    
    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "package_type", nullable = false, length = 10)
    private Integer packageType;
    
    @Column(name = "payment_record_id", length = 20)
    private Long paymentRecordId;
    
    @Column(name = "ai_count", nullable = false, columnDefinition = "int default 0")
    private Integer aiCount = 0;
    
    @Column(name = "ai_used_count", nullable = false, columnDefinition = "int default 0")
    private Integer aiUsedCount = 0;
    
    @Column(name = "pdf_count", nullable = false, columnDefinition = "int default 0")
    private Integer pdfCount = 0;
    
    @Column(name = "pdf_used_count", nullable = false, columnDefinition = "int default 0")
    private Integer pdfUsedCount = 0;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "status", nullable = false, columnDefinition = "int default 0")
    private Integer status = 0;
}