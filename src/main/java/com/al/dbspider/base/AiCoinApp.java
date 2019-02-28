package com.al.dbspider.base;

import com.al.dbspider.base.api.AiCoin;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import com.al.dbspider.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.al.bcoin.AiCoin.AiCoinApi;

@Slf4j
@Component
@ConfigurationProperties("rest.aicoin")
public class AiCoinApp extends BaseRest {

    @Autowired
    private AiCoin aiCoin;

    @Autowired
    private InfluxDbMapper influxDbMapper;

    private ScheduledExecutorService scheduler;
    private ScheduledExecutorService tickerScheduler;

    private Map<String, Coin> pairMap = new HashMap<>();

    private Map<String, String> checkPair = new HashMap<String, String>() {{
        put("Bitfinex_YOYO_USD", "Bitfinex_YYW_USD");
        put("Bitfinex_YOYO_ETH", "Bitfinex_YYW_ETH");
        put("Bitfinex_YOYO_BTC", "Bitfinex_YYW_BTC");
        put("Bitfinex_SPANK_USD", "Bitfinex_SPK_USD");
        put("Bitfinex_SPANK_ETH", "Bitfinex_SPK_ETH");
        put("Bitfinex_SPANK_BTC", "Bitfinex_SPK_BTC");
        put("Bitfinex_QTUM_USD", "Bitfinex_QTM_USD");
        put("Bitfinex_QTUM_ETH", "Bitfinex_QTM_ETH");
        put("Bitfinex_QTUM_BTC", "Bitfinex_QTM_BTC");
        put("Bitfinex_QASH_USD", "Bitfinex_QSH_USD");
        put("Bitfinex_QASH_ETH", "Bitfinex_QSH_ETH");
        put("Bitfinex_QASH_BTC", "Bitfinex_QSH_BTC");
        put("Bitfinex_MANA_USD", "Bitfinex_MNA_USD");
        put("Bitfinex_MANA_ETH", "Bitfinex_MNA_ETH");
        put("Bitfinex_MANA_BTC", "Bitfinex_MNA_BTC");
        put("Bitfinex_IOTA_USD", "Bitfinex_IOT_USD");
        put("Bitfinex_IOTA_EUR", "Bitfinex_IOT_EUR");
        put("Bitfinex_IOTA_ETH", "Bitfinex_IOT_ETH");
        put("Bitfinex_IOTA_BTC", "Bitfinex_IOT_BTC");
        put("Bitfinex_DATA_USD", "Bitfinex_DAT_USD");
        put("Bitfinex_DATA_ETH", "Bitfinex_DAT_ETH");
        put("Bitfinex_DATA_BTC", "Bitfinex_DAT_BTC");
        put("Bitfinex_DASH_USD", "Bitfinex_DSH_USD");
        put("Bitfinex_DASH_BTC", "Bitfinex_DSH_BTC");
    }};

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        this.scheduler = schedule;
        //tickerScheduler = Executors.newScheduledThreadPool(20);
        //scheduler.schedule(this::delayStart, 0, TimeUnit.MINUTES);
        com.al.bcoin.AiCoin.KlineListener listener = (coin, kline) -> {
            List<KLine> kline_1D = kline.stream().map(KLine::new).map(myKLine -> {
                //获取Okex期货K线
                //getOkexFuturesKlineModel(coin, myKLine);
                //获取Okex现货K线
                //getOkexSpotKlineModel(coin, myKLine);
                //获取HuobiK线
                //getHuobiSpotKlineModel(coin, myKLine);
                //获取BinanceK线
                getBinanceSpotKlineModel(coin, myKLine);
                //获取BitfinexK线
                //getBitfinexSpotKlineModel(coin, myKLine);
                return myKLine;
            }).collect(Collectors.toList());
            if (kline_1D == null || kline_1D.size() == 0) {
                log.error("本次未返回K线数据！");
                return;
            }
            if (kline_1D.get(0).getSymbol().contains("this")) {
                log.error("返回的什么乱七八糟的！");
                return;
            }
            influxDbMapper.writeBeans(kline_1D);
            Long time = kline_1D.get(0).getTimestamp();
            log.warn("现在执行K线OnlyKey为{},补足时间大概在{}", kline_1D.get(0).getOnlyKey(), time);
            influxDbMapper.postData(kline_1D);
        };

