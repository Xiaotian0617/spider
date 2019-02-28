package com.al.dbspider.base;

import com.al.dbspider.base.api.Bitfinex;
import com.al.dbspider.dao.domain.LongShortPO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bitfinex")
public class BitfinexExchange extends BaseRest {

    private static final Pattern PATTERN = Pattern.compile("(ETH|BTC|BNB|USD|EUR|GBP|JPY|EOS)$");

    @Autowired
    private Bitfinex bitfinex;

    List<String> pairs = Arrays.asList("btcusd", "ltcusd", "ltcbtc", "ethusd", "ethbtc", "etcbtc", "etcusd", "rrtusd", "rrtbtc", "zecusd", "zecbtc", "xmrusd", "xmrbtc", "dshusd", "dshbtc", "btceur", "xrpusd", "xrpbtc", "iotusd", "iotbtc", "ioteth", "eosusd", "eosbtc", "eoseth", "sanusd", "sanbtc", "saneth", "omgusd", "omgbtc", "omgeth", "bchusd", "bchbtc", "bcheth", "neousd", "neobtc", "neoeth", "etpusd", "etpbtc", "etpeth", "qtmusd", "qtmbtc", "qtmeth", "avtusd", "avtbtc", "avteth", "edousd", "edobtc", "edoeth", "btgusd", "btgbtc", "datusd", "datbtc", "dateth", "qshusd", "qshbtc", "qsheth", "yywusd", "yywbtc", "yyweth", "gntusd", "gntbtc", "gnteth", "sntusd", "sntbtc", "snteth", "ioteur", "batusd", "batbtc", "bateth", "mnausd", "mnabtc", "mnaeth", "funusd", "funbtc", "funeth", "zrxusd", "zrxbtc", "zrxeth", "tnbusd", "tnbbtc", "tnbeth", "spkusd", "spkbtc", "spketh");

    /**
     * 勿执行 IO 操作
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        //每24小时调用一次获取市场接口
        schedule.scheduleAtFixedRate(getALLPairs, 0, 24, TimeUnit.HOURS);
        //每1分调用一次获取市场接口
        schedule.scheduleWithFixedDelay(getLongShort, 1, 3, TimeUnit.MINUTES);
    }

    /**
     * 获取所有交易对
     */
    private Runnable getALLPairs = () -> {
        getSymbols();
    };

    private void getSymbols() {
        try {
            String symbolsStr = bitfinex.symbols().execute().body();
            pairs = JSONObject.parseArray(symbolsStr).toJavaList(String.class);
        } catch (Exception e) {
            log.error("Bitfnex 获取交易对出错！使用默认交易对！" + e.getMessage(), e);
        }
    }

    /**
     * 获取多空占比
     */
    private Runnable getLongShort = () -> {
        pairs.forEach(pair -> {
            try {
                String tPair = "t" + pair.toUpperCase();
                String longBody = bitfinex.stats("pos.size", "1m", tPair, "long", "hist").execute().body();
                String shortBody = bitfinex.stats("pos.size", "1m", tPair, "short", "hist").execute().body();
                JSONArray longArrs = JSON.parseArray(longBody);
                JSONArray shortArrs = JSON.parseArray(shortBody);
                if (longArrs == null || shortArrs == null) {
                    return;
                }
                Map<Long, BigDecimal> longMap = getStatModelMap(longArrs);
                Map<Long, BigDecimal> shortMap = getStatModelMap(shortArrs);
                List<LongShortPO> longShortModels = new ArrayList<>(20);
                longMap.entrySet().forEach(map -> {
                    LongShortPO longShortModel = new LongShortPO();
                    longShortModel.setLastTime(map.getKey());
                    longShortModel.setLongAmount(map.getValue());
                    longShortModel.setShortAmount(shortMap.get(map.getKey()));
                    Matcher matcher = PATTERN.matcher(pair.toUpperCase());
                    if (!matcher.find()) {
                        return;
                    }
                    String unit = matcher.group();
                    String symbol = matcher.replaceAll("");
                    longShortModel.setOnlyKey(getExchangeName().name(), symbol, unit);
                    longShortModels.add(longShortModel);
                });
                log.debug("Bitfinex long short info:{}", JSON.toJSONString(longShortModels));
                influxDbMapper.postData(longShortModels);
                Thread.sleep(100);
            } catch (IOException e) {
                log.error("获取多空数据异常", e);
            } catch (Throwable e) {
                log.error("解析多空数据出错！", e);
            }
        });
    };

    private List<StatModel> getStatModelList(JSONArray array) {
        return array.stream().map(arr -> {
            JSONArray jsonArray = JSON.parseArray(arr.toString());
            StatModel statModel = new StatModel(jsonArray.getLong(0), jsonArray.getBigDecimal(1));
            return statModel;
        }).collect(Collectors.toList());
    }

    private Map<Long, BigDecimal> getStatModelMap(JSONArray array) {
        return getStatModelList(array).stream().collect(Collectors.toMap(StatModel::getLastTime, StatModel::getAmount));
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitfinex;
    }

    @Data
    class StatModel {
        Long lastTime;
        BigDecimal amount;

        public StatModel(Long lastTime, BigDecimal amount) {
            this.lastTime = lastTime;
            this.amount = amount;
        }
    }
}
