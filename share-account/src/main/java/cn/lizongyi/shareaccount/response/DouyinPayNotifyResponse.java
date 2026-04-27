package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DouyinPayNotifyResponse {

    private Boolean success;

    private String message;

    private String orderId;

    private String tradeNo;

    private String tradeStatus;
}
