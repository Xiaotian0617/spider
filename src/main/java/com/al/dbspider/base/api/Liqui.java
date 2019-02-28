package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * https://liqui.io/api
 */
public interface Liqui {

    /**
     * This method provides all the information about currently active pairs
     *
     * @return <pre>
     * decimal_places: number of decimals allowed during trading.
     * min_price: minimum price allowed during trading.
     * max_price: maximum price allowed during trading.
     * min_amount: minimum sell / buy transaction size.
     * hidden: whether the pair is hidden, 0 or 1.
     * fee: commission for this pair.
     * </pre>
     */
    @GET("api/3/info")
    Call<String> info();

    //pair: eth_btc-xrp_btc-... 可以实现一次查询所有交易对
    @GET("api/3/ticker/{pair}?ignore_invalid=1")
    Call<String> ticker(@Path("pair") String pair);

    @GET("api/3/depth/{pair}?ignore_invalid=1")
    Call<String> depth(@Path("pair") String pair, @Query("limit") int limit);

    @GET("api/3/trades/{pair}?ignore_invalid=1")
    Call<String> trade(@Path("pair") String pair, @Query("limit") int limit);

    /**
     * 主页上交易所自己使用的接口
     *
     * @return
     */
    @GET("https://cacheapi.liqui.io/Market/Pairs")
    Call<String> pairs();

    /**
     * https://charts.liqui.io/chart/history?symbol=213&resolution=1&from=1516587739&to=1516596739
     * Liqui主页上自己使用的K线接口
     *
     * @param id   币种ID,需要通过info接口来获取
     * @param type K线类型, 值为 1，5，15，30，60，120，240，D
     * @param from
     * @param to
     * @return
     */
    @GET("https://charts.liqui.io/chart/history")
    Call<String> kline(@Query("symbol") String id, @Query("resolution") String type, @Query("from") Long from, @Query("to") Long to);

}
