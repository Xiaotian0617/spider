package com.al.dbspider.websocket;

import com.al.dbspider.base.BinanceExchange;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.api.Binance;
import com.al.dbspider.dao.domain.DepthDTO;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 只订阅trade 和 market
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-07-28
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.binance", name = "depth", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties("websocket.binance")
public class BinanceDepthWebsocket extends BaseWebsocket {

    public static final String AGGTRADE_STREAM_NAME_TEMP = "%s@depth20";

    public static final Map<String, Binance.Symbol> symbolsMap = new HashMap<>(400);

    @Autowired
    BinanceExchange binanceExchange;

    @Autowired
    InfluxDbMapper influxDbMapper;

    //连接上即订阅
    @PostConstruct
    public void buildUrl() {
        log.info("初始化 {} 交易对", getClass().getSimpleName());
        BinanceExchange.ExchangeInfo exchangeInfo = binanceExchange.allSymbols();
        if (exchangeInfo == null) {
            url = null;
            return;
        }
        List<Binance.Symbol> symbols = exchangeInfo.getSymbols();
        symbols.forEach(symbol -> {
            symbolsMap.put(symbol.getSymbol(), symbol);
        });
        StringBuilder sb = new StringBuilder(url + "stream?streams=");
        symbolsMap.values().forEach(symbol -> {
            String depth = String.format(AGGTRADE_STREAM_NAME_TEMP, symbol.getSymbol().toLowerCase());
            sb.append(depth).append("/");
        });
        url = sb.toString();
    }

    private List<DepthDTO> getJsonArrayByJob(JSONObject jsonObject) {
        JSONArray result = new JSONArray();
        String stream = jsonObject.getString("stream").replace("@depth20", "").toUpperCase();
        Binance.Symbol symbol = symbolsMap.get(stream);
        JSONObject tick = new JSONObject();
        List<DepthDTO.PriceLevel> asks = jsonObject.getJSONObject("data").getJSONArray("asks").stream().map(ask -> getPriceLevel(ask, 0, 1)).collect(Collectors.toList());
        List<DepthDTO.PriceLevel> bids = jsonObject.getJSONObject("data").getJSONArray("bids").stream().map(bid -> getPriceLevel(bid, 0, 1)).collect(Collectors.toList());
        jsonObject.put("onlyKey", "Binance_" + symbol.getBaseAsset() + "_" + symbol.getQuoteAsset());
        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("type", 0);
        tick.put("asks", asks);
        tick.put("bids", bids);
        jsonObject.put("tick", tick);
        result.add(jsonObject);
        return result.toJavaList(DepthDTO.class);
    }

    @Override
    public void subscribe() {
        //nothing 没有订阅事件
    }

    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        log.trace("Binance Depth:{}", s);
        if (!StringUtils.hasText(s)) {
            log.debug("Binance Depth is Empty,continues.");
            return new ArrayList<>();
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(s);
            List<DepthDTO> depthDTOS = getJsonArrayByJob(jsonObject);
            if (depthDTOS.size() == 0) {
                return new ArrayList<>();
            }
            List<OnlyKey> onlyKeys = new ArrayList<>();
            depthDTOS.stream().forEach(depthDTO -> onlyKeys.add(depthDTO));
            getBuyAndSellFirst(depthDTOS, 0, 0);
            log.debug("After analysis Binance depth data:{}", JSONObject.toJSONString(onlyKeys));
            return onlyKeys;
        } catch (Throwable e) {
            log.error("Analysis the Binance Depth error, data is :" + s, e);
        }
        return new ArrayList<>();
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Binance;
    }


    /**
     * {
     * "stream": "icxbtc@depth",
     * "data": {
     * <p>
     * }
     * }
     */
    @Data
    static class BinanceDepthModel {
        private String stream;
        @JSONField(name = "data")
        private static BinanceDepthAllData binanceDepthData;
    }


    /**
     * {
     * "lastUpdateId": 160,  // Last update ID
     * "bids": [             // Bids to be updated
     * [
     * "0.0024",         // Price level to be updated
     * "10",             // Quantity
     * []                // Ignore
     * ]
     * ],
     * "asks": [             // Asks to be updated
     * [
     * "0.0026",         // Price level to be updated
     * "100",            // Quantity
     * []                // Ignore
     * ]
     * ]
     * }
     */
    @Data
    public class BinanceDepthAllData {
        private Long lastUpdateId;
        @JSONField(name = "bids")
        private List<List> bids;
        @JSONField(name = "asks")
        private List<List> asks;
    }

    /**
     * {
     * "e": "depthUpdate", // Event type
     * "E": 123456789,     // Event time
     * "s": "BNBBTC",      // Symbol
     * "U": 157,           // First update ID in event
     * "u": 160,           // Final update ID in event
     * "b": [              // Bids to be updated
     * [
     * "0.0024",       // Price level to be updated
     * "10",
     * []              // Ignore
     * ]
     * ],
     * "a": [              // Asks to be updated
     * [
     * "0.0026",       // Price level to be updated
     * "100",          // Quantity
     * []              // Ignore
     * ]
     * ]
     * }
     */
    @Data
    class BinanceDepthUpdateData {
        @JSONField(name = "e")
        private String eventType;
        @JSONField(name = "E")
        private String eventTime;
        @JSONField(name = "s")
        private String symbol;
        @JSONField(name = "U")
        private String firstUpdateId;
        @JSONField(name = "u")
        private String FinalUpdateId;
        @JSONField(name = "b")
        private List<List> bids;
        @JSONField(name = "a")
        private List<List> asks;
    }

}
