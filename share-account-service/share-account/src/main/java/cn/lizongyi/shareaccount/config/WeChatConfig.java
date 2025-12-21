package cn.lizongyi.shareaccount.config;

import cn.lizongyi.shareaccount.services.BaseHandler;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class WeChatConfig {

    @Autowired
    private BaseHandler baseHandler;
    
    /**
     * 获取微信App ID
     */
    public String getAppid() {
        return baseHandler.getWechatV2Appid();
    }
    
    /**
     * 获取微信App Secret
     */
    public String getAppsecret() {
        return baseHandler.getWechatV2Appsecret();
    }

    @Bean
    public WxPayConfig wxPayConfig() throws IOException {
        WxPayConfig payConfig = new WxPayConfig();
        
        // 从BaseHandler获取微信支付V2配置
        payConfig.setAppId(baseHandler.getWechatV2Appid());
        payConfig.setMchId(baseHandler.getWechatV2MchId());
        payConfig.setMchKey(baseHandler.getWechatV2MchKey());
        
        // 处理证书路径，从配置中获取证书路径，然后转换为Resource
        String keyPathStr = baseHandler.getWechatV2KeyPath();
        Resource keyPath = new ClassPathResource(keyPathStr.replace("classpath:", ""));
        
        // 将证书资源复制到临时文件并设置路径
        File tempFile = File.createTempFile("apiclient_cert", ".p12");
        try (InputStream inputStream = keyPath.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
        payConfig.setKeyPath(tempFile.getAbsolutePath());

        payConfig.setNotifyUrl(baseHandler.getWechatV2NotifyUrl());

        return payConfig;
    }

    @Bean
    public WxPayService wxPayService() throws Exception {
        WxPayServiceImpl wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig());
        return wxPayService;
    }
}