package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * <a href="https://www.zb.com/i/developer/">ZB网</a> Rest API 接口<br>
 * 限制: 1000次/1m/ip 10次/1s/user<br>
 */
public interface Zb {

    @GET("markets")
    Call<String> allTradePairs();

    /**
     * 行情
     *
     * @param tradePair btc_usdt
     * @return {<br>
     * "ticker": {<br>
     * "vol": "40.463",<br>
     * "last": "0.899999",<br>
     * "sell": "0.5",<br>
     * "buy": "0.225",<br>
     * "high": "0.899999",<br>
     * "low": "0.081"<br>
     * },<br>
     * "date": "1507875747359"}
     */
    @GET("ticker")
    Call<String> ticker(@Query("market") String tradePair);


    /**
     * 深度
     *
     * @param tradePair
     * @param size      50 items after designate transaction ID
     * @return
     */
    @GET("depth")
    Call<String> depth(@Query("market") String tradePair, @Query("size") Integer size);

    /**
     * 历史成交记录
     *
     * @param tradePair
     * @param tid       指定
     * @return {
     * "amount": 0.541,交易额
     * "date": 1472711925,
     * "price": 81.87,
     * "tid": 16497097,交易 id
     * "trade_type": "ask", 委托类型
     * "type": "sell"
     * }...
     */
    @GET("trades")
    Call<String> trade(@Query("market") String tradePair, @Query("since") Long tid);


    /**
     * K线
     *
     * @param tradePair btc_usdt
     * @param period    1min
     *                  3min
     *                  5min
     *                  15min
     *                  30min
     *                  1day
     *                  3day
     *                  1week
     *                  1hour
     *                  2hour
     *                  4hour
     *                  6hour
     *                  12hour
     * @param timestamp
     * @param limit     Limit of returning data(default 1000,it only return 1000 data if that more than 1000 date )
     * @return {
     * "data": [
     * [
     * 1472107500000,
     * 3840.46, open
     * 3843.56, high
     * 3839.58, low
     * 3843.3, close
     * 492.456 volume
     * ]...
     * ],
     * "moneyType": "btc",
     * "symbol": "ltc"
     * }
     */
    @GET("kline")
    Call<String> kline(@Query("market") String tradePair, @Query("type") String period, @Query("since") Long timestamp, @Query("size") Integer limit);


}
