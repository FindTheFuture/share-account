package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long toUserId;
    private Integer type;
    private String content;
}
