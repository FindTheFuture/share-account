package cn.lizongyi.shareaccount.response;

import lombok.Data;

@Data
public class ThemeConfigResponse {

    private String primaryColor;      // 主色调，用于导航栏、按钮等核心UI元素

    private String secondaryColor;    // 辅助色，用于次要按钮、图标等

    private String appName;           // 应用名称，用于导航栏标题等

    private String appLogo;           // 应用Logo URL，用于启动页、分享图标等
}