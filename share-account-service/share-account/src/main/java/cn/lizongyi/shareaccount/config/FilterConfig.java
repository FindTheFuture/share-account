package cn.lizongyi.shareaccount.config;

import cn.lizongyi.shareaccount.filter.JwtRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtRequestFilter> jwtRequestFilterRegistration(JwtRequestFilter jwtRequestFilter) {
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(jwtRequestFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}