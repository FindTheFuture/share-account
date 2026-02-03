package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * PDF导出记录表
 */
@Data
@Entity
@Table(name = "pdf_export_record")
public class PdfExportRecord implements Serializable {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 请求参数（JSON格式）
     */
    @Column(name = "request_params")
    private String requestParams;
    
    /**
     * 状态：0-处理中，1-成功，2-失败，3-已删除
     */
    @Column(name = "status")
    private Integer status;
    
    /**
     * 文件名
     */
    @Column(name = "file_name")
    private String fileName;
    
    /**
     * 文件URL
     */
    @Column(name = "file_url")
    private String fileUrl;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
}