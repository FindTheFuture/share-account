package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class ApplyThemeRequest {
    /** 选择的纯色十六进制，如 #2979ff；可为空 */
    private String colorHex;
    /** 选择的皮肤ID；可为空（两者至少一个不为空） */
    private Long skinId;
}