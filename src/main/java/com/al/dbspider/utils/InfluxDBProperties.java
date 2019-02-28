package com.al.dbspider.utils;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 15/01/2018 16:01
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "spring.influxdb")
public class InfluxDBProperties {
    @NotEmpty
    private String dataBase;

    @NotEmpty
    private String url;

    @NotEmpty
    private String port;

    @NotEmpty
    private String userName;

    @NotEmpty
    private String password;

    private boolean enable;

    private int connectTimeout = 10;

    private int readTimeout = 30;

    private int writeTimeout = 10;

    private boolean gzip = false;

    private String retentionPolicy = "autogen";

}
