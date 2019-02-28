package com.al.dbspider.base;

import com.al.dbspider.base.api.Quintar;
import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.dao.domain.Pairs;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 20:16  王楷
 * @version 20:16 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.ucoin")
public class QuintarExchange extends BaseRest {

    @Autowired
    private Quintar quintar;

    public static JSONObject tickerAll = new JSONObject();

    public static Set<String> pairsStatic = new HashSet<>();

    /**
     * 勿执行 IO 操作
     */
    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
        schedule.scheduleAtFixedRate(getAllTicker, 0, 1, TimeUnit.DAYS);
        //获得所有的交易所和交易所下的交易对
        schedule.scheduleWithFixedDelay(getMarketCoinList, 0, 1, TimeUnit.HOURS);
        //schedule.scheduleWithFixedDelay(getKlineByOnlyKey,1,15,TimeUnit.MINUTES);
    }

    /**
     * 获得所有交易所和交易所下的交易对的K线
     */
    private Runnable getKlineByOnlyKey = () -> {
        //增加一个List供批量保存
        List<OnlyKey> kLines = new ArrayList<>(1000000);
        //用来记录本次保存的记录数
        log.debug("金塔所有的ONLYKEY：" + pairsStatic.size());
        try {
            pairsStatic.forEach(key -> {
                String[] strs = key.split("_");
                if (strs.length != 3) {
                    return;
                }
                String exchange = strs[0].replace(".", "").toLowerCase();
                String pair = exchange + "_";
                //金塔的逻辑为如果为法定货币的话就只需传交易所加币种即可
                if ("USD_CNY_KRW_USDT_JPY".contains(strs[2])) {
                    pair = pair + strs[1];
                } else {
                    pair = pair + strs[1] + strs[2];
                }
                try {
                    String body = quintar.getKlineByKey(pair.toLowerCase(), 60).execute().body();
                    JSONArray klineList = JSONArray.parseArray(body);
                    if (klineList != null) {
                        klineList.forEach(o -> {
                            JSONArray list = (JSONArray) o;
                            ExchangeConstant exchangeConstant = ExchangeConstant.valueExist(exchange);
                            if (exchangeConstant == null) {
                                return;
                            }
                            KLine kLine = new KLine(exchangeConstant, strs[1], strs[2]);
                            kLine.setTimestamp(list.getTimestamp(0).getTime() * 1000);
                            kLine.setOpen(list.getBigDecimal(1));
                            kLine.setHigh(list.getBigDecimal(2));
                            kLine.setLow(list.getBigDecimal(3));
                            kLine.setClose(list.getBigDecimal(4));
                            kLine.setVolume(list.getBigDecimal(5));
                            kLine.setType("Quintar");
                            kLines.add(kLine);
                        });
                        influxDbMapper.postData(kLines);
                    }
                    kLines.clear();
                } catch (IOException e) {
                    log.error("QuintarExchange获取K线" + key + "时出错", e);
                } catch (Exception e) {
                    log.error("QuintarExchange解析K线" + key + "时出错", e);
                }
            });

            log.debug("保存金塔的K线数据" + kLines.size());

        } catch (Exception e) {
            log.error("循环获得QuintarExchangeK线时出错", e);
        }

    };

    /**
     * 获得所有的交易所和交易所下的交易对
     */
    private Runnable getMarketCoinList = () -> {
        try {
            String body = quintar.getMarketCoinList().execute().body();
            List<OnlyKey> pairsList = new ArrayList<>();
            if (Objects.equals(JSONObject.parseObject(body)
                    .getString("success"), "true")) {
                JSONObject.parseObject(body).getJSONArray("result").forEach(o -> {
                    JSONObject exchangeList = (JSONObject) o;
                    exchangeList.getJSONArray("coinMarketList").forEach(o1 -> {
                        JSONObject coinInfo = (JSONObject) o1;
                        Pairs pairs = new Pairs(exchangeList.getString("marketName"),
                                coinInfo.getString("coinMarketCode").replace(coinInfo.getString("currency"), "").toUpperCase(),
                                coinInfo.getString("currency"));
                        pairs.setCoinMarketCode(coinInfo.getString("coinMarketCode"));
                        pairs.setCoinMarketEn(coinInfo.getString("coinMarketEn"));
                        pairs.setCoinMarketName(coinInfo.getString("coinMarketName"));
                        pairs.setMarketCode(coinInfo.getString("marketCode"));
                        pairs.setMarketName(coinInfo.getString("marketName"));
                        pairs.setStatus(coinInfo.getInteger("status"));
                        pairs.setCurrency(coinInfo.getString("currency"));
                        pairs.setTimestamp(System.currentTimeMillis());
                        pairsList.add(pairs);
                        StringBuilder pairsStr = new StringBuilder();
                        pairsStr.append(pairs.getMarketCode().replace(".", "").toLowerCase())
                                .append(":")
                                .append(pairs.getCoinMarketCode().toLowerCase());
                        pairsStatic.add(pairsStr.toString());
                    });
                });
                influxDbMapper.postData(pairsList);
            }
        } catch (IOException e) {
            log.error("QuintarExchange获得所有的交易所和交易所下的交易对网络失败", e);
        } catch (Exception e) {
            log.error("QuintarExchange获得所有的交易所和交易所下的交易对解析失败", e);
        }
    };

    private Runnable getAllTicker = () -> {
        try {
            String body = quintar.getAllTicker().execute().body();
            tickerAll = JSONObject.parseObject(body);
        } catch (IOException e) {
            log.error("QuintarExchange" + e.getMessage(), e);
        }
    };

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Quintar;
    }
}
