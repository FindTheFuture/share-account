package cn.lizongyi.shareaccount.response;

import lombok.Data;

@Data
public class ShareConfigResponse {

    private String shareTitleBill;      // 账单分享标题

    private String shareTitleLedger;    // 账本分享标题

    private String shareImageUrl;       // 分享图片URL
}