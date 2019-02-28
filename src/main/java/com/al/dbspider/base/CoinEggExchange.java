package com.al.dbspider.base;

import com.al.dbspider.base.api.CoinEgg;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 11:50  王楷
 * @version 11:50 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.coinegg")
public class CoinEggExchange extends BaseRest {
    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getBTC, 0, 1, TimeUnit.SECONDS);
        schedule.scheduleAtFixedRate(getUsc, 5, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    @Autowired
    private CoinEgg coinEgg;

    private Runnable getBTC = () -> {
        try {
            parse(coinEgg.getMarketBTCList().execute().body(), "BTC");
        } catch (Exception e) {
            log.error("CoinEgg获取BTC出错，错误信息为：", e);
        }
    };

    private Runnable getUsc = () -> {
        try {
            parse(coinEgg.getMarketUSCList().execute().body(), "USC");
        } catch (Exception e) {
            log.error("CoinEgg获取USC出错，错误信息为：", e);
        }
    };

    private void parse(String json, String unit) {
        if (Objects.equals(json, null) || Objects.equals(json, "")) {
            return;
        }
        JSONObject object = JSONObject.parseObject(json);
        List<OnlyKey> list = object.values().stream().map(o -> mapper(o, unit)).collect(Collectors.toList());
        influxDbMapper.postData(list);
    }

    private Market mapper(Object array, String unit) {
        JSONArray arr = ((JSONArray) array);
        Market market = new Market(ExchangeConstant.Coinegg, arr.getString(0), unit);
        market.setLast(arr.getBigDecimal(1));
        market.setHigh(arr.getBigDecimal(4));
        market.setLow(arr.getBigDecimal(5));
        market.setVolume(arr.getBigDecimal(6));
        market.setAmount(arr.getBigDecimal(7));
        market.setChange(arr.getBigDecimal(8));
        market.setTimestamp(System.currentTimeMillis());
        log.debug("{} {}", ExchangeConstant.Coinegg, market);
        return market;
    }


    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coinegg;
    }
}