        int oneday = 86400;
        // 获取aicoin所有K线信息，最后一个参数为filter, 传空表示取所有交易所交易对K线信息
        try {
            //Predicate<com.al.bcoin.AiCoin.Coin> filter =
            //com.al.bcoin.AiCoin.Coin coin = new com.al.bcoin.AiCoin.Coin();
            // Okex 期货 填入 okcoinfutures Huobi填入 huobipro 其他的小写交易所全称
            AiCoinApi.getKlineAll(oneday, listener, coin -> coin.getMid().contains("binance"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getBitfinexSpotKlineModel(com.al.bcoin.AiCoin.Coin coin, KLine myKLine) {
        myKLine.setMeasurement("kline_1D");
        myKLine.setExchange(StringUtils.toUpperCaseFirstOne(coin.getMid()));
        String symbol = coin.getCoin();
        myKLine.setTimestamp(myKLine.getTimestamp() * 1000L);
        myKLine.setSymbol(symbol.toUpperCase());
        myKLine.setUnit(coin.getCurrency().toUpperCase());
        String aicoinKey = myKLine.getExchange() + "_" + myKLine.getSymbol() + "_" + myKLine.getUnit();
        if (org.springframework.util.StringUtils.hasText(checkPair.get(aicoinKey))) {
            aicoinKey = checkPair.get(aicoinKey);
        }
        myKLine.setOnlyKey(aicoinKey);
        myKLine.setType("AIcoin");
    }

    private void getBinanceSpotKlineModel(com.al.bcoin.AiCoin.Coin coin, KLine myKLine) {
        myKLine.setMeasurement("kline_1D");
        myKLine.setExchange(StringUtils.toUpperCaseFirstOne(coin.getMid()));
        String symbol = coin.getCoin();
        myKLine.setTimestamp(myKLine.getTimestamp() * 1000L);
        myKLine.setSymbol(symbol.toUpperCase());
        myKLine.setUnit(coin.getCurrency().toUpperCase());
        String aicoinKey = myKLine.getExchange() + "_" + myKLine.getSymbol() + "_" + myKLine.getUnit();
        myKLine.setOnlyKey(aicoinKey);
        myKLine.setType("AIcoin");
    }

    private void getHuobiSpotKlineModel(com.al.bcoin.AiCoin.Coin coin, KLine myKLine) {
        myKLine.setMeasurement("kline_1D");
        myKLine.setExchange("Huobi");
        String symbol = coin.getCoin();
        myKLine.setTimestamp(myKLine.getTimestamp() * 1000L - 28800000L);
        myKLine.setSymbol(symbol.toUpperCase());
        myKLine.setUnit(coin.getCurrency().toUpperCase());
        String aicoinKey = myKLine.getExchange() + "_" + myKLine.getSymbol() + "_" + myKLine.getUnit();
        myKLine.setOnlyKey(aicoinKey);
        myKLine.setType("AIcoin");
    }

    private void getOkexSpotKlineModel(com.al.bcoin.AiCoin.Coin coin, KLine myKLine) {
        myKLine.setMeasurement("kline_1D");
        myKLine.setExchange("Okex");
        String symbol = coin.getCoin();
        myKLine.setTimestamp(myKLine.getTimestamp() * 1000L - 28800000L);
        myKLine.setSymbol(symbol.toUpperCase());
        myKLine.setUnit(coin.getCurrency().toUpperCase());
        String aicoinKey = myKLine.getExchange() + "_" + myKLine.getSymbol() + "_" + myKLine.getUnit();
        myKLine.setOnlyKey(aicoinKey);
        myKLine.setType("AIcoin");
    }

    private void getOkexFuturesKlineModel(com.al.bcoin.AiCoin.Coin coin, KLine myKLine) {
        myKLine.setMeasurement("kline_1D");
        //myKLine.setExchange(StringUtils.toUpperCaseFirstOne(coin.getMid()));
        myKLine.setExchange("Okex");
        String symbol = coin.getCoin() + "";
        if (coin.getCoin().contains("week") && !coin.getCoin().contains("nextweek")) {
            symbol = coin.getCoin().replace("week", "thisweek");
        }
        myKLine.setTimestamp(myKLine.getTimestamp() * 1000L - 28800000L);
        myKLine.setSymbol(symbol.toUpperCase());
        myKLine.setUnit(coin.getCurrency().toUpperCase());
        String aicoinKey = myKLine.getExchange() + "_" + myKLine.getSymbol() + "_" + myKLine.getUnit();
//                if (org.springframework.util.StringUtils.hasText(checkPair.get(aicoinKey))){
//                    aicoinKey = checkPair.get(aicoinKey);
//                }
        myKLine.setOnlyKey(aicoinKey);
        myKLine.setType("AIcoin");
    }

    private void delayStart() {
        List<Call<String>> allPairs = new ArrayList<>();
        try {
            allPairs = aiCoin.allPairs();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        for (Call<String> call : allPairs) {
            try {
                Response<String> response = call.execute();
                if (response.raw().protocol() != Protocol.HTTP_2) {
                    log.warn("aicoin不支持h2");
                }
                log.debug("Aicoin开始抓取！");
                Result result = JSONObject.parseObject(response.body(), Result.class);
                List<Coin> coins = result.coins.values().stream().filter(this::need).peek(this::savePairs).collect(Collectors.toList());
                //startKline(coins);
            } catch (Exception e) {
                log.error("获取出错" + e.getMessage(), e);
            }
        }
        //定时Ticker信息获取
        delayTicker();
    }

    private void delayTicker() {
        log.debug("开始获取ticker");
        int delay = 0;
        List<Call<String>> calls = new ArrayList<>();
        try {
            calls = aiCoin.allTickers();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        for (Call<String> call : calls) {
            tickerScheduler.scheduleAtFixedRate(() -> {
                JSONObject result = null;
                try {
                    Response<String> response = call.clone().execute();
                    String body = response.body();
                    log.info("Aicoin 返回数据 {} ", body);
                    result = JSONObject.parseObject(body);

//                    List<String> availableKeys = result.tickers.keySet().stream().filter(pairMap::containsKey).collect(Collectors.toList());
//                    Map<String, Ticker> tickers = result.getTickers();
//                    List<OnlyKey> markets = availableKeys.stream().map(s -> tickerMapper(tickers, s)).collect(Collectors.toList());
//                    influxDbMapper.postData(markets);
                } catch (Exception e) {
                    log.info(result.toJSONString());
                    log.error(e.getMessage(), e);
                }
            }, delay++ % 110, 120, TimeUnit.SECONDS);
        }
    }

    private Market tickerMapper(Map<String, Ticker> tickers, String key) {
        Coin coin = pairMap.get(key);
        Ticker ticker = tickers.get(key);
        log.debug("ticker,v:{}", key);
        Market market = new Market(coin.exchange, coin.coin, coin.currency);
        market.setType("Alcoin");
        market.setBid(ticker.buy);
        market.setClose(ticker.closing);
        market.setChange(ticker.degree);
        market.setHigh(ticker.hight);
        market.setLast(ticker.last);
        market.setLow(ticker.low);
        market.setOpen(ticker.opening);
        market.setAsk(ticker.sell);
        market.setTimestamp(ticker.timestamp * 1000);
        market.setVolume(ticker.vol);
        return market;
    }

    //保存交易对信息，key为aicoin的ticker信息json中的key对应
    private void savePairs(Coin coin) {
        if (coin.exchange == ExchangeConstant.Okex && coin.coin.contains("week") && !coin.coin.contains("nextweek")) {
            coin.coin = coin.coin.replace("week", "thisweek");
        }
        pairMap.put(coin.dbKey, coin);
    }

    private boolean need(Coin coin) {
        if (coin.mid.contains("huobipro")) {
            coin.exchange = ExchangeConstant.Huobi;
            return true;
        }
        if (coin.mid.contains("okcoinfutures")) {
            coin.exchange = ExchangeConstant.Okex;
            return true;
        }

        String firstLetter = coin.mid.substring(0, 1).toUpperCase();
        try {
            coin.exchange = ExchangeConstant.valueOf(firstLetter + coin.mid.substring(1));
        } catch (Exception e) {
            //不在枚举里
            log.debug("has no exchange {}", coin.mid);
            return false;
        }
        return true;
    }

    private void startKline(List<Coin> coins) {
        int delay = 0;
        for (Coin coin : coins) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String response = aiCoin.kline(coin.symbol).execute().body();
                    log.debug(response);
                    JSONArray data = JSONObject.parseObject(response).getJSONArray("data");
                    List<OnlyKey> klines = data.stream().map(o -> mapper((JSONArray) o, coin)).collect(Collectors.toList());
                    log.debug("wirte kline {}", coin);
                    influxDbMapper.postData(klines);
                } catch (Exception e) {
                    log.error("", e);
                }
            }, delay++, 500, TimeUnit.MINUTES);
        }
    }

    private KLine mapper(JSONArray k, Coin coin) {
        KLine kLine = new KLine(coin.exchange, coin.coin, coin.currency);
        kLine.setType("Alcoin");
        kLine.setTimestamp(k.getLong(0) * 1000);
        kLine.setOpen(k.getBigDecimal(1));
        kLine.setHigh(k.getBigDecimal(2));
        kLine.setLow(k.getBigDecimal(3));
        kLine.setClose(k.getBigDecimal(4));
        kLine.setVolume(k.getBigDecimal(5));
        return kLine;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.AiCoin;
    }

    @Data
    static class Result {
        Map<String, Coin> coins;
        Map<String, Ticker> tickers;
    }

    @Data
    static class Coin {
        String coin;
        String currency;
        String mid;
        String symbol;
        String dbKey;
        ExchangeConstant exchange;
    }

    @Data
    static class Ticker {
        BigDecimal buy;
        BigDecimal closing;
        BigDecimal degree;
        BigDecimal hight;
        BigDecimal last;
        BigDecimal low;
        BigDecimal incrSpeed;
        BigDecimal opening;
        BigDecimal sell;
        long timestamp;
        BigDecimal vol;
    }

}
