package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.api.Bitfinex;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Trade;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.bitfinex", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.bitfinex")
public class BitfinexWebsocket extends BaseWebsocket {
    private static final Pattern PATTERN = Pattern.compile("(ETH|BTC|BNB|USD|EUR|GBP|JPY|EOS)$");
    // 从BitFinex服务器抓取币种symbols

    @Resource
    Bitfinex bitfinex;

    private Map<Integer, MarketSubscibe> markets = new ConcurrentHashMap<Integer, MarketSubscibe>();
    private Map<Integer, KlineSubscribe1m> klines_1m = new ConcurrentHashMap<Integer, KlineSubscribe1m>();
    private Map<Integer, KlineSubscribe1D> klines_1d = new ConcurrentHashMap<Integer, KlineSubscribe1D>();
    private Map<Integer, TradeSubscribe> trades = new ConcurrentHashMap<>();


    private List<String> getSymbols() {
        try {
            String symbolsStr = bitfinex.symbols().execute().body();
            return JSONObject.parseArray(symbolsStr).toJavaList(String.class);
        } catch (Exception e) {
            log.error("使用默认币种。" + e.getMessage(), e);
        }
        String[] arr = {"btcusd", "ltcusd", "ltcbtc", "ethusd", "ethbtc", "etcbtc", "etcusd", "rrtusd", "rrtbtc", "zecusd", "zecbtc", "xmrusd", "xmrbtc", "dshusd", "dshbtc", "btceur", "xrpusd", "xrpbtc", "iotusd", "iotbtc", "ioteth", "eosusd", "eosbtc", "eoseth", "sanusd", "sanbtc", "saneth", "omgusd", "omgbtc", "omgeth", "bchusd", "bchbtc", "bcheth", "neousd", "neobtc", "neoeth", "etpusd", "etpbtc", "etpeth", "qtmusd", "qtmbtc", "qtmeth", "avtusd", "avtbtc", "avteth", "edousd", "edobtc", "edoeth", "btgusd", "btgbtc", "datusd", "datbtc", "dateth", "qshusd", "qshbtc", "qsheth", "yywusd", "yywbtc", "yyweth", "gntusd", "gntbtc", "gnteth", "sntusd", "sntbtc", "snteth", "ioteur", "batusd", "batbtc", "bateth", "mnausd", "mnabtc", "mnaeth", "funusd", "funbtc", "funeth", "zrxusd", "zrxbtc", "zrxeth", "tnbusd", "tnbbtc", "tnbeth", "spkusd", "spkbtc", "spketh"};
        return Arrays.asList(arr);
    }

    @Override
    public void subscribe() {
        markets.clear();
        klines_1m.clear();
        klines_1d.clear();
        trades.clear();
        List<String> symbols = getSymbols();
        symbols.forEach((pair) -> {
            // 获得ticket命令
            if (market) {
                MarketSubscibe marketSubscibe = new MarketSubscibe();
                marketSubscibe.setPair(pair.toUpperCase());
                String marketSubString = JSONObject.toJSONString(marketSubscibe);
                send(marketSubString);
                log.debug("Bitfinex subscribe market {}", marketSubString);
            }
            if (kline) {
                // 获得candle命令
                KlineSubscribe1m klineSubscribe1m = new KlineSubscribe1m();
                klineSubscribe1m.setKey(klineSubscribe1m.getKey() + pair.toUpperCase());
                String oneMklineSubString = JSONObject.toJSONString(klineSubscribe1m);
                send(oneMklineSubString);
                log.debug("Bitfinex subscribe 1mkline {}", oneMklineSubString);
                KlineSubscribe1D klineSubscribe1D = new KlineSubscribe1D();
                klineSubscribe1D.setKey(klineSubscribe1D.getKey() + pair.toUpperCase());
                String oneDSubString = JSONObject.toJSONString(klineSubscribe1D);
                send(oneDSubString);
                log.debug("Bitfinex subscribe 1dkline {}", oneDSubString);
            }
            if (trade) {
                // 获得Trade命令
                TradeSubscribe tradeSubscribe = new TradeSubscribe();
                tradeSubscribe.setPair(pair.toUpperCase());
                String tradeSubString = JSONObject.toJSONString(tradeSubscribe);
                send(tradeSubString);
                log.debug("Bitfinex subscribe trade {}", tradeSubString);
            }
        });
    }


    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        if (s.contains("hb") || s.contains("info")) {
            return null;
        }

        if (s.contains("ticker")) {
            MarketSubscibe sub = JSONObject.parseObject(s, MarketSubscibe.class);
            markets.put(sub.getChanId(), sub);
            return null;
        }

        if (s.contains("candle")) {
            if (s.contains("trade:1m:")) {
                KlineSubscribe1m sub2 = JSONObject.parseObject(s, KlineSubscribe1m.class);
                klines_1m.put(sub2.getChanId(), sub2);
            }
            if (s.contains("trade:1D:")) {
                KlineSubscribe1D sub4 = JSONObject.parseObject(s, KlineSubscribe1D.class);
                klines_1d.put(sub4.getChanId(), sub4);
            }
            return null;
        }

