package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.HuobiExchange;
import com.al.dbspider.config.KlineConfig;
import com.al.dbspider.dao.domain.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import okio.ByteString;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * 火币网 WebsocketAPI 请求
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-12
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.huobi", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.huobi")
public class HuobiWebsocket extends BaseWebsocket {
    @Autowired
    HuobiExchange huobiExchange;

    String depthStep = "step0";//default
    private static ConcurrentLinkedQueue<Long> MARKET_IDS = Queues.newConcurrentLinkedQueue();
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    static {
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                int size = MARKET_IDS.size();
                log.info("清理 Huobi 重复市场行情 ID 缓存,{}", size);
                for (int j = 0; j < size - 2000; j++) {
                    MARKET_IDS.poll();
                }
            }
        }, 1, 15, TimeUnit.MINUTES);
    }

    List<String> periods = KlineConfig.getPeriod();


    @Override
    public void subscribe() {
        if (HuobiExchange.TRADE_PAIRS.keySet().size() == 0) {
            huobiExchange.requestAllSymbols();
        }
        for (String tradePair : HuobiExchange.TRADE_PAIRS.keySet()) {
            subMarket(tradePair);
            for (String period : periods) {
                subKline(tradePair, period);
            }
            subTrade(RandomStringUtils.randomAlphabetic(8), tradePair);
            subDepth(RandomStringUtils.randomAlphabetic(8), tradePair);
        }
    }

    private void subMarket(String tradePair) {
        if (market) {
            String marketTopic = String.format("market.%s.detail", tradePair);
            String marketReqId = RandomStringUtils.randomAlphabetic(8);
            send(String.format("{\"sub\": \"%s\",\"id\": \"%s\"}", marketTopic, marketReqId));
        }
    }

    private void subTrade(String id, String tradePair) {
        if (trade) {
            String marketTopic = String.format("market.%s.trade.detail", tradePair);
            send(String.format("{\"sub\": \"%s\",\"id\": \"%s\"}", marketTopic, id));
            log.debug("订阅 huobi {} , trade 数据 id= {}", tradePair, id);
        }
    }

    private void subKline(String tradePair, String period) {
        if (kline) {
            String klineSubId = RandomStringUtils.randomAlphabetic(8);
            subKline(klineSubId, tradePair, period);
        }
    }

    private void subDepth(String id, String tradePair) {
        if (depth) {
            String marketTopic = String.format("market.%s.depth.%s", tradePair, depthStep);
            webSocket.send(String.format("{\"sub\": \"%s\",\"id\": \"%s\"}", marketTopic, id));
            log.debug("订阅 huobi {} , depth 数据 id= {}", tradePair, id);
        }
    }

    @Override
    public void ping() {
        webSocket.send(String.format("{\"ping\":%s}", RandomUtils.nextLong()));
    }

    @Override
    protected boolean isNeedPing() {
        return true;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Huobi;
    }

