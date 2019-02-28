package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.binance", name = "kline", havingValue = "true", matchIfMissing = true)
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConfigurationProperties("websocket.binance")
public class BinanceKline1mWebsocket extends BinanceKlineWebsocket {

    //连接上即订阅
    @PostConstruct
    public void buildUrl() {
        log.info("初始化 {}", getClass().getSimpleName());
        StringBuilder sb = new StringBuilder(url + "stream?streams=");
        symbolsMap.values().forEach(symbol -> {
            String kline1m = String.format(KLINE_STREAM_NAME_TEMP, symbol.getSymbol().toLowerCase(), "1m");
            sb.append(kline1m).append("/");
        });
        url = sb.toString();
    }

    @Override
    protected String getKlineMeasurement() {
        return "kline";
    }

    @Override
    public void subscribe() {

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Binance;
    }
}
