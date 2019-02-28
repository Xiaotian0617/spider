package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.TradePair;
import com.al.dbspider.base.ZbExchange;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <a href="https://www.zb.com/i/developer/">ZB网</a> WebsocketAPI 请求<br>
 * 每个 1000次/1m/ip 10次/1s/user<br>
 * 1000	Success<br>
 * 1001	Error Tips<br>
 * 1002	Internal Error<br>
 * 1003	Validate No Pass<br>
 * 1004	Transaction Password Locked<br>
 * 1005	Transaction Password Error<br>
 * 1006	Real-name verification is pending approval or not approval<br>
 * 1007	Channel is empty<br>
 * 1008	Event is empty<br>
 * 1009	This interface is in maintaining<br>
 * 2001	Insufficient CNY Balance<br>
 * 2002	Insufficient BTC Balance<br>
 * 2003	Insufficient LTC Balance<br><br>
 * 2005	Insufficient ETH Balance<br>
 * 2006	Insufficient ETC Balance<br>
 * 2007	Insufficient BTS Balance<br>
 * 2008	Insufficient EOS Balance<br>
 * 2009	账户余额不足<br>
 * 3001	Not Found Order<br>
 * 3002	Invalid Money<br>
 * 3003	Invalid Amount<br>
 * 3004	No Such User<br>
 * 3005	Invalid Parameters<br>
 * 3006	Invalid IP or Differ From the Bound IP<br>
 * 3007	Invalid Request Time<br>
 * 3008	Not Found Transaction Record<br>
 * 4001	API Interface is locked or not enabled<br>
 * 4002	Request Too Frequently<br>
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-12
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.zb", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.zb")
public class ZbWebsocket extends BaseWebsocket {
    public static final String EVENT_TEMP = "{'event': '%s','channel': '%s'}";
    public static final String EVENT_ADDCHANNEL = "addChannel";
    public static final String CHANNEL_MARKETS = "markets";
    public static final String CHANNEL_TICKER = "ticker";
    public static final String CHANNEL_TRADES = "trades";
    public static final String CHANNEL_KLINE = "kline";
    public static final String CHANNEL_DEPTH = "depth";
    @Autowired
    ZbExchange zbExchange;
    //tradepair tradepair
    private Map<String, TradePair> allTradePair = new HashMap<>();
    //channel tradepair
    private Map<String, TradePair> channelTradePair = new HashMap<>();

    /**
     * 订阅市场配置信息后订阅其他
     *
     * @param tradePair
     */
    private void subTrade(TradePair tradePair) {
        if (trade) {
            log.info("Zb subscribe trade {}", tradePair);
            webSocket.send(String.format(EVENT_TEMP, EVENT_ADDCHANNEL, tradePair.getSymbol() + tradePair.getUnit() + "_" + CHANNEL_TRADES));
        }
    }

    private void subMarkets() {
        webSocket.send(String.format(EVENT_TEMP, EVENT_ADDCHANNEL, CHANNEL_MARKETS));
        log.info("订阅获取 zb websocket 市场格式");
    }

    private void subTikcer(TradePair tradePair) {
        if (market) {
            log.info("Zb subscribe tikcer {}", tradePair);
            webSocket.send(String.format(EVENT_TEMP, EVENT_ADDCHANNEL, tradePair.getSymbol() + tradePair.getUnit() + "_" + CHANNEL_TICKER));
        }
    }

    private void subKline(TradePair tradePair) {
        if (kline) {
            log.info("Zb subscribe kline {}", tradePair);
            webSocket.send(String.format(EVENT_TEMP, EVENT_ADDCHANNEL, tradePair.getSymbol() + tradePair.getUnit() + "_" + CHANNEL_KLINE));
        }
    }

    private void subDepth(TradePair tradePair) {
        if (depth) {
            log.info("Zb subscribe depth {}", tradePair);
            webSocket.send(String.format(EVENT_TEMP, EVENT_ADDCHANNEL, tradePair.getSymbol() + tradePair.getUnit() + "_" + CHANNEL_DEPTH));
        }
    }

    @Override
    public void subscribe() {
        subMarkets();
    }

    @Override
    public List<OnlyKey> onMessageInPool(String msg) {
        JSONObject msgJson = JSON.parseObject(msg);
        ArrayList<OnlyKey> objects = new ArrayList<>();
        String channel = msgJson.getString("channel");
        if (StringUtils.isBlank(channel)) {
            log.error("zb 响应信息频道为 null ,msg {}", msgJson);
            return null;
        }
        if (CHANNEL_MARKETS.equalsIgnoreCase(channel)) {
            log.debug("Zb markets {}", msgJson);
            JSONObject data = msgJson.getJSONObject("data");
            Set<String> strings = data.keySet();
            strings.forEach(o -> {
                TradePair tradePair = new TradePair(o, "_");
                allTradePair.put(tradePair.getNoSeparatorTradePair(), tradePair);
                channelTradePair.put(tradePair.getNoSeparatorTradePair() + "_trades", tradePair);
                channelTradePair.put(tradePair.getNoSeparatorTradePair() + "_ticker", tradePair);
            });
            allTradePair.values().forEach(tradePair -> {
                subTikcer(tradePair);
                subTrade(tradePair);
                //todo 未完成 zb depth kline
//                subDepth(tradePair);
//                subKline(tradePair);
            });
            return null;
        }
        TradePair tradePair = channelTradePair.get(channel);
        if (tradePair == null) {
            log.error("zb tradepair 未知 {}", channel);
            return null;
        }
        if (StringUtils.endsWithIgnoreCase(channel, CHANNEL_TRADES)) {
            log.debug("Zb trade origin{}", msgJson);
            JSONArray datas = msgJson.getJSONArray("data");
            for (Object obj : datas) {
                JSONObject data = (JSONObject) obj;
                Trade trade = new Trade(ExchangeConstant.Zb, tradePair.getSymbol(), tradePair.getUnit());
                trade.setTradeId(data.getString("tid"));
                String cachedid = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                trade.setTimestamp(millToNano(data.getLong("date") * 1000));
                String isOK = cacheTid(cachedid, trade.getTimestamp().toString());
                if (isOK == null) {
                    log.debug("{} exist", cachedid);
                    continue;
                }
                trade.setVolume(data.getBigDecimal("amount"));
                trade.setPrice(data.getBigDecimal("price"));
                trade.setSide(data.getString("type"));
                objects.add(trade);
                log.debug("Zb trade own {}", trade);
            }
            return objects;
        }
        if (StringUtils.endsWithIgnoreCase(channel, CHANNEL_TICKER)) {
            log.debug("Zb ticker origin {}", msgJson);
            JSONObject ticker = msgJson.getJSONObject("ticker");
            Market market = new Market(ExchangeConstant.Zb, tradePair.getSymbol(), tradePair.getUnit());
            market.setLast(ticker.getBigDecimal("last"));
            market.setHigh(ticker.getBigDecimal("high"));
            market.setLow(ticker.getBigDecimal("low"));
            market.setAsk(ticker.getBigDecimal("sell"));
            market.setBid(ticker.getBigDecimal("buy"));
            market.setVolume(ticker.getBigDecimal("vol"));
            market.setTimestamp(msgJson.getLong("date"));
            objects.add(market);
            log.debug("Zb market own {}", market);
            return objects;
        }
//        if (StringUtils.endsWithIgnoreCase(channel, CHANNEL_KLINE)) {
//            return null;
//        }
//        if (StringUtils.endsWithIgnoreCase(channel, CHANNEL_DEPTH)) {
//            return null;
//        }

        return null;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Zb;
    }

}
