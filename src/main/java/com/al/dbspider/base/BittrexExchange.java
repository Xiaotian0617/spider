package com.al.dbspider.base;

import com.al.dbspider.base.api.Bittrex;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bittrex")
public class BittrexExchange extends BaseRest {
    private static JsonPath JSON_PATH;
    @Autowired
    private Bittrex bittrex;

    @Override
    protected void init() {
        JSON_PATH = JsonPath.compile("$.result.*.Summary.['MarketName','Last','Volume','TimeStamp']");
    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {

        // 每30s抓一次交易信息
        schedule.scheduleAtFixedRate(() -> {
            try {
                String response = bittrex.ticker().execute().body();
                // 解析抓取到的结果，取得需要的数据
                List<Map> resp = JSON_PATH.read(response);
                // 创建一个用于批量保存的List
                List<OnlyKey> markets = new ArrayList<>();
                // 遍历储存结果的Map，格式化需要的几个数据
                resp.forEach(map -> {
                    // 币种和单位
                    String[] marketName = map.get("MarketName").toString().split("-");
                    String coin = marketName[1];
                    String unit = marketName[0];
                    // 最后价格
                    BigDecimal price = new BigDecimal(map.get("Last").toString());
                    // 交易量
                    BigDecimal vol = new BigDecimal(map.get("Volume").toString());
                    // 时间
                    String timeStamp = map.get("TimeStamp").toString().replace("T", " ");
                    long time = Timestamp.valueOf(timeStamp).getTime();
                    // 交易所名称
                    // 利用以上参数创建Market对象，并存入List集合
                    Market market = new Market(ExchangeConstant.Bittrex, coin, unit, price, vol, time);
                    markets.add(market);
                    log.debug("{} {}", ExchangeConstant.Bittrex, market);
                });
                // 批量保存Market数据
                influxDbMapper.postData(markets);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }, 2, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    public static void main(String[] args) {
        new BittrexExchange().start();
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bittrex;
    }
}
