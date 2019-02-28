package com.al.dbspider.rest;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

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
public class KucoinRest extends BaseRest {

    private final static String JYS_FULL_NAME = "Kucoin";

    private final static String MARKET_URL;

    static {
        MARKET_URL = "https://kitchen-5.kucoin.com/v1/market/open/symbols";
    }

    public static void start() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 30, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);

            JSONObject jsonObject = JSONObject.parseObject(resultMap);
            if (!Objects.equals(true, jsonObject.getBoolean("success"))) {
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                // 创建存入数据库的最新价格对象
                // 设置币种(取币币交易的第一个)
                // 设置单位(取币币交易的第二个)
                Market market = new Market(ExchangeConstant.Kucoin, json.getString("coinType"), json.getString("coinTypePair"));
                // 设置最新价格
                market.setLast(json.getBigDecimal("lastDealPrice"));
                // 设置交易所全名
                // 设置创建时间和更新时间
                market.setTimestamp(System.currentTimeMillis());
                //System.out.println(JSONObject.toJSONString(market));
//				HttpUtils.get().post(MONEY_URL, JSONObject.toJSONString(market));
            }
        } catch (Exception e) {
            log.error("Gate market " + e.getMessage(), e);
        }

    };

    public static void main(String... args) {
        KucoinRest.start();
    }

}
