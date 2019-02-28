package com.al.dbspider.utils;

import com.al.dbspider.base.api.TopCoin;
import com.al.dbspider.dao.domain.*;
import com.al.dbspider.websocket.OnlyKeyMessage;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * file:InfluxDbInitialize
 * <p>
 * InfluxDb的初始化方法
 *
 * @author 11:03  王楷
 * @author yangjunxiao
 * @version 11:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Service
public class InfluxDbMapper {
    @Autowired
    TopCoin topCoin;

    @Autowired
    KafkaSender kafkaSender;

    @Value("${kafka.topic.market}")
    private String marketTopic;

    @Value("${kafka.topic.marketcap}")
    private String marketCapTopic;

    @Value("${kafka.topic.kline}")
    private String klineTopic;

    @Value("${kafka.topic.trade}")
    private String tradeTopic;

    @Value("${kafka.topic.real-trade}")
    private String realTradeTopic;

    @Value("${kafka.topic.depth}")
    private String depthTopic;

    @Value("${kafka.topic.first-buy-sell}")
    private String firstBuySellTopic;

    @Value("${kafka.topic.long-short}")
    private String longShortTopic;


    @Value("${market.push:true}")
    private boolean push;


    public void setPush(boolean push) {
        this.push = push;
        log.info("{}推送服务", push ? "启用" : "禁用");
    }

    @Autowired
    private OkHttpClient.Builder okHttpClientBuilder;

    private InfluxDB influxDB;

    @Autowired
    private InfluxDBProperties properties;

    //@Resource
    //ThreadPoolExecutor executorService;

    @PostConstruct
    public InfluxDB getConnection() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory
                    .connect(properties.getUrl() + ":" + properties.getPort(), properties.getUserName(), properties.getPassword(), okHttpClientBuilder);
            log.debug("Using InfluxDB '{}' on '{}'", properties.getDataBase(), properties.getUrl());
            if (properties.isGzip()) {
                log.debug("Enabled gzip compression for HTTP requests");
                influxDB.enableGzip();
            }
        }
        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        influxDB.setDatabase(properties.getDataBase());
        return influxDB;
    }

    private static Callback empty = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            if (response.code() >= HttpStatus.SC_BAD_REQUEST) {
                log.error("POST 推送出错,{},{}", response, JSON.toJSONString(call));
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            log.error("POST 推送失败{}", t.getMessage());
        }

    };

    @Resource
    ScheduledExecutorService scheduledExecutorService;

    public void writeBeans(final List<?> beans) {
        if (beans.size() > 200) {
            int i1 = beans.size() / 200;
            int skipNum;
            int limitNum = 200;
            log.warn("本次总条数：{}", beans.size());
            for (int i = 1; i <= i1 + 1; i++) {
                skipNum = (i - 1) * limitNum;
                List<?> collect = beans.stream().skip(skipNum).limit(limitNum).collect(Collectors.toList());
                saveDataToDB(collect);
                log.warn("本次保存条数{}", collect.size());
            }
        } else {
            saveDataToDB(beans);
        }
    }

    private void saveDataToDB(List<?> beans) {
        scheduledExecutorService.submit(() -> {
            try {
                List<String> collect = beans.stream().map(PointExt::lineProtocal).collect(Collectors.toList());
                influxDB.write(collect);
            } catch (Exception e) {
                log.error("系统在写入InfluxDB时出错，请检查", e);
            }
        });
    }

    public void postData(List<? extends OnlyKey> marketDatas) {
        if (marketDatas.size() == 0) {
            return;
        }
        if (!push) {
            return;
        }
        log.trace("发送到Kafka的数据：{}", marketDatas);
        OnlyKey onlyKey = marketDatas.get(0);
        if (onlyKey instanceof Market) {
            kafkaSender.send(marketTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof MarketCap) {
            kafkaSender.send(marketCapTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof KLine) {
            kafkaSender.send(klineTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof Trade) {
            kafkaSender.send(tradeTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof DepthDTO) {
            kafkaSender.send(depthTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof RealTrade) {
            kafkaSender.send(realTradeTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof BuyAndSellFirstPO) {
            kafkaSender.send(firstBuySellTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
        if (onlyKey instanceof LongShortPO) {
            kafkaSender.send(longShortTopic, JSON.toJSONString(marketDatas), onlyKey.onlyKey());
            return;
        }
    }

    public void postData(OnlyKeyMessage marketDatas) {
        if (marketDatas.size() == 0) {
            return;
        }
        if (!push) {
            return;
        }
        log.trace("发送到Kafka的数据：{}", marketDatas);
        kafkaSender.send(marketDatas);
    }

    public void postData(OnlyKey onlyKey) {
        ArrayList<OnlyKey> objects = Lists.newArrayList(onlyKey);
        postData(objects);
    }


}
