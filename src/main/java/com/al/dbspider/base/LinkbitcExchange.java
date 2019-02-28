package com.al.dbspider.base;

import com.al.dbspider.base.api.Linkbitc;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.linkbitc")
public class LinkbitcExchange extends BaseRest {


    List<String> pair = new ArrayList<String>() {{
        add("BTC_USDT");
        add("BTC_LBT");
        add("IMU_USDT");
        add("ETH_USDT");
        add("LBT_USDT");
        add("LTC_USDT");
        add("QTUM_USDT");
    }};

    @Autowired
    private Linkbitc linkbitc;

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(() -> {
            try {
                String result = linkbitc.market().execute().body();
                if (result == null) {
                    return;
                }
                saveMarkets(result);
            } catch (Throwable e) {
                log.error("Linkbitc 数据获取错误", e);
            }
        }, 5, 5 * 60, TimeUnit.SECONDS);
        schedule.scheduleAtFixedRate(() -> {
            try {
                String result = linkbitc.kline().execute().body();
                if (result == null) {
                    return;
                }
                saveKlines(result);
            } catch (Throwable e) {
                log.error("Linkbitc 数据获取错误", e);
            }
        }, 3, 30, TimeUnit.SECONDS);
    }

    private void saveKlines(String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject == null) {
                return;
            }
            log.trace("LinkBitc Markets 抓取结果：{}", result);
            JSONObject lastKLine = jsonObject.getJSONObject("lastKLine");
            if (lastKLine == null) {
                return;
            }
            List<KLine> collect = lastKLine.entrySet().stream()
                    .filter(object -> ((JSONArray) object.getValue()).size() != 0 && object.getKey().split("_").length == 2)
                    .map(object -> {
                        JSONArray klineJsons = (JSONArray) object.getValue();
                        String[] keys = object.getKey().split("_");
                        return klineMapper(klineJsons.getJSONObject(0).getJSONObject("payload"), keys);
                    }).collect(Collectors.toList());
            influxDbMapper.postData(collect);
        } catch (Throwable e) {
            log.error("linkbitc ", e);
        }
    }

    private void saveMarkets(String result) {
        try {
            JSONArray jsonArray = JSONArray.parseArray(result);
            if (jsonArray == null || jsonArray.size() == 0) {
                return;
            }
            log.trace("LinkBitc Klines 抓取结果：{}", result);
            List<Market> collect = jsonArray.stream().map(object -> {
                JSONObject marketJson = (JSONObject) object;
                String[] keys = marketJson.getString("coinCode").split("_");
                Market market = new Market(ExchangeConstant.Linkbitc, keys[0], keys[1]);
                market.setLast(marketJson.getBigDecimal("lastExchangPrice"));
                market.setOpen(marketJson.getBigDecimal("openPrice"));
                market.setHigh(marketJson.getBigDecimal("maxPrice"));
                market.setLow(marketJson.getBigDecimal("minPrice"));
                market.setClose(marketJson.getBigDecimal("yesterdayPrice"));
                market.setVolume(marketJson.getBigDecimal("transactionSum"));
                market.setTimestamp(System.currentTimeMillis());
                return market;
            }).collect(Collectors.toList());
            influxDbMapper.postData(collect);
        } catch (Throwable e) {
            log.error("linkbitc ", e);
        }
    }

    private KLine klineMapper(JSONObject jsonObject, String[] symbol) {
        KLine k = new KLine(ExchangeConstant.Linkbitc, symbol[0], symbol[1]);
        k.setTimestamp(jsonObject.getLong("time") * 1000L);
        k.setOpen(jsonObject.getBigDecimal("priceOpen"));
        k.setHigh(jsonObject.getBigDecimal("priceHigh"));
        k.setLow(jsonObject.getBigDecimal("priceLow"));
        k.setClose(jsonObject.getBigDecimal("priceLast"));
        k.setVolume(jsonObject.getBigDecimal("amount"));
        return k;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Linkbitc;
    }
}
