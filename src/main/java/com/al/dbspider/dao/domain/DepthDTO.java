package com.al.dbspider.dao.domain;

import lombok.Data;
import org.influxdb.annotation.Column;

import java.math.BigDecimal;
import java.util.List;

/**
 * Market 数据传输对象</p>
 * 内部应用间传输  从 蜘蛛 到 数据中心
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 15:27
 */
@Data
public class DepthDTO implements OnlyKey {
    @Column(name = "onlyKey")
    private String onlyKey;
    @Column(name = "timestamp")
    private Long timestamp;
    @Column(name = "tick")
    private Tick tick;
    /**
     * 0全部,1更新
     */
    @Column(name = "type")
    private Integer type;

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Data
    public static class Tick {
        @Column(name = "asks")
        private List<PriceLevel> asks;
        @Column(name = "bids")
        private List<PriceLevel> bids;
    }

    @Data
    public static class PriceLevel {
        @Column(name = "price")
        private BigDecimal price;
        @Column(name = "count")
        private BigDecimal count;
    }
}

