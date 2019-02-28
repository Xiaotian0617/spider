package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 20:06  王楷
 * @version 20:06 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Quintar {


    @POST("marketCenter/pc/market/marketCoinList")
    Call<String> getMarketCoinList();

    @GET("marketCenter/market/v0/ticker?symbol=all")
    Call<String> getAllTicker();

    @GET("marketCenter/market/v0/trades")
    Call<String> getTradesByKey(@Query("symbol") String symbol);

    /**
     * 获取所有交易所的交易对的K线数据
     *
     * @param symbol 交易对
     * @param time   60  分钟K线
     * @return
     */
    @GET("marketCenter/market/v0/kline")
    Call<String> getKlineByKey(@Query("symbol") String symbol, @Query("type") int time);


}
