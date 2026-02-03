package cn.lizongyi.shareaccount.response;

import lombok.Data;
import java.util.Date;

/**
 * 消息响应类
 */
@Data
public class MessageResponse {
    private Long id;
    private String title;
    private String content;
    private Integer type; // 1-系统消息，2-业务消息
    private Integer priority; // 0-普通，1-重要
    private Integer isRead; // 0-未读，1-已读
    private Date createdAt;
    private Date readAt;
}