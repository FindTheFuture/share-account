package cn.lizongyi.shareaccount.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息实体类
 */
@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private Integer type; // 1-系统消息，2-业务消息
    private Integer priority; // 0-普通，1-重要
    private Integer status; // 0-正常，1-已删除
    private Date createdAt;
    private Date updatedAt;
}