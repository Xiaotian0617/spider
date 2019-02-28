package com.al.dbspider.newExchange;

import com.al.bcoin.Bitmex;
import com.al.bcoin.WSListener;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.*;
import com.al.dbspider.websocket.BaseWebsocket;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.al.bcoin.Bitmex.paser;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/25 20:52
 */
@Slf4j
@ConditionalOnProperty(prefix = "websocket.bitmex", name = "disable", havingValue = "false", matchIfMissing = true)
@Component
@ConfigurationProperties("websocket.bitmex")
public class BitmexDP extends BaseWebsocket {
    String SUBSCRIBE = "{\"op\": \"subscribe\", \"args\": [\"%s\"]}";

    //"orderBookL2","orderBook10" 订阅深度数据
    List<String> SUB_TYPE = Arrays.asList("trade", "tradeBin1m", "tradeBin1d","orderBook10");

    static Map<String, Bitmex.Api.BitmexPair> pairsMap = new HashMap<String, Bitmex.Api.BitmexPair>() {{
        put("XBTUSD", new Bitmex.Api.BitmexPair("XBT", "USD"));
        put("XBT7D_U105", new Bitmex.Api.BitmexPair("XBT7DU105", "USD"));
        put("XBT7D_D95", new Bitmex.Api.BitmexPair("XBT7DD95", "USD"));
        put("ETHUSD", new Bitmex.Api.BitmexPair("ETH", "USD"));
    }};


    @Override
    protected List<OnlyKey> onMessageInPool(String message) {
        WSListener.Info<String> info = paser(message);
        log.trace("Bitmex 原始数据{}", info);
        return getInfoForModel(info);

    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitmex;
    }

    protected List<OnlyKey> getInfoForModel(WSListener.Info info) {
        if (info == null) {
            return null;
        }
        List<OnlyKey> onlyKeys = new ArrayList<>();
        switch (info.getType()) {
            case "tradeBin1m":
                return getKlineForInfo(onlyKeys, info, "1m");
            case "tradeBin1d":
                return getKlineForInfo(onlyKeys, info, "1D");
            case "trade":
                return getTradeForInfo(onlyKeys, info);
            case "orderBook10":
                return getDepthForInfo(onlyKeys,info);
            default:
                break;
        }
        return null;
    }

    private List<OnlyKey> getDepthForInfo(List<OnlyKey> onlyKeys, WSListener.Info info) {
        String data = checkDataHasText(info);
        if (data == null) {
            return null;
        }
        List<OnlyKey> depths = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(data);
        jsonArray.forEach(array -> {
            JSONObject job = (JSONObject) array;
            List<DepthDTO> depth = getBitmexDepth(job,info);
            if (depth!=null&&!depth.isEmpty()){
                onlyKeys.addAll(depth);
            }
        });
        log.debug("Bitmex depth:{}",JSON.toJSONString(onlyKeys));
        return onlyKeys;
    }

    private List<DepthDTO> getBitmexDepth(JSONObject jsonObject, WSListener.Info info) {
        JSONArray result = new JSONArray();
        Date date = coverStringFormatTime(jsonObject.getString("timestamp"));
        if (date == null) {
            return null;
        }
        JSONObject tick = new JSONObject();
        List<DepthDTO.PriceLevel> asks = jsonObject.getJSONArray("asks").stream().map(ask -> getPriceLevel(ask, 0, 1)).collect(Collectors.toList());
        List<DepthDTO.PriceLevel> bids = jsonObject.getJSONArray("bids").stream().map(bid -> getPriceLevel(bid, 0, 1)).collect(Collectors.toList());
        jsonObject.put("onlyKey", getExchangeName()+"_"+info.getCoin()+"_"+info.getUnit());
        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("type", 0);
        tick.put("asks", asks);
        tick.put("bids", bids);
        jsonObject.put("tick", tick);
        result.add(jsonObject);
        return result.toJavaList(DepthDTO.class);
    }

    private List<OnlyKey> getTradeForInfo(List<OnlyKey> onlyKeys, WSListener.Info info) {
        String data = checkDataHasText(info);
        if (data == null) {
            return null;
        }
        List<OnlyKey> markets = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(data);
        jsonArray.forEach(array -> {
            JSONObject job = (JSONObject) array;
            Trade trade = getBitmexTrade(job,info);
            if (trade == null) {
                Market market = getBitmexMarket(job,info);
                markets.add(market);
                return;
            }
            onlyKeys.add(trade);
            Market market = getBitmexMarket(job,info);
            markets.add(market);
        });
        if (markets != null && markets.size() > 0) {
            postData(markets);
        }
        return onlyKeys;
    }

