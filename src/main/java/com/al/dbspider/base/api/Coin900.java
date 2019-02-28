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
 * @author 16:09  王楷
 * @version 16:09 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Coin900 {

    /**
     * Returns the kline by pairs
     * <p>
     * Eg.
     * https://coin900.com/api/v2/k_with_pending_trades.json?market=ethbtc&limit=768&period=1&trade_id=9608889
     * limit: 官网默认768条
     * period :  1 一分钟  5 五分钟  1440 一天 最大一周
     * trade_id： 目前取最新一笔交易的id
     *
     * @return
     */
    @GET("api/v2/k_with_pending_trades.json")
    @Headers("user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
    Call<String> getKlineByPair(@Query("market") String market,
                                @Query("limit") int limit,
                                @Query("period") int period,
                                @Query("trade_id") String trade_id);

}
