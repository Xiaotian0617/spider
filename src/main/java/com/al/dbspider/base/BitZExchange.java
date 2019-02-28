package com.al.dbspider.base;

import com.al.dbspider.base.api.BitZ;
import com.al.dbspider.dao.domain.Market;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
@ConfigurationProperties(prefix = "rest.bitz")
public class BitZExchange extends BaseRest {

    @Autowired
    private BitZ bitZ;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        // 10秒 抓一次交易信息
        schedule.scheduleWithFixedDelay(() -> {
            try {
                BitZ.Info response = bitZ.tickerall().execute().body();
                response.getData().forEach((s, ticker) -> {
                    String[] key = s.split("_");
                    Market market = new Market(ExchangeConstant.Bitz, key[0], key[1]);
                    market.setLast(ticker.getLast());
                    market.setVolume(ticker.getVol());
                    market.setTimestamp(ticker.getDate() * 1000);
                    influxDbMapper.postData(market);
                    log.debug("{} {}", ExchangeConstant.Bitz, market);
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 2, 1, TimeUnit.SECONDS);


        // 1分钟 抓一次K线交易
        // schedule.scheduleWithFixedDelay(() -> {
        //     try {
//                Map map = JsonPath.read(BitZ.API.kline("eth_btc","1m").execute().body()
//                        ,"$.data.datas[\"contractUnit\",\"moneyType\",\"data\",\"marketName\"]");
//                log.debug(BitZ.API.kline("eth_btc", "1min", "1000").execute().body());
//                Map map = JsonPath.read(BitZ.API.kline("eth_btc", "1min", "1000").execute().body()
//                        , "$.datas[\"contractUnit\",\"moneyType\",\"data\",\"marketName\"]");
//                JSONArray jsonArray = JSONArray.parseArray(map.get("data").toString());

//                influxDbMapper.addKlineBatchData("BitZ", "ETH", "BTC", jsonArray.toJavaList(List.class));
        //  } catch (Exception e) {
        //    log.error(e.getMessage(), e);
        //  }
        //  }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void onStart() {

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitz;
    }
}
