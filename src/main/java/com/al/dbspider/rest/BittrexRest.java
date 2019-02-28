package com.al.dbspider.rest;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.utils.HttpUtils;
import com.al.dbspider.utils.InfluxDbMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
//@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BittrexRest {
    private final static ScheduledExecutorService SCHEDULER;
    private final static JsonPath JSON_PATH;
    private final static String MARKET_URL;

    @Autowired
    private InfluxDbMapper influxDbMapper;

    static {
        SCHEDULER = Executors.newScheduledThreadPool(2);
        MARKET_URL = "https://bittrex.com/api/v2.0/pub/Markets/GetMarketSummaries";
        JSON_PATH = JsonPath.compile("$.result.*.Summary.['MarketName','Last','Volume']");
    }


    public void start() {
        SCHEDULER.scheduleWithFixedDelay(runnable, 1, 30, TimeUnit.SECONDS);
    }

    private Runnable runnable = () -> {
        try {
            List<Map> markets = JsonPath.parse(HttpUtils.get().get(MARKET_URL)).read(JSON_PATH);
            markets.forEach(map -> {
                String[] marketNames = map.get("MarketName").toString().split("-");
                String symbol = marketNames[1];
                String unit = marketNames[0];
                BigDecimal price = BigDecimal.valueOf((Double) map.get("Last"));
                BigDecimal vol = new BigDecimal(map.get("Volume").toString());
                log.debug("测试：{}:{}:{}:{}:::{}", symbol, unit, price, vol, ExchangeConstant.Bittrex);
                Market market = new Market(ExchangeConstant.Bittrex, symbol, unit);
                market.setLast(price);
                market.setVolume(vol);
                market.setTimestamp(System.currentTimeMillis());
                influxDbMapper.postData(market);
            });
        } catch (Exception e) {
            log.error("bittrex market " + e.getMessage(), e);
        }

    };
}