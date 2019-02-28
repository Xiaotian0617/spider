package com.al.dbspider.base;

import com.al.dbspider.base.api.Korbit;
import com.al.dbspider.dao.domain.Market;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * spider
 * file:topcoin
 * <p>
 *
 * @author mr.wang
 * @version 12 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.korbit")
public class KorbitExchange extends BaseRest {


    /**
     * 勿执行 IO 操作
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleWithFixedDelay(getDetailedTiker, 2, 5, TimeUnit.SECONDS);
    }

    @Autowired
    private Korbit korbit;


    ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * {
     * "timestamp": 1394590350000,
     * "last": "663699",
     * "bid": "660001",
     * "ask": "663699",
     * "low": "642000",
     * "high": "663699",
     * "volume": "52.50203662"
     * timestamp	Unix timestamp in milliseconds of the last filled order.
     * last	Price of the last filled order.
     * bid	Best bid price.
     * ask	Best ask price.
     * low	Lowest price within the last 24 hours.
     * high	Highest price within the last 24 hours.
     * volume	Transaction volume within the last 24 hours.
     * }
     */
    private Runnable getDetailedTiker = () -> Korbit.list.forEach(pair -> {
        executorService.execute(() -> {
            try {
                String str = korbit.getDetailedTiker(pair).execute().body();
                if (str == null) {
                    log.error("被拦截了！");
                    return;
                }
                JSONObject jsonObject = JSONObject.parseObject(str);
                String[] pairs = pair.split("_");
                Market market = new Market(ExchangeConstant.Korbit, pairs[0], pairs[1]);
                market.setLast(jsonObject.getBigDecimal("last"));
                market.setBid(jsonObject.getBigDecimal("bid"));
                market.setAsk(jsonObject.getBigDecimal("ask"));
                market.setTimestamp(jsonObject.getLong("timestamp"));
                market.setVolume(jsonObject.getBigDecimal("volume"));
                market.setHigh(jsonObject.getBigDecimal("high"));
                market.setLow(jsonObject.getBigDecimal("low"));
                market.setChange(jsonObject.getBigDecimal("changePercent"));
                influxDbMapper.postData(market);
                log.debug("{} {} {}", System.currentTimeMillis(), ExchangeConstant.Korbit, market.toString());
            } catch (IOException e) {
                log.error("KorbitExchange 抓取时出现问题请检查，{}", e);
            } catch (Exception e) {
                log.error("KorbitExchange 解析时出现问题请检查，{}", e);
            }
        });
    });

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Korbit;
    }
}
