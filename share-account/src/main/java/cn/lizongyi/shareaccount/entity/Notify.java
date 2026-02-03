package cn.lizongyi.shareaccount.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 公告表实体类
 */
@Data
@Accessors(chain = true)
@Table(name = "notify")
@Entity
public class Notify {
    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 标题
     */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /**
     * 内容
     */
    @Lob
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
