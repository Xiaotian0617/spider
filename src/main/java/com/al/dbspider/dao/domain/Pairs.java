package com.al.dbspider.dao.domain;


import com.al.dbspider.utils.PointExt;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.influxdb.annotation.Measurement;

@Data
@Measurement(name = "pairs")
public class Pairs implements OnlyKey {

    @PointExt.Measurement
    private String measurement = "pairs";

    //中文名
    private String coinMarketName;

    //币种简称
    private String coinMarketCode;

    //币种英文名称
    private String coinMarketEn;

    //交易所名称
    private String marketName;

    //交易所代号
    private String marketCode;

    //交易所本项目枚举中的名称
    @PointExt.Tag
    private String exchange; //交易所

    @PointExt.Tag
    private String symbol; //币种

    @PointExt.Tag
    private String unit; //单位

    /**
     * 这个币种在这个交易所所对应的货币单位
     * 比如 coinMarketCode = ETH
     * marketCode = huobipro
     * currency = BTC
     * 则onlykey为 Huobi_ETH_BTC
     */
    private String currency;

    // TODO 应该是各个交易所这个币种的可用状态（待验证）
    private Integer status;

    @PointExt.Time
    private Long timestamp;

    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    public Pairs(String exchange, String symbol, String unit) {
        this.exchange = exchange;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pairs pairs = (Pairs) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(onlyKey, pairs.onlyKey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(onlyKey)
                .toHashCode();
    }
}
