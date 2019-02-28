package com.al.dbspider.base;

import com.al.dbspider.base.api.Gate;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 9:27  王楷
 * @version 9:27 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.gate")
public class GateExchange extends BaseRest {

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getAllTickers, 2, 15, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    public Map<String, TradePair> allPairs() {
        HashMap<String, TradePair> pairHashMap = new HashMap<>();
        Response<String> execute = execute(gate.allPairs());
        String body = execute.body();
        JSONArray jsonArray = JSON.parseArray(body);
        for (int i = 0; i < jsonArray.size(); i++) {
            String string = jsonArray.getString(i);
            pairHashMap.put(string, new TradePair(string, "_"));
        }
        return pairHashMap;
    }

    @Autowired
    private Gate gate;
    private Runnable getAllTickers = () -> {

        // 创建一个用于批量保存的List
        List<OnlyKey> markets = new ArrayList<>();
        try {
            String body = gate.getTickerAll().execute().body();
            Map<String, JSONObject> marketJson = com.alibaba.fastjson.JSON.parseObject(body, Map.class);
            if (Objects.equals(marketJson, null)) {
                return;
            }
            marketJson.forEach((key, value) -> {
                // 获取币币交易名称
                String[] marketNames = key.split("_");
                if (marketNames.length != 2) {
                    return;
                }
                // 创建存入数据库的最新价格对象
                Market market = new Market(ExchangeConstant.Gate, marketNames[0], marketNames[1]);
                // 设置最新价格
                market.setLast(value.getBigDecimal("last"));
                market.setAsk(value.getBigDecimal("lowestAsk"));
                market.setBid(value.getBigDecimal("highestBid"));
                market.setChange(value.getBigDecimal("percentChange"));
                market.setVolume(value.getBigDecimal("quoteVolume"));
                market.setHigh(value.getBigDecimal("high24hr"));
                market.setLow(value.getBigDecimal("low24hr"));
                // 设置创建时间和更新时间
                market.setTimestamp(System.currentTimeMillis());
                log.debug("{} {} ", ExchangeConstant.Gate, market);
                markets.add(market);
            });
            influxDbMapper.postData(markets);
        } catch (Exception e) {
            log.error("Gate market " + e.getMessage(), e);
        }
    };

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Gate;
    }
}
