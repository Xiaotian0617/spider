package com.al.dbspider.config;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 市场行情网站配置信息
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 04/01/2018 18:00
 */
@Data
@Component
@ConfigurationProperties
public class MarketQuatationSiteConfig {

    private Map<String, MarketSite> marketSite = Maps.newHashMap();


}
