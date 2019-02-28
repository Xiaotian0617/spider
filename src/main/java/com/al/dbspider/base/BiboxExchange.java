package com.al.dbspider.base;

import com.al.dbspider.base.api.Bibox;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 14:59  王楷
 * @version 14:59 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bibox")
public class BiboxExchange extends BaseRest {

    private String klineCommand = "kline";
    private String allMarketCommand = "marketAll";
    private String dealsCommand = "deals";

    private static Set<String> pairs = new HashSet<String>();

    //['1min', '3min', '5min', '15min', '30min', '1hour', '2hour', '4hour', '6hour', '12hour', 'day', 'week']
    private List<String> klineType = new ArrayList<String>() {{
        add("1min");
        add("day");
    }};

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        // 5秒抓一次交易信息
        schedule.scheduleWithFixedDelay(requestTradeInfo, 5, 2, TimeUnit.SECONDS);
        // 15秒 抓一次市场信息
        schedule.scheduleAtFixedRate(requestAllMarket, 10, 15, TimeUnit.SECONDS);
        //每120秒，开启抓取K线数据
        schedule.scheduleWithFixedDelay(requestKlineByKey, 20, 120, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    @Autowired
    private Bibox bibox;

    /**
     * 获取全部交易信息
     */
    private Runnable requestTradeInfo = () -> {
        try {
            pairs.forEach(str -> {
                String body = null;
                try {
                    body = bibox.getDeals(dealsCommand, str, "200").execute().body();
                } catch (IOException e) {
                    log.error("Bibox获取Trade信息出错！", e);
                }
                JSONObject jsonObject = JSONObject.parseObject(body);
                if (jsonObject == null) {
                    return;
                }
                JSONArray result = jsonObject.getJSONArray("result");
                List<OnlyKey> onlyKeys = getTradeByJson(result, str);
                postData(onlyKeys);
            });
        } catch (Throwable e) {
            log.error("Bibox解析Trade信息出错！", e);
        }
    };

    private List<OnlyKey> getTradeByJson(JSONArray result, String pair) {
        String[] split = pair.split("_");
        // 创建一个用于批量保存的List
        List<OnlyKey> trades = result.stream().map(job -> {
            JSONObject jsonObject = (JSONObject) job;
            Trade trade = new Trade(ExchangeConstant.Bibox, split[0], split[1]);
            trade.setTradeId(jsonObject.getString("id"));
            String cachedId = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
            String isOK = cacheTid(cachedId, trade.getTradeId());
            if (isOK == null) {
                log.debug("{} exist", cachedId);
                return null;
            }
            trade.setTimestamp(jsonObject.getLong("time") * 1000000L);
            trade.setPrice(jsonObject.getBigDecimal("price"));
            trade.setVolume(jsonObject.getBigDecimal("amount"));
            trade.setSide(getSide(jsonObject.getInteger("side")));
            log.debug("Bibox Exchange trade {}", trade);
            return (OnlyKey) trade;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return trades;
    }

    private String getSide(Integer side) {
        switch (side) {
            case 1:
                return "buy";
            case 2:
                return "sell";
            default:
                return "";
        }
    }

    /**
     * 获取全部市场行情
     */
    private Runnable requestAllMarket = () -> {
        try {
            String body = bibox.getMarketAll().execute().body();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("result");
            // 创建一个用于批量保存的List
            List<OnlyKey> markets = new ArrayList<>();
            jsonArray.forEach(o -> {
                JSONObject jsonObject = (JSONObject) o;
                Market market = new Market(ExchangeConstant.Bibox,
                        jsonObject.getString("coin_symbol"),
                        jsonObject.getString("currency_symbol"));
                pairs.add(jsonObject.getString("coin_symbol") + "_" + jsonObject.getString("currency_symbol"));
                market.setVolume(jsonObject.getBigDecimal("vol24H"));
                market.setAmount(jsonObject.getBigDecimal("amount"));
                market.setChange(new BigDecimal(jsonObject.getString("percent").replace("%", "")));
                market.setLast(jsonObject.getBigDecimal("last"));
                market.setHigh(jsonObject.getBigDecimal("high"));
                market.setLow(jsonObject.getBigDecimal("low"));
                market.setType("Api");
                market.setTimestamp(System.currentTimeMillis());
                markets.add(market);
                log.debug("Bibox Exchange market {}", market);
            });
            postData(markets);
        } catch (IOException e) {
            log.error("获取Bibox市场信息出错！", e);
        } catch (Exception e) {
            log.error("解析或保存Bibox市场信息出错", e);
        }
    };

    /**
     * 获取K线数据
     */
    private Runnable requestKlineByKey = () -> {
        pairs.forEach(pair -> {
            try {
                String[] pairs = pair.split("_");
                if (pairs.length != 2) {
                    return;
                }
                for (String period : klineType) {
                    List<OnlyKey> list = new ArrayList<>();
                    String body = bibox.getKline("kline", pair, period, "1000").execute().body();
                    if (!StringUtils.hasText(body)) {
                        return;
                    }
                    JSONArray result = JSONObject.parseObject(body).getJSONArray("result");
                    if (result == null) {
                        return;
                    }
                    result.forEach(o -> {
                        JSONObject jsonObject = (JSONObject) o;
                        KLine kLine = new KLine(ExchangeConstant.Bibox, pairs[0], pairs[1]);
                        kLine.setVolume(jsonObject.getBigDecimal("vol"));
                        kLine.setLow(jsonObject.getBigDecimal("low"));
                        kLine.setHigh(jsonObject.getBigDecimal("high"));
                        kLine.setClose(jsonObject.getBigDecimal("close"));
                        kLine.setOpen(jsonObject.getBigDecimal("open"));
                        kLine.setTimestamp(jsonObject.getTimestamp("time").getTime());
                        kLine.setType("Api");
                        kLine.setMeasurement(getMeasurement(period));
                        list.add(kLine);
                        log.debug("Bibox Exchange {} kline {}", period, kLine);
                    });
                    postData(list);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                log.error("BiboxExchange" + e.getMessage(), e);
            }
        });
    };

    private String getMeasurement(String period) {
        switch (period) {
            case "1min":
                return "kline";
            case "day":
                return "kline_1D";
        }
        return "kline";
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bibox;
    }
}
