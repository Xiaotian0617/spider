package com.al.dbspider.dao.domain;

import com.al.dbspider.base.ExchangeConstant;
import lombok.Data;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/18 14:46
 */
@Data
public class Depth implements OnlyKey {
    private String onlykey;
    /**
     * 0全部,1更新
     */
    private Integer type;
    private Long timestamp;
    private DepthTick tick;

    public Depth(ExchangeConstant exchange, String symbol, String unit) {
        this.onlykey = exchange + "_" + symbol + "_" + unit;
    }

    @Override
    public String onlyKey() {
        return this.onlykey;
    }
}
