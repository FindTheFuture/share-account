package cn.lizongyi.shareaccount.config;

import cn.lizongyi.shareaccount.services.BaseHandler;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Slf4j
@Configuration
public class WeChatPayV3Config {
    
    @Autowired
    private BaseHandler baseHandler;


    @Bean
    public Config weChatPayConfigBuild() throws Exception {
        // 从BaseHandler获取微信支付V3配置
        String mchId = baseHandler.getWechatV3MchId();
        String serialNo = baseHandler.getWechatV3SerialNo();
        String apiV3Key = baseHandler.getWechatV3ApiV3Key();
        String privateKeyPathStr = baseHandler.getWechatV3PrivateKeyPath();
        
        // 处理证书路径，从配置中获取证书路径，然后转换为Resource
        Resource privateKeyPath = new ClassPathResource(privateKeyPathStr.replace("classpath:", ""));
        
        // 将证书资源复制到临时文件并设置路径
        File tempFile = File.createTempFile("apiclient_key", ".pem");
        try (InputStream inputStream = privateKeyPath.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            IOUtils.copy(inputStream, outputStream);
        }

        return new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(tempFile.getAbsolutePath())
                .merchantSerialNumber(serialNo)
                .apiV3Key(apiV3Key)
                .build();
    }

    /**
     * 微信支付对象
     * @param config Config
     * @return JsapiServiceExtension
     */
    @Bean
    public JsapiServiceExtension jsapiServiceExtension(Config config){
        log.info("==========加载微信支付对象");
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(config).build();
        log.info("==========微信支付对象加載完成");
        return service;
    }

    /**
     * 微信回调对象
     *
     * @param config Config
     * @return NotificationParser
     */
    @Bean
    public NotificationParser notificationParser(Config config) {
        log.info("==========加载微信回调解析对象");
        NotificationParser parser = new NotificationParser((NotificationConfig) config);
        return parser;
    }

    @Bean
    public RefundService refundService(Config config){
        // 构建退款Service
        log.info("==========加载微信对款对象");
        return new RefundService.Builder().config(config).build();
    }

    @Bean
    public ProfitsharingService profitsharingService(Config config){
        // 构建退款Service
        log.info("==========加载微信分账对象");
        return new ProfitsharingService.Builder().config(config).build();
    }
}