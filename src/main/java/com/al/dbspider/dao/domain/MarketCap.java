package com.al.dbspider.dao.domain;

import com.al.dbspider.utils.PointExt;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 17:19  王楷
 * @version 17:19 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class MarketCap implements OnlyKey {

    @PointExt.Measurement
    String m = "market_cap";
    //币种全称
    @PointExt.Tag
    String id;
    //币种名称
    String name;
    @PointExt.Tag
    //币种简称
            String symbol;
    //币种市值排名
    Integer rank;
    //币种美元市值
    BigDecimal priceUsd;
    //币种比特币市值
    BigDecimal priceBtc;
    //币种24小时交易量
    @JSONField(name = "24h_volume_usd")
    BigDecimal allDayVolumeUsd;
    //市场总值
    BigDecimal marketCapUsd;
    //可购买量
    BigDecimal availableSupply;
    //市场总量
    BigDecimal totalSupply;
    //币种总量
    BigDecimal maxSupply;
    //一小时价格变化
    BigDecimal percentChange1h;
    //二十四小时价格变化
    BigDecimal percentChange24h;
    //七天的价格变化
    BigDecimal percentChange7d;
    //最后一次更新时间
    @PointExt.Time(TimeUnit.SECONDS)
    long lastUpdated;
    //市场的唯一标识 例如：MarketCap_BTC
    private String onlyKey;


    @Override
    public String toString() {
        return "MarketCap{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", rank=" + rank +
                ", priceUsd=" + priceUsd +
                ", priceBtc=" + priceBtc +
                ", allDayVolumeUsd=" + allDayVolumeUsd +
                ", marketCapUsd=" + marketCapUsd +
                ", availableSupply=" + availableSupply +
                ", totalSupply=" + totalSupply +
                ", maxSupply=" + maxSupply +
                ", percentChange1h=" + percentChange1h +
                ", percentChange24h=" + percentChange24h +
                ", percentChange7d=" + percentChange7d +
                ", lastUpdated=" + lastUpdated +
                '}';
    }


    public MarketCap(String id, String name, String symbol, Integer rank, BigDecimal priceUsd, BigDecimal priceBtc, BigDecimal allDayVolumeUsd, BigDecimal marketCapUsd, BigDecimal availableSupply, BigDecimal totalSupply, BigDecimal maxSupply, BigDecimal percentChange1h, BigDecimal percentChange24h, BigDecimal percentChange7d, long lastUpdated) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.rank = rank;
        this.priceUsd = priceUsd;
        this.priceBtc = priceBtc;
        this.allDayVolumeUsd = allDayVolumeUsd;
        this.marketCapUsd = marketCapUsd;
        this.availableSupply = availableSupply;
        this.totalSupply = totalSupply;
        this.maxSupply = maxSupply;
        this.percentChange1h = percentChange1h;
        this.percentChange24h = percentChange24h;
        this.percentChange7d = percentChange7d;
        this.lastUpdated = lastUpdated;
        setOnlyKey(id);
    }

    public void setOnlyKey(String id) {
        onlyKey = String.format("%s_%s", "MarketCap", id.toUpperCase());
    }


    @Override
    public String onlyKey() {
        return onlyKey;
    }
}

