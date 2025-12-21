package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxadResponse {
    private Long id;
    private Long userId;
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
    private String createTime;
}