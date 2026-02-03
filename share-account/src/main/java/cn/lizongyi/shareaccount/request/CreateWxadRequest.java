package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateWxadRequest {
    private Long lotteryId;
    private Integer source;
    private Integer type;
    private Integer status;
    private Integer code;
    private String message;
    private String adUnitId;
    private Long loadTime;
    private Boolean isShown;
    private Boolean isCompleted;
    private Integer retryCount;
}