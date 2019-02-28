package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.QuintarExchange;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 10:10  王楷
 * @version 10:10 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.quintar", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.quintar")
public class QuintarWebSocket extends BaseWebsocket {

    @Autowired
    private QuintarExchange quintarExchange;

    private static final String UN_SUB_REQ = "42[\"unsubscribe:ticker\",\"\"]";

    private static final String UN_MARKET_REQ = "42[\"unsubscribe:market\",\"\"]";

    private static final String SUB_REQ = "42[\"subscribe:ticker\",\"%s\"]";

    private static final String MARKET_REQ = "42[\"subscribe:market\",\"%s\"]";

    boolean isSend = true;

    @Override
    public void subscribe() {
        //先取消全部订阅
        send(UN_SUB_REQ);
        send(UN_MARKET_REQ);
        //ScheduledExecutorService::scheduleAtFixedRate(sendSubAll,2,3, TimeUnit.SECONDS);
//        scheduler.scheduleAtFixedRate(sendSubAll(webSocket), 1, 60 * 12, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(sendTwo(webSocket), 1, 2, TimeUnit.SECONDS);

    }

    private Runnable sendTwo(WebSocket webSocket) {
        return new Runnable() {

            @Override
            public void run() {
                send("2");
            }
        };
    }


    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        ArrayList<OnlyKey> objects = Lists.newArrayList();
        log.debug("金塔推送回的数据：" + s);
        try {
            String body = s.substring(0, 1);
            if (Objects.equals(body, "3") || Objects.equals(body, "0") || Objects.equals(s, "40")) {
                return null;
            }
            JSONArray jsonArray = JSONArray.parseArray(s.substring(s.indexOf("[")));
            String type = jsonArray.getString(0);
            switch (type) {
                case "ticker":
                    JSONObject keyObject = jsonArray.getJSONObject(1);
                    if (keyObject.isEmpty()) {
                        return null;
                    }
                    keyObject.entrySet().forEach(stringObjectEntry -> {
                        JSONObject valueObject = (JSONObject) stringObjectEntry.getValue();
                        String originalExchange = stringObjectEntry.getKey().split(":")[0];
                        //String nowExchange = originalExchange.substring(0,1).toUpperCase()+originalExchange.substring(1);
                        String unit = "";
                        if (!"USD_CNY_KRW_USDT_JPY".contains(valueObject.getString("currency"))) {
                            unit = valueObject.getString("currency").replace(originalExchange, "");
                        } else {
                            unit = valueObject.getString("currency");
                        }
                        String coin = stringObjectEntry.getKey().replace(originalExchange + ":", "").replace(unit, "");
                        ExchangeConstant exchangeConstant = ExchangeConstant.valueExist(originalExchange);
                        if (exchangeConstant == null) {
                            log.warn("金塔新增了交易所 {} ，我们目前尚未入库", originalExchange);
                            return;
                        }
                        Market market = new Market(exchangeConstant, coin, unit);
                        market.setVolume(valueObject.getBigDecimal("vol"));
                        market.setLast(valueObject.getBigDecimal("last"));
                        market.setClose(valueObject.getBigDecimal("closing"));
                        market.setOpen(valueObject.getBigDecimal("opening"));
                        market.setLow(valueObject.getBigDecimal("low"));
                        market.setChange(valueObject.getBigDecimal("degree").multiply(new BigDecimal("100")));
                        market.setAsk(valueObject.getBigDecimal("sell"));
                        market.setBid(valueObject.getBigDecimal("buy"));
                        //hight 没写错  传值就是如此
                        market.setHigh(valueObject.getBigDecimal("hight"));
                        market.setType("Quintar");
                        market.setTimestamp(System.currentTimeMillis());
                        objects.add(market);
                    });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return objects;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return null;
    }

//    private void sendTwo (WebSocket webSocket){
//        send("2");
//    }

//    private Runnable sendSubAll() {
//        while (true) {
//            try {
//                Thread.sleep(2000);
//                if (QuintarExchange.pairsStatic.size() != 0 && isSend) {
//                    QuintarExchange.pairsStatic.forEach(key -> {
//                        send(String.format(SUB_REQ, key));
//                        send(String.format(MARKET_REQ, key));
//                        log.debug("QuintarWebSocket发送Ticker订阅消息：" + String.format(SUB_REQ, key));
//                        log.debug("QuintarWebSocket发送Market订阅消息：" + String.format(MARKET_REQ, key));
//                        try {
//                            Thread.sleep(100);
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//                        }
//                    });
//                    log.debug("循环订阅已发送完成！");
//                    isSend = false;
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }
//    }

}
