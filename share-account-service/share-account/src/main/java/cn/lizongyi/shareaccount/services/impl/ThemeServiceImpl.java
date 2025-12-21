package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.SkinMapper;
import cn.lizongyi.shareaccount.dao.UserThemeMapper;
import cn.lizongyi.shareaccount.entity.Skin;
import cn.lizongyi.shareaccount.entity.UserTheme;
import cn.lizongyi.shareaccount.entity.FreeColor;
import cn.lizongyi.shareaccount.services.ThemeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThemeServiceImpl implements ThemeService {

    private final SkinMapper skinMapper;
    private final UserThemeMapper userThemeMapper;

    public ThemeServiceImpl(SkinMapper skinMapper, UserThemeMapper userThemeMapper) {
        this.skinMapper = skinMapper;
        this.userThemeMapper = userThemeMapper;
    }

    @Override
    public List<FreeColor> getFreeColors() {
        List<Skin> freeSkins = skinMapper.findAllFree();
        List<FreeColor> result = new java.util.ArrayList<>();
        for (Skin s : freeSkins) {
            FreeColor c = new FreeColor();
            c.setName(s.getName());
            c.setHex(s.getPreviewColor());
            c.setStatus(0);
            result.add(c);
        }
        return result;
    }

    @Override
    public List<Skin> getPublishedSkins() {
        return skinMapper.findAllPublished();
    }

    // 新增：仅会员皮肤
    @Override
    public List<Skin> getVipSkins() {
        return skinMapper.findAllVip();
    }

    @Override
    public UserTheme getUserTheme(Long userId) {
        return userThemeMapper.findLatestByUserId(userId);
    }

    @Override
    public UserTheme applyTheme(Long userId, Long skinId, String colorHex) {
        UserTheme latest = userThemeMapper.findLatestByUserId(userId);
        if (latest == null) {
            latest = new UserTheme();
            latest.setUserId(userId);
            latest.setSkinId(skinId);
            latest.setPrimaryColor(org.springframework.util.StringUtils.hasText(colorHex) ? colorHex : null);
            userThemeMapper.insert(latest);
        } else {
            latest.setSkinId(skinId);
            latest.setPrimaryColor(org.springframework.util.StringUtils.hasText(colorHex) ? colorHex : null);
            userThemeMapper.update(latest);
        }
        return latest;
    }
}