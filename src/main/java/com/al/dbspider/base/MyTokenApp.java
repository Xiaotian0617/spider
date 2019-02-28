package com.al.dbspider.base;

import com.al.dbspider.base.api.MyToken;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 需要http2支持
 * https://www.eclipse.org/jetty/documentation/9.3.x/alpn-chapter.html
 * <p>
 * 启动需要额外的jar包 java -Xbootclasspath/p:<path_to_alpn_boot_jar> ...
 * https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/alpn-boot
 * 下载要与本机的版本对应
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.mytoken")
public class MyTokenApp extends BaseRest {

    private ScheduledExecutorService scheduler;

    private ExecutorService pool = Executors.newCachedThreadPool();

    @Autowired
    private InfluxDbMapper influxDbMapper;
    @Autowired
    private MyToken myToken;

    private static Map<String, ExchangeConstant> exchangeNames = new HashMap<>();  // key为mytoken的market_id, value为自己的exchange
//    private static Map<String, List<String>> exchangePairs = new HashMap<>();  // key为mytoken的market_id

    private AtomicInteger counter = new AtomicInteger();

    private void initExchangeInfo() {
        try {
            Response<String> response = myToken.marketList().execute();
            if (response.raw().protocol() != Protocol.HTTP_2) {
                log.error("不支持http2, mytoken不启动");
                return;
            }
            Result result = JSONObject.parseObject(response.body(), Result.class);
            if (result.code == 0) {
                result.data.list.stream()
                        .filter(this::need)
                        .forEach(exchange -> pool.submit(() -> fetchPair(exchange, 1)));
                return;
            }
            log.warn("get mytoken market list error code: {}", result.code);
        } catch (Exception e) {
            log.error("no exchange list ", e);
        }
        //没抓到市场信息3分钟后再请求一次
        scheduler.schedule(this::initExchangeInfo, 3, TimeUnit.MINUTES);
    }

    //获取交易对
    private void fetchPair(Exchange exchange, int page) {
        try {
            Response<String> response = myToken.pair(exchange.marketId, page).execute();
            log.debug("http protocol: {}", response.raw().protocol());
            if (!response.isSuccessful()) {
                return;
            }
            //log.trace("response: ", response.body());
            Result result = JSONObject.parseObject(response.body(), Result.class);
            if (result.data.list == null || result.data.list.size() == 0) {
                return;  //可能翻到最后一页了
            }
            startKline(result.data.list.stream()
                    .filter(Exchange::isKlineEnabled).collect(Collectors.toList()));

            log.debug("pairs size:{}", counter.getAndAdd(result.data.list.size()));
        } catch (Exception e) {
            log.error("fetch pair error ", e);
        }
        //翻页
        fetchPair(exchange, ++page);
    }

    private void startKline(List<Exchange> collect) {
        log.debug("启动定时任务");
        int delay = 0;
        for (Exchange exchange : collect) {
            scheduler.scheduleAtFixedRate(() -> kline(exchange), delay % 60, 60, TimeUnit.SECONDS);
            delay += 3;
        }
    }

    private void kline(Exchange e) {
        pool.submit(() -> {
            try {
                String response = myToken.kline(e.marketId, e.symbol, e.anchor).execute().body();
                if (response == null) {
                    return;
                }
                log.debug("response:{}", response);
                Result result = JSONObject.parseObject(response, Result.class);
                if (result == null || result.code != 0 || result.data == null) {
                    return;
                }

                if (result.data.kline.size() == 0) {
                    return;
                }
                ExchangeConstant exchange = exchangeNames.get(e.marketId);
                List<OnlyKey> klines = result.data.kline.stream().map(k -> mapperKline(k, exchange, e.symbol, e.anchor)).collect(Collectors.toList());
                log.debug("wirte point:{} {}", exchange);
                influxDbMapper.postData(klines);
            } catch (Exception ex) {
                log.error("kline error {}", ex);
            }
        });
    }

    private KLine mapperKline(KlineMapper k, ExchangeConstant exchange, String symbol, String anchor) {
        KLine kline = new KLine(exchange, symbol, anchor);
        kline.setType("Mytoken");
        kline.setHigh(k.high);
        kline.setOpen(k.open);
        kline.setClose(k.close);
        kline.setLow(k.low);
        kline.setVolume(k.volumefrom);
        kline.setTimestamp(k.time * 1000);
        return kline;
    }

    private boolean need(Exchange exchange) {
        String name = exchange.name;
        ExchangeConstant exchangeEnum = null;
        if (name.contains("OKEx")) {
            exchangeEnum = ExchangeConstant.Okex;
        } else if (name.contains("Huobi.pro")) {
            exchangeEnum = ExchangeConstant.Huobi;
        } else if (name.contains("Gate.io")) {
            exchangeEnum = ExchangeConstant.Gate;
        } else if (name.contains("Bit-Z")) {
            exchangeEnum = ExchangeConstant.Bitz;
        }/* else if (name.contains("U-COIN")) {
            exchangeEnum = ExchangeConstant.Ucoin;
        }*/

        if (exchangeEnum != null) {
            exchangeNames.put(exchange.marketId, exchangeEnum);
            return true;
        }

        String firstLetter = exchange.name.substring(0, 1).toUpperCase();
        exchange.name = firstLetter + exchange.name.toLowerCase().substring(1);

        try {
            exchangeNames.put(exchange.marketId, ExchangeConstant.valueOf(exchange.name));
        } catch (Exception e) {
            // 枚举中没有的交易所暂时不要
            log.error("no exchange {}", exchange.name);
            return false;
        }
        return true;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        this.scheduler = schedule;
        schedule.scheduleAtFixedRate(() -> log.info("task count: {}", ((ScheduledThreadPoolExecutor) scheduler).getQueue().size()), 0, 10, TimeUnit.SECONDS);
        initExchangeInfo();
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.MyToken;
    }

    @Data
    static class Result {
        int code;
        String message;
        DataList data;
        Long timestamp;
    }

    @Data
    static class DataList {
        List<Exchange> list;
        List<KlineMapper> kline;
    }

    @Data
    static class KlineMapper {
        BigDecimal close;
        BigDecimal high;
        BigDecimal low;
        BigDecimal open;
        long time;
        BigDecimal volumefrom;
    }

    @Data
    static class Exchange {
        String marketId;
        String name;

        //pair
        boolean klineEnabled;
        String symbol;
        String anchor;
        String pair;
    }
}
