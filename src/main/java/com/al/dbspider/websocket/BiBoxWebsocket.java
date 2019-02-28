package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.OnlyKey;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket.bibox", name = "disable", havingValue = "false", matchIfMissing = true)
@ConfigurationProperties("websocket.bibox")
public class BiBoxWebsocket extends BaseWebsocket {

    private static final String SUB_REQ = "{\"event\":\"addChannel\",\"channel\":\"bibox_sub_spot_ALL_ALL_market\"}";

    @Override
    public void setKline(boolean kline) {
        super.kline = false;
    }

    @Override
    public void setDepth(boolean depth) {
        super.setDepth(depth);
    }

    @Override
    public void subscribe() {
        if (market) {
            send(SUB_REQ);
        }
        if (trade) {
        }
        if (kline) {
        }
        if (depth) {
        }
    }


    @Override
    public List<OnlyKey> onMessageInPool(String s) {
        JSONObject job = JSONArray.parseArray(s).getJSONObject(0);
        Object obj = JSON.parse(job.getString("data"));
        List<OnlyKey> onlyKeys = new ArrayList<>();
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            Market market = new Market(ExchangeConstant.Bibox, jsonObject.getString("coin_symbol"), jsonObject.getString("currency_symbol"));
            market.setLast(jsonObject.getBigDecimal("last"));
            market.setVolume(jsonObject.getBigDecimal("amount"));//amount 指代 volume
            market.setTimestamp(System.currentTimeMillis());
            onlyKeys.add(market);
        } else if (obj instanceof JSONArray) {
            List<JSONObject> jsonObjects = ((JSONArray) obj).toJavaList(JSONObject.class);
            jsonObjects.forEach(jsonObject -> {
                Market market = new Market(ExchangeConstant.Bibox, jsonObject.getString("coin_symbol"), jsonObject.getString("currency_symbol"));
                market.setLast(jsonObject.getBigDecimal("last"));
                market.setAmount(jsonObject.getBigDecimal("amount"));
                market.setTimestamp(System.currentTimeMillis());
                onlyKeys.add(market);
            });
        }
        return onlyKeys;
    }

    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Bibox;
    }

}
