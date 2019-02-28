package com.al.dbspider.base;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * symbol+unit
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 21/01/2018 18:15
 */
@Data
public class TradePair {
    private String noSeparatorTradePair;
    private String symbol;//base
    private String unit;//quote
    private String tradePair;
    private Integer priceDecimal = 0;
    private Integer volumeDecimal = 0;

    public TradePair(String symbol, String unit, String separator) {
        this.symbol = symbol;
        this.unit = unit;
        tradePair = StringUtils.joinWith(separator, symbol, unit);
        this.noSeparatorTradePair = this.symbol + this.unit;
    }

    public TradePair(String tradePair, String separator) {
        String[] split = StringUtils.split(tradePair, separator);
        Assert.noNullElements(split, "初始化交易对出错");
        this.symbol = split[0];
        this.unit = split[1];
        this.tradePair = tradePair;
        this.noSeparatorTradePair = this.symbol + this.unit;
    }

    public String noSeparatorTradePair() {
        return this.noSeparatorTradePair;
    }

    public void setPriceDecimal(Integer priceDecimal) {
        this.priceDecimal = priceDecimal;
    }

    public Integer getPriceDecimal() {
        return priceDecimal;
    }

    public void setVolumeDecimal(Integer volumeDecimal) {
        this.volumeDecimal = volumeDecimal;
    }

    public Integer getVolumeDecimal() {
        return volumeDecimal;
    }
}

