package com.al.dbspider.base;

import com.al.dbspider.base.api.Bitstamp;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * trading-pairs-info/
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-10-22
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bitstamp")
public class BitstampExchange extends BaseRest {

    @Autowired
    private Bitstamp bitstamp;

    private Map<String, TradePair> symbolMap = new HashMap<>();


    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {
        initAllPairs();
    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getTicker, 15000, 1700, TimeUnit.MILLISECONDS);
    }

    private Runnable getTicker = () -> {
        symbolMap.forEach((urlSymbol, tradePair) -> {
            Response<String> execute = execute(bitstamp.ticker(urlSymbol));
            if (execute == null) {
                log.error("{} {} 未获取到行情", getExchangeName(), urlSymbol);
            } else {
                String body = execute.body();
                JSONObject marketJsonObject = JSON.parseObject(body);
                List<OnlyKey> onlyKeys = Lists.newArrayList();
                Market market = new Market(getExchangeName(), tradePair.getSymbol(), tradePair.getUnit());
                market.setLast(marketJsonObject.getBigDecimal("last"));
                market.setBid(marketJsonObject.getBigDecimal("bid"));
                market.setAsk(marketJsonObject.getBigDecimal("ask"));
                market.setHigh(marketJsonObject.getBigDecimal("high"));
                market.setLow(marketJsonObject.getBigDecimal("low"));
                market.setVolume(marketJsonObject.getBigDecimal("volume"));
                market.setOpen(marketJsonObject.getBigDecimal("open"));
                market.setTimestamp(marketJsonObject.getLong("timestamp"));
                onlyKeys.add(market);
                log.info("{} market {}", getExchangeName(), market);
                postData(onlyKeys);
            }
        });
    };

    @Override
    protected void onStart() {

    }

    public void initAllPairs() {
        Response<String> execute = execute(bitstamp.allPairs());
        String body = execute.body();
        if (StringUtils.isNotBlank(body)) {
            JSONArray tradePairJsonArray = JSON.parseArray(body);
            for (Object symbolObject : tradePairJsonArray) {
                JSONObject symbolJsonObject = (JSONObject) symbolObject;
                String urlSymbol = symbolJsonObject.getString("url_symbol");
                if ("Enabled".equalsIgnoreCase(symbolJsonObject.getString("trading"))) {
                    TradePair tradePair = new TradePair(symbolJsonObject.getString("name"), "/");
                    symbolMap.put(urlSymbol, tradePair);
                } else {
                    log.debug("{} {} not market", getExchangeName(), urlSymbol);
                }
            }
        }
    }


    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitstamp;
    }
}
