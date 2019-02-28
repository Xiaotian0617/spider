package com.al.dbspider.base;

import com.al.dbspider.base.api.Binance;
import com.al.dbspider.config.BinanceConfig;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.binance")
public class BinanceExchange extends BaseRest {

    @Autowired
    private Binance binance;

    private ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * 交易所信息，以供Websocket订阅交易信息使用
     */
    private ExchangeInfo exchangeInfo = new ExchangeInfo();

    public ExchangeInfo getExchangeInfo() {
        return exchangeInfo;
    }

    private List<String> intervals = new ArrayList<String>() {{
        add("1m");
        add("1d");
    }};

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        try {
            if (exchangeInfo.symbols != null) {
                return;
            }
            ExchangeInfo exchangeInfo = allSymbols();
            if (exchangeInfo != null) {
                this.exchangeInfo = exchangeInfo;
                startKline(this.exchangeInfo.symbols, schedule);
                return;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.warn("no product symbol; retry after 3min");
        schedule.schedule(() -> onSchedule(schedule), 3, TimeUnit.MINUTES);
    }

    public ExchangeInfo allSymbols() {
        String body = BinanceConfig.binanceSymbols;
        try {
            Response<String> response = execute(binance.symbols());
            if (response != null||!StringUtils.isEmpty(response.body())) {
                body = response.body();
            }
            log.debug("p:{}", response);
        }catch (Throwable e){
            log.error("Binance 获取交易对出错，已启用默认交易对");
        }
        ExchangeInfo exchangeInfo = JSONObject.parseObject(body, ExchangeInfo.class);
        if (exchangeInfo == null) {
            return null;
        }
        Iterator<Binance.Symbol> symbolIterator = exchangeInfo.symbols.iterator();
        for (; symbolIterator.hasNext(); ) {
            Binance.Symbol symbol = symbolIterator.next();
            if (symbol.getStatus().equalsIgnoreCase("break")) {
                symbolIterator.remove();
            }
        }
        return exchangeInfo;
    }

    private void startKline(List<Binance.Symbol> data, ScheduledExecutorService schedule) {
        log.info("开始启动币安K线Rest抓取");
        int delay = 0;
        for (Binance.Symbol symbol : data) {
            schedule.scheduleAtFixedRate(() -> kline(symbol), delay++ % 20, 20, TimeUnit.SECONDS);
        }
    }

    private void kline(Binance.Symbol symbol) {

        String key = symbol.getSymbol();
        if (key == null) {
            return;
        }
        pool.submit(() -> {
            try {
                intervals.forEach(interval -> {
                    Response<String> response = null;
                    try {
                        response = binance.kline(interval, key).execute();
                    } catch (IOException e) {
                        log.error("Binance get kline net error !", e);
                    }
                    log.debug("{}", response);
                    String body = response.body();
                    if (body == null) {
                        return;
                    }
                    log.trace("Binance Kline info is {}", body);
                    List<OnlyKey> kline = JSONArray.parseArray(body)
                            .stream()
                            .map(o -> klineMapper(o, symbol, interval))
                            .collect(Collectors.toList());
                    influxDbMapper.postData(kline);
                });
            } catch (Exception e) {
                log.error(symbol.getSymbol(), e);
            }
        });
    }

    private KLine klineMapper(Object o, Binance.Symbol symbol, String interval) {
        JSONArray k = ((JSONArray) o);
        KLine kline = new KLine(ExchangeConstant.Binance, symbol.getBaseAsset(), symbol.getQuoteAsset());
        if (intervals.get(1).equals(interval)) {
            kline.setMeasurement("kline_1D");
        }
        kline.setTimestamp(k.getLong(0));
        kline.setOpen(k.getBigDecimal(1));
        kline.setHigh(k.getBigDecimal(2));
        kline.setLow(k.getBigDecimal(3));
        kline.setClose(k.getBigDecimal(4));
        kline.setVolume(k.getBigDecimal(5));
        log.debug("{}", kline);
        return kline;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Binance;
    }

    @Data
    public static class ExchangeInfo {
        List<Binance.Symbol> symbols;
    }



}
