package com.al.dbspider.base;

import com.al.dbspider.base.api.Coin900;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.websocket.Coin900Websocket;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 16:16  王楷
 * @version 16:16 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.coin900")
public class Coin900Exchange extends BaseRest {

    @Autowired
    private Coin900 coin900;

    private ScheduledExecutorService scheduler;

    private ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * 初始化
     * 如果要使用retrofit, 可以在这里创建API接口实例
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        this.scheduler = schedule;
        initData();
    }

    private void initData() {
        try {
            if (Coin900Websocket.pairs.size() == 0) {
                log.error("Coin900交易对尚未获取完毕，3分钟后重试！");
                scheduler.schedule(this::initData, 1, TimeUnit.MINUTES);
            }
            if (Coin900Websocket.pairs.size() != 0) {
                getKline();
                return;
            }
        } catch (Throwable e) {
            log.error("Coin900数据获取异常，异常原因{}", e);
        }
    }

    private void getKline() {
        int delay = 0;
        //做TradeId的筛选是因为下方请求数据时，须传递TradeId才可查询
        List<Coin900Websocket.CoinInfo> list = Coin900Websocket.pairs.stream().filter(coinInfo -> {
            return !Objects.equals(null, coinInfo.getTradeId());
        }).collect(Collectors.toList());
        //TODO fix kline 接口
        //getKlineByPairs(list);
    }

    private void getKlineByPairs(List<Coin900Websocket.CoinInfo> coinInfos) {
        int delay = 0;
        for (Coin900Websocket.CoinInfo coinInfo : coinInfos) {
            scheduler.scheduleAtFixedRate(() -> pool.submit(() -> {
                try {
                    String str = coin900.getKlineByPair(coinInfo.getPair(), 768, 1, coinInfo.getTradeId()).execute().body();
                    if (str == null || str == "") {
                        log.debug("Coin返回数据为{},跳过本次解析", str);
                        return;
                    }
                    //目前币新的交易市场中只有三个市场 BTC ETH USDT 市场 所以暂取截取后三位判断币种和单位
                    String channels = coinInfo.getPair().toUpperCase();
                    String coin, unit;
                    if (channels.contains("USDT")) {
                        coin = channels.replace("USDT", "");
                        unit = "USDT";
                    } else if ("ETH,BTC".contains(channels.substring(channels.length() - 3))) {
                        unit = channels.substring(channels.length() - 3);
                        coin = channels.replace(unit, "");
                    } else {
                        return;
                    }
                    List<List> lists = JSONObject.parseObject(str).getJSONArray("k").toJavaList(List.class);
                    List<OnlyKey> klines = new ArrayList<OnlyKey>();
                    /**
                     * [
                     1516562040, 时间戳
                     0.0045612,  开
                     0.0045612,  收
                     0.0045612,  低
                     0.0045612,  高
                     0.0245
                     ]
                     */
                    lists.stream().forEach(list -> {
                        KLine kLine = new KLine(ExchangeConstant.Coin900, coin, unit);
                        kLine.setTimestamp(Long.valueOf(list.get(0).toString()) * 1000L);
                        kLine.setOpen(new BigDecimal(list.get(1).toString()));
                        kLine.setClose(new BigDecimal(list.get(4).toString()));
                        kLine.setHigh(new BigDecimal(list.get(2).toString()));
                        kLine.setLow(new BigDecimal(list.get(3).toString()));
                        kLine.setVolume(new BigDecimal(list.get(5).toString()));
                        klines.add(kLine);
                    });
                    influxDbMapper.postData(klines);
                } catch (Exception e) {
                    log.error("Coin900Exchange" + e.getMessage(), e);
                }
            }), delay++ % 30, 30, TimeUnit.SECONDS);
        }
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Coin900;
    }
}
