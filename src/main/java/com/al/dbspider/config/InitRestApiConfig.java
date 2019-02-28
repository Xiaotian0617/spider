package com.al.dbspider.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 15/01/2018 19:28
 */
@Component
@ConfigurationProperties("api")
public class InitRestApiConfig implements ApplicationListener<ApplicationReadyEvent> {

    String topcoin;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

    }
} 
