package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Skin;
import cn.lizongyi.shareaccount.entity.UserTheme;

import java.util.List;

public interface ThemeService {
    List<cn.lizongyi.shareaccount.entity.FreeColor> getFreeColors();
    List<Skin> getPublishedSkins();
    // 新增：仅会员皮肤
    List<Skin> getVipSkins();
    UserTheme getUserTheme(Long userId);
    UserTheme applyTheme(Long userId, Long skinId, String colorHex);
}