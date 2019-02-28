package com.al.dbspider.base;

import com.al.dbspider.base.api.Huobi;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 火币网 Rest API 请求
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-12
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.huobi")
public class HuobiExchange extends BaseRest {
    public static final Map<String, Symbol> TRADE_PAIRS = Maps.newHashMap();

    @Autowired
    private Huobi huobi;

    @Override
    protected void init() {

    }

    public void requestAllSymbols() {
        try {
            String body = huobi.symbols(Huobi.CONTENTTYPE, Huobi.USERAGENT).execute().body();
            SymbolResponse symbol = JSON.parseObject(body, SymbolResponse.class);
            List<Symbol> symbols = symbol.getData();
            if (symbols == null) {
                log.error("没有获取到火币货币信息");
            } else {
                TRADE_PAIRS.clear();
                log.info("清空火币货币信息");
                symbols.forEach(o -> TRADE_PAIRS.put(o.getTradePair(), o));
                log.info("获取火币货币信息,{}", TRADE_PAIRS);
            }
        } catch (IOException e) {
            log.error("抓取火币全部币种信息出错", e);
        }

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(this::requestAllSymbols, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    protected void onStart() {

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Huobi;
    }

    @Data
    public static class SymbolResponse {
        private String status;
        private List<Symbol> data;
    }

    @Data
    public static class Symbol {
        private String baseCurrency;
        private String quoteCurrency;
        private String pricePrecision;
        private String amountPrecision;
        private String symbolPartition;
        private String tradePair;

        public String getTradePair() {
            return tradePair == null ? baseCurrency + quoteCurrency : tradePair;
        }
    }

}
