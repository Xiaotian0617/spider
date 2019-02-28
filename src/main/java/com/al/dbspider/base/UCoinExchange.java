package com.al.dbspider.base;

import com.al.dbspider.base.api.UCoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 19:24  王楷
 * @version 19:24 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "rest.ucoin")
public class UCoinExchange extends BaseRest {
    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {

        schedule.scheduleAtFixedRate(runnable, 3, 15, TimeUnit.SECONDS);

    }

    @Override
    protected void onStart() {

    }

    @Autowired
    private UCoin uCoin;
    private Runnable runnable = () -> {

        // 创建一个用于批量保存的List
       /* List<OnlyKey> markets = new ArrayList<>();
        try {
            String body = uCoin.getTickerAll().execute().body();
            JSONArray jsonArray = JSONArray.parseArray(body);
            if (jsonArray != null) {
                jsonArray.forEach(o -> {
                    JSONObject jsonObject = (JSONObject) o;
                    String[] str = jsonObject.getString("symbol").split("_");
                    Market market = new Market(ExchangeConstant.Ucoin, str[0], str[1]);
                    market.setLast(jsonObject.getBigDecimal("price"));
                    market.setVolume(jsonObject.getBigDecimal("volume"));
                    market.setAmount(jsonObject.getBigDecimal("amount"));
                    market.setTimestamp(System.currentTimeMillis());
                    markets.add(market);
                });
                influxDbMapper.postData(markets);
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }*/

    };

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.UCoin;
    }
}
