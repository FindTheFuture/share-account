package cn.lizongyi.shareaccount.entity;


import lombok.Data;
import lombok.experimental.Accessors;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * AI任务表实体类
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "ai_task")
public class AiTask {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    /**
     * 聊天内容、关联对象ID（支持多种文件类型，如图片ID）
     */
    @Column(name = "request")
    private String request;

    /**
     * 任务类型：0-消费截图识别 1-发票识别 2-收据识别 3-通用OCR 4-聊天内容
     */
    @Column(name = "type", columnDefinition = "int default 0")
    private Integer type = 0;
    
    /**
     * 识别状态：0-等待识别 1-识别中 2-识别成功 3-识别失败
     */
    @Column(name = "status", columnDefinition = "int default 0")
    private Integer status = 0;
    
    /**
     * 识别结果
     */
    @Lob
    @Column(name = "result", columnDefinition = "text")
    private String result;
    
    /**
     * 错误信息
     */
    @Column(name = "error_msg")
    private String errorMsg;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count", columnDefinition = "int default 0")
    private Integer retryCount = 0;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 识别时间
     */
    @Column(name = "recognize_time")
    private LocalDateTime recognizeTime;
    
    /**
     * 任务结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
}