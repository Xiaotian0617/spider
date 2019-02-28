package com.al.dbspider.base.api;

import org.springframework.http.MediaType;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * @author 19:40  王楷
 * @author yangjunxiao
 * @version 19:40 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface TopCoin {

    /**
     * 发送Websocket信息通知
     *
     * @param markets
     * @return
     */
    @POST("/api/receivemarket")
    @Headers({"Content-Type: application/json"})
    Call<String> sendMarketInfo(@Body String markets);

    @POST("/api/receivemarketcap")
    @Headers({"Content-Type: application/json"})
    Call<String> sendMarketCapInfo(@Body String marketCaps);

    @POST("/api/receivekline")
    @Headers({"Content-Type: application/json"})
    Call<String> sendKlineInfo(@Body String klines);

    @POST("/api/receivepair")
    @Headers({"Content-Type: application/json"})
    Call<String> sendPairInfo(@Body String pairs);

    @POST("/api/receivetrade")
    @Headers({"Content-Type: application/json"})
    Call<String> sendTradeInfo(@Body String trades);

    /**
     * 发送Websocket信息通知
     *
     * @param json
     * @return
     */
    @POST("/ws/flushws")
    @Headers({"Content-Type: " + MediaType.APPLICATION_JSON_UTF8_VALUE})
    Call<String> sendInfo(@Body String json);
}
