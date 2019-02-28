package com.al.dbspider.base;

import com.al.dbspider.base.api.BitFlyer;
import com.al.dbspider.dao.domain.Market;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 20:02  王楷
 * @version 20:02 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.bitflyer")
public class BitflyerExchange extends BaseRest {
    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    private static List<JSONObject> marketList;

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        //每12小时调用一次获取市场接口
        schedule.scheduleAtFixedRate(getAllMarketList, 0, 12, TimeUnit.HOURS);
        //每15秒调用一次获取市场接口
        schedule.scheduleAtFixedRate(getAllTicker, 20, 15, TimeUnit.SECONDS);
    }

    @Override
    protected void onStart() {

    }

    @Autowired
    private BitFlyer bitFlyer;
    private Runnable getAllMarketList = () -> {
        try {
            String str = bitFlyer.getMarketList().execute().body();
            JSONArray jsonArray = JSONArray.parseArray(str);
            marketList = jsonArray.toJavaList(JSONObject.class);
        } catch (Exception e) {
            log.error("BitFlyer 获取市场列表失败", e);
        }
    };

    private Runnable getAllTicker = () -> {
        marketList.forEach(marketInfo -> {
            String body = null;
            try {
                body = bitFlyer.getMarketAll(marketInfo.getString("product_code")).execute().body();
                JSONObject jsonObject = JSONObject.parseObject(body);
                String[] str = jsonObject.getString("product_code").split("_");
                if (str.length != 2) {
                    return;
                }
                Market market = new Market(ExchangeConstant.Bitflyer, str[0], str[1]);
                market.setTimestamp(LocalDateTime.parse(jsonObject.getString("timestamp")).toInstant(ZoneOffset.UTC).toEpochMilli());
                market.setBid(jsonObject.getBigDecimal("best_bid"));
                market.setAsk(jsonObject.getBigDecimal("best_ask"));
                market.setLast(jsonObject.getBigDecimal("ltp"));
                market.setVolume(jsonObject.getBigDecimal("volume"));
                influxDbMapper.postData(market);
                log.debug("{} {}", ExchangeConstant.Bitflyer, market);
                Thread.sleep(1000);
            } catch (IOException e) {
                log.error("BitFlyer 获取市场信息失败", e);
            } catch (InterruptedException e) {
                log.error("BitFlyer 线程休眠失败", e);
            } catch (Exception e) {
                log.error("BitFlyer 获取市场信息失败", e);
            }
        });
    };

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bitflyer;
    }
}