package com.al.dbspider.rest;

import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSONArray;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BitZKlineRest extends BaseRest {

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
        MARKET_URL = "https://www.bit-z.com/api_v1/kline?coin=gxs_btc&type=1m";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 10, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);
            Map map = JsonPath.read(resultMap, "$.data.datas['contractUnit','moneyType','data']");
            JSONArray.parseArray(map.get("data").toString());

        } catch (Exception e) {
            log.error("BIT-Z market " + e.getMessage(), e);
        }

    };

    public static void main(String... args) {
        BitZKlineRest.start();
    }


}