    private Market getBitmexMarket(JSONObject jsonObject, WSListener.Info info) {
        Market market = new Market(ExchangeConstant.Bitmex,  info.getCoin(), info.getUnit());
        market.setType("Api");
        market.setLast(jsonObject.getBigDecimal("price"));
        Date date = coverStringFormatTime(jsonObject.getString("timestamp"));
        if (date == null) {
            return null;
        }
        market.setTimestamp(date.getTime());
        log.debug("Bitmex market: {}", market);
        return market;
    }

    private Trade getBitmexTrade(JSONObject jsonObject, WSListener.Info info) {
        String trdMatchID = jsonObject.getString("trdMatchID");
        Trade trade = new Trade(ExchangeConstant.Bitmex, info.getCoin(), info.getUnit());
        String cachedid = trade.getOnlyKey() + trdMatchID;
        String isok = cacheTid(cachedid, trdMatchID);
        if (isok == null) {
            log.debug("{} exist", cachedid);
            return null;
        }
        trade.setVolume(jsonObject.getBigDecimal("size"));
        trade.setSide(jsonObject.getString("side").toLowerCase());
        trade.setPrice(jsonObject.getBigDecimal("price"));
        trade.setTradeId(trdMatchID);
        Date date = coverStringFormatTime(jsonObject.getString("timestamp"));
        if (date == null) {
            return null;
        }
        trade.setTimestamp(millToNano(date.getTime()));
        log.debug("Bitmex trade: {}", trade);
        return trade;
    }

    private List<OnlyKey> getKlineForInfo(List<OnlyKey> onlyKeys, WSListener.Info info, String timeType) {
        String data = checkDataHasText(info);
        if (data == null) {
            return null;
        }
        JSONArray jsonArray = JSONArray.parseArray(data);
        if (jsonArray.size() == 1) {
            JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(0).toString());
            KLine kLine = getBitmexkLine(jsonObject, timeType,info);
            if (kLine == null) {
                return null;
            }
            onlyKeys.add(kLine);
            return onlyKeys;
        }
        jsonArray.forEach(array -> {
            JSONObject job = (JSONObject) array;
            KLine bitmexkLine = getBitmexkLine(job, timeType,info);
            if (bitmexkLine == null) {
                return;
            }
            onlyKeys.add(bitmexkLine);
        });
        return onlyKeys;
    }

    private String checkDataHasText(WSListener.Info info) {
        String data = info.getData().toString();
        if (!StringUtils.hasText(data) || data.equals("[]")) {
            return null;
        }
        return data;
    }

    private KLine getBitmexkLine(JSONObject jsonObject, String timeType,WSListener.Info info) {
        KLine kLine = new KLine(ExchangeConstant.Bitmex, info.getCoin(), info.getUnit());
        kLine.setType("Api");
        if (timeType.equals("1D")) {
            kLine.setMeasurement("kline_" + timeType);
        }
        kLine.setHigh(jsonObject.getBigDecimal("high"));
        kLine.setLow(jsonObject.getBigDecimal("low"));
        kLine.setClose(jsonObject.getBigDecimal("close"));
        kLine.setOpen(jsonObject.getBigDecimal("open"));
        kLine.setVolume(jsonObject.getBigDecimal("volume"));
        kLine.setHigh(jsonObject.getBigDecimal("high"));
        Date date = coverStringFormatTime(jsonObject.getString("timestamp"));
        if (date == null) {
            return null;
        }
        kLine.setTimestamp(date.getTime());
        log.debug("Bitmex kline: {}", kLine);
        return kLine;
    }

    @Override
    public void subscribe() {
        String sendText;
        if (trade){
            sendText = String.format(SUBSCRIBE, "trade");
            log.info("bitmex trade 订阅 {} ", sendText);
            send(sendText);
        }
        if (kline){
            sendText = String.format(SUBSCRIBE, "tradeBin1m");
            log.info("bitmex tradeBin1m 订阅 {} ", sendText);
            send(sendText);
            sendText = String.format(SUBSCRIBE, "tradeBin1d");
            log.info("bitmex tradeBin1d 订阅 {} ", sendText);
            send(sendText);
        }
        if (depth){
            sendText = String.format(SUBSCRIBE, "orderBook10");
            log.info("bitmex orderBook10 订阅 {} ", sendText);
            send(sendText);
        }
    }
}
