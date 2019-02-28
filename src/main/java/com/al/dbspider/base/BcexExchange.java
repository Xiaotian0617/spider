package com.al.dbspider.base;

import com.al.dbspider.base.api.Bcex;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bcex")
public class BcexExchange extends BaseRest {

    @Autowired
    private Bcex bcex;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(this::ticker, 20, 1, TimeUnit.SECONDS);
    }

    private void ticker() {
        try {
            Response<String> response = bcex.tickers().execute();
            log.trace("{}", response);
            Result result = JSONObject.parseObject(response.body(), Result.class);
            if (result.code == 0 && result.data != null) {
                List<OnlyKey> tickers = result.data.values().stream()
                        .flatMap(Collection::stream)
                        .map(this::mapper)
                        .collect(Collectors.toList());

                log.trace("{}", tickers);
                influxDbMapper.postData(tickers);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static final BigDecimal H = new BigDecimal(BigInteger.valueOf(100));

    private Market mapper(Ticker ticker) {
        Market market = new Market(ExchangeConstant.Bcex, ticker.coinFrom, ticker.coinTo);
        market.setLast(ticker.current);
        market.setChange(ticker.ups.multiply(H));
        market.setVolume(ticker.count);
        market.setTimestamp(System.currentTimeMillis());
        return market;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bcex;
    }


    @Data
    static class Result {
        int code;
        String msg;
        Map<String, List<Ticker>> data;
    }

    @Data
    static class Ticker {
        String coinFrom;
        String coinTo;
        //        @JSONField(name = "7_ups")
//        String ups7; //7天涨跌幅
        @JSONField(name = "24_ups")
        BigDecimal ups; //24小时涨跌幅
        BigDecimal count;
        BigDecimal current;
    }
}
