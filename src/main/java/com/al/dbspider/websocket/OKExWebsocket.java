package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.base.api.OKEx;
import com.al.dbspider.dao.domain.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import okio.ByteString;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.okex", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.okex")
public class OKExWebsocket extends BaseWebsocket {

    // 币币行情
    private final static String SPOT_TICKER_SUB = "{'event':'addChannel','channel':'ok_sub_spot_%s_ticker'}";
    // kline
    private final static String SPOT_KLINE_SUB = "{'event':'addChannel','channel':'ok_sub_spot_%s_kline_%s'}";
    private final static String[] KLINE_TYPE = {"1min", "3min", "5min", "15min", "30min", "1hour", "2hour", "4hour", "6hour", "12hour", "day", "3day", "week"};
    // 交易信息
    private final static String SPOT_DEALS_SUB = "{'event':'addChannel','channel':'ok_sub_spot_%s_deals'}";

    private final static String SPOT_DEPTH_SUB = "{'event':'addChannel','channel':'ok_sub_spot_%s_depth_20'}";

    private final static String FUTURE_DEPTH_SUB = "{'event':'addChannel','channel':'ok_sub_futureusd_%s_depth_%s_20'}";


    // 合约行情
    private final static String FUTURE_TICKER_SUB = "{'event':'addChannel','channel':'ok_sub_futureusd_%s_ticker_%s'}";
    private final static String[] FUTURE_COINS = {"btc", "ltc", "eth", "etc", "bch", "xrp", "eos", "btg"};
    private final static String THIS_WEEK = "this_week";
    private final static String NEXT_WEEK = "next_week";
    private final static String QUARTER = "quarter";
    private final static String[] FUTURE_TYPES = {THIS_WEEK, NEXT_WEEK, QUARTER};
    private final static String FUTURE_KINLE_SUB = "{'event':'addChannel','channel':'ok_sub_futureusd_%s_kline_%s_%s'}";
    private final static String FUTURE_TRADE_SUB = "{'event':'addChannel','channel':'ok_sub_futureusd_%s_trade_%s'}";

    // 指数行情
    //{event:'addChannel',parameters:{"base":"f_usd_btc","binary":"1","product":"futures","quote":"usd","type":"index_ticker"}}
    private final static String INDEX_SUB_REQ = "{event:'addChannel',parameters:{\"base\":\"f_usd_%s\",\"binary\":\"1\",\"product\":\"futures\",\"quote\":\"usd\",\"type\":\"index_ticker\"}}";
    //{event:'addChannel',parameters:{"base":"f_usd_btc","binary":"1","period":"day","product":"futures","quote":"usd","type":"kline"}}
    private final static String INDEX_KINLE_SUB = "{event:'addChannel',parameters:{\"base\":\"f_usd_%s\",\"binary\":\"1\",\"period\":\"%s\",\"product\":\"futures\",\"quote\":\"usd\",\"type\":\"kline\"}}";


    // 取coin正则
    private final static Pattern PATTERN = Pattern.compile(".+(?:futureusd_|spot_)|(?:_ticker|_kline|_trade|_deals|_depth).+");
    //private static final Pattern PATTERN = Pattern.compile("ok_sub_(spot|futureusd)_(.+)_(ticker|depth|deals|trades|kline)_?(this_week|next_week|quarter)?_?(1min|1day)?");

    private static OKEx.Products products;
    @Autowired
    private OKEx okex;

    @Override
    public void ping() {
        log.info("okex ping");
        send("{'event':'ping'}");
    }

    @Override
    protected boolean isNeedPing() {
        return true;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Okex;
    }

    @Override
    public void subscribe() {
        try {
            if (products == null) {
                products();
            } else {
                sendSub();
            }
        } catch (Exception e) {
            log.error("Okex 订阅出错", e);
        }
    }

