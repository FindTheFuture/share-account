package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Config;
import cn.lizongyi.shareaccount.response.QueryFeatureListResponse;

public interface ConfigService {

    String getConfigValue(Long userId, String key);
    Config getConfig(Long userId, String key);
    Config getConfigByType(Integer type);
    QueryFeatureListResponse getFeatureList();
    
    // 获取字符串类型配置值
    String getStringValue(String key, String defaultValue);
    
    // 获取布尔类型配置值
    Boolean getBooleanValue(String key, Boolean defaultValue);
    
    // 获取整数类型配置值
    Integer getIntegerValue(String key, Integer defaultValue);
    
    // 获取JSON对象配置值
    <T> T getJsonValue(String key, Class<T> clazz, T defaultValue);

    // 获取主题配置
    cn.lizongyi.shareaccount.response.ThemeConfigResponse getThemeConfig();

    // 获取分享配置
    cn.lizongyi.shareaccount.response.ShareConfigResponse getShareConfig();

    // 更新配置值
    void updateConfigValue(Long userId, String key, String value);

}
