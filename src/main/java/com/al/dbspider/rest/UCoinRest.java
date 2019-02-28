package com.al.dbspider.rest;

import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UCoinRest extends BaseRest {

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
        MARKET_URL = "https://ucoins.cc/api/v1/coins/";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 30, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);
            Map<String, Object> market = JSON.parseObject(resultMap, Map.class);
//            if(!Objects.equals(0,result.get("code"))){
//                return;
//            }
            for (Map.Entry<String, Object> entry : market.entrySet()) {
                log.debug("Key = {}, Value = {}", entry.getKey(), entry.getValue());
                //如果返回是数组类型
                if (entry.getValue().getClass().equals(JSONArray.class)) {
                    JSONArray jsonArray = JSONArray.parseArray(entry.getValue().toString());
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
//                        HttpUtils.get().post(MONEY_URL,
//                                JSONObject.toJSONString(new Market("U-COIN",
//                                        jsonObject.getString("name"),entry.getKey(),new BigDecimal(jsonObject.getString("price")),
//                                        System.currentTimeMillis(),new Date())));
                    }
                } else {
                    //返回是对象类型
                    JSONObject jsonObject = JSONObject.parseObject(entry.getValue().toString());
                    Iterator<String> sIterator = jsonObject.keySet().iterator();
                    while (sIterator.hasNext()) {
                        // 获得key
                        String key = sIterator.next();
                        // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                        String value = jsonObject.getString(key);
                        log.debug("key: {},value:{}", key, value);
//                        HttpUtils.get().post(MONEY_URL,
//                                JSONObject.toJSONString(new Market("U-COIN",
//                                        entry.getKey(),key,new BigDecimal(value),
//                                        System.currentTimeMillis(),new Date())));
                    }
                }
            }
        } catch (Exception e) {
            log.error("UCoinRest-Z market " + e.getMessage(), e);
        }

    };

    public static void main(String... args) {
        UCoinRest.start();
    }

}
