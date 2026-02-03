package cn.lizongyi.shareaccount.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web配置类，提供必要的Web相关组件
 */
@Configuration
public class WebConfig {

    /**
     * 配置RestTemplate bean，供服务中使用
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}