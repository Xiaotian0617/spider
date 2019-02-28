package com.al.dbspider.base;

import com.al.dbspider.base.api.Bigone;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-21
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bigone")
public class BigoneExchange extends BaseRest {
    public static Map<String, BigonePairs> TRADE_PAIRS = new HashMap<>();//tradepair:onlykey

    @Autowired
    Bigone bigone;

    @Override
    protected void init() {


    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getAllTicker, 2, 1, TimeUnit.SECONDS);
//        schedule.scheduleWithFixedDelay(getKLine, 2, 8, TimeUnit.SECONDS);
//        schedule.scheduleWithFixedDelay(getAllTicker, 2, 3, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {
        log.info("初始化 {}", this.getClass().getSimpleName());
        JSONArray datas = getPairsData();
        initTradePair(datas);
        log.info("交易对数量 {}", TRADE_PAIRS.size());
    }


    /**
     * 获取全部行情
     */
    private Runnable getAllTicker = () -> {
        try {
            JSONArray datas = getMarketData();
            List<OnlyKey> markets = getMarkets(datas);
            influxDbMapper.postData(markets);
            log.trace("Bigone发送数据中心成功，数据为：{}", markets);
        } catch (Exception e) {
            log.error("bigone 解析或保存失败!" + e.getMessage(), e);
        }
    };

    private JSONArray getPairsData() {
        String body = null;
        try {
            body = bigone.getPairs().execute().body();
        } catch (IOException e) {
            log.error("bigone 获取行情失败!" + e.getMessage(), e);
        }
        JSONObject jsonObject = JSONObject.parseObject(body);
        return jsonObject.getJSONArray("data");
    }

    private JSONArray getMarketData() {
        String body = null;
        try {
            body = bigone.ticker().execute().body();
        } catch (IOException e) {
            log.error("bigone 获取行情失败!" + e.getMessage(), e);
        }
        JSONObject jsonObject = JSONObject.parseObject(body);
        return jsonObject.getJSONArray("data");
    }

    private Runnable getKLine = () -> {
        TRADE_PAIRS.entrySet().forEach(o -> {
            //TODO bigone kline
        });
    };

    private List<OnlyKey> getMarkets(JSONArray datas) {
        ArrayList<OnlyKey> markets = Lists.newArrayList();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String uuid = data.getString("market_uuid");
            BigonePairs tradePair = TRADE_PAIRS.get(uuid);
            Market market = new Market(ExchangeConstant.Bigone, tradePair.getSymbol(), tradePair.getUnit());
            market.setLast(data.getBigDecimal("close"));
            market.setOpen(data.getBigDecimal("open"));
            market.setClose(data.getBigDecimal("close"));
            market.setHigh(data.getBigDecimal("high"));
            market.setLow(data.getBigDecimal("low"));
            market.setVolume(data.getBigDecimal("volume"));
            market.setAsk(data.getJSONObject("ask").getBigDecimal("price"));
            market.setBid(data.getJSONObject("bid").getBigDecimal("price"));
            market.setTimestamp(System.currentTimeMillis());
            markets.add(market);
            log.debug("BigoneExchange {}", market);
        }
        return markets;
    }

    private void initTradePair(JSONArray datas) {
        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String symbol = data.getJSONObject("baseAsset").getString("symbol");
            String unit = data.getJSONObject("quoteAsset").getString("symbol");
            String uuid = data.getString("uuid");
            TRADE_PAIRS.put(uuid, new BigonePairs(symbol, unit, uuid));
        }
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bigone;
    }

    @Data
    class BigonePairs {
        /**
         * base symbol
         */
        private String symbol;
        /**
         * quote symbol
         */
        private String unit;

        private String uuid;

        public BigonePairs(String symbol, String unit, String uuid) {
            this.symbol = symbol;
            this.unit = unit;
            this.uuid = uuid;
        }
    }

}
