package com.al.dbspider.base;

import com.al.dbspider.base.api.CoinW;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.coinw")
public class CoinwExchange extends BaseRest {

    @Autowired
    private CoinW coinw;

    @Resource
    private InfluxDbMapper influxDbMapper;

    // 币种及其相应的编号
    private static Map<String, Integer> symbolMap = new HashMap<>();

    @Override
    protected void onStart() {
        getSymbols();
    }

    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {

        // 每24H抓取一次币种信息
        schedule.scheduleAtFixedRate(this::getSymbols, 0, 24, TimeUnit.HOURS);

        // 每15s抓取一次市场信息
        schedule.scheduleAtFixedRate(this::getMarket, 5, 15, TimeUnit.SECONDS);

        // 每1m抓取一次K线信息
        schedule.scheduleAtFixedRate(this::getKLine, 5, 60, TimeUnit.SECONDS);
    }

    /**
     * 抓取各个币种的K线信息
     */
    private void getKLine() {
        if (symbolMap.keySet().isEmpty()) {
            log.debug("币种信息还未初始化，等待其初始化。");
            getSymbols();
        }
        List<OnlyKey> kLines = new ArrayList<>();
        symbolMap.forEach((m, n) -> {
            try {
                CoinW.KLineInfo response = coinw.kline(n).execute().body();
                if (response.getPeriod().getData().size() == 0) {
                    return;
                }
                List<String> data = response.getPeriod().getData().get(0);
                Long time = Long.valueOf(data.get(0));
                KLine kLine = new KLine(ExchangeConstant.Coinw, m, "CNY");
                kLine.setTimestamp(time);
                kLine.setOpen(new BigDecimal(data.get(1)));
                kLine.setHigh(new BigDecimal(data.get(2)));
                kLine.setLow(new BigDecimal(data.get(3)));
                kLine.setClose(new BigDecimal(data.get(4)));
                kLine.setVolume(new BigDecimal(data.get(5)));
                kLines.add(kLine);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        log.debug("KLine列表>>>>>>>>：" + kLines);
        try {
            influxDbMapper.postData(kLines);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 抓取各个币种的市场信息
     */
    private void getMarket() {
        if (symbolMap.keySet().isEmpty()) {
            log.debug("币种信息还未初始化，等待其初始化。");
            getSymbols();
        }
        List<OnlyKey> markets = new ArrayList<>();
        symbolMap.forEach((m, n) -> {
            try {
                CoinW.MarketInfo response = coinw.market(n).execute().body();
                BigDecimal price = new BigDecimal(response.getPNew().get(0));
                BigDecimal vol = new BigDecimal(response.getVol().get(0));
                Market market = new Market(ExchangeConstant.Coinw, m, "CNY", price, vol, System.currentTimeMillis());
                log.debug("{} {}", ExchangeConstant.Coinw, market);
                markets.add(market);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        log.debug("Market列表>>>>>>>>：" + markets);
        try {
            influxDbMapper.postData(markets);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 抓取所有币种
     */
    public void getSymbols() {
        try {
            List<CoinW.Symbol> fMap = coinw.symbol().execute().body().getFMap();
            for (CoinW.Symbol f : fMap) {
                symbolMap.put(f.getFShortName2().toUpperCase(), f.getFid());
            }
            log.debug("Symbol的Map：" + symbolMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coinw;
    }
}
