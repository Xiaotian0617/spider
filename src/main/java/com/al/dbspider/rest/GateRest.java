package com.al.dbspider.rest;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.network.StartHttp;
import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author HYK
 * @Company 河南艾鹿
 * @Date 2018/1/4 0004 11:08
 */
@Slf4j
public class GateRest extends BaseRest {

    private final static String JYS_FULL_NAME = "Gate";

    private final static String MARKET_URL;

    static {
        MARKET_URL = "http://data.gate.io/api2/1/tickers";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 30, TimeUnit.SECONDS);
        StartHttp.getInstence().start(runnable, 30);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);
            Map<String, JSONObject> marketJson = JSON.parseObject(resultMap, Map.class);
            if (Objects.equals(marketJson, null)) {
                return;
            }
            for (Map.Entry<String, JSONObject> entry : marketJson.entrySet()) {
                //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                // 获取币币交易名称
                String[] marketNames = entry.getKey().split("_");
                // 创建存入数据库的最新价格对象
                Market market = new Market(ExchangeConstant.Gate, marketNames[0], marketNames[1]);
                // 设置币种(取币币交易的第一个)
                market.setSymbol(marketNames[0]);
                // 设置单位(取币币交易的第二个)
                market.setUnit(marketNames[1]);
                // 设置最新价格
                market.setLast(new BigDecimal(entry.getValue().getString("last")));
                // 设置交易所全名
                // 设置创建时间和更新时间
                market.setTimestamp(System.currentTimeMillis());
                //System.out.println(market.toString());
//				HttpUtils.get().post(MONEY_URL, JSONObject.toJSONString(market));
            }
        } catch (Exception e) {
            log.error("Gate market " + e.getMessage(), e);
        }

    };

    public static void main(String... args) {
        GateRest.start();
    }

}
