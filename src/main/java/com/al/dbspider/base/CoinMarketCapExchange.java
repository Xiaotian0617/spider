package com.al.dbspider.base;

import com.al.dbspider.base.api.CoinMarketCap;
import com.al.dbspider.dao.domain.MarketCap;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 10:26  王楷
 * @version 10:26 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.coinmarketcap")
public class CoinMarketCapExchange extends BaseRest {
    @Autowired
    private CoinMarketCap coinMarketCap;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        // 1分钟 抓一次交易信息
        schedule.scheduleAtFixedRate(() -> {
            try {
                String ret = coinMarketCap.ticker(0, null).execute().body();
                List<MarketCap> list = JSONArray.parseArray(ret, MarketCap.class);
                if (list != null && list.size() > 0) {
                    influxDbMapper.postData(list);
                }
                log.debug("抓取到的市值信息为{}", JSONArray.toJSONString(list));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 1, 15, TimeUnit.SECONDS);

    }

    @Override
    protected void onStart() {

    }

    public static void main(String... args) {
        try {
            new CoinMarketCapExchange().start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.CoinMarketCap;
    }
}
