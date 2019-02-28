package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.TradePair;
import com.al.dbspider.base.api.Fcoin;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://developer.fcoin.com/zh.html
 * <pre>market/ticker 格式[
 *   "最新成交价",
 *   "最近一笔成交的成交量",
 *   "最大买一价",
 *   "最大买一量",
 *   "最小卖一价",
 *   "最小卖一量",
 *   "24小时前成交价",
 *   "24小时内最高价",
 *   "24小时内最低价",
 *   "24小时内基准货币成交量, 如 btcusdt 中 btc 的量",
 *   "24小时内计价货币成交量, 如 btcusdt 中 usdt 的量"
 * ]</pre>
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-12
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.fcoin", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.fcoin")
public class FcoinWebsocket extends BaseWebsocket {
    public static AtomicInteger RID = new AtomicInteger(0);
    public static final String TICKER_TOPIC_TEMP = "ticker.%s";
    public static final String DEPTH_TOPIC_TEMP = "depth.%s.%s";//level symbol
    public static final String TRADE_TOPIC_TEMP = "trade.%s";
    public static final String KLINE_TOPIC_TEMP = "candle.%s.%s";//resolution symbol
    public static final String SUB_TEMP = "{\"cmd\":\"sub\",\"args\":[%s],\"id\":\"%s\"}";
    public static final HashMap<String, String> TRADE_TOPICS = new HashMap<>();
    public static final HashMap<String, String> MARKET_TOPICS = new HashMap<>();
    public static final HashMap<String, String> KLINE_TOPICS = new HashMap<>();
    public static final HashMap<String, String> DEPTH_TOPICS = new HashMap<>();
    //SYMBOL TRADEPAIR
    private Map<String, TradePair> allTradePair = new HashMap<>(200);
    private Map<String, TradePair> topicTradePair = new HashMap<>(2000);

    @Autowired
    Fcoin fcoin;

    private void subTrades(String topics) {
        if (trade) {
            log.info("Fcoin subscribe trade {}", topics);
            send(String.format(SUB_TEMP, topics, RID.incrementAndGet()));
        }
    }

    private void subTicker(String topics) {
        if (market) {
            send(String.format(SUB_TEMP, topics, RID.incrementAndGet()));
            log.debug("Fcoin subscribe market {}", topics);
        }
    }

    private void subKline(String topics) {
        if (kline) {
            send(String.format(SUB_TEMP, topics, RID.incrementAndGet()));
            log.debug("Fcoin subscribe kline {} ", topics);
        }
    }

    private void subDepth(String topics) {
        if (depth) {
            send(String.format(SUB_TEMP, topics, RID.incrementAndGet()));
            log.debug("Fcoin subscribe depth {}", topics);
        }
    }

    @Override
    public void subscribe() {
        try {
            Response<String> execute = fcoin.symbols().execute();
            if (execute.isSuccessful()) {
                String body = execute.body();
                log.info("Fcoin symbols {}", body);
                JSONObject jsonObject = JSON.parseObject(body);
                JSONArray datas = jsonObject.getJSONArray("data");
                if (datas != null && datas.size() > 0) {
                    for (int i = 0; i < datas.size(); i++) {
                        JSONObject data = datas.getJSONObject(i);
                        TradePair tradePair = new TradePair(data.getString("base_currency"), data.getString("quote_currency"), "_");
                        tradePair.setPriceDecimal(data.getInteger("price_decimal"));
                        tradePair.setVolumeDecimal(data.getInteger("amount_decimal"));
                        allTradePair.put(tradePair.getNoSeparatorTradePair(), tradePair);
                    }
                }
            }
        } catch (IOException e) {
            log.error("fcoin 获取symbols error ", e);
        }

        if (allTradePair.size() > 0) {
            allTradePair.forEach((key, value) -> {
                String tradeTopic = String.format(TRADE_TOPIC_TEMP, key);
                TRADE_TOPICS.put(tradeTopic, StringUtils.wrap(tradeTopic, "\""));
                topicTradePair.put(tradeTopic, value);
                String tickerTopic = String.format(TICKER_TOPIC_TEMP, key);
                MARKET_TOPICS.put(tickerTopic, StringUtils.wrap(tickerTopic, "\""));
                topicTradePair.put(tickerTopic, value);
                for (Fcoin.Resolution resolution : Fcoin.Resolution.values()) {
                    String klineTopic = String.format(KLINE_TOPIC_TEMP, resolution, key);
                    KLINE_TOPICS.put(klineTopic, StringUtils.wrap(klineTopic, "\""));
                    topicTradePair.put(klineTopic, value);
                }
                String depthTopic = String.format(DEPTH_TOPIC_TEMP, Fcoin.Level.L20, key);
                DEPTH_TOPICS.put(depthTopic, StringUtils.wrap(depthTopic, "\""));
                topicTradePair.put(depthTopic, value);
            });
            subTrades(StringUtils.join(TRADE_TOPICS.values(), ","));
            subTicker(StringUtils.join(MARKET_TOPICS.values(), ","));
            subKline(StringUtils.join(KLINE_TOPICS.values(), ","));
            subDepth(StringUtils.join(DEPTH_TOPICS.values(), ","));
        }
    }

