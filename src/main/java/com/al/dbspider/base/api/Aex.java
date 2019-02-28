package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * <a href="https://www.aex.com/page/api_detailed.html">https://www.aex.com/page/api_detailed.html</a><br>
 * 只有 unit 等于 BTC 数据<br>
 * 请求形式:rest<br>
 * 限制:60秒120次限制,按交易对查询<br>
 */
public interface Aex {

    /**
     * 15更新一次的行情
     *
     * @param symbol all 返回所有 symbol 行情
     * @param unit
     * @return
     */
    @GET("ticker.php")
    Call<String> ticker(@Query("c") String symbol, @Query("mk_type") String unit);


    /**
     * 深度,买卖各30
     *
     * @param symbol
     * @param unit
     * @return
     */
    @GET("depth.php")
    Call<String> depth(@Query("c") String symbol, @Query("mk_type") String unit);

    /**
     * 历史成交记录
     *
     * @param symbol
     * @param unit
     * @param tid    订单号,可查历史数据,不传为最新30条,1为最早数据
     * @return
     */
    @GET("trades.php")
    Call<String> trade(@Query("c") String symbol, @Query("mk_type") String unit, @Query("tid") Long tid);


}
