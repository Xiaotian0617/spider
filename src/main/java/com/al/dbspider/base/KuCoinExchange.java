package com.al.dbspider.base;

import com.al.dbspider.base.api.KuCoin;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.kucoin")
public class KuCoinExchange extends BaseRest {

    @Autowired
    private KuCoin kuCoin;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(this::ticker, 10, 1, TimeUnit.SECONDS);
    }

    private void ticker() {
        try {
            Response<String> response = kuCoin.tickers().execute();
            log.trace("{}", response.body());
            Result result = JSONObject.parseObject(response.body(), Result.class);
            if (result.success && result.data != null) {
                List<OnlyKey> tickers = result.data.stream().map(this::mapper).collect(Collectors.toList());
                log.trace("{}", tickers);
                influxDbMapper.postData(tickers);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Market mapper(Ticker ticker) {
        Market market = new Market(ExchangeConstant.Kucoin, ticker.coinType, ticker.coinTypePair);
        market.setLast(ticker.lastDealPrice);
        market.setBid(ticker.buy);
        market.setAsk(ticker.sell);
        market.setAmount(ticker.volValue);
        market.setVolume(ticker.vol);
        market.setHigh(ticker.high);
        market.setLow(ticker.low);
        market.setChange(ticker.changeRate);
        market.setTimestamp(ticker.datetime);
        return market;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Kucoin;
    }

    @Data
    static class Result {
        boolean success;
        long timestamp;
        List<Ticker> data;
    }

    @Data
    static class Ticker {
        String coinType;
        boolean trading;
        BigDecimal lastDealPrice;
        BigDecimal buy;
        BigDecimal sell;
        BigDecimal change;
        String coinTypePair;
        BigDecimal volValue;
        BigDecimal high;
        BigDecimal vol;
        BigDecimal low;
        BigDecimal changeRate;
        long datetime;
    }
}
