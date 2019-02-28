package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.assertj.core.util.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/10/23 10:05
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.coinbase", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.coinbase")
public class CoinBaseWebsocket extends BaseWebsocket {

    private final static String SUBSCRIBE_STR = "{\"type\": \"subscribe\",\"product_ids\": [%s],\"channels\": [\"ticker\",\"matches\"]}";

    private final static String SUBSCRIBE_SYMBOL = "\"BTC-EUR\",\"BTC-GBP\",\"BTC-USD\",\"ETH-EUR\",\"ETH-GBP\",\"ETH-BTC\",\"ETH-USD\",\"LTC-EUR\",\"LTC-GBP\",\"LTC-BTC\",\"LTC-USD\",\"BCH-EUR\",\"BCH-GBP\",\"BCH-BTC\",\"BCH-USD\",\"ETC-EUR\",\"ETC-GBP\",\"ETC-BTC\",\"ETC-USD\",\"ZRX-EUR\",\"ZRX-BTC\",\"ZRX-USD\"";

    private final String TICKER = "ticker";

    private final String MATCH = "match";

    @Override
    public void subscribe() {
        send(String.format(SUBSCRIBE_STR,SUBSCRIBE_SYMBOL));
    }

    /**
     * 在线程中处理message，避免message过多时阻塞websocket线程
     * onMessage和onMessageInPool 方法至少要重写一个
     *
     * @param message
     */
    @Override
    protected List<OnlyKey> onMessageInPool(String message) {
        ArrayList<OnlyKey> onlyKeys = Lists.newArrayList();
        try {
            JSONObject jsonObject = JSON.parseObject(message);
            String type = jsonObject.getString("type");
            String product_id = jsonObject.getString("product_id");
            if (!StringUtils.hasText(product_id)){
                return null;
            }
            String[] split = product_id.split("-");
            if (split.length!=2){
                return null;
            }
            if (Objects.equals(TICKER,type)) {
                Market market = new Market(getExchangeName(),split[0],split[1]);
                market.setLast(jsonObject.getBigDecimal("price"));
                market.setOpen(jsonObject.getBigDecimal("open_24h"));
                market.setClose(jsonObject.getBigDecimal("price"));
                market.setHigh(jsonObject.getBigDecimal("high_24h"));
                market.setLow(jsonObject.getBigDecimal("low_24h"));
                market.setBid(jsonObject.getBigDecimal("best_bid"));
                market.setAsk(jsonObject.getBigDecimal("best_ask"));
                market.setVolume(jsonObject.getBigDecimal("volume_24h"));
                market.setTimestamp(System.currentTimeMillis());
                onlyKeys.add(market);
            }
            if (Objects.equals(MATCH,type)){
                Trade trade = new Trade(getExchangeName(),split[0],split[1]);
                trade.setPrice(jsonObject.getBigDecimal("price"));
                trade.setTradeId(jsonObject.getString("taker_order_id"));
                String cachedId = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                String isOK = cacheTid(cachedId, trade.getTradeId());
                if (isOK == null) {
                    log.debug("{} exist", cachedId);
                    return null;
                }
                trade.setVolume(jsonObject.getBigDecimal("size"));
                trade.setSide(jsonObject.getString("side"));
                Date date = coverStringFormatTime(jsonObject.getString("time"));
                if (date == null) {
                    return null;
                }
                trade.setTimestamp(millToNano(date.getTime()));
                onlyKeys.add(trade);
            }
            if (log.isDebugEnabled()){
                log.debug("CoinBase data : {}",JSON.toJSONString(onlyKeys));
                log.debug("本次CoinBase发送条数为{}", onlyKeys.size());
            }
            //return onlyKeys;
        } catch (Throwable e) {
            log.error("解析 CoinBase 返回数据出错！", e);
        }
        return onlyKeys;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coinbase;
    }
}
