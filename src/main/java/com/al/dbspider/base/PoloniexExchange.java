package com.al.dbspider.base;

import com.al.dbspider.base.api.Poloniex;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.poloniex")
public class PoloniexExchange extends BaseRest {
    @Autowired
    private Poloniex poloniex;

    private Runnable marketRunnable = () -> {
        try {
            String resultMap = poloniex.getTicker("returnTicker").execute().body();
            Map<String, JSONObject> marketMap = JSON.parseObject(resultMap, Map.class);
            // 创建一个用于批量保存的List
            List<OnlyKey> markets = new ArrayList<>();
            if (Objects.equals(null, marketMap)) {
                return;
            }
            if (marketMap != null) {
                marketMap.entrySet().stream().filter(stringJSONObjectEntry -> stringJSONObjectEntry.getValue().getString("isFrozen").equals("0")).forEach(jsonObjectEntry -> {
                    log.debug("Key = {}, Value = {}", jsonObjectEntry.getKey(), jsonObjectEntry.getValue());
                    String[] str = jsonObjectEntry.getKey().split("_");
                    if (str.length != 2) {
                        return;
                    }
                    //这个地方传入的Key值与保存相反，BTC_BCN
                    // 实质上在这个网站是BCN/BTC 所以下方数组先传第二个后传第一个
                    Market market = new Market(ExchangeConstant.Poloniex, str[1], str[0]);
                    market.setChange(jsonObjectEntry.getValue().getBigDecimal("percentChange"));
                    market.setBid(jsonObjectEntry.getValue().getBigDecimal("highestBid"));
                    market.setAsk(jsonObjectEntry.getValue().getBigDecimal("lowestAsk"));
                    market.setLow(jsonObjectEntry.getValue().getBigDecimal("low24hr"));
                    market.setLast(jsonObjectEntry.getValue().getBigDecimal("last"));
                    market.setHigh(jsonObjectEntry.getValue().getBigDecimal("high24hr"));
//                    market.setVolume(jsonObjectEntry.getValue().getBigDecimal("baseVolume"));
                    market.setVolume(jsonObjectEntry.getValue().getBigDecimal("quoteVolume"));
                    market.setTimestamp(System.currentTimeMillis());
                    log.debug("{} {}", ExchangeConstant.Poloniex, market);
                    markets.add(market);
                });
                influxDbMapper.postData(markets);
            }
        } catch (Exception e) {
            log.error("PoloniexRest market " + e.getMessage(), e);
        }
    };

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        // 1秒 抓一次币种信息
        schedule.scheduleAtFixedRate(marketRunnable, 0, 1, TimeUnit.SECONDS);
        // 30秒 抓一次交易记录信息
        //schedule.scheduleAtFixedRate(tradeHistoryRunnable, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Poloniex;
    }
}
