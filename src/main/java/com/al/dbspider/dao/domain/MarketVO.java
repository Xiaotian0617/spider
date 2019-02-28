package com.al.dbspider.dao.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * topcoin
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 01 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
public class MarketVO implements OnlyKey {

    //private Long id;
    //private String name;//symbol的全称
    //private String tradePair;//e.g. BTC/USD   symbol/unit
    //private Integer side;//1买2卖
    @JSONField(name = "la")
    private BigDecimal last;//last;//最新价
    @JSONField(name = "h")
    private BigDecimal high;//high;//最高价
    @JSONField(name = "l")
    private BigDecimal low;//low;//最低价
    @JSONField(name = "o")
    private BigDecimal open;//open;//24小时开盘价
    @JSONField(name = "c")
    private BigDecimal close;//close;//24小时收盘价
    @JSONField(name = "vol")
    private BigDecimal volume;//volume; //24成交量
    @JSONField(name = "amt")
    private BigDecimal amount;//amount;//24小时成交额
    @JSONField(name = "ask")
    private BigDecimal ask;//卖一
    @JSONField(name = "bid")
    private BigDecimal bid;//买一
    @JSONField(name = "ch")
    private BigDecimal change;//change;//24小时涨跌幅
    @JSONField(name = "z8")
    private BigDecimal changeForZeroHour;//changeForZeroHour;//changeForZeroHour 中国式涨跌幅  从北京时间00辰开始计算
    @JSONField(name = "ts")
    private long timestamp;//timestamp
    //市场的唯一标识 例如：Okex_ETH_BTC
    @JSONField(name = "key")
    private String onlyKey;//onlyKey;
    @JSONField(name = "exch")
    private String exchange;//exchange; //交易所
    @JSONField(name = "sym")
    private String symbol;//e.g. btc,eth...
    @JSONField(name = "unit")
    private String unit;//兑换货币单位 BTC/USD 中的 USD
    //是否需要发送
    //@JSONField(serialize = false)
    @JSONField(name = "needSend")
    private boolean needSend;
    @JSONField(name = "from")
    private String from;
    //最后一次发送时间
    //private Long sendTime;


    public MarketVO() {
    }

    public MarketVO(Market market) {
        this.last = market.getLast();
        this.high = market.getHigh();
        this.low = market.getLow();
        this.open = market.getOpen();
        this.close = market.getClose();
        this.volume = market.getVolume();
        this.amount = market.getAmount();
        this.ask = market.getAsk();
        this.bid = market.getBid();
        this.change = market.getChange();
        this.changeForZeroHour = market.getChange();
        this.timestamp = market.getTimestamp();
        this.onlyKey = market.getOnlyKey();
        this.exchange = market.getExchange();
        this.symbol = market.getSymbol();
        this.unit = market.getUnit();
    }

    public MarketVO(BigDecimal last, BigDecimal high, BigDecimal low, BigDecimal open, BigDecimal close, BigDecimal volume, BigDecimal amount, BigDecimal ask, BigDecimal bid, BigDecimal change, BigDecimal changeForZeroHour, long timestamp, String onlyKey, String exchange, String symbol, String unit, boolean needSend) {
        this.last = last;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.volume = volume;
        this.amount = amount;
        this.ask = ask;
        this.bid = bid;
        this.change = change;
        this.changeForZeroHour = changeForZeroHour;
        this.timestamp = timestamp;
        this.onlyKey = onlyKey;
        this.exchange = exchange;
        this.symbol = symbol;
        this.unit = unit;
        this.needSend = needSend;
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }


}
