package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.GateExchange;
import com.al.dbspider.base.TradePair;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-12
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.Gate", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.Gate")
public class GateWebsocket extends BaseWebsocket {
    public static AtomicInteger RID = new AtomicInteger(0);
    public static final String REQUEST_TEMP = "{\"id\": %s,\"method\": \"%s\",\"params\":[%s]}";
    public static final String METHOD_MARKETS = "ticker.subscribe";
    public static final String METHOD_TRADES = "trades.subscribe";
    public static final String METHOD_DEPTH = "depth.subscribe";
    public static final String METHOD_KLINE = "kline.subscribe";
    //symbol_unit tradepair
    private Map<String, TradePair> allTradePair = new HashMap<>();
    public static final Set<Integer> KLINE_INTERVALS = Sets.newHashSet(60, 86400);

    @Autowired
    GateExchange GateExchange;

    private void subTrades(String tradePairs) {
        if (trade) {
            log.info("Gate subscribe trades {}", tradePairs);
            String format = String.format(REQUEST_TEMP, RID.incrementAndGet(), METHOD_TRADES, tradePairs);
            send(format);
        }
    }

    private void subTickers(String tradePairs) {
        if (market) {
            send(String.format(REQUEST_TEMP, RID.incrementAndGet(), METHOD_MARKETS, tradePairs));
            log.info("Gate subscribe tickers {}", tradePairs);
        }
    }

    //一个ws 连接只能订阅一个 tradepair
    private void subKline(String tradePairs) {
        kline = false;
//        if (kline) {
//            send(String.format(REQUEST_TEMP, RID.incrementAndGet(), METHOD_KLINE, tradePairs));
//            log.info("Gate subscribe kline {}", tradePairs);
//        }
    }

    private void subDepth(String tradePairs) {
        if (depth) {
            send(String.format(REQUEST_TEMP, RID.incrementAndGet(), METHOD_DEPTH, tradePairs));
            log.info("Gate subscribe depth {}", tradePairs);
        }
    }

    @Override
    public void subscribe() {
        allTradePair.clear();
        allTradePair.putAll(GateExchange.allPairs());
        Iterator<String> tradePairs = allTradePair.keySet().iterator();
        StringBuilder ticker = new StringBuilder();
        while (tradePairs.hasNext()) {
            String next = tradePairs.next();
            String wrapedTradePair = StringUtils.wrap(next, "\"");
            ticker.append(wrapedTradePair);
//            subKline(wrapedTradePair + "," + klineInterval);
//            for (Integer klineInterval : KLINE_INTERVALS) {
//                break;
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    log.error("Gate 订阅 kline 出错 {} ", wrapedTradePair);
//                }
//            }
            if (!tradePairs.hasNext()) {
                break;
            }
            ticker.append(",");
        }
        String s = ticker.toString();
        subTickers(s);
        subTrades(s);
        subKline("\"BTC_USDT\",60");
    }

    @Override
    public void ping() {
        send(String.format(REQUEST_TEMP, RID.incrementAndGet(), "server.ping", ""));
    }

    @Override
    protected boolean isNeedPing() {
        return true;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Gate;
    }

    @Override
    public List<OnlyKey> onMessageInPool(String msg) {
        JSONObject msgJson = JSON.parseObject(msg);
        JSONObject error = msgJson.getJSONObject("error");
        if (error != null) {
            log.error("Gate 订阅错误 {}", msgJson);
            return null;
        }
        ArrayList<OnlyKey> onlyKeys = new ArrayList<>();
        String method = msgJson.getString("method");
        if ("trades.update".equalsIgnoreCase(method)) {
            log.debug("Gate trade origin {}", msgJson);
            JSONArray params = msgJson.getJSONArray("params");
            String tradePairString = params.getString(0);
            TradePair tradePair = allTradePair.get(tradePairString);
            if (tradePair == null) {
                log.error("Gate tradePair 未知 {}", tradePairString);
                return null;
            }
            JSONArray datas = params.getJSONArray(1);
            for (Object obj : datas) {
                JSONObject data = (JSONObject) obj;
                Trade trade = new Trade(ExchangeConstant.Gate, tradePair.getSymbol(), tradePair.getUnit());
                trade.setTradeId(data.getString("id"));
                String cachedid = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                String isOK = cacheTid(cachedid, trade.getTradeId());
                if (isOK == null) {
                    log.debug("{} exist", cachedid);
                    continue;
                }
                trade.setTimestamp(millToNano(Long.valueOf(StringUtils.substringBefore(data.getString("time"), ".") + "000")));
                trade.setVolume(data.getBigDecimal("amount"));
                trade.setPrice(data.getBigDecimal("price"));
                trade.setSide(data.getString("type"));
                log.debug("Gate trade own {}", trade);
                onlyKeys.add(trade);
            }
            return onlyKeys;
        }
        if ("ticker.update".equalsIgnoreCase(method)) {
            log.debug("Gate ticker origin {}", msgJson);
            JSONArray params = msgJson.getJSONArray("params");
            String tradePairString = params.getString(0);
            TradePair tradePair = allTradePair.get(tradePairString);
            if (tradePair == null) {
                log.error("Gate tradePair 未知 {}", tradePairString);
                return null;
            }
            JSONObject ticker = params.getJSONObject(1);
            Market market = new Market(ExchangeConstant.Gate, tradePair.getSymbol(), tradePair.getUnit());
            market.setLast(ticker.getBigDecimal("last"));
            market.setHigh(ticker.getBigDecimal("high"));
            market.setLow(ticker.getBigDecimal("low"));
            market.setVolume(ticker.getBigDecimal("baseVolume"));
            market.setClose(ticker.getBigDecimal("close"));
            market.setOpen(ticker.getBigDecimal("open"));
            market.setTimestamp(System.currentTimeMillis());
            onlyKeys.add(market);
            log.debug("Gate ticker own {}", market);
            return onlyKeys;
        }
        if ("kline.update".equalsIgnoreCase(method)) {
            log.debug("Gate kline {}", msgJson);
        }
        return null;
    }

}