    private void sendSub() {
        StringBuilder klines_1m = new StringBuilder("[");
        StringBuilder klines_1d = new StringBuilder("[");
        StringBuilder tickers = new StringBuilder("[");
        StringBuilder deals = new StringBuilder("[");
        StringBuilder depths = new StringBuilder("[");
        StringBuilder indexs = new StringBuilder("[");
        for (Iterator<OKEx.Symbol> iterator = products.getData().iterator(); iterator.hasNext(); ) {
            OKEx.Symbol symbol = iterator.next();
            String s = symbol.getSymbol();
            klines_1m.append(String.format(SPOT_KLINE_SUB, s, "1min"));
            klines_1d.append(String.format(SPOT_KLINE_SUB, s, "day"));
            tickers.append(String.format(SPOT_TICKER_SUB, s));
            deals.append(String.format(SPOT_DEALS_SUB, s));
            depths.append(String.format(SPOT_DEPTH_SUB, s));
            indexs.append(String.format(INDEX_SUB_REQ,s));
            if (iterator.hasNext()) {
                klines_1m.append(",");
                klines_1d.append(",");
                tickers.append(",");
                deals.append(",");
                depths.append(",");
                indexs.append(",");
            }
        }
        klines_1m.append("]");
        klines_1d.append("]");
        tickers.append("]");
        deals.append("]");
        depths.append("]");
        indexs.append("]");
        //订阅一分钟K线信息
        if (kline) {
            webSocket.send(klines_1m.toString());
            //订阅一天K线信息
            webSocket.send(klines_1d.toString());
            log.debug("Okex subscribe kline {} {}", klines_1m, klines_1d);
        }
        if (market) {
            //订阅ticker信息
            log.debug("Okex subscribe market {}", tickers);
            webSocket.send(tickers.toString());
        }
        if (trade) {
            //订阅交易信息
            log.debug("Okex subscribe trade {}", deals);
            webSocket.send(deals.toString());
        }
        if (depth) {
            //订阅深度信息
            log.debug("Okex subscribe depth {}", depths);
            webSocket.send(depths.toString());
        }

        //指数数据只有期货的币种有，所以在订阅期货的时候也订阅指数数据
        for (String futureCoin : FUTURE_COINS) {
            for (String type : FUTURE_TYPES) {
                if (market) {
                    String format = String.format(FUTURE_TICKER_SUB, futureCoin, type);
                    log.debug("Okex subscribe futures market {}", format);
                    //订阅合约ticker
                    webSocket.send(format);
                }
                if (kline) {
                    //订阅合约K线
                    String minFormat = String.format(FUTURE_KINLE_SUB, futureCoin, type, "1min");
                    log.debug("Okex subscribe futures min kline {}", minFormat);
                    webSocket.send(minFormat);
                    String dayFormat = String.format(FUTURE_KINLE_SUB, futureCoin, type, "day");
                    log.debug("Okex subscribe futures day kline {}", dayFormat);
                    webSocket.send(dayFormat);
                }
                if (trade) {
                    //订阅合约 trade
                    String format = String.format(FUTURE_TRADE_SUB, futureCoin, type);
                    log.debug("Okex subscribe futures trade {}", format);
                    webSocket.send(format);
                }
                if (depth) {
                    //订阅合约 depth
                    String format = String.format(FUTURE_DEPTH_SUB, futureCoin, type);
                    log.debug("Okex subscribe futures depth {}", format);
                    webSocket.send(format);
                }
            }
            if (index){
                String indexTicker = String.format(INDEX_SUB_REQ,futureCoin);
                //订阅指数ticker
                webSocket.send(indexTicker);
                log.debug("Okex index ticker send {}",indexTicker);
                //指数分钟K
                String indexMinFormat = String.format(INDEX_KINLE_SUB,futureCoin,"1min");
                webSocket.send(indexMinFormat);
                log.debug("Okex index min kline send {}",indexMinFormat);
                //指数日K
                String indexDayFormat = String.format(INDEX_KINLE_SUB,futureCoin,"day");
                webSocket.send(indexDayFormat);
                log.debug("Okex index day kline send {}",indexDayFormat);
            }
        }


        //24小时后清空产品列表以便更新
        scheduler.schedule(() -> products = null, 24, TimeUnit.HOURS);
    }

