package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * file:spider
 * <p>
 * https://github.com/Biboxcom/api_reference/wiki/api_reference
 *
 * @author 14:45  王楷
 * @version 14:45 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Bibox {


    /**
     * command = marketAll
     * Returns the ticker for all markets.
     *
     * @return
     */
    @GET("v1/mdata?cmd=marketAll")
    Call<String> getMarketAll();

    /**
     * command = pairList
     * Returns the all pairs
     *
     * @return
     */
    @GET("v1/mdata?cmd=pairList")
    Call<String> getPairList();


    /**
     * 查询成交记录
     *
     * @param command deals
     * @param pair    交易对 比如BIX_BTC
     * @param size    大小 1-200 不传的话返回200
     * @return
     */
    @GET("v1/mdata")
    Call<String> getDeals(@Query("cmd") String command, @Query("pair") String pair, @Query("size") String size);

    /**
     * 查询深度信息
     * https://api.bibox.com/v1/mdata?cmd=depth&pair=BIX_BTC&size=10
     *
     * @param command depth
     * @param pair    交易对 比如BIX_BTC
     * @param size    大小 1-200 不传的话返回200
     * @return
     */
    @GET("v1/mdata")
    Call<String> getDepth(@Query("cmd") String command, @Query("pair") String pair, @Query("size") String size);

    /**
     * 查询K线
     *
     * @param command kline
     * @param pair    交易对 比如BIX_BTC
     * @param period  k线周期，取值 ['1min', '3min', '5min', '15min', '30min', '1hour', '2hour', '4hour', '6hour', '12hour', 'day', 'week']
     * @param size    长度 1-1000 默认1000
     * @return
     */
    @GET("v1/mdata")
    Call<String> getKline(@Query("cmd") String command, @Query("pair") String pair, @Query("period") String period, @Query("size") String size);


}
