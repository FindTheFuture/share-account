package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.ConfigMapper;
import cn.lizongyi.shareaccount.entity.Config;
import cn.lizongyi.shareaccount.response.QueryFeatureListResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ConfigService;
import cn.lizongyi.shareaccount.constants.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置服务实现类
 * @author lizongyi
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private BaseHandler baseHandler;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取配置
     * @param userId
     * @param key
     * @return
     */
    @Override
    public String getConfigValue(Long userId, String key) {
        Config config = getConfig(userId, key);
        if (config != null) {
            return config.getValue();
        }
        return null;
    }

    /**
     * 获取配置
     * @param userId
     * @param key
     * @return
     */
    @Override
    public Config getConfig(Long userId, String key) {
        
        List<Config> configList = null;
        
        if ((userId == null || userId == 0) && StringUtils.isEmpty(key)) {
            return null;
        }
        if(userId == null || userId == 0) {
            configList = configMapper.getConfigByKey(key);
        } else {
            configList = configMapper.getConfigByUserIdAndKey(userId, key);
        }
        
        Config config = null;
        if (CollectionUtils.isNotEmpty(configList)) {
            config = configList.getFirst();
        }
        
        return config;
    }

    /**
     * 获取配置
     * @param type
     * @return
     */
    @Override
    public Config getConfigByType(Integer type) {
        Long userId = baseHandler.getUserId();
        List<Config> configList = configMapper.getConfigByUserIdAndType(userId, type);
        if (CollectionUtils.isNotEmpty(configList)) {
            return configList.getFirst();
        }
        return null;
    }

    /**
     * 获取功能列表
     * @return
     */
    @Override
    public QueryFeatureListResponse getFeatureList() {
        return baseHandler.getFeatureList(baseHandler.getUserId());
    }
    
    /**
     * 获取字符串类型配置值
     * @param key 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public String getStringValue(String key, String defaultValue) {
        try {
            String value = getConfigValue(null, key);
            return StringUtils.isNotEmpty(value) ? value : defaultValue;
        } catch (Exception e) {
            log.error("获取字符串配置失败: key={}", key, e);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔类型配置值
     * @param key 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        try {
            String value = getConfigValue(null, key);
            if (StringUtils.isNotEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
            return defaultValue;
        } catch (Exception e) {
            log.error("获取布尔配置失败: key={}", key, e);
            return defaultValue;
        }
    }
    
    /**
     * 获取整数类型配置值
     * @param key 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public Integer getIntegerValue(String key, Integer defaultValue) {
        try {
            String value = getConfigValue(null, key);
            if (StringUtils.isNotEmpty(value)) {
                return Integer.parseInt(value);
            }
            return defaultValue;
        } catch (Exception e) {
            log.error("获取整数配置失败: key={}", key, e);
            return defaultValue;
        }
    }
    
    /**
     * 获取JSON对象配置值
     * @param key 配置键名
     * @param clazz 目标类
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public <T> T getJsonValue(String key, Class<T> clazz, T defaultValue) {
        try {
            String value = getConfigValue(null, key);
            if (StringUtils.isNotEmpty(value)) {
                return objectMapper.readValue(value, clazz);
            }
            return defaultValue;
        } catch (Exception e) {
            log.error("获取JSON配置失败: key={}", key, e);
            return defaultValue;
        }
    }

    /**
     * 获取主题配置
     * @return 主题配置
     */
    @Override
    public cn.lizongyi.shareaccount.response.ThemeConfigResponse getThemeConfig() {
        try {
            cn.lizongyi.shareaccount.response.ThemeConfigResponse response = getJsonValue(
                Constants.THEME_CONFIG,
                cn.lizongyi.shareaccount.response.ThemeConfigResponse.class,
                null
            );
            if (response == null) {
                response = new cn.lizongyi.shareaccount.response.ThemeConfigResponse();
            }
            return response;
        } catch (Exception e) {
            log.error("获取主题配置失败: {}", e.getMessage());
            cn.lizongyi.shareaccount.response.ThemeConfigResponse response = new cn.lizongyi.shareaccount.response.ThemeConfigResponse();
            return response;
        }
    }

    /**
     * 获取分享配置
     * @return 分享配置
     */
    @Override
    public cn.lizongyi.shareaccount.response.ShareConfigResponse getShareConfig() {
        try {
            cn.lizongyi.shareaccount.response.ShareConfigResponse response = getJsonValue(
                Constants.SHARE_CONFIG,
                cn.lizongyi.shareaccount.response.ShareConfigResponse.class,
                null
            );
            if (response == null) {
                response = new cn.lizongyi.shareaccount.response.ShareConfigResponse();
            }
            return response;
        } catch (Exception e) {
            log.error("获取分享配置失败: {}", e.getMessage());
            cn.lizongyi.shareaccount.response.ShareConfigResponse response = new cn.lizongyi.shareaccount.response.ShareConfigResponse();
            return response;
        }
    }

    /**
     * 更新配置值
     * @param userId 用户ID（如果为null则更新全局配置）
     * @param key 配置键名
     * @param value 配置值
     */
    @Override
    public void updateConfigValue(Long userId, String key, String value) {
        try {
            configMapper.updateConfigByKey(key, value);
            log.info("配置更新成功: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("更新配置失败: key={}", key, e);
            throw new RuntimeException("更新配置失败");
        }
    }
}
