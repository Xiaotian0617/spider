package com.al.dbspider.websocket;

import com.al.dbspider.base.BinanceExchange;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.api.Binance;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 由于binance 是在 websocket 连接时带参数实现订阅.交易所nginx 对url 长度限制,批量订阅每次只能订阅一种时间维度的kline
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-08-01
 */
@Slf4j
public abstract class BinanceKlineWebsocket extends BaseWebsocket {
    public static final String KLINE_STREAM_NAME_TEMP = "%s@kline_%s";
    public static final Map<String, Binance.Symbol> symbolsMap = new HashMap<>(400);
    @Autowired
    BinanceExchange binanceExchange;

    //连接上即订阅
    @PostConstruct
    public void buildSymbol() {
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
    }

    @Override
    public void subscribe() {

    }

    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        ArrayList<OnlyKey> onlyKeys = Lists.newArrayList();
        try {
            JSONObject jsonObject = JSON.parseObject(s);
            Object dataObject = jsonObject.get("data");
            JSONObject data = (JSONObject) dataObject;
            String type = data.getString("e");
            String symbolInMsg = data.getString("s");
            Binance.Symbol symbol = symbolsMap.get(symbolInMsg);
            if ("kline".equals(type)) {
                //K线
                if (symbol != null) {
                    log.debug("Binance kline origin {}", data);
                    KLine kLine = new KLine(ExchangeConstant.Binance, symbol.getBaseAsset(), symbol.getQuoteAsset());
                    kLine.setOpen(data.getJSONObject("k").getBigDecimal("o"));
                    kLine.setClose(data.getJSONObject("k").getBigDecimal("c"));
                    kLine.setHigh(data.getJSONObject("k").getBigDecimal("h"));
                    kLine.setLow(data.getJSONObject("k").getBigDecimal("l"));
                    kLine.setVolume(data.getJSONObject("k").getBigDecimal("v"));
                    kLine.setTimestamp(data.getJSONObject("k").getLong("t"));
                    kLine.setMeasurement(getKlineMeasurement());
                    onlyKeys.add(kLine);
                    log.debug("Binance kline {}", kLine);
                }
            }
        } catch (Throwable e) {
            log.error("解析币安返回数据出错！", e);
        }
        return onlyKeys;
    }

    protected abstract String getKlineMeasurement();

}