//    private void startMarketTask() {
//        scheduler.scheduleWithFixedDelay(() -> HuobiExchange.TRADE_PAIRS.forEach(o -> {
//            String marketTopic = String.format("market.%s.detail", o);
//            String marketReqId = RandomStringUtils.randomAlphabetic(8);
//            webSocket.send(String.format("{\"req\": \"%s\",\"id\": \"%s\"}", marketTopic, marketReqId));
////                    ids.put(marketReqId, marketTopic);
//            log.debug("请求 huobi {} , market 数据 id= {}", o, marketReqId);
//        }), 0, 1, TimeUnit.SECONDS);
//
//    }

    private void subKline(String id, String tradePair, String period) {
        String marketTopic = String.format("market.%s.kline.%s", tradePair, period);
        send(String.format("{\"sub\": \"%s\",\"id\": \"%s\"}", marketTopic, id));
        log.debug("订阅 huobi {} , {} ,kline 数据 id= {}", tradePair, period, id);
    }


    @Override
    public List<OnlyKey> onMessageInPool(ByteString bytes) {
        ArrayList<OnlyKey> objects = Lists.newArrayList();
        try {
            String msg = uncompress(bytes.toByteArray());
            log.debug(msg);
            if (msg.isEmpty()) {
                log.info("uncompress msg is empty");
                return null;
            }
            JSONObject context = JSON.parseObject(msg);
            Object subid = context.get("subbed");//sub response
            if (subid != null) {
                log.debug("订阅 huobi {} 成功", msg);
                return null;
            }
            //error
            Object error = context.get("err-code");
            if (error != null) {
                log.error("huobi websocket 出错 {}", msg);
                return null;
            }
            //push
            Object topicIdObj = context.get("ch");
            if (topicIdObj != null) {
                String topicId = topicIdObj.toString();
                String[] split = StringUtils.split(topicId, ".");
                String tradepair = split[1];
                HuobiExchange.Symbol symbolObj = HuobiExchange.TRADE_PAIRS.get(tradepair);
                if (symbolObj == null) {
                    log.info("symbol is null {}", tradepair);
                    return null;
                }
                String symbol = symbolObj.getBaseCurrency();
                String unit = symbolObj.getQuoteCurrency();
                //kline
                if (StringUtils.contains(topicId, ".kline.1min")) {
                    JSONObject kline = context.getJSONObject("tick");
                    log.debug("反序列化 kline {}", kline);
                    KLine kLinedb = getkLine(symbol, unit, kline);
                    objects.add(kLinedb);
                    return objects;
                }
                if (StringUtils.contains(topicId, ".kline.1day")) {
                    JSONObject kline = context.getJSONObject("tick");
                    log.debug("反序列化 kline {}", kline);
                    KLine kLinedb = getkLine(symbol, unit, kline);
                    kLinedb.setMeasurement("kline_1D");
                    objects.add(kLinedb);
                    return objects;
                }
                if (StringUtils.contains(topicId, ".trade.detail")) {
                    JSONObject tick = context.getJSONObject("tick");
                    JSONArray trades = tick.getJSONArray("data");
                    log.debug("反序列化 trades {}", tick);
                    ArrayList<Trade> tradeForReal = Lists.newArrayList();
                    for (int i = 0; i < trades.size(); i++) {

                        JSONObject tradeJson = trades.getJSONObject(i);
                        Trade trade = new Trade(ExchangeConstant.Huobi, symbol, unit);
                        String tid = tradeJson.getString("id");
                        Long ts = tradeJson.getLong("ts");
                        //Huobi tid 会重复,需要加上data 里的时间戳来判断是否唯一
                        String cachedid = String.format("%s_%s_%s", trade.getOnlyKey(), tid, ts);
                        String isOK = cacheTid(cachedid, cachedid);
                        if (isOK == null) {
                            log.debug("{} exist", cachedid);
                            continue;
                        }
                        trade.setTradeId(tid);
                        trade.setPrice(tradeJson.getBigDecimal("price"));
                        trade.setVolume(tradeJson.getBigDecimal("amount"));
                        trade.setSide(tradeJson.getString("direction"));
                        trade.setTimestamp(millToNano(ts));
                        tradeForReal.add(trade);
                        objects.add(trade);
                    }
                    realTradeList(tradeForReal, ExchangeConstant.Huobi, getHundredBuyAndSellFirstMap());
                    return objects;
                }
                //market data
                if (StringUtils.contains(topicId, ".detail")) {
                    JSONObject marketJson = context.getJSONObject("tick");
                    boolean add = MARKET_IDS.add(marketJson.getLong("id"));
                    if (!add) {
                        log.debug("行情重复 market {}", marketJson);
                        return null;
                    }
                    log.debug("反序列化 market {}", marketJson);
                    Market marketdb = new Market(ExchangeConstant.Huobi, symbol, unit);
                    marketdb.setLast(marketJson.getBigDecimal("close"));
                    marketdb.setVolume(marketJson.getBigDecimal("amount"));
                    marketdb.setHigh(marketJson.getBigDecimal("high"));
                    marketdb.setLow(marketJson.getBigDecimal("low"));
                    marketdb.setTimestamp(context.getLong("ts"));
                    objects.add(marketdb);
                    return objects;
                }

                //depth data
                if (StringUtils.contains(topicId, ".depth")) {
                    List<DepthDTO> depthDTOS = getJsonArrayByJob(context, symbol, unit);
                    if (depthDTOS.size() == 0) {
                        return new ArrayList<>();
                    }
                    getBuyAndSellFirst(depthDTOS, 0, 0);
                    objects.addAll(depthDTOS);
                    return objects;
                }

            }

            //response
            Object repid = context.get("rep");//req response
            if (repid != null) {
//                MarketMsg marketMsg = JSON.parseObject(msg, MarketMsg.class);
//                Market market = marketMsg.getData();
//                log.debug("请求响应 huobi market 数据 {},repid = {}, id = {}", market, repid, marketMsg.getId());
//                com.al.dbspider.dao.domain.Market marketdb = new com.al.dbspider.dao.domain.Market();
//                marketdb.setExchange(EXCHANGE_NAME);
//                marketdb.setSymbol(repid.substring(7, 10));
//                marketdb.setUnit(repid.substring(10, 13));
//                marketdb.setLast(market.getClose());
//                marketdb.setVolume(market.getAmount());
//                marketdb.setHigh(market.getHigh());
//                marketdb.setLow(market.getLow());
//                marketdb.setTimestamp(market.getTs());
//                influxDbMapper.addMarketData(marketdb);
//                return;
            }

            Object unsubid = context.get("unsubbed");//unsub response
            if (unsubid != null) {
                //TODO
                return null;
            }

            Object ping = context.get("ping");//server ping client
            if (ping != null) {
                log.debug("响应 ping {}", ping);
                webSocket.send(String.format("{\"pong\":%s}", ping));
                return null;
            }

            Object pong = context.get("pong");//server ping client
            if (pong != null) {
                log.debug("pong={}", pong);
                return null;
            }
            log.debug("未解析到的数据:{}", msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return objects;
    }

    private List<DepthDTO> getJsonArrayByJob(JSONObject jsonObject, String symbol, String unit) {
        List<DepthDTO.PriceLevel> asks;
        List<DepthDTO.PriceLevel> bids;
        JSONArray result = new JSONArray();
        JSONObject tick = new JSONObject();
        asks = jsonObject.getJSONObject("tick").getJSONArray("asks").stream().map(ask -> getPriceLevel(ask, 0, 1)).collect(Collectors.toList());
        bids = jsonObject.getJSONObject("tick").getJSONArray("bids").stream().map(bid -> getPriceLevel(bid, 0, 1)).collect(Collectors.toList());
        jsonObject.put("onlyKey", ExchangeConstant.Huobi + "_" + symbol.toUpperCase() + "_" + unit.toUpperCase());
        jsonObject.put("timestamp", jsonObject.getJSONObject("tick").getLongValue("ts"));
        jsonObject.put("type", 0);
        tick.put("asks", asks);
        tick.put("bids", bids);
        jsonObject.put("tick", tick);
        result.add(jsonObject);
        return result.toJavaList(DepthDTO.class);
    }

    private DepthLevel createDepthLevel(JSONArray asksJson, int i) {
        JSONArray depthLevelJson = asksJson.getJSONArray(i);
        DepthLevel depthLevel = new DepthLevel();
        depthLevel.setAmount(depthLevelJson.getBigDecimal(1));
        depthLevel.setCount(0);
        depthLevel.setPrice(depthLevelJson.getBigDecimal(0));
        return depthLevel;
    }

    private KLine getkLine(String symbol, String unit, JSONObject kline) {
        KLine kLinedb = new KLine(ExchangeConstant.Huobi, symbol, unit);
        kLinedb.setOpen(kline.getBigDecimal("open"));
        kLinedb.setClose(kline.getBigDecimal("close"));
        kLinedb.setHigh(kline.getBigDecimal("high"));
        kLinedb.setLow(kline.getBigDecimal("low"));
        kLinedb.setVolume(kline.getBigDecimal("amount"));
        kLinedb.setTimestamp(kline.getLong("id") * 1000);//id是交易时间, ts只是消息发送时间
        return kLinedb;
    }

    private static String uncompress(byte[] bb) {
        String s = StringUtils.EMPTY;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in = new ByteArrayInputStream(bb);
             GZIPInputStream gunzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            s = out.toString();
        } catch (IOException e) {
            log.error("umcompress error " + e.getMessage(), e);
        }
        return s;
    }
}