        if (s.contains("trades")) {
            TradeSubscribe sub3 = JSONObject.parseObject(s, TradeSubscribe.class);
            trades.put(sub3.getChanId(), sub3);
//                log.info("Trades subscibe :  {}",JSON.toJSONString(trades));
            return null;
        }

        ArrayList<OnlyKey> objects = Lists.newArrayList();
        if (s.contains("[")) {
            DocumentContext parse = JsonPath.parse(s);
            int chanId = parse.read("$.[0]");
            // 获得chanId分别与存储ticket和candle的map匹配；
            MarketSubscibe sub = markets.get(chanId);
            KlineSubscribe1m sub2 = klines_1m.get(chanId);
            KlineSubscribe1D sub4 = klines_1d.get(chanId);
            TradeSubscribe sub3 = trades.get(chanId);
            // 如果ticket中有，说明是ticket的数据；
            if (sub != null) {
                // 获得ticket的map中相应chanId的symbol；
                String symbols = sub.getPair().toUpperCase();
                Matcher matcher = PATTERN.matcher(symbols);
                if (matcher.find()) {
                    String unit = matcher.group();
                    String symbol = matcher.replaceAll("");
                    Market market = new Market(ExchangeConstant.Bitfinex, symbol, unit);
                    JSONArray jsonArray = JSON.parseArray(parse.read("$.[1]").toString());
                    market.setLast(jsonArray.getBigDecimal(6));
                    market.setVolume(jsonArray.getBigDecimal(7));
                    market.setHigh(jsonArray.getBigDecimal(8));
                    market.setLow(jsonArray.getBigDecimal(9));
                    market.setTimestamp(System.currentTimeMillis());
                    objects.add(market);
                    log.debug("Bitfinex market {}", market);
                }
                // 如果candle中有，说明是candle的数据
            } else if (sub2 != null) {
                // 获得candle的map中相应的chanId的key（其中包含symbol）；
                String symbol = sub2.getKey().substring(10).toUpperCase();
                Matcher matcher = PATTERN.matcher(symbol);
                String unit;
                String coin;
                if (!matcher.find()) {
                    return null;
                }
                unit = matcher.group();
                coin = matcher.replaceAll("");
                // 因外层都会判断为JSONArray，所以取它内层的数据进行判断；
                List ls = JSONObject.parseArray(parse.read("$.[1]").toString());
                Object obj = ls.get(0);
                // 如果是不是JSONArray类型，说明是之后发送的单条的数据；
                if (!(obj instanceof JSONArray)) {
                    KLine kLine = getkLineByObj(parse, unit, coin);
                    objects.add(kLine);
                } else {
                    // 如果是JSONArray类型，说明是第一次发送来的大量数据；
                    List<KLine> klineList = new ArrayList<>();
                    ls.forEach(l -> {
                        KLine kLine = getkLineByArray(unit, coin, l);
                        klineList.add(kLine);
                        log.debug("Bitfinex kline {}", kLine);
                    });
                    objects.addAll(klineList);
                }
            } else if (sub3 != null) {
                //说明返回回来的是交易数据
                // 获得trade的map中相应的chanId的key（其中包含symbol）；
                String symbol = sub3.getPair().toUpperCase();
                Matcher matcher = PATTERN.matcher(symbol);
                String unit;
                String coin;
                if (!matcher.find()) {
                    return null;
                }
                unit = matcher.group();
                coin = matcher.replaceAll("");
                if ("tu".equals(parse.read("$.[1]"))) {
                    //tu 代表 已经update的
                    //te 代表 刚提交还未执行的  2018年5月26日 10:29:51 翻Bitfinex源码得知 所以这里只要tu的

                    Trade trade = new Trade(ExchangeConstant.Bitfinex, coin, unit);
                    trade.setTradeId(parse.read("$.[2].[0]").toString());
                    String cachedid = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                    String isok = cacheTid(cachedid, cachedid);
                    if (isok == null) {
                        log.debug("{} exist", cachedid);
                        return null;
                    }
                    trade.setTimestamp(millToNano(Long.valueOf(parse.read("$.[2].[1]").toString())));
                    BigDecimal volume = new BigDecimal(parse.read("$.[2].[2]").toString());
                    trade.setVolume(volume.abs());
                    trade.setPrice(new BigDecimal(parse.read("$.[2].[3]").toString()));
                    trade.setSide(volume.compareTo(BigDecimal.ZERO) < 0 ? "sell" : "buy");
                    objects.add(trade);
                    log.debug("Bitfinex trade {}", trade);
                } else if ("te".equals(parse.read("$.[1]"))) {
                    return null;
                } else {
                    //after open websocket
                    //如果外层数组第二列不是tu te的话，有可能是第一次发送一堆交易信息过来
                    //尝试将read转换为数组，如果转换后数组为null 或者 长度为0 跳过
                    JSONArray jsonArray = JSONArray.parseArray(parse.read("$.[1]").toString());
                    if (jsonArray == null || jsonArray.size() == 0) {
                        return null;
                    }
                    for (Object object : jsonArray) {
                        JSONArray o = (JSONArray) object;
                        Trade trade = new Trade(ExchangeConstant.Bitfinex, coin, unit);
                        trade.setTradeId(o.getString(0));
                        trade.setTimestamp(millToNano(o.getLongValue(1)));
                        String tid = String.format("%s_%s", ExchangeConstant.Bitfinex, trade.getTradeId());
                        String isok = cacheTid(tid, tid);
                        if (isok == null) {
                            log.debug("{} exist", tid);
                            continue;
                        }
                        BigDecimal volume = o.getBigDecimal(2);
                        trade.setVolume(volume.abs());
                        trade.setPrice(o.getBigDecimal(3));
                        trade.setSide(volume.compareTo(BigDecimal.ZERO) < 0 ? "sell" : "buy");
                        objects.add(trade);
                        log.debug("Bitfinex trade {}", trade);
                    }
                    ;
                }
            } else if (sub4 != null) {
                // 获得candle的map中相应的chanId的key（其中包含symbol）；
                String symbol = sub4.getKey().substring(10).toUpperCase();
                Matcher matcher = PATTERN.matcher(symbol);
                String unit;
                String coin;
                if (!matcher.find()) {
                    return null;
                }
                unit = matcher.group();
                coin = matcher.replaceAll("");
                // 因外层都会判断为JSONArray，所以取它内层的数据进行判断；
                List ls = JSONObject.parseArray(parse.read("$.[1]").toString());
                Object obj = ls.get(0);
                // 如果是不是JSONArray类型，说明是之后发送的单条的数据；
                if (!(obj instanceof JSONArray)) {
                    KLine kLine = getkLineByObj(parse, unit, coin);
                    kLine.setMeasurement("kline_1D");
                    objects.add(kLine);
                } else {
                    // 如果是JSONArray类型，说明是第一次发送来的大量数据；
                    List<KLine> klineList = new ArrayList<>();
                    ls.forEach(l -> {
                        KLine kLine = getkLineByArray(unit, coin, l);
                        kLine.setMeasurement("kline_1D");
                        klineList.add(kLine);
                        log.debug("Bitfinex 1dkline {}", kLine);
                    });
                    objects.addAll(klineList);
                }
            }
        }
        return objects;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitfinex;
    }

    private KLine getkLineByArray(String unit, String coin, Object l) {
        JSONArray list = JSONObject.parseArray(l.toString());
        KLine kLine = new KLine(ExchangeConstant.Bitfinex, coin, unit);
        kLine.setOpen(new BigDecimal(list.get(1).toString()));
        kLine.setClose(new BigDecimal(list.get(2).toString()));
        kLine.setHigh(new BigDecimal(list.get(3).toString()));
        kLine.setLow(new BigDecimal(list.get(4).toString()));
        kLine.setVolume(new BigDecimal(list.get(5).toString()));
        kLine.setTimestamp(Long.valueOf(list.get(0).toString()));
        return kLine;
    }

    private KLine getkLineByObj(DocumentContext parse, String unit, String coin) {
        long time = (long) parse.read("$.[1].[0]");
        BigDecimal open = new BigDecimal(parse.read("$.[1].[1]").toString());
        BigDecimal close = new BigDecimal(parse.read("$.[1].[2]").toString());
        BigDecimal high = new BigDecimal(parse.read("$.[1].[3]").toString());
        BigDecimal low = new BigDecimal(parse.read("$.[1].[4]").toString());
        BigDecimal vol = new BigDecimal(parse.read("$.[1].[5]").toString());
        KLine kLine = new KLine(ExchangeConstant.Bitfinex, coin, unit);
        kLine.setOpen(open);
        kLine.setClose(close);
        kLine.setHigh(high);
        kLine.setLow(low);
        kLine.setVolume(vol);
        kLine.setTimestamp(time);
        return kLine;
    }

    /**
     * 订阅行情
     */
    @Data
    static class MarketSubscibe {
        private String event = "subscribe";
        private String channel = "ticker";
        private int chanId;
        private String pair;
    }

    /**
     * 订阅K线
     */
    //{"event":"subscribe","channel":"candles","key":"trade:1m:tBTCUSD"}
    @Data
    static class KlineSubscribe1m {
        private String event = "subscribe";
        private String channel = "candles";
        private int chanId;
        private String key = "trade:1m:t";
    }

    /**
     * 订阅K线
     */
    //{"event":"subscribe","channel":"candles","key":"trade:1m:tBTCUSD"}
    @Data
    static class KlineSubscribe1D {
        private String event = "subscribe";
        private String channel = "candles";
        private int chanId;
        private String key = "trade:1D:t";
    }

    /**
     * 订阅交易
     */
    //订阅实例： {"event":"subscribe","pair":"BTCUSD","channel":"trades"}
    //第一次返回实例：{"event":"subscribed","channel":"trades","chanId":2,"symbol":"tBTCUSD","pair":"BTCUSD"}
    @Data
    static class TradeSubscribe {
        private String event = "subscribe";
        private String channel = "trades";
        private int chanId;
        private String pair;
    }

}
