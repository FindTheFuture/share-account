package cn.lizongyi.shareaccount.controller;

import cn.lizongyi.shareaccount.entity.Skin;
import cn.lizongyi.shareaccount.entity.UserTheme;
import cn.lizongyi.shareaccount.request.ApplyThemeRequest;
import cn.lizongyi.shareaccount.response.ApiResponse;
import cn.lizongyi.shareaccount.response.SkinResponse;
import cn.lizongyi.shareaccount.response.UserThemeResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/theme")
public class ThemeController {

    private final ThemeService themeService;

    @Autowired
    private BaseHandler baseHandler;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    /** 免费颜色列表（DB） */
    @GetMapping("/freeColors")
    public ApiResponse<List<cn.lizongyi.shareaccount.response.FreeColorResponse>> freeColors() {
        return ApiResponse.success(
                themeService.getFreeColors().stream()
                        .map(cn.lizongyi.shareaccount.response.FreeColorResponse::fromEntity)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    /** 已发布皮肤列表（会员 & 免费） */
    @GetMapping("/skins")
    public ApiResponse<List<SkinResponse>> skins() {
        List<Skin> skins = themeService.getPublishedSkins();
        return ApiResponse.success(skins.stream().map(SkinResponse::fromEntity).collect(java.util.stream.Collectors.toList()));
    }

    /** 仅会员皮肤列表（type=1） */
    @GetMapping("/skins/vip")
    public ApiResponse<List<SkinResponse>> vipSkins() {
        List<Skin> skins = themeService.getVipSkins();
        return ApiResponse.success(skins.stream().map(SkinResponse::fromEntity).collect(java.util.stream.Collectors.toList()));
    }

    /** 当前用户已应用的主题状态 */
    @GetMapping("/current")
    public ApiResponse<UserThemeResponse> current() {
        Long userId = baseHandler.getUserId();
        UserTheme ut = themeService.getUserTheme(userId);
        if (ut == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.success(UserThemeResponse.fromEntity(ut));
    }

    /** 应用主题：支持 colorHex（免费纯色）或 skinId（皮肤）。两者至少一个存在。 */
    @PostMapping("/apply")
    public ApiResponse<UserThemeResponse> apply(@RequestBody ApplyThemeRequest request) {
        Long userId = baseHandler.getUserId();
        boolean hasColor = org.springframework.util.StringUtils.hasText(request.getColorHex());
        boolean hasSkin = request.getSkinId() != null && request.getSkinId() > 0;
        if (!hasColor && !hasSkin) {
            return ApiResponse.error("colorHex 或 skinId 至少一个必须提供");
        }
        // 非会员皮肤校验可在此扩展（略）
        UserTheme ut = themeService.applyTheme(userId, request.getSkinId(), request.getColorHex());
        return ApiResponse.success(UserThemeResponse.fromEntity(ut));
    }
}