package com.al.dbspider.base;

import com.al.dbspider.base.api.Kraken;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
 * @author 12:40  王楷
 * @version 12:40 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.kraken")
public class KrakenExchange extends BaseRest {

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getAssetPairs, 0, 24, TimeUnit.HOURS);
        schedule.scheduleAtFixedRate(getTickers, 10, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    @Autowired
    private Kraken kraken;
    /**
     * <pair_name> = pair name
     * a = ask array(<price>, <whole lot volume>, <lot volume>),
     * b = bid array(<price>, <whole lot volume>, <lot volume>),
     * c = last trade closed array(<price>, <lot volume>),
     * v = volume array(<today>, <last 24 hours>),
     * p = volume weighted average price array(<today>, <last 24 hours>),
     * t = number of trades array(<today>, <last 24 hours>),
     * l = low array(<today>, <last 24 hours>),
     * h = high array(<today>, <last 24 hours>),
     * o = today's opening price
     */
    private Runnable getTickers = () -> {
        try {
            String pairsStr = Kraken.pairs.stream().collect(Collectors.joining(","));
            // 创建一个用于批量保存的List
            List<OnlyKey> markets = new ArrayList<>();
            String body = kraken.getTickers(pairsStr).execute().body();
            if (Objects.equals(null, body) || Objects.equals("", body)) {
                return;
            }
            JSONObject.parseObject(body).getJSONObject("result").entrySet().stream().filter(stringObjectEntry -> !".".contains(stringObjectEntry.getKey())).forEach(stringObjectEntry -> {
                String[] strings = new String[5];
                strings[1] = stringObjectEntry.getKey().substring(stringObjectEntry.getKey().length() - 3);
                strings[0] = stringObjectEntry.getKey().replace(strings[1], "");
                Market market = new Market(ExchangeConstant.Kraken, strings[0], strings[1]);
                JSONObject jsonObject = (JSONObject) stringObjectEntry.getValue();
                market.setLast(jsonObject.getJSONArray("c").getBigDecimal(0));
                market.setVolume(jsonObject.getJSONArray("v").getBigDecimal(0));
                market.setLow(jsonObject.getJSONArray("l").getBigDecimal(0));
                market.setHigh(jsonObject.getJSONArray("h").getBigDecimal(0));
                market.setOpen(jsonObject.getBigDecimal("o"));
                market.setClose(jsonObject.getJSONArray("c").getBigDecimal(0));
                market.setTimestamp(System.currentTimeMillis());
                log.debug("{} {}", ExchangeConstant.Kraken, market);
                markets.add(market);
            });
            influxDbMapper.postData(markets);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    };

    private Runnable getAssetPairs = () -> {
        try {
            String body = kraken.assetPairs().execute().body();
            Kraken.pairs.addAll(JSONObject.parseObject(body).getJSONObject("result").keySet());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Kraken;
    }
}
