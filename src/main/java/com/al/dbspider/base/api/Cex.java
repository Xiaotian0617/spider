package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * <a href="https://coding.net/u/byoneself/p/cex_api/git/blob/master/cex_api.md">https://coding.net/u/byoneself/p/cex_api/git/blob/master/cex_api.md</a><br>
 * 请求形式:rest<br>
 * 限制:按交易对查询<br>
 */
public interface Cex {

    /**
     * 行情
     *
     * @param tradePair
     * @return
     */
    @GET("ticker.do")
    Call<String> ticker(@Query("symbol") String tradePair);


    /**
     * 深度
     *
     * @param tradePair
     * @return
     */
    @GET("depth.do")
    Call<String> depth(@Query("symbol") String tradePair);

    /**
     * 历史成交记录
     *
     * @param tradePair
     * @return 最新99条成交记录
     */
    @GET("trade.do")
    Call<String> trade(@Query("symbol") String tradePair);


}
