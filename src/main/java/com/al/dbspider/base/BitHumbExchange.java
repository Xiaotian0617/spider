package com.al.dbspider.base;

import com.al.dbspider.base.api.BitHumb;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bithumb")
public class BitHumbExchange extends BaseRest {

    @Autowired
    private BitHumb bitHumb;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(this::getTicker, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    private void getTicker() {
        try {
            String response = bitHumb.tickers().execute().body();
            Result result = JSONObject.parseObject(response, Result.class);
            if ("0000".equals(result.getStatus())) {
                Long date = Long.valueOf(result.getData().remove("date"));
                List<OnlyKey> markets = result.getData().entrySet().stream()
                        .map(this::mapper)
                        .peek(market -> market.setTimestamp(date))
                        .collect(Collectors.toList());
                influxDbMapper.postData(markets);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Market mapper(Map.Entry<String, String> pair) {
        Market market = new Market(ExchangeConstant.Bithumb, pair.getKey().toUpperCase(), "KRW");
        Ticker t = JSONObject.parseObject(pair.getValue(), Ticker.class);
        market.setOpen(t.openingPrice);
        market.setClose(t.closingPrice);
        market.setLast(t.closingPrice);
        market.setLow(t.minPrice);
        market.setHigh(t.maxPrice);
        market.setAverage(t.averagePrice);
        market.setAmount(t.unitsTraded);
        market.setVolume(t.volume1day);
        market.setBid(t.buy_price);
        market.setAsk(t.sell_price);
        market.setTimestamp(System.currentTimeMillis());
        log.debug("{} {}", ExchangeConstant.Bithumb, market);
        return market;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bithumb;
    }

    @Data
    static class Result {
        String status;  //result status code (normal: 0000, refer to error code for codes other than the normal)
        Map<String, String> data; //Timestamp on the current time
    }

    @Data
    static class Ticker {
        BigDecimal openingPrice; //transaction amount starting within the recent 24 hours
        BigDecimal closingPrice; //last transaction amount within the recent 24 hours
        BigDecimal minPrice; //lowest transaction amount within the recent 24 hours
        BigDecimal maxPrice; //highest transaction amount within the recent 24 hours
        BigDecimal averagePrice; //average transaction amount within the recent 24 hours
        BigDecimal unitsTraded; //Currency transactions in the recent 24 hours
        BigDecimal volume1day; //Currency trading volume within the day
        BigDecimal volume7day; //Currency trading volume within the recent 7 days
        BigDecimal buy_price; //maximum Buy amount on the standby
        BigDecimal sell_price; //minimum Sell amount on standby
    }
}
