package com.al.dbspider.dao.domain;

import com.al.bcoin.AiCoin;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.utils.PointExt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.annotation.Measurement;

import java.math.BigDecimal;

/**
 * K线数据
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 10/01/2018 18:52
 */
@Data
@Measurement(name = "kline")
public class KLine implements OnlyKey {

    public static final String KLINE = "kline";
    public static final String KLINE_3M = "kline_3m";
    public static final String KLINE_5M = "kline_5m";
    public static final String KLINE_15M = "kline_15m";
    public static final String KLINE_30M = "kline_30m";
    public static final String KLINE_1H = "kline_1h";
    public static final String KLINE_2H = "kline_2h";
    public static final String KLINE_4H = "kline_4h";
    public static final String KLINE_6H = "kline_6h";
    public static final String KLINE_12H = "kline_12h";
    public static final String KLINE_1D = "kline_1d";
    public static final String KLINE_3D = "kline_3d";
    public static final String KLINE_1W = "kline_1w";

    @PointExt.Measurement
    private String measurement = KLINE;
    @PointExt.Tag
    private String exchange; //交易所
    @PointExt.Tag
    private String symbol; //币种
    @PointExt.Tag
    private String unit; //单位

    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;
    @PointExt.Time
    private Long timestamp;
    private String type;
    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC
    private String id;

    public KLine(ExchangeConstant exchange, String symbol, String unit) {
        this.exchange = exchange.toString();
        setSymbol(symbol);
        setUnit(unit);
        setOnlyKey(exchange.toString(), this.symbol, this.unit);
    }

    private void setOnlyKey(String exchange, String symbol, String unit) {
        if (onlyKey == null) {
            onlyKey = String.format("%s_%s_%s", exchange, symbol, unit);
        }
    }

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    public void setSymbol(String symbol) {
        this.symbol = StringUtils.upperCase(symbol);
    }

    public void setUnit(String unit) {
        this.unit = StringUtils.upperCase(unit);
    }


    public KLine(String measurement, String exchange, String symbol, String unit, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal volume, Long timestamp, String type, String onlyKey) {
        this.measurement = measurement;
        this.exchange = exchange;
        this.symbol = symbol;
        this.unit = unit;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.timestamp = timestamp;
        this.type = type;
        this.onlyKey = onlyKey;
    }

    public KLine(AiCoin.Kline kline) {
        timestamp = Long.valueOf(kline.getTs());
        open = kline.getOpen();
        high = kline.getHigh();
        low = kline.getLow();
        close = kline.getClose();
        volume = kline.getVol();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