    private void products() {
        try {
            products = okex.products().execute().body();
            if (products.getCode() == 0) {
                sendSub();
                return;
            }
            log.debug("p:{}", products);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        scheduler.schedule(this::products, 3, TimeUnit.MINUTES);
    }

    private String[] symbol(String channel) {
        return PATTERN.matcher(channel).replaceAll("").toUpperCase().split("_");
    }

    private String[] indexSymbol(String base){
        String[] result = new String[2];
        if (StringUtils.isEmpty(base)){
            log.error("okex index symbol error,message base is {}",base);
            return null;
        }
        String[] split = base.split("_");
        result[0] = split[2]+"FOKEX";
        result[1] = split[1];
        return result;
    }

    private String furturesymbol(String channel) {
        String symbol = PATTERN.matcher(channel).replaceAll("").toUpperCase();
        if (channel.contains("this_week")) {
            return symbol + "THISWEEK";
        } else if (channel.contains("next_week")) {
            return symbol + "NEXTWEEK";
        } else if (channel.contains("quarter")) {
            return symbol + "QUARTER";
        }
        return symbol.toUpperCase();
    }

    @Override
    public List<OnlyKey> onMessageInPool(String message) {
        if (message.contains("addChannel")) {
            boolean success = JsonPath.read(message, "$.[0].data.result");
            //TODO 判断是否成功
            return null;
        }
        if (message.contains("pong")) {
            log.info("okex pong");
            return null;
        }

        ArrayList<OnlyKey> objects = Lists.newArrayList();

        DocumentContext parse = JsonPath.parse(message);
        Map map = parse.read("$.[0]");

        // 保存价格指数
        if (message.contains("f_usd_")&&map.get("type")!=null){
            log.debug("okex index message {}",message);
            String[] tradePair = indexSymbol(parse.read("$.[0].base"));
            if (tradePair==null){
                return null;
            }
            String symbol = StringUtils.upperCase(tradePair[0]);
            String unit = StringUtils.upperCase(tradePair[1]);
            if (Objects.equals("index_ticker",map.get("type").toString())) {
                Map data = parse.read("$.[0].data");
                Market market = new Market(ExchangeConstant.Okex, symbol, unit);
                market.setLast(new BigDecimal(data.get("last").toString()));
                market.setHigh(new BigDecimal(data.get("high").toString()));
                market.setLow(new BigDecimal(data.get("low").toString()));
                market.setOpen(new BigDecimal(data.get("open").toString()));
                market.setClose(new BigDecimal(data.get("last").toString()));
                market.setChange(new BigDecimal(data.get("changePercent").toString()));
                market.setTimestamp(System.currentTimeMillis());
                objects.add(market);
                log.debug("Okex index market {}", market);
                return objects;
            }
            if (Objects.equals("kline",map.get("type").toString())){
                List<List> datas = parse.read("$.[0].data");
                List<KLine> klines = datas.stream().map(line -> {
                    KLine kLine = getKLine(symbol, line, "USD");
                    if (Objects.equals("day",map.get("period").toString())){
                        kLine.setMeasurement("kline_1D");
                    }
                    return kLine;
                }).collect(Collectors.toList());
                objects.addAll(klines);
                log.debug("Okex index kline {}", klines);
                return objects;
            }
        }

        //期货数据
        if (message.contains("_futureusd")) {
            String channel = parse.read("$.[0].channel");
            String symbol = furturesymbol(channel);
            if (channel.contains("_ticker_")) {
                Map data = parse.read("$.[0].data");
                Market market = new Market(ExchangeConstant.Okex, symbol, "USD");
                market.setLast(new BigDecimal(data.get("last").toString()));
                market.setVolume(new BigDecimal(data.get("vol").toString()));
                market.setHigh(new BigDecimal(data.get("high").toString()));
                market.setLow(new BigDecimal(data.get("low").toString()));
                market.setBid(new BigDecimal(data.get("buy").toString()));
                market.setAsk(new BigDecimal(data.get("sell").toString()));
                market.setTimestamp(System.currentTimeMillis());
                objects.add(market);
                log.debug("Okex futures market {}", market);
                return objects;
            }

            if (channel.contains("_1min")) {
                List<List> datas = parse.read("$.[0].data");
                List<KLine> klines = datas.stream().map(line -> {
                    KLine kLine = getKLine(symbol, line, "USD");
                    log.debug("Okex futures 1mkline {}", kLine);
                    return kLine;
                }).collect(Collectors.toList());
                objects.addAll(klines);
                return objects;
            }
            if (channel.contains("_day")) {
                List<List> datas = parse.read("$.[0].data");
                List<KLine> klines = datas.stream().map(line -> {
                    KLine kLine = getKLine(symbol, line, "USD");
                    kLine.setMeasurement("kline_1D");
                    log.debug("Okex futures 1dkline {}", kLine);
                    return kLine;
                }).collect(Collectors.toList());
                objects.addAll(klines);
                return objects;
            }

            if (channel.contains("_trade_")) {
                List<List> datas = parse.read("$.[0].data");
                ArrayList<Trade> trades = new ArrayList<>();
                int timeInterval = 0;
                for (List data : datas) {
                    Trade trade = new Trade(ExchangeConstant.Okex, symbol, "USD");
                    trade.setTradeId(data.get(0).toString());
                    String cachedId = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                    String isOK = cacheTid(cachedId, trade.getTradeId());
                    if (isOK == null) {
                        log.debug("{} exist", cachedId);
                        return null;
                    }
                    trade.setPrice(new BigDecimal(data.get(1).toString()));
                    trade.setVolume(new BigDecimal(data.get(2).toString()));
                    trade.setSide("ask".equals(data.get(4).toString()) ? "sell" : "buy");
                    trade.setTimestamp(millToNano(toTs(data.get(3).toString(), timeInterval++)));
                    trades.add(trade);
                    log.debug("Okex futures trade {}", trade);
                }
                realTradeList(trades, ExchangeConstant.Okex, getHundredBuyAndSellFirstMap());
                objects.addAll(trades);
                return objects;
            }

            //深度这里没有区分其期货还是现货，在方法内部做了区分
            if (message.contains("_depth_")) {
                List<DepthDTO> depthDTOS = getJsonArrayByJob(message);
                log.debug("Okex futures depth {}", JSON.toJSONString(depthDTOS));
                objects.addAll(depthDTOS);
                getBuyAndSellFirst(depthDTOS, 0, depthDTOS.size() - 1);
            }

            return objects;
        }

        // 保存币币行情
        if (message.contains("_ticker")) {
            String[] tradePair = symbol(parse.read("$.[0].channel"));
            Map data = parse.read("$.[0].data");
            String symbol = StringUtils.upperCase(tradePair[0]);
            String unit = StringUtils.upperCase(tradePair[1]);
            Market market = new Market(ExchangeConstant.Okex, symbol, unit);
            market.setLast(new BigDecimal(data.get("last").toString()));
            market.setVolume(new BigDecimal(data.get("vol").toString()));
            market.setHigh(new BigDecimal(data.get("dayHigh").toString()));
            market.setLow(new BigDecimal(data.get("dayLow").toString()));
            market.setBid(new BigDecimal(data.get("buy").toString()));
            market.setChange(new BigDecimal(data.get("change").toString()));
            market.setAsk(new BigDecimal(data.get("sell").toString()));
            market.setTimestamp(Long.valueOf(data.get("timestamp").toString()));
            objects.add(market);
            log.debug("Okex market {}", market);
            return objects;
        }

        if (message.contains("_kline_1min")) {
            String channel = parse.read("$.[0].channel");
            List<List> data = parse.read("$.[0].data");
            List first = data.get(0);
            String[] tradePair = symbol(channel);
            String symbol = StringUtils.upperCase(tradePair[0]);
            String unit = StringUtils.upperCase(tradePair[1]);
            KLine kLine = getKLine(symbol, first, unit);
            objects.add(kLine);
            log.debug("Okex 1mkline {}", kLine);
            return objects;
        }

        if (message.contains("_kline_day")) {
            String channel = parse.read("$.[0].channel");
            List<List> data = parse.read("$.[0].data");
            List first = data.get(0);
            String[] tradePair = symbol(channel);
            String symbol = StringUtils.upperCase(tradePair[0]);
            String unit = StringUtils.upperCase(tradePair[1]);
            KLine kLine = getKLine(symbol, first, unit);
            kLine.setMeasurement("kline_1D");
            objects.add(kLine);
            log.debug("Okex 1dkline {}", kLine);
            return objects;
        }

        if (message.contains("_deals")) {
            String channel = parse.read("$.[0].channel");
            List<List> datas = parse.read("$.[0].data");
            ArrayList<Trade> trades = new ArrayList<>();
            String[] symbols = symbol(channel);
            String symbol = symbols[0];
            String unit = symbols[1];
            int timeInterval = 0;
            for (List data : datas) {
                Trade trade = new Trade(ExchangeConstant.Okex, symbol, unit);
                trade.setTradeId(data.get(0).toString());
                String cachedId = String.format("%s_%s", trade.getOnlyKey(), trade.getTradeId());
                String isOK = cacheTid(cachedId, trade.getTradeId());
                if (isOK == null) {
                    log.debug("{} exist", cachedId);
                    return null;
                }
                trade.setPrice(new BigDecimal(data.get(1).toString()));
                trade.setVolume(new BigDecimal(data.get(2).toString()));
                trade.setSide("ask".equals(data.get(4).toString()) ? "sell" : "buy");
                trade.setTimestamp(millToNano(toTs(data.get(3).toString(), timeInterval++)));
                objects.add(trade);
                trades.add(trade);
                log.debug("Okex trade {}", trade);
            }
            realTradeList(trades, ExchangeConstant.Okex, getHundredBuyAndSellFirstMap());
        }

        //深度这里没有区分其期货还是现货，在方法内部做了区分
        if (message.contains("_depth_")) {
            List<DepthDTO> depthDTOS = getJsonArrayByJob(message);
            log.debug("Okex spot depth {}", JSON.toJSONString(depthDTOS));
            objects.addAll(depthDTOS);
            getBuyAndSellFirst(depthDTOS, 0, depthDTOS.size() - 1);
        }
        return objects;
    }

    @Override
    protected List<OnlyKey> onMessageInPool(ByteString message) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.toByteArray());
             Deflate64CompressorInputStream compresser = new Deflate64CompressorInputStream(byteArrayInputStream)) {
            byte[] buffer = new byte[256];
            int n;
            while ((n = compresser.read(buffer)) >= 0) {
                byteArrayOutputStream.write(buffer, 0, n);
            }
            return onMessageInPool(byteArrayOutputStream.toString());
        } catch (IOException e) {
            log.error("{} 解析小心出错 ", this.exchangeName);
        }
        return null;
    }

    private List<DepthDTO> getJsonArrayByJob(String str) {
        JSONArray array = JSONArray.parseArray(str);
        if (array == null || array.size() == 0) {
            return new ArrayList<>();
        }
        JSONObject jsonObject = array.getJSONObject(0);
        String channel = jsonObject.getString("channel");
        String symbol;
        String unit;
        List<DepthDTO.PriceLevel> asks;
        List<DepthDTO.PriceLevel> bids;
        Boolean isSpot;
        if (channel.contains("futureusd")) {
            symbol = furturesymbol(channel);
            unit = "USD";
            isSpot = false;
        } else {
            String[] symbols = symbol(channel);
            symbol = StringUtils.upperCase(symbols[0]);
            unit = StringUtils.upperCase(symbols[1]);
            isSpot = true;
        }
        JSONArray result = new JSONArray();
        JSONObject tick = new JSONObject();
        asks = jsonObject.getJSONObject("data").getJSONArray("asks").stream().map((Object ask) -> getPriceLevel(ask, isSpot)).collect(Collectors.toList());
        bids = jsonObject.getJSONObject("data").getJSONArray("bids").stream().map((Object bid) -> getPriceLevel(bid, isSpot)).collect(Collectors.toList());
        jsonObject.put("onlyKey", "Okex_" + symbol + "_" + unit);
        jsonObject.put("timestamp", jsonObject.getJSONObject("data").getLongValue("timestamp"));
        jsonObject.put("type", 0);
        tick.put("asks", asks);
        tick.put("bids", bids);
        jsonObject.put("tick", tick);
        result.add(jsonObject);
        return result.toJavaList(DepthDTO.class);
    }

    @Override
    public DepthDTO.PriceLevel getPriceLevel(Object obj, Boolean isSpot) {
        //由于期货和现货的返回结果不同，所以这里做区分
        JSONArray array = JSON.parseArray(obj.toString());
        DepthDTO.PriceLevel priceLevel = new DepthDTO.PriceLevel();
        if (isSpot) {
            priceLevel.setCount(array.getBigDecimal(1));
            priceLevel.setPrice(array.getBigDecimal(0));
        } else {
            priceLevel.setCount(array.getBigDecimal(1));
            priceLevel.setPrice(array.getBigDecimal(0));
        }
        return priceLevel;
    }

    private KLine getKLine(String symbol, List line, String usd) {
        KLine kLine = new KLine(ExchangeConstant.Okex, symbol, usd);
        kLine.setOpen(new BigDecimal(line.get(1).toString()));
        kLine.setClose(new BigDecimal(line.get(4).toString()));
        kLine.setHigh(new BigDecimal(line.get(2).toString()));
        kLine.setLow(new BigDecimal(line.get(3).toString()));
        kLine.setVolume(new BigDecimal(line.get(5).toString()));
        kLine.setTimestamp(Long.valueOf(line.get(0).toString()));
        return kLine;
    }

    private static long toTs(String time, int interval) {
        return LocalDate.now()
                .atTime(LocalTime.parse(time))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .plusMillis(interval)
                .toEpochMilli();
    }


}
