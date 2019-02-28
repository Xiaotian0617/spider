package com.al.dbspider.base;


import com.al.dbspider.base.api.Liqui;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.liqui")
public class LiquiExchange extends BaseRest {

    @Autowired
    private Liqui liqui;

    @Resource
    private InfluxDbMapper influxDbMapper;

    private static String pairsList;
    private static List<Pair> pairs;

    private static HashMap<String, String> klineMap;

    private ScheduledExecutorService klineScheduler;

    @Override
    protected void init() {
        klineMap = new HashMap<>();
        klineMap.put("5", KLine.KLINE_5M);
//        klineMap.put("15",KLine.KLINE_15M);
//        klineMap.put("30",KLine.KLINE_30M);
//        klineMap.put("60",KLine.KLINE_1H);
//        klineMap.put("120",KLine.KLINE_2H);
//        klineMap.put("240",KLine.KLINE_4H);
        klineScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        // 24小时 抓一次币种信息
        schedule.scheduleAtFixedRate(this::getTradePairs, 0, 24, TimeUnit.HOURS);
        // 15秒 抓一次市场信息
        schedule.scheduleAtFixedRate(this::getTicker, 5, 1, TimeUnit.SECONDS);

        // 每12小时更新一次所有K线
        for (String s : klineMap.keySet()) {
            klineScheduler.scheduleAtFixedRate(() -> getKline(s), 10, 10 * 60, TimeUnit.MINUTES);
        }
    }


    @Override
    protected void onStart() {

    }

    private void getKline(String type) {
        for (Pair pair : pairs) {
            try {
                String response = liqui.kline(pair.id, type
                        , Instant.now().minus(12, ChronoUnit.HOURS).getEpochSecond()
                        , Instant.now().getEpochSecond()).execute().body();
                K k = JSONObject.parseObject(response, K.class);
                int size = k.t.size();
                String[] symbol = pair.name.split("/");
                List<OnlyKey> save = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    KLine kline = new KLine(ExchangeConstant.Liqui, symbol[0], symbol[1]);
                    kline.setOpen(k.o.get(i));
                    kline.setClose(k.c.get(i));
                    kline.setHigh(k.h.get(i));
                    kline.setLow(k.l.get(i));
                    kline.setVolume(k.v.get(i));
                    kline.setTimestamp(k.t.get(i) * 1000);
                    kline.setMeasurement(klineMap.get(type));
                    save.add(kline);
                }
                influxDbMapper.postData(save);
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void getTradePairs() {
        try {
            String response = liqui.pairs().execute().body();
            pairs = JSON.parseArray(response, Pair.class);
            pairsList = pairs.stream()
                    .map(pair -> pair.name.replace("/", "_").toLowerCase())
                    .collect(Collectors.joining("-"));
            log.trace("lastest liqui info {}", response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    final static Type TYPE = new TypeReference<Map<String, Ticker>>() {
    }.getType();

    private void getTicker() {
        if (pairsList == null) {
            getTradePairs();
        }
        String response = "";
        try {
            response = liqui.ticker(pairsList).execute().body();
            Map<String, Ticker> tickers = JSON.parseObject(response, TYPE);
            List<OnlyKey> markets;
            if (tickers != null) {
                markets = tickers.entrySet().stream().map(this::mapper).collect(Collectors.toList());
                influxDbMapper.postData(markets);
            }
        } catch (Exception e) {
            log.error(response + ",{}", e.getMessage());
        }
    }

    private Market mapper(Map.Entry<String, Ticker> pair) {
        String[] symbol = pair.getKey().split("_");
        Market market = new Market(ExchangeConstant.Liqui, symbol[0], symbol[1]);
        Ticker p = pair.getValue();
        market.setLast(p.last);
        market.setHigh(p.high);
        market.setLow(p.low);
        market.setVolume(p.vol);
        market.setAmount(p.volCur);
        market.setBid(p.buy);
        market.setAsk(p.sell);
        market.setTimestamp(p.updated * 1000);
        log.debug("{} {}", ExchangeConstant.Liqui, market);
        return market;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Liqui;
    }

    @Data
    static class Pair {
        String id;
        String name;
    }


    @Data
    static class Ticker {
        BigDecimal high;
        BigDecimal low;
        BigDecimal avg;
        BigDecimal vol;
        BigDecimal volCur;
        BigDecimal last;
        BigDecimal buy;
        BigDecimal sell;
        Long updated;
    }

    @Data
    static class K {
        List<Long> t;
        List<BigDecimal> c;
        List<BigDecimal> o;
        List<BigDecimal> h;
        List<BigDecimal> l;
        List<BigDecimal> v;
    }
}
