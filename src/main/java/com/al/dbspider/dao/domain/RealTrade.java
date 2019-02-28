package com.al.dbspider.dao.domain;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.utils.PointExt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/8/29 16:03
 */
@Data
public class RealTrade implements OnlyKey {


    private Long localtime;//本地时间毫秒

    @PointExt.Measurement
    private String measurement = "trade";

    private String tradeId;//交易所成交 id
    @PointExt.Tag
    private String exchange;//交易所
    @PointExt.Tag
    private String symbol;//股票代码
    @PointExt.Tag
    private String unit;//兑换货币单位
    private String tradePaire;//e.g. symbol_unit
    private BigDecimal price;//成交价
    private BigDecimal volume;//成交量
    private BigDecimal amount;//成交额
    private String side;//方向,买卖
    @PointExt.Time
    private Long timestamp;//成交时间毫秒
    private String onlyKey;


    public RealTrade(ExchangeConstant exchange, String symbol, String unit) {
        this.exchange = exchange.toString();
        setSymbol(symbol);
        setUnit(unit);
        setOnlyKey(this.exchange, this.symbol, this.unit);
    }

    private void setOnlyKey(String exchange, String symbol, String unit) {
        onlyKey = String.format("%s_%s_%s", exchange, symbol.toUpperCase(), unit.toUpperCase());
    }

    public String getOnlyKey() {
        return onlyKey == null ? String.format("%s_%s_%s", exchange, symbol, unit) : onlyKey;
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
}
