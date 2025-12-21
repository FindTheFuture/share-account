package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.UserTheme;
import lombok.Data;

@Data
public class UserThemeResponse {
    private Long skinId;
    private String primaryColor;

    public static UserThemeResponse fromEntity(UserTheme ut) {
        if (ut == null) return null;
        UserThemeResponse r = new UserThemeResponse();
        r.setSkinId(ut.getSkinId());
        r.setPrimaryColor(ut.getPrimaryColor());
        return r;
    }
}