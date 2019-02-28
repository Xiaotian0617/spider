package com.al.dbspider.rest;

import com.al.dbspider.utils.HttpUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 04/01/2018 11:18
 */
@Slf4j
//@Service
public class AllcoinRest extends BaseRest {


    private String url = "https://www.allcoin.com/Home/MarketOverViewDetail/";
    private List<Header> headers = Lists.newArrayList();
    private List<Cookie> cookies = Lists.newArrayList();
    private List<NameValuePair> params = Lists.newArrayList();

    private final static String JYS_FULL_NAME = "Kucoin";

    private final static String MARKET_URL;

    static {
        MARKET_URL = "https://kitchen-5.kucoin.com/v1/market/open/symbols";
    }

    public void startProcess() {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, 30, TimeUnit.SECONDS);
    }

    private static Runnable runnable = () -> {
        try {
            String resultMap = HttpUtils.get().get(MARKET_URL);

            JSONObject jsonObject = JSONObject.parseObject(resultMap);
            if (!Objects.equals(true, jsonObject.getBoolean("success"))) {
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                // 创建存入数据库的最新价格对象
//                Market market = new Market(ExchangeConstant.Allcoin,json.getString("coinType"));
//                 设置币种(取币币交易的第一个)
//                market.setSymbol(json.getString("coinType"));
//                 设置单位(取币币交易的第二个)
//                market.setUnit(json.getString("coinTypePair"));
//                 设置最新价格
//                market.setLast(json.getBigDecimal("lastDealPrice"));
//                 设置交易所全名
//                market.setJysFullName(JYS_FULL_NAME);
//                 设置创建时间和更新时间
//                market.setTimestamp(new Date());
//                market.setuTime(new Date());
                //System.out.println(JSONObject.toJSONString(market));
//                HttpUtils.get().post(MONEY_URL, JSONObject.toJSONString(market));
            }
        } catch (Exception e) {
            log.error("Gate market " + e.getMessage(), e);
        }

    };

}

