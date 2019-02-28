package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-07-26
 */
public interface Fcoin {


    /**
     * 服务器时间
     *
     * @return
     */
    @GET("public/server-time")
    Call<String> serverTime();

    /**
     * 全部币种
     *
     * @return
     */
    @GET("public/currencies")
    Call<String> currencies();

    /**
     * 全部交易对
     *
     * @return
     */
    @GET("public/symbols")
    Call<String> symbols();

    /**
     * 行情
     *
     * @param pair
     * @return
     */
    @GET("market/ticker/${symbol}")
    Call<String> ticker(@Path("symbol") String pair);

    /**
     * 深度
     *
     * @param level L20 L100 full
     * @param pair
     * @return
     */
    @GET("market/depth/${level}/${symbol}")
    Call<String> depth(@Path("level") String level, @Path("symbol") String pair);

    /**
     * 交易
     *
     * @param pair
     * @param before 查询某个id 之前的trade
     * @param limit  默认为20条
     * @return
     */
    @GET("market/trades/${symbol}")
    Call<String> trades(@Path("symbol") String pair, @Query("before") Long before, @Query("limit") Integer limit);


    /**
     * k线
     *
     * @param resolution {@link Resolution}
     * @param pair
     * @param before     查询某个id 之前的candles
     * @param limit      默认为20条
     * @return
     */
    @GET("market/candles/${resolution}/${symbol}")
    Call<String> kline(@Path("resolution") Resolution resolution, @Path("symbol") String pair, @Query("before") Long before, @Query("limit") Integer limit);

    enum Resolution {
        M1, D1
    }

    enum Level {
        L20, L100, full
    }
}
