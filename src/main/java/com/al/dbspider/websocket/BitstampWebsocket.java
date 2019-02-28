package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.TradePair;
import com.al.dbspider.base.api.Bitstamp;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.bitstamp", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.bitstamp")
public class BitstampWebsocket extends BaseWebsocket {

    String appkey = "de504dc5763aeef9ff52";

    @Autowired
    Bitstamp bitstamp;

    private Map<String, TradePair> symbolMap = new HashMap<>();

    private static final String SUB_OTHER = "live_trades_%s";

    Pusher pusher;

    @PostConstruct
    public void buildBitstampWebsocket() {
        PusherOptions pusherOptions = new PusherOptions();
        pusherOptions.setEncrypted(true);
        pusher = new Pusher(appkey, pusherOptions);
    }

    @Override
    protected void open() {
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                log.info("{} websocket status change from {} to {}", exchangeName, change.getPreviousState(), change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                log.error(exchangeName + " connection error,message " + message + ",code " + code, e);
            }
        });
    }

    @Override
    public boolean close(int code, String reason) {
        pusher.disconnect();
        manualClose = true;
        return true;
    }

    @Override
    public void send(String text) {
        Channel channel = pusher.subscribe(text, new ChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channel) {
                log.info("{} {} 订阅成功", exchangeName, channel);
            }

            @Override
            public void onEvent(String channel, String event, String data) {
                log.info("{} {} 订阅 {} 数据 ,msg {}", exchangeName, channel, event, data);
            }
        });
        channel.bind("trade", (channelName, eventName, data) -> {
            pool.submit(new MessageHandler(this, channelName + "@" + data));
        });
    }

    @Override
    public void subscribe() {
        try {
            Response<String> execute = bitstamp.allPairs().execute();
            String body = execute.body();
            if (StringUtils.isEmpty(body)) {
                log.error("{} 未获取到 pairs", exchangeName);
                return;
            }
            JSONArray tradePairJsonArray = JSON.parseArray(body);
            for (Object symbolObject : tradePairJsonArray) {
                JSONObject symbolJsonObject = (JSONObject) symbolObject;
                String urlSymbol = symbolJsonObject.getString("url_symbol");
                if ("Enabled".equalsIgnoreCase(symbolJsonObject.getString("trading"))) {
                    String format = String.format(SUB_OTHER, urlSymbol);
                    send(format);
                    TradePair tradePair = new TradePair(symbolJsonObject.getString("name"), "/");
                    symbolMap.put(format, tradePair);
                    log.debug("{} subscribe trade {}", exchangeName, urlSymbol);
                } else {
                    log.debug("{} {} not trade", exchangeName, urlSymbol);
                }
            }
        } catch (IOException e) {
            log.error(exchangeName + "获取交易对出错", e);
        }
    }


    @Override
    protected List<OnlyKey> onMessageInPool(String text) {
        List<OnlyKey> onlyKeys = new ArrayList<>();
        try {
            log.info("{}", text);
            String[] strings = StringUtils.split(text,"@");
            String channel = strings[0];
            String message = strings[1];
            //{"amount": 0.00894, "buy_order_id": 2305213047, "sell_order_id": 2305213000, "amount_str": "0.00894000", "price_str": "5592.67", "timestamp": "1540285237", "price": 5592.6700000000001, "type": 0, "id": 76330611}
            JSONObject data = JSON.parseObject(message);
            TradePair tradePair = symbolMap.get(channel);
            if (tradePair == null) {
                log.error("{} {} 未订阅", getExchangeName(), channel);
            }
            Trade trade = new Trade(getExchangeName(), tradePair.getSymbol(), tradePair.getUnit());
            trade.setTradeId(data.getString("id"));
            String cachedid = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
            String isOK = cacheTid(cachedid, trade.getTradeId());
            if (isOK == null) {
                log.debug("{} exist", cachedid);
                return onlyKeys;
            }
            trade.setTimestamp(millToNano(Long.valueOf(data.getString("timestamp") + "000")));
            trade.setVolume(data.getBigDecimal("amount"));
            trade.setPrice(data.getBigDecimal("price_str"));
            trade.setSide(data.getInteger("type") == 0 ? "buy" : "sell");
            log.debug("{} trade own {}", getExchangeName(), trade);
            onlyKeys.add(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return onlyKeys;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitstamp;
    }

    public static void main(String[] args) {
        JSONArray objects = JSON.parseArray("[Public Channel: name=live_trades_btceur]");
        System.out.println(objects);
    }
}
