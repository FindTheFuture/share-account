package cn.lizongyi.shareaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryFeatureListResponse {

    private List<FeatureItem> square;
    private List<FeatureItem> strip;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeatureItem {
        private String icon;
        private String title;
        private String desc;
        private String url;
        private boolean important;
        private boolean isTabBar;
        private String color;
        private String role;
    }

}




