package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * <a href="https://developer.big.one/">https://developer.big.one/</a><br>
 * Authentication ¶<br>
 * Get your API Key from Big ONE Settings;<br>
 * Generate a UUID as a Device ID for identifying your device;<br>
 * Add the following headers to all the HTTPS requests:<br>
 * User-Agent: standard browser user agent format;<br>
 * Authorization: Bearer <API Key>;
 * Big-Device-Id: <Device ID>.<br>
 * 请求形式:rest<br>
 * 限制:按交易对查询<br>
 */
public interface Bigone {


    /**
     * 获取交易对 UUID
     */
    @GET("markets")
    Call<String> getPairs();

    /**
     * 全部市场行情
     *
     * @return
     */
    @GET("tickers")
    Call<String> ticker();

    /**
     * 返回数据结构复杂,包含行情data.ticker/委托data.asks|bids/最近50条交易data.trades/分别240条k线data.metrics.0000001|0000005|0000015|0000060|0000360|0001440
     *
     * @param tradePair
     * @return
     */
    @GET("/{tradePair}")
    Call<String> kline(@Path("tradePair") String tradePair);


    /**
     * 深度
     *
     * @param tradePair
     * @return 最新深度数据各50条
     */
    @GET("/{tradePair}/book")
    Call<String> depth(@Query("tradePair") String tradePair);

    /**
     * 历史成交记录
     *
     * @param tradePair
     * @return 最新50条成交记录
     */
    @GET("/{tradePair}/trades")
    Call<String> trade(@Query("tradePair") String tradePair);


}
