package cn.lizongyi.shareaccount.config;

import cn.lizongyi.shareaccount.services.BaseHandler;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosConfig {

    @Autowired
    private BaseHandler baseHandler;

    @Bean
    public COSClient cosClient() {
        // 从数据库获取COS配置
        String secretId = baseHandler.getCosSecretId();
        String secretKey = baseHandler.getCosSecretKey();
        String region = baseHandler.getCosRegion();

        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域, COS地域的简称(如: ap-beijing)
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}