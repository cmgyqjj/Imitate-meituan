package com.hmdp.config;

import com.hmdp.fliter.XSSConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.FilterRegistration;

/**
 * 配置过滤器
 */
@Configuration
public class AntiSamyConfig {

    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new XSSConfig());
        filterRegistrationBean.addUrlPatterns("/");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

}
