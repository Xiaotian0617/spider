package com.al.dbspider.websocket;

import com.al.dbspider.base.BinanceExchange;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.api.Binance;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.al.dbspider.utils.InfluxDbMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@ConditionalOnProperty(prefix = "websocket.binance", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.binance")
public class BinanceTradeWebsocket extends BaseWebsocket {
    public static final String AGGTRADE_STREAM_NAME_TEMP = "%s@aggTrade";
    public static final String ALLMARKET_STREAM_NAME = "!miniTicker@arr";
    public static final Map<String, Binance.Symbol> symbolsMap = new HashMap<>(400);
    @Autowired
    BinanceExchange binanceExchange;

    @Autowired(required = false)
    BinanceDepthWebsocket binanceDepthWebsocket;

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
        StringBuilder sb = new StringBuilder(url + "stream?streams=");
        symbols.forEach(symbol -> {
            symbolsMap.put(symbol.getSymbol(), symbol);
            String trade = String.format(AGGTRADE_STREAM_NAME_TEMP, symbol.getSymbol().toLowerCase());
            sb.append(trade).append("/");
        });
        if (market) {
            sb.append(ALLMARKET_STREAM_NAME);
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        url = sb.toString();
    }

    @Override
    public void subscribe() {
        //nothing 没有订阅事件
    }

    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        ArrayList<OnlyKey> onlyKeys = Lists.newArrayList();
        ArrayList<Trade> trades = Lists.newArrayList();
        try {
            JSONObject jsonObject = JSON.parseObject(s);
            Object dataObject = jsonObject.get("data");
            if (dataObject instanceof JSONObject) {
                JSONObject data = (JSONObject) dataObject;
                String type = data.getString("e");
                String symbolInMsg = data.getString("s");
                Binance.Symbol symbol = symbolsMap.get(symbolInMsg);
                if ("aggTrade".equals(type)) {
                    //交易数据
                    if (symbol != null) {
                        String binancetid = data.getString("a");
                        Trade trade = new Trade(ExchangeConstant.Binance, symbol.getBaseAsset(), symbol.getQuoteAsset());
                        String cachedid = String.format("%s_%s", trade.getOnlyKey(), binancetid);
                        String isok = cacheTid(cachedid, cachedid);
                        if (isok == null) {
                            log.debug("{} exist", cachedid);
                            return null;
                        }
                        trade.setPrice(data.getBigDecimal("p"));
                        trade.setTimestamp(millToNano(data.getLong("T")));
                        trade.setVolume(data.getBigDecimal("q"));
                        boolean side = data.getBooleanValue("m");
                        trade.setSide(side ? "buy" : "sell");
                        trade.setTradeId(binancetid);
                        onlyKeys.add(trade);
                        trades.add(trade);
                        log.debug("Binance aggtrade {}", trade);
                    }
                }
            } else {
                JSONArray jsonArray = (JSONArray) dataObject;
                for (Object obj : jsonArray) {
                    JSONObject json = (JSONObject) obj;
                    BigDecimal colse = json.getBigDecimal("c");
                    BigDecimal vol = json.getBigDecimal("q");
                    BigDecimal open = json.getBigDecimal("o");
                    BigDecimal high = json.getBigDecimal("h");
                    BigDecimal low = json.getBigDecimal("l");
                    BigDecimal last = json.getBigDecimal("c");
                    Long time = json.getLong("E");
                    String symbolInMsg = json.getString("s");
                    Binance.Symbol symbol = symbolsMap.get(symbolInMsg);
                    if (symbol != null) {
                        Market market = new Market(ExchangeConstant.Binance, symbol.getBaseAsset(), symbol.getQuoteAsset(), last, vol, time);
                        market.setHigh(high);
                        market.setLow(low);
                        market.setOpen(open);
                        market.setClose(colse);
                        onlyKeys.add(market);
                        log.debug("Binance market {}", market);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("解析币安返回数据出错！", e);
        }
        if (binanceDepthWebsocket != null) {
            realTradeList(trades, ExchangeConstant.Binance, binanceDepthWebsocket.getHundredBuyAndSellFirstMap());
        }
        log.trace("Binance return info : {}", JSON.toJSONString(onlyKeys));
        log.trace("本次币安发送条数为{}", onlyKeys.size());
        return onlyKeys;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Binance;
    }

}
