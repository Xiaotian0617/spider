package com.al.dbspider.base;

import com.al.dbspider.base.api.Zb;
import com.al.dbspider.dao.domain.Market;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * zb 交易所 rest 接口
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-20
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.zb")
@Lazy
public class ZbExchange extends BaseRest {
    public static Set<TradePair> TRADE_PAIRS = Sets.newHashSet();
    private BigDecimal period;
    private ForkJoinPool forkJoinPool;

    @Autowired
    private Zb zb;

    @Override
    protected void init() {

    }


    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleWithFixedDelay(ticker, 2, period.intValue(), TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {
        log.info("初始化{}", ExchangeConstant.Zb);
        initAllTradePair();
        period = new BigDecimal(60).divide(new BigDecimal(1000), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(TRADE_PAIRS.size() + 20));//1000次/60s/ip/100交易对
        forkJoinPool = new ForkJoinPool(period.intValue());
        log.info("交易对数量{},执行周期{}", TRADE_PAIRS.size(), period);
    }


    /**
     * 获取全部行情
     */
    private Runnable ticker = () -> {
        log.info("{} 开始更新行情", ExchangeConstant.Zb);
        try {
            forkJoinPool.submit(() -> TRADE_PAIRS.parallelStream().forEach(this::getMarket));
        } catch (Exception e) {
            log.error("Zb数据解析失败，错误原因：{}", e);
        }
        log.info("{} 更新行情结束", ExchangeConstant.Zb);
    };

    private void getMarket(TradePair tradePair) {
        Response<String> response = execute(zb.ticker(tradePair.getTradePair()));
        if (!response.isSuccessful()) {
            log.error(ExchangeConstant.Zb + "获取行情状态码 code= " + response.code());
        }
        JSONObject body = JSONObject.parseObject(response.body());
        JSONObject ticker = body.getJSONObject("ticker");
        Market market = new Market(ExchangeConstant.Zb, tradePair.getSymbol(), tradePair.getUnit());
        market.setVolume(ticker.getBigDecimal("vol"));
        market.setLast(ticker.getBigDecimal("last"));
        market.setHigh(ticker.getBigDecimal("high"));
        market.setLow(ticker.getBigDecimal("low"));
        market.setBid(ticker.getBigDecimal("buy"));
        market.setAsk(ticker.getBigDecimal("sell"));
        market.setTimestamp(body.getLong("date"));
        log.debug("{} 更新 {} 行情 {}", ExchangeConstant.Zb, tradePair.getTradePair(), market);
        influxDbMapper.postData(market);
    }

    public Set<TradePair> allTradePair() {
        Response<String> response = execute(zb.allTradePairs());
        JSONObject jsonObject = JSONObject.parseObject(response.body());
        Set<TradePair> tradePairs = new HashSet<>();
        jsonObject.keySet().forEach(o -> tradePairs.add(new TradePair(o, "_")));
        return tradePairs;
    }

    public void initAllTradePair() {
        allTradePair().forEach(o -> TRADE_PAIRS.add(o));
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Zb;
    }
}