    @Override
    public List<OnlyKey> onMessageInPool(String msg) {
        OnlyKeyMessage<OnlyKey> objects = new OnlyKeyMessage<>();
        JSONObject msgJO = JSON.parseObject(msg);
        String type = msgJO.getString("type");
        TradePair tradePair = topicTradePair.get(type);
        log.debug("Fcoin type {} {}", type, msg);
        if (TRADE_TOPICS.containsKey(type)) {
            log.debug("Fcoin trade origin {}", msgJO);
            Trade trade = new Trade(ExchangeConstant.Fcoin, tradePair.getSymbol(), tradePair.getUnit());
            trade.setTradeId(msgJO.getString("id"));
            String cachedId = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
            String isOK = cacheTid(cachedId, trade.getTradeId());
            if (isOK == null) {
                log.debug("{} exist", cachedId);
                return null;
            }
            trade.setTimestamp(millToNano(msgJO.getLong("ts")));
            trade.setVolume(msgJO.getBigDecimal("amount"));
            trade.setPrice(msgJO.getBigDecimal("price"));
            trade.setSide(msgJO.getString("side"));
            objects.add(trade);
            log.debug("Fcoin trade own {}", trade);
            return objects;
        }
        if (MARKET_TOPICS.containsKey(type)) {
            log.debug("Fcoin market origin {}", msgJO);
            JSONArray ticker = msgJO.getJSONArray("ticker");
            Market market = new Market(ExchangeConstant.Fcoin, StringUtils.remove(tradePair.getSymbol(), "_"), StringUtils.remove(tradePair.getUnit(), "_"));
            market.setLast(ticker.getBigDecimal(0));
            market.setBid(ticker.getBigDecimal(2));
            market.setAsk(ticker.getBigDecimal(4));
            market.setHigh(ticker.getBigDecimal(7));
            market.setLow(ticker.getBigDecimal(8));
            market.setVolume(ticker.getBigDecimal(9));
            market.setTimestamp(System.currentTimeMillis());
            objects.add(market);
            log.debug("Fcoin market own {}", market);
            return objects;
        }
        if (KLINE_TOPICS.containsKey(type)) {
            log.debug("Fcoin kline origin {}", msgJO);
            KLine kLine = new KLine(ExchangeConstant.Fcoin, tradePair.getSymbol(), tradePair.getUnit());
            kLine.setId(msgJO.getString("seq"));
            kLine.setOpen(msgJO.getBigDecimal("open"));
            kLine.setClose(msgJO.getBigDecimal("close"));
            kLine.setHigh(msgJO.getBigDecimal("high"));
            kLine.setLow(msgJO.getBigDecimal("low"));
            kLine.setVolume(msgJO.getBigDecimal("base_vol"));
            kLine.setTimestamp(msgJO.getLong("id") * 1000);
            if (msgJO.getString("type").contains(Fcoin.Resolution.D1.name())) {
                kLine.setMeasurement("kline_1D");
            }
            objects.add(kLine);
            log.debug("Fcoin kline own {}", kLine);
            return objects;
        }
        if (DEPTH_TOPICS.containsKey(type)) {
            return null;
        }
        return null;

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Fcoin;
    }
}
