package com.al.dbspider.config;

import lombok.Data;

/**
 * 网站配置信息
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 08/01/2018 18:21
 */
@Data
public class MarketSite {
    private Integer id;//: 1
    private String site;//: "1site"
    private String url;//: "1http"
    private String protocol;//: "websocket"
    private boolean enable;//: true
}
