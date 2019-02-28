package com.al.dbspider.dao.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 买一卖一对象存储至influxdb
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author SUNLEILEI
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/8/23
 */
@Data
public class BuyAndSellFirstPO implements OnlyKey {

    private String measurement = "BuyAndSellFirst";

    private String onlyKey;

    private Long time;

    /**
     * sell卖,buy买
     */
    private String side;

    private BigDecimal price;

    @Override
    public String onlyKey() {
        return onlyKey;
    }
}

