package cn.lizongyi.shareaccount.request;

import lombok.Data;

import java.util.List;

/**
 * 创建/更新消息请求DTO
 */
@Data
public class CreateMessageRequest {
    /**
     * 消息ID，更新时必填，创建时无需填写
     */
    private Long id;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-系统消息，2-业务消息
     */
    private Integer type;
    
    /**
     * 优先级：0-普通，1-重要
     */
    private Integer priority;
    
    /**
     * 接收对象目标类型：all-所有用户，specific-指定用户
     */
    private String target;
    
    /**
     * 当target为specific时，指定接收消息的用户ID列表
     */
    private List<Long> userIds;
}