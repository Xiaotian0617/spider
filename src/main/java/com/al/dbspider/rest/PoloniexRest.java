package com.al.dbspider.rest;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PoloniexRest extends BaseRest {


    //返回JSON路径
    // private final static JsonPath JSON_PATH;
    //市场URL
    private final static String MARKET_URL;

    static {
        //https://jsonpath.herokuapp.com/  在线调试
        //https://github.com/json-path/JsonPath  获取返回JSON中的属性
        //如："$.result.*.Summary.['MarketName','Last']"  代表取summary里两个属性  这样也可以：$.result[*].Summary.['MarketName','Last']
        //JSON_PATH = JsonPath.compile("$");
        // MARKET_URL = initUrl("https://poloniex.com/public?command=returnTicker");
        MARKET_URL = "https://poloniex.com/public?command=returnTicker";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 30, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);
            Map<String, JSONObject> marketsJson = JSON.parseObject(resultMap, Map.class);
            for (Map.Entry<String, JSONObject> entry : marketsJson.entrySet()) {
                log.debug("Key = {}, Value = {}", entry.getKey(), entry.getValue());
                String[] marketNames = entry.getKey().split("_");
                Market market = new Market(ExchangeConstant.Poloniex, marketNames[1], marketNames[0]);
                market.setLast(new BigDecimal(entry.getValue().getString("last")));
                //log.info(JSONObject.toJSONString(priceJson));
//                HttpUtils.get().post(MONEY_URL, JSONObject.toJSONString(market));
            }
        } catch (Exception e) {
            log.error("PoloniexRest market " + e.getMessage());
        }

    };

    public static void main(String... args) {
        PoloniexRest.start();
    }


}
