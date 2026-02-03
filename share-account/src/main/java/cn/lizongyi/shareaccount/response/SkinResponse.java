package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.Skin;
import lombok.Data;

@Data
public class SkinResponse {
    private Long id;
    private String name;
    private Integer type;
    private String previewColor;

    public static SkinResponse fromEntity(Skin skin) {
        if (skin == null) return null;
        SkinResponse r = new SkinResponse();
        r.setId(skin.getId());
        r.setName(skin.getName());
        r.setType(skin.getType());
        r.setPreviewColor(skin.getPreviewColor());
        return r;
    }
}