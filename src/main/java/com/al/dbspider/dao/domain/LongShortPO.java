package com.al.dbspider.dao.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/10/8 15:13
 */
@Data
public class LongShortPO implements OnlyKey {

    Long lastTime;
    BigDecimal longAmount;
    BigDecimal shortAmount;
    String onlyKey;

    public void setOnlyKey(String exchange, String symbol, String unit) {
        this.onlyKey = exchange + "_" + symbol + "_" + unit;
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }
}
