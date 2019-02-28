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
public class UcxRest extends BaseRest {

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
        MARKET_URL = "http://u.cx/api/trade/all/";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 10, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);
            Map<String, JSONObject> markets = JSON.parseObject(resultMap, Map.class);

            for (Map.Entry<String, JSONObject> entry : markets.entrySet()) {
                log.debug("Key = {}, Value = {}", entry.getKey(), entry.getValue());
                String[] marketNames = entry.getKey().split("_");
                Market market = new Market(ExchangeConstant.Ucx, marketNames[0], marketNames[1].substring(3));
                market.setLast(new BigDecimal(entry.getValue().getString("new_price")));
                //System.out.println(JSONObject.toJSONString(priceJson));
//                HttpUtils.get().post(MONEY_URL, JSONObject.toJSONString(market));
            }
        } catch (Exception e) {
            log.error("UCX market " + e.getMessage(), e);
        }

    };

    public static void main(String... args) {
        UcxRest.start();
    }


}
