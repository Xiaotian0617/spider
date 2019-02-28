package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 10:22  王楷
 * @version 10:22 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Poloniex {

    /**
     * command = returnTicker
     * Returns the ticker for all markets.
     *
     * @param command
     * @return
     */
    @GET("public")
    @Headers("user-agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.4694.400 QQBrowser/10.0.569.400")
    Call<String> getTicker(@Query("command") String command);


    /**
     * Returns the past 200 trades for a given market,
     * or up to 50,000 trades between a range specified in UNIX timestamps by the "start" and "end" GET parameters.
     *
     * @param command      returnTradeHistory
     * @param currencyPair BTC_NXT
     * @param start        1410158341
     * @param end          1410499372
     * @return
     */
    @GET("public")
    @Headers("user-agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.4694.400 QQBrowser/10.0.569.400")
    Call<String> getTradeHistory(@Query("command") String command, @Query("currencyPair") String currencyPair, @Query("start") String start, @Query("end") String end);

}
