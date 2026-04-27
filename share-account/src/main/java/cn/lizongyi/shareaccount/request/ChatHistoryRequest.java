package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class ChatHistoryRequest {
    private Long userId;
    private Integer limit;
}
