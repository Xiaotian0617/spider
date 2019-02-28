package com.al.dbspider.base;

import com.al.dbspider.base.api.Coinone;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 18:25  王楷
 * @version 18:25 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.coinone")
public class CoinoneExchange extends BaseRest {

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getAllTicker, 2, 15, TimeUnit.SECONDS);
    }


    @Override
    protected void onStart() {

    }

    @Autowired
    private Coinone coinone;
    /**
     * 获取全部行情
     */
    private Runnable getAllTicker = () -> {

        // 创建一个用于批量保存的List
        List<OnlyKey> markets = new ArrayList<>();
        try {
            String body = coinone.getTickerAll().execute().body();
            JSONObject jsonObject = JSONObject.parseObject(body);
          /*  jsonObject.entrySet().stream().filter(stringObjectEntry -> !"result,timestamp,errorCode".contains(stringObjectEntry.getKey())).forEach(stringObjectEntry -> {
                        JSONObject objectEntryValue = (JSONObject)stringObjectEntry.getValue();
                        Market market = new Market(ExchangeConstant.Coinone,stringObjectEntry.getKey(),"KRW");
                        market.setTimestamp(jsonObject.getTimestamp("timestamp").getTime()*1000L);
                        market.setHigh(objectEntryValue.getBigDecimal("high"));
                        market.setLow(objectEntryValue.getBigDecimal("low"));
                        market.setClose(objectEntryValue.getBigDecimal("last"));
                        market.setLast(objectEntryValue.getBigDecimal("last"));
                        market.setOpen(objectEntryValue.getBigDecimal("first"));
                        market.setVolume(objectEntryValue.getBigDecimal("volume"));
                        market.setChange(objectEntryValue.getBigDecimal("last").divide(objectEntryValue.getBigDecimal("yesterday_last"),4, RoundingMode.HALF_UP));
                        markets.add(market);
                    });*/
            influxDbMapper.postData(markets);
        } catch (IOException e) {
            log.error("Coinone 获取行情失败！", e);
        } catch (Exception e) {
            log.error("Coinone 解析或保存失败！", e);
        }

    };


    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coinone;
    }
}
