package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 10:18  王楷
 * @version 10:18 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.coin900", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.coin900")
public class Coin900Websocket extends BaseWebsocket {
    private static final String SUB_REQ = "{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"market-global\"}}";

    private static final String TRADE_REQ = "{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"market-%s-global\"}}";


    //临时获取的Tickers的交易对  供获取Trade数据使用
    public Set<CoinInfo> temp_pairs = new HashSet<CoinInfo>();

    //获取的Trade交易对（带交易ID）供Rest方式查询K线使用
    public static Set<CoinInfo> pairs = new HashSet<CoinInfo>();

    @Override
    public void subscribe() {
        send(SUB_REQ);
    }

    boolean isSend = true;

    @Override
    public List<OnlyKey> onMessageInPool(String msg) {
        List<OnlyKey> datas = new ArrayList<>();
        try {
            JSONObject jsonObject = JSONObject.parseObject(msg);
            // 创建一个用于批量保存的List
            if (Objects.equals(jsonObject.getString("event"), "tickers")) {
                jsonObject.getJSONObject("data").forEach((key, value) -> {
                    temp_pairs.add(new CoinInfo(key, null));
                    JSONObject obj = (JSONObject) value;
                    if (obj.size() != 12) {
                        //TODO Coin900会返回一些无用的数据导致下方出现nulL指针，所以做拦截处理
                        return;
                    }
                    Market market = new Market(ExchangeConstant.Coin900, obj.getString("base_unit"), obj.getString("quote_unit"));
                    market.setLow(obj.getBigDecimal("low"));
                    market.setHigh(obj.getBigDecimal("high"));
                    market.setLast(obj.getBigDecimal("last"));
                    market.setOpen(obj.getBigDecimal("open"));
                    market.setClose(obj.getBigDecimal("close"));
                    market.setVolume(obj.getBigDecimal("volume"));
                    market.setBid(obj.getBigDecimal("buy"));
                    market.setAsk(obj.getBigDecimal("ask"));
                    market.setTimestamp(obj.getTimestamp("at").getTime() * 1000L);
                    datas.add(market);
                });
                //发送这个是为了订阅下方的Trades 目的是为了保证K线数据的正常获取
                if (isSend) {
                    temp_pairs.forEach(temp_pair -> {
//                    send(String.format(TRADE_REQ, temp_pair.getPair()));
                    });
                    isSend = false;
                }
            } else if (Objects.equals(jsonObject.getString("event"), "trades")) {
                //channel:"market-ethbtc-global"
                //目前币新的交易市场中只有三个市场 BTC ETH USDT 市场 所以暂取截取后三位判断币种和单位
                String channels = jsonObject.getString("channel").split("-")[1].toUpperCase();
                String coin, unit;
                if (channels.contains("USDT")) {
                    coin = channels.replace("USDT", "");
                    unit = "USDT";
                } else if ("ETH,BTC".contains(channels.substring(channels.length() - 3))) {
                    unit = channels.substring(channels.length() - 3);
                    coin = channels.replace(unit, "");
                } else {
                    return null;
                }
                JSONArray trades = jsonObject.getJSONObject("data").getJSONArray("trades");
                trades.forEach(obj -> {
                    JSONObject jsonObj = (JSONObject) obj;
                    Market market = new Market(ExchangeConstant.Coin900, coin, unit);
                    market.setLast(jsonObj.getBigDecimal("price"));
                    market.setAmount(jsonObj.getBigDecimal("amount"));
                    market.setTimestamp(jsonObj.getTimestamp("date").getTime() * 1000L);
                    pairs.add(new CoinInfo(channels.toLowerCase(), jsonObj.getString("tid")));
                    datas.add(market);
                });
            }
            log.debug("目前Coin900的交易对数量为：{}", pairs.size());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return datas;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coin900;
    }

    @Data
    @EqualsAndHashCode(exclude = "tradeId")
    public class CoinInfo {
        String pair;
        String tradeId;

        public CoinInfo(String pair, String tradeId) {
            this.pair = pair;
            this.tradeId = tradeId;
        }

    }

}
