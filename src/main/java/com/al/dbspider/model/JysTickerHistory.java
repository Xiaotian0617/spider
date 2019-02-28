package com.al.dbspider.model;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * file:topcoin
 * <p>
 * 文件简要说明
 *
 * @author 15:33  王楷
 * @version 15:33 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights
 * Reserved.
 */
@Measurement(name = "JysTickerHistory")
public class JysTickerHistory {

    @Column(name = "jysFullName", tag = true)
    private String jysFullName;

    @Column(name = "coinFullName", tag = true)
    private String coinFullName;

    @Column(name = "unit", tag = true)
    private String unit;

    @Column(name = "lastPrice")
    private String lastPrice;

    @Override
    public String toString() {
        return "JysTickerHistory{" +
                "jysFullName='" + jysFullName + '\'' +
                ", coinFullName='" + coinFullName + '\'' +
                ", unit='" + unit + '\'' +
                ", lastPrice='" + lastPrice + '\'' +
                '}';
    }

}
