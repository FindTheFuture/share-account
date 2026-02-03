package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.FreeColor;
import lombok.Data;

@Data
public class FreeColorResponse {
    private String name;
    private String hex;

    public static FreeColorResponse fromEntity(FreeColor c) {
        if (c == null) return null;
        FreeColorResponse r = new FreeColorResponse();
        r.setName(c.getName());
        r.setHex(c.getHex());
        return r;
    }
}